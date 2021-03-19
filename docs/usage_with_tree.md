# Building a bill-of-materials from a dependency tree

## Usage

```shell
<print_tree_output> | spdx-builder tree -f <format> -o <output_file>
```

Where "print_tree_output" is the relevant command of the project build
environment to list the hierarchy of packages and their dependencies, and "
format" specifies the shorthand name of the formatting for the resulting tree.

_Note: The list of supported formats is output when no format option is
specified.

_Note: If no "output_file" is specified, the output is written to a file named
`bom.spdx` in the current directory. If the file has no extension, `.spdx`
is automatically appended._

## Tree format specification

Built-in formats are specified in
a [YAML file](../src/main/resources/treeformats.yml).

The YAML file contains a `formats` array, containing objects that each specify a
format that is applied using the format name in the `format` field. When a
format specifies a `parent`, the format is (recursively) based on the indicated
format:

```yaml
format: "<format>" # Shorthand name of the format
parent: "<format>" # (Optional) base configuration
description: "<description>" # Summary description of when to use this format
tool: "<invocation>" # Example build tool invocation to generate the tree output
```

To constrain the output of the tree tool to the relevant section, the following
fields are available to bound the relevant section, and e.g. remove a line
preamble:

```yaml
# Bto what is interpreted by the parser.
# (Applied to the raw (full) lines of the input.)
start: <regex> # (Optional) Regular expression marking the start of the relevant tree
end: <regex> # (Optional) Regular expressino marking the end of the relevant tree
cleanup: <regex> # (Optional) Regular expression for text fragments to be removed prior to parsing
```

The hierarchical relation between packages is identified by the indentation
level of the package name. This defaults to finding the first "word" character,
but can optionally be customized:

```yaml
# Hierarchical tree analysis
identifier: <regex> # (Optional) Regular expression matching on the tree indent position.
```

Packages are identified by a Package URL that is extracted from the fragment
that remains after removing the indentation:

```yaml
# Identification of the package type.
# (Applied to the indented fragment.)
types:
  "": <type> # Default package type if none is specified
  "<identifier>": <type> # (Optional) marker-to-type conversion
type:
  regex: <regex> # Regular expression extracting the type marker 
  group: <index> # Matching group holding the type marker (defaults to 1)

# Package URL construction
# (Applied to the indented fragment.)
skip: <regex> # (Optional) identification of an excluded package (and nested subpackages).
namespace: # (Optional) namespace matcher
  regex: <regex> # Pattern to capture the Package URL namespace
  group: <index> # Matching group holding the namespace (defaults to 1)
name:
  regex: <regex> # Pattern to capture the Package URL name
  group: <index> # Matching group holding the name (defaults to 1)
version:
  regex: <regex> # Pattern to capture the Package URL version 
  group: <index> # Matching group holding the version (defaults to 1)
```

The relationship between packages defaults to dynamically linked. This default
can be overridden for the format, and markers can be used to map a dependency to
a specific relationship.

```yaml
# Package relation
# (Applied to the indented fragment.)
relationships:
  "": <type> # Default relationship if none is specified
  "<identifier>": <type> # (Optional) marker-to-relationship conversion
relationship: # (Optional) relationship marker pattern 
  regex: <regex> # Regular expression extracting the (optional) relationship marker 
  group: <index> # Matching group holding the relationship marker (defaults to 1)
```
