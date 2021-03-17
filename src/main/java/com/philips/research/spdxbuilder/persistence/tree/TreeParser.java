/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.tree;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.philips.research.spdxbuilder.core.domain.BillOfMaterials;
import com.philips.research.spdxbuilder.core.domain.Package;
import com.philips.research.spdxbuilder.core.domain.Relation;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses consecutive lines of a textual tree into unique packages and their relations.
 */
class TreeParser {
    private static final Pattern ID_PATTERN = compile("^([\\w-\\.]+):([\\w-\\.]+):([\\w-\\.]+)");

    private final BillOfMaterials bom;
    private final Map<PackageURL, Package> packages = new HashMap<>();
    private final Stack<Integer> indentStack = new Stack<>();
    private final Stack<@NullOr Package> packageStack = new Stack<>();
    private int skipLevel = Integer.MAX_VALUE;
    private @NullOr Pattern startSection;
    private @NullOr Pattern endSection;
    private @NullOr String cleanup;
    private Pattern identifierPattern = compile("\\w");
    private @NullOr Pattern skipPattern;
    private @NullOr Pattern typePattern;
    private int typeGroup = 0;
    private Pattern namespacePattern = ID_PATTERN;
    private int namespaceGroup = 1;
    private Pattern namePattern = ID_PATTERN;
    private int nameGroup = 2;
    private Pattern versionPattern = ID_PATTERN;
    private int versionGroup = 3;
    private @NullOr Pattern relationshipPattern;
    private int relationshipGroup = 0;
    private Map<String, String> typeMapping = Map.of("", "");
    private Map<String, Relation.Type> relationshipMapping = Map.of("", Relation.Type.DYNAMIC_LINK);
    private boolean started = true;
    private boolean ended = false;

    TreeParser(BillOfMaterials bom) {
        this.bom = bom;
    }

    private static Pattern compile(String regEx) {
        return Pattern.compile(regEx);
    }

    /**
     * (Optionally) specifies the start of the relevant section.
     *
     * @param regEx regular expression to match the unique start marker
     */
    TreeParser withStartSection(String regEx) {
        startSection = compile(regEx);
        started = false;
        return this;
    }

    /**
     * (Optionally) specifies the end of the relevant section.
     *
     * @param regEx regular expression to match the unique end marker
     */
    TreeParser withEndSection(String regEx) {
        endSection = compile(regEx);
        return this;
    }

    /**
     * (Optionally) strips parts of the line before parsing to avoid false matches.
     *
     * @param regEx regular expression for any fragment that is to be removed
     */
    TreeParser withCleanup(String regEx) {
        cleanup = regEx;
        return this;
    }

    /**
     * Specifies the start of a valid identifier.
     * (Default is <code>@</code>, <code>1..9</code>, <code>A..Z</code> and <code>a..z</code>)
     *
     * @param regEx regular expression to match at the first character of the identifier
     */
    TreeParser withIdentifier(String regEx) {
        identifierPattern = compile(regEx);
        return this;
    }

    /**
     * Specifies which packages (and dependent subpackages) to skip.
     *
     * @param regEx regular expression to match a package that is to be skipped
     */
    TreeParser withSkip(String regEx) {
        skipPattern = compile(regEx);
        return this;
    }

    /**
     * Specifies the default and alternate package type(s).
     *
     * @param mapping mapping from the default empty marker and (optional) custom markers to the related type identifier.
     * @see #withType(String, int) (Optional) format of a custom marker
     */
    TreeParser withTypes(Map<String, String> mapping) {
        this.typeMapping = mapping;
        return this;
    }

    /**
     * (Optionally) specifies the mask to find custom package type markers.
     *
     * @param regEx regular expression for the mask
     * @param group the matching group holding the mask
     */
    TreeParser withType(String regEx, int group) {
        typePattern = compile(regEx);
        typeGroup = group;
        return this;
    }

    /**
     * Specifies the mask to find the package namespace.
     *
     * @param regEx regular expression for the mask
     * @param group the matching group holding the mask
     */
    TreeParser withNamespace(String regEx, int group) {
        namespacePattern = compile(regEx);
        namespaceGroup = group;
        return this;
    }

    /**
     * Specifies the mask to find the package name.
     *
     * @param regEx regular expression for the mask
     * @param group the matching group holding the mask
     */
    TreeParser withName(String regEx, int group) {
        namePattern = compile(regEx);
        nameGroup = group;
        return this;
    }

    /**
     * Specifies the mask to find the package version.
     *
     * @param regEx regular expression for the mask
     * @param group the matching group holding the mask
     */
    TreeParser withVersion(String regEx, int group) {
        versionPattern = compile(regEx);
        versionGroup = group;
        return this;
    }

