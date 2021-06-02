# Building a bill-of-materials from a dependency tree

## Usage

```shell
<print_tree_output> | spdx-builder tree -f <format> -o <output_file> --bombase <bombase_url>
```

Where "print_tree_output" is the relevant command of the project build
environment to list the hierarchy of packages and their dependencies, and "
format" specifies the shorthand name of the formatting for the resulting tree.
The "bombase_url" refers to
the [BOM-Base](https://github.com/philips-software/bom-base) knowledge base
instance that supplies the metadata for the packages parsed from the dependency
tree.

_Note: Configuration information (see below) is read from a file
named `.spdx-builder.yml`. This name can be overridden on the command line._

_Note: The list of supported formats is output when no format option is
specified._

_Note: If no "output_file" is specified, the output is written to a file named
`bom.spdx` in the current directory. If the file has no extension, `.spdx`
is automatically appended._

Root packages of the tree are by default exported without a package URL to
indicate they are not (yet) public. This can be overridden by the `--release`
option on the command line. Some formats allow for deriving internal packages
from the parsed tree structure, else additional internal packages can be
indicated in the configuration file by package URLs that may contain "*" as
wildcard.

When a project consists of multiple trees, a pre-processing script can be used
to first merge the generated trees into a single input to SPDX-Builder. If the
separate trees are in different formats, these sections can be separated by
a `### <new_format` marker fragment (that can be anywhere in the line) to switch
to a different tree format. The tree indentation level is maintained across
format changes, making it even possible (by adding indents) to insert a sub-tree
in a different format.

## Configuration file format

```yaml
document:
  title: <string> # Name of the product (=root of the tree)
  organization: <string> # Organization publishing the SBOM
  comment: <string> # (Optional) document level comment text
  key: <string> # (Optional) document reference (is appendended to "SPDXRef-")
  namespace: <url> # (Optional) base URL of the SPDX document namespace
internal: # Package URL globs to match any product-internal packages
  - <string> # (Simplified) package URL glob with optional "*" wildcards
```

Simplified package URL globs can take various forms:

- `type/namespace/name@version`: Exact match ("pkg:" prefix is optional)
- `type/namespace/name`: Matches any version
- `type/name`: Matches any namespace
- `name`: Matches any type and any namespace
- 'so*ng': Matches names like "something" or "song" (but not "songs")

## Supported trees

Various common tree formats are already included, including:

- Maven dependency tree (`mvn dependency:tree`)
- Gradle dependency
  tree (`gradlew -q dependencies --configuration runtimeClasspath`)
- NPM packages list (`npm list --all --production`)
- Rust dependency tree (`cargo tree -e no-dev,no-build --locked`)
- Python pip freeze (`pip freeze`)
- Python pipenv graph (`pipenv graph --bare`)

All supported formats are listed if no format is provided in the invocation:

```shell
spdx-builder tree
```

If your format is not supported, or your build tool produces a slightly
different format, you can
use [this configuration file](../src/main/resources/treeformats.yml)
for inspiration to build your custom format.

Feel free to contribute your format specification back to SPDX-Builder, so
others can also benefit from your work!

## Tree format specification

Custom tree formats can be added by providing the `--custom <formats_file>`
option. The "formats_file" has the same format as
the [internal formats file](../src/main/resources/treeformats.yml):

The YAML file consists of a `formats` array, containing objects that each
specify a format that is applied using the format name in the `format` field.
When a format specifies a `parent`, the format is (recursively) based on the
indicated format:

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
  "<marker>": <type> # (Optional) marker-to-type conversion
type:
  regex: <regex> # Regular expression extracting the type marker 
  group: <index> # Matching group holding the type marker (defaults to 1)

# Package URL construction
# (Applied to the indented fragment.)
skip: <regex> # (Optional) identification of an excluded package (and nested subpackages).
internal: <regex> # (Optional) identification of application packages.
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

The relationship between packages defaults to "dynamically linked". This default
can be overridden for the format, and markers can be used to map a dependency to
a specific relationship:

```yaml
# Package relation
# (Applied to the indented fragment.)
relationships:
  "": <type> # Default relationship if none is specified
  "<marker>": <type> # (Optional) marker-to-relationship conversion
relationship: # (Optional) relationship marker pattern 
  regex: <regex> # Regular expression extracting the (optional) relationship marker 
  group: <index> # Matching group holding the relationship marker (defaults to 1)
```

