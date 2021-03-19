<div align="center">

# SPDX-Builder

[![Release](https://img.shields.io/github/release/philips-software/spdx-builder.svg)](https://github.com/philips-software/spdx-builder/releases)

CI/CD tool to generate Bill-of-Materials reports in SPDX format.
> **Status**: Experimental research prototype

</div>

## Contents

- [SPDX-Builder](#spdx-builder)
- [Contents](#contents)
- [Dependencies](#dependencies)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [How to test the software](#how-to-test-the-software)
- [Known issues](#known-issues)
- [Contact / Getting help](#contact--getting-help)
- [License](#license)
- [Credits and references](#credits-and-references)

## SPDX-Builder

CI/CD tool to generate Bill-of-Materials reports in SPDX format.

**Status**: Experimental research prototype

(See the [architecture document](docs/architecture.md) for a detailed technical
description.)

Converts project dependencies into a standard
[SPDX](https://spdx.github.io/spdx-spec) tag-value Software Bill-of-Materials
file, optionally integrating externally detected and curated license details.

Inputs for the SBOM are:

* Package information by YAML files from
  [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) (ORT)
  Analyzer.
* Projects analyzed using
  the [Synoptic Black Duck](https://www.synopsys.com/software-integrity/security-testing/software-composition-analysis.html)
  SCA service.
* Curated license scan results from the REST API of a
  [License Scanning Service](https://github.com/philips-software/license-scanner)
  backend service.

## Installation

Build the application using the standard gradle command:

```shell
./gradlew clean install
```

Then make the resulting files from the `build/install/spdx-builder/bin`
available in the path.

Alternatively the application can be run directly from Gradle:

```shell
./gradlew run --args="ort -c .spdx-builder.yml <command> <parameters>"
```

## Usage

The commandline application has usage instructions built-in

```shell
spdx-builder --help
```

_NOTE: This application requires Java 11 or higher._

### Usage modes

Instructions for the various subcommands are:

- "[ort](docs/usage_with_ort.md)": Merge output
  of [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) with
  licenses detected by
  the [License Scanner service](https://github.com/philips-software/license-scanner)
  using the [ScanCode Toolkit](https://github.com/nexB/scancode-toolkit) license
  scanner.
- "[blackduck](docs/usage_with_black_duck.md)": Export
  from [Synoptic Black Duck SCA server](https://www.synopsys.com/software-integrity/security-testing/software-composition-analysis.html)
  using the server API.
- "[tree](docs/usage_with_tree.md)": Merge the dependency tree of a build tool
  with metadata from
  the [BOM-base](https://github.com/philips-software/bom-base) knowledge base
  implementation.

### Uploading the resulting SPDX file

It is possible to automatically upload the generated SDPX file to a server. This
will POST the SPDX file using a multi-part file upload in the `file` parameter .

To upload the extracted bill-of-materials from an ORT file
to [BOM-bar](https://github.com/philips-software/bom-bar), the invocation
becomes:

```shell
spdx-builder ort -c <config_yaml_file> -upload=https://<server>:8080/projects/<uuid>/upload <ort_yaml_file>
```


### GitHub actions

You can use the SPDX-builder in a GitHub Action. This can be found on
<https://github.com/philips-software/spdx-action>. The Action performs an ORT
scan, pushes the data to SPDX-builder and can use a self hosted license scanner
service and upload service like BOM-Bar. (plain upload function for a spdx-file,
so you can also use this for other systems.)

## How to test the software

The unit test suite is run via the standard Gradle command:

```shell
./gradlew clean test
```

A local ORT-based test can be run by:

```shell
./gradlew run --args="ort -c src/test/resources/.spdx-builder.yml src/test/resources/ort_sample.yml"
```

## Known issues

(Ticked checkboxes indicate topics currently under development.)

Must-have:

- [x] Properly expand Black Duck origin identifiers to package URLs.
- [x] Skip ignored packages from Black Duck output.
- [x] Recursively import sub-projects from Black Duck.
- [ ] Abort if ORT Analyzer raised errors.

Should-have:

- [ ] Support output "flavors" for the purpose of the generated SBOM.

Other ideas:

- [ ] Support RDF/XML SPDX output format
- [ ] Integration with [Quartermaster (QMSTR)](https://qmstr.org/).

## Contact / Getting help

Submit tickets to
the [issue tracker](https://github.com/philips-software/spdx-builder/issues).

## License

See [LICENSE.md](LICENSE.md).

## Credits and references

1. [The SPDX Specification](https://spdx.github.io/spdx-spec) documents the SPDX
   file standard.
2. [The ORT Project](https://github.com/oss-review-toolkit) provides a toolset
   for generating and analyzing various aspects of the Bill-of-Materials.