    /**
     * Specifies the default and alternate package relationship type(s).
     *
     * @param mapping mapping from the default empty marker and (optional) custom markers to the related type
     * @see Relation.Type Valid type names
     * @see #withRelationship(String, int) (Optional) format of a custom marker
     */
    TreeParser withRelationships(Map<String, String> mapping) {
        relationshipMapping = mapping.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Relation.Type.valueOf(e.getValue().toUpperCase())));
        return this;
    }

    /**
     * (Optionally) specifies the mask to find custom package relationship type markers.
     *
     * @param regEx regular expression for the mask
     * @param group the matching group holding the mask
     */
    TreeParser withRelationship(String regEx, int group) {
        relationshipPattern = compile(regEx);
        relationshipGroup = group;
        return this;
    }

    /**
     * Extracts a new package and its implicit relationship from a line of text, using the assumption sub-packages
     * are hierarchically indented from containing packages.
     *
     * @param line line of ascii characters
     */
    TreeParser parse(String line) {
        if (ignoredLine(line)) {
            return this;
        }

        final var clean = clean(line);
        final var indent = firstPackageCharacter(clean);
        final var name = clean.substring(indent);
        if (name.isBlank()) {
            return this;
        }

        popUntil(indent);

        if (!ignoredPackage(name)) {
            final var pkg = processPackage(indent, name);
            pushPackage(indent, pkg);
        } else {
            pushPackage(indent, null);
        }

        return this;
    }

    private boolean ignoredLine(String line) {
        if (!started) {
            assert startSection != null;
            started = startSection.matcher(line).find();
            return true;
        }

        ended |= (endSection != null) && endSection.matcher(line).find();
        return ended;
    }

    private String clean(String line) {
        return (cleanup != null) ? line.replaceAll(cleanup, "") : line;
    }

    private int firstPackageCharacter(String line) {
        final var matcher = identifierPattern.matcher(line);
        if (!matcher.find()) {
            return line.length();
        }
        return matcher.start();
    }

    private boolean ignoredPackage(String name) {
        if (indentStack.size() < skipLevel) {
            if (skipPattern != null && skipPattern.matcher(name).find()) {
                skipLevel = indentStack.size() + 1;
                return true;
            } else {
                skipLevel = Integer.MAX_VALUE;
                return false;
            }
        }
        return true;
    }

    private PackageURL purlFromLine(String line) {
        final var type = extractType(line);
        final var namespace = match(namespacePattern, line, namespaceGroup);
        final var name = match(namePattern, line, nameGroup);
        final var version = match(versionPattern, line, versionGroup);
        return toPurl(type, namespace, name, version);
    }

    private String extractType(String line) {
        final var id = match(typePattern, line, typeGroup);
        final @NullOr String type = typeMapping.get(id);
        if (type == null) {
            throw new TreeException("Not a supported type identifier: '" + id
                    + "'. Expected one of " + typeMapping.keySet());
        }
        return type;
    }

    private Relation.Type extractRelationship(String line) {
        final var id = match(relationshipPattern, line, relationshipGroup);
        final Relation.@NullOr Type relationship = relationshipMapping.get(id);
        if (relationship == null) {
            throw new TreeException("Not a supported relationship identifier: '" + id
                    + "'. Expected one of " + relationshipMapping.keySet());
        }
        return relationship;
    }

    private String match(@NullOr Pattern pattern, String line, int group) {
        if (pattern == null) {
            return "";
        }
        final var matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(group);
    }

    private PackageURL toPurl(String type, String namespace, String name, String version) {
        try {
            return new PackageURL(type, namespace, name, version, null, null);
        } catch (MalformedPackageURLException e) {
            throw new TreeException("Invalid package identifier: type=" + type + ", namespace=" + namespace + ", name=" + name + ", version=" + version);
        }
    }

    private Package processPackage(int indent, String name) {
        final var purl = purlFromLine(name);
        final Package pkg =  storePackage(purl);

        if (!indentStack.isEmpty() && indent > indentStack.peek()) {
            bom.addRelation(packageStack.peek(), pkg, extractRelationship(name));
        }

        return pkg;
    }

    private Package storePackage(PackageURL purl) {
        return packages.computeIfAbsent(purl, purl1 -> {
            final var pkg = Package.fromPurl(purl1);
            bom.addPackage(pkg);
            return pkg;
        });
    }

    private void popUntil(int indent) {
        while (!indentStack.isEmpty() && indent <= indentStack.peek()) {
            indentStack.pop();
            packageStack.pop();
        }
    }

    private void pushPackage(int indent, @NullOr Package pkg) {
        indentStack.push(indent);
        //noinspection ConstantConditions
        packageStack.push(pkg);
    }

}