/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackageIdentifierTest {
    @Test
    void decodesFormats() {
        // (Formats are based on external namespaces list provided by Black Duck.)
        assertIdentifier("alpine", "name/version/architecture", "alpine/name@version");
        // alt_linux
        // anaconda
        // android
        // android_sdk
        // apache_software
        assertIdentifier("arch_linux", "name/version/architecture", "arch/name@version");
        // automotive_linux
        assertIdentifier("bitbucket", "org/name:version", "bitbucket/org/name@version");
        assertIdentifier("bitbucket", "org/name", "bitbucket/org/name");
        assertIdentifier("bower", "name/version", "bower/name@version");
        assertIdentifier("bower", "@scope/name/version", "bower/%40scope/name@version");
        assertIdentifier("centos", "name/version/architecture", "rpm/centos/name@version");
        // clearlinux
        assertIdentifier("cocoapods", "name/version", "cocoapods/name@version");
        // codeplex
        // codeplex_group
        assertIdentifier("conan", "name/version", "conan/name@version");
        assertIdentifier("cpan", "name/version", "cpan/name@version");
        // cpe
        assertIdentifier("cran", "name/version", "cran/name@version");
        assertIdentifier("crates", "name/version", "cargo/name@version");
        assertIdentifier("dart", "name/version", "pub/name@version");
        assertIdentifier("debian", "name/version/architecture", "deb/name@version");
        // eclipse
        // efisbot
        assertIdentifier("fedora", "name/version/architecture", "rpm/fedora/name@version");
        // freedesktop_org
        // gitcafe
        assertIdentifier("github", "org/name:version", "github/org/name@version");
        assertIdentifier("github", "org/name", "github/org/name");
        // github_gist
        assertIdentifier("gitlab", "org/name:version", "gitlab/org/name@version");
        assertIdentifier("gitlab", "org/name", "gitlab/org/name");
        // gitorious
        // gnu
        assertIdentifier("golang", "name/version", "golang/name@version");
        assertIdentifier("golang", "domain/name/version", "golang/domain/name@version");
        // googlecode
        // hackage
        assertIdentifier("hex", "name/version", "hex/name@version");
        assertIdentifier("hex", "namespace/name/version", "hex/namespace/name@version");
        // java_net
        // kb_classic
        // launchpad
        // long_tail
        assertIdentifier("maven", "group:name:version", "maven/group/name@version");
        // mongo_db
        assertIdentifier("npmjs", "name/version", "npm/name@version");
        assertIdentifier("npmjs", "@scope/name/version", "npm/%40scope/name@version");
        assertIdentifier("nuget", "name/version", "nuget/name@version");
        // openembedded
        // openjdk
        assertIdentifier("opensuse", "name/version/architecture", "rpm/opensuse/name@version");
        // oracle_linux
        // packagist
        // pear
        // photon
        // protocode_sc
        assertIdentifier("pypi", "name/version", "pypi/name@version");
        // raspberry_pi
        assertIdentifier("redhat", "dist/name/version", "rpm/dist/name@version");
        assertIdentifier("redhat", "name/version", "rpm/name@version");
        // ros
        // rubyforge
        assertIdentifier("rubygems", "name/version", "gem/name@version");
        // runtime
        // sourceforge
        // sourceforge_ip
        // tianocore
        assertIdentifier("ubuntu", "name/version/architecture", "deb/ubuntu/name@version");
        // yokto
    }

    private void assertIdentifier(String namespace, String identifier, String expected) {
        try {
            final var id = new PackageIdentifier(namespace, identifier);
            assertThat(id.getPurl()).contains(new PackageURL("pkg:" + expected));
        } catch (MalformedPackageURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
