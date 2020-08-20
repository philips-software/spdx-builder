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

## Usage
See the command line help for the exact invocation syntax:

`java -jar convert2spdx.jar --help`

This Java application requires Java 11 or higher.

## TO DO List
(Ticked checkboxes indicate topics currently under development.)

Must-have:
- [x] Skip excluded scopes and packages from the ORT Analyzer output.
- [ ] Manually override license to support project-specific license choices.
- [ ] Abort if ORT Analyzer raised errors.
- [x] Expose hierarchical dependencies between product and packages.
- [ ] Pass checksum to scanner and SPDX report.
- [ ] Support non-SPDX licenses. 

Should-have:
- [ ] Support RDF/XML SPDX output format
- [ ] Support output "flavors" for the purpose of the generated SBOM.
- [ ] Include CPE identifiers for correlation with CVE/NVD security vulnerabilities.
- [ ] Include full SBOM copyright information.

Others:
- [ ] Integration with [Quartermaster (QMSTR)](https://qmstr.org/).
