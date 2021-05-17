<div align="center">

# SPDX-Builder

[![Release](https://img.shields.io/github/release/philips-software/spdx-builder.svg)](https://github.com/philips-software/spdx-builder/releases)

CI/CD tool to generate Bill-of-Materials reports in SPDX format.
> **Status**: Experimental research prototype

</div>

## Contents

- [Description](#Description)
- [Installation](#installation)
- [Usage](#usage)
- [How to test the software](#how-to-test-the-software)
- [Known issues](#known-issues)
- [Contact / Getting help](#contact--getting-help)
- [License](#license)
- [Credits and references](#credits-and-references)

## Description

Converts project dependencies into a standard
[SPDX](https://spdx.github.io/spdx-spec) tag-value Software Bill-of-Materials
file, optionally integrating externally collected and curated license details.

A Bill-of-Materials can be generated from various types of inputs:

1. From the output of
   the [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) (ORT)
   Analyzer tool, optionally in combination with scanned licences provided by
   [License Scanning Service](https://github.com/philips-software/license-scanner)
   or the [BOM-Base](https://github.com/philips-software/bom-base) metadata
   harvesting service. (See [ORT mode usage](docs/usage_with_ort.md))

2. From the REST API of
   a [Synoptic Black Duck](https://www.synopsys.com/software-integrity/security-testing/software-composition-analysis.html)
   SCA server. (See [Black Duck mode usage](docs/usage_with_black_duck.md))

3. From the "tree" output of many build environments, in combination with
   metadata from a [BOM-Base](https://github.com/philips-software/bom-base)
   metadata harvesting service. (See [Tree mode usage](docs/usage_with_tree.md))

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

Separate usage details are found per mode for: [ort mode](docs/usage_with_ort.md)
,[blackduck mode](docs/usage_with_black_duck.md),
and [tree mode](docs/usage_with_tree.md).

_NOTE: This application requires Java 11 or higher._

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
scan, pushes the data to SPDX-builder and can use a self-hosted license scanner
service and upload service like BOM-Bar. 

## How to test the software

The unit test suite is run via the standard Gradle command:

```shell
./gradlew clean test
```

A local ORT-based self-test (if ORT is installed locally) can be run by:

```shell
./gradlew run --args="ort -c src/test/resources/.spdx-builder.yml src/test/resources/ort_sample.yml"
```

## Known issues

(Ticked checkboxes indicate topics currently under development.)

Must-have:

- [x] Properly expand Black Duck origin identifiers to package URLs.
- [ ] Recursively import sub-projects from Black Duck.
- [ ] Abort if ORT Analyzer raised errors.
- [ ] Support the new (more compact) ORT tree structure. (Currently breaks Gradle projects.)
- [ ] Add hashes of build results (where possible).
- [ ] (Optionally) Add source artefacts as "GENERATED_FROM" relationship.

Should-have:

- [ ] Treat internal (=non-OSS) packages differently for output SBOM.
- [ ] Support output "flavors" for the purpose of the generated SBOM.

Other ideas:

- [ ] Integration with [Quartermaster (QMSTR)](https://qmstr.org/).

## Contact / Getting help

Submit tickets to
the [issue tracker](https://github.com/philips-software/spdx-builder/issues).

See the [architecture document](docs/architecture.md) for a detailed technical
description.

## License

See [LICENSE.md](LICENSE.md).

## Credits and references

1. [The SPDX Specification](https://spdx.github.io/spdx-spec) documents the SPDX
   file standard.
2. [The ORT Project](https://github.com/oss-review-toolkit) provides a toolset
   for generating and analyzing various aspects of the Bill-of-Materials.
