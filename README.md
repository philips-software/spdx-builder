# SPDX-Builder

**Description**: Converts dependencies for many package managers into a standard
[SPDX](https://spdx.github.io/spdx-spec) tag-value Software Bill-of-Materials file, 
optionally integrating externally detected and curated license details.

Inputs for the SBOM are:
* Package information by YAML files from 
[OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) (ORT) Analyzer.
* Curated license scan results from the REST API of a 
[License Scanning Service](https://github.com/philips-labs/license-scanner)
backend service.

**Status**: Research prototype

This is an experimental application for generating reference data in the context
of [investigations into Bill-of-Materials and open source licensing](https://gitlab.ta.philips.com/swat/bom/comparison).

## Dependencies

This software requires Java 11 (or later) to run.

## Installation

Build the application using the standard gradle command:
```
./gradlew clean build
```
Then copy the resulting JAR file from the `build/libs` directory.

## Configuration

All configuration is available through command line parameters.

## Usage

See the command line help for the exact invocation syntax, with the exact right java version installed:
```
java -jar spdx-builder.jar --help
```

This application requires Java 11 or higher.

You can also use gradle to provide you with the proper java environment:
```
./gradlew -q run --args='--help'
```

## How to test the software

The unit test suite is run via the standard Gradle command:
```
./gradlew clean test
```

## Known issues
(Ticked checkboxes indicate topics currently under development.)

Must-have:
- [x] Expose hierarchical dependencies between product and packages.
- [ ] Manually override concluded license (to support project-specific license choices).
- [ ] Abort if ORT Analyzer raised errors.
- [ ] Pass checksum to scanner and SPDX report.
- [ ] Support non-SPDX licenses. 

Should-have:
- [ ] Support output "flavors" for the purpose of the generated SBOM.
- [ ] Include CPE identifiers for correlation with CVE/NVD security vulnerabilities.
- [ ] Include full SBOM copyright information.

Others:
- [ ] Support RDF/XML SPDX output format
- [ ] Integration with [Quartermaster (QMSTR)](https://qmstr.org/).

## Contact / Getting help

Submit tickets to the [issue tracker](https://github.com/philips-labs/spdx-builder/issues).

## License

See [LICENSE.md](LICENSE.md).

## Credits and references

1. [The SPDX Specification](https://spdx.github.io/spdx-spec) documents the SPDX file
standard.
2. [The ORT Project](https://github.com/oss-review-toolkit) provides a toolset for
generating and analyzing various aspects of the Bill-of-Materials.
