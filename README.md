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
### Basic usage
This application requires Java 11 or higher.

The input of SDPX-Builder is the YAML file that is output by the 
[OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) Analyzer.
This tool interprets the configuration files of many package managers to
and outputs the information found to a common ORT file format.

Typical command line invocation of ORT Analyzer is:
```
ort analyze -i <project_directory> -o <result_directory>
```
_Note: To avoid the "this and base files have different roots" error, it is
better to provide an absolute path._

The ORT Analyzer produces an `analyzer-result.yml` file in the indicated 
result directory containing the bill-of-materials of all identified packages 
in ORT format. (Note that the tool fails if an ORT file already exists.)

This output of the Analyzer can be converted to an SPDX tag-value file using:
```
java -jar <path>/spdx-builder.jar -i <ort_yaml_file>
```
The output is written to a file named `bom.spdx` in the current directory.
By adding `-o <filename>`, the name of the output file can be specified.
(If the file has no extension, `.spdx` is automatically appended.)

### Integration with license scanner service
To submit packages to the [License Scanner Service](https://github.com/philips-labs/license-scanner)
and use the scan results as "detected licenses", add the location of the
License Scanner service:
```term
java -jar <path>/spdx-builder.jar -i <ort_yaml_file> --scanner <scanner_url>
```

### Suppressing directories and scopes from the BOM
SPDX-Builder takes exclusions specified to the ORT Analyzer into account. These
exclusions can be configured in an `.ort.yml` file in the root of the project. 
```yaml
excludes:
  paths:
  - pattern: <glob_pattern>
    reason: <path_reason>
    comment: "Free text comment"
  scopes:
  - pattern: <glob_pattern>
    reason: <scope_reason>
    comment: "Free text comment"
```
Where <glob_pattern> follows the [common glob format](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob).

The <path_reason> is any of:
- BUILD_TOOL_OF
- DATA_FILE_OF
- DOCUMENTATION_OF
- EXAMPLE_OF
- OPTIONAL_COMPONENT_OF
- OTHER
- PROVIDED_BY
- TEST_OF

and <scope_reason> is any of:
- BUILD_DEPENDENCY_OF
- DEV_DEPENDENCY_OF
- PROVIDED_DEPENDENCY_OF
- TEST_DEPENDENCY_OF
- RUNTIME_DEPENDENCY_OF

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
