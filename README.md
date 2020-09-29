# SPDX-Builder

## Introduction
This command line tool converts package information from 
[OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) (ORT) Analyzer 
YAML files together with curated license scan results from a
[License Scanning Service](https://github.com/philips-labs/license-scanner)
backend service into an [SPDX 2.2](https://spdx.github.io/spdx-spec/) software 
bill-of-material (SBOM) file.

This tool can be used in combination with the ORT Analyzer in CI/CD pipelines 
to automatically generate an SPDX 2.2 SBOM for a many types of package manager-based 
projects. (See the ORT documentation.)

## License scanner
The SBOM includes scanned licenses as "detected", and only overrides the "declared" 
license from the package manager when the scanner indicates it was "confirmed", or
the package manager did not provide a declared license.

## Usage
See the command line help for the exact invocation syntax:

`java -jar spdx-builder.jar --help`

This Java application requires Java 11 or higher.

## TO DO List
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
