# SPDX-Builder Architecture

## Introduction

### Purpose
This document provides a comprehensive architectural overview of the system,
using a number of different architectural views to depict differnt aspects of
the system. It is intended to convey the significant architectural decisions
which have been made on the system.

### Scope
The system is an **experimental** bill-of-materials formatting tool that
converts the output of OSS Review Toolkit (ORT) to SPDX, while optionally
integrating licenses from the License Scanner Service.

### Definition, Acronyms and Abbreviations
Term | Description
-----|------------
PURL | Package URL
SPDX | "The Software Package Data Exchange" - An open standard for communicating software bill of material information, including components, licenses, copyrights, and security references. SPDX reduces redundant work by providing a common format for companies and communities to share important data, thereby streamlining and improving compliance.

### References
- [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort)
- [License Scanner Service](https://github.com/philips-internal/license-scanner)
- [SPDX License list](https://spdx.org/licenses/)

## Goals and constraints
Goals of SPDX-Builder are:

1. Produce bill-of-materials reports in SPDX format for (almost) any software
development projects.
2. Integrate independently scanned package licenses.

The stakeholders of this application are:

- CI/CD operators, responsible for running automated build pipelines.
- All consumers of SPDX bill-of-materials reports.

The most significant requirements are:

- The tool shall be easy to integrate into CI/CD build pipelines.
- The generated bill-of-materials report shall conform to the (latest) SPDX
  standard.
- Licenses from an optional external license scanner shall be integrated in the
  report.
- The tool shall leverage existing open source tooling where possible.

Design constraints are:

- Maintainability: Code must be easy to maintain for average programmers. This
  is why the code tries to adhere to "Clean Code" guidelines, and a strict
layering model is applied.

## Use-Case view

### Generate SPDX bill-of-materials report
1. CI/CD pipeline starts bill-of-materials extraction.
2. An external (open source) pre-processing tool is used to extract the package
structure and metadata from the package manager used in the project.
3. The tool unifies the report format into the SPDX format.

### Integrate scanned licenses
1. The tool reads packages and metadata from the pre-processing tool.
2. The tool submits each package with its source code location to an external license scanner service.
3. The license scanning service provides its latest scan result (if available) for the package.
4. The tool integrates the license information with the package metadata.

## Logical view
### Overview
SPDX-Builder is a command-line application that converts the output of the [OSS
Review Toolkit (ORT)](https://github.com/oss-review-toolkit/ort) Analyzer tool
into a SPDX bill-of-materials report in tag-value format, while (optionally)
merging license information scanned from package source code by the [License
Scanner service](https://github.com/philips-internal/license-scanner).

### Bill-of-materials
The domain for this application consists of the classes and relations depicted
in the figure below:

![UML class diagram](domain.png "Domain classes modeling a bill-of-materials")

A `BillOfMaterials` is a container that holds `Package`s that are connected by
typed `Relation`s. All other information is represented in attibutes of these
domain classes.

### Integration of license scan results
If the URL to a license scanner is specified, the source code location (if
available) of every package is submitted for scanning. The license scanner
immediately responds with available license information, or else schedules the
package for scanning later. This leads to two licenses per package:

- "Declared license" is the license provided in the metadata from the package
  manager.
- "Detected license" is the license independently scanned from the package
  source code files.

SPDX-Builder uses these licenses to derive a "Concluded" license per package:

1. If the declared and detected match or no detected license is available
(yet), the concluded license defaults to the declared license.
2. If no declared license is available, then the detected license (if
available) is assumed.
3. If the detected license was confirmed by a human curator, then it overrides
the declared license.
4. if the detected license does not match the declared license but was not
confirmed by a human curator, then the declared license is used and the
mismatch indicated to the license scanner by "contesting" the detected license.

## Process view
### Single threaded application
The appliation runs on a single thread, blocking at every request to the
license scanner service. Since most products consist of up to 1000 unique
(transitive) dependencies and all requests are targeting the same server,
parallelizing these requests would not dramatically speed up processing. For
bigger projects most time is actually spent in parsing and sequentially writing
files.

## Deployment view
The application is a stand-alone Java executable that is invoked from the
command line.

## Implementation view
### Overview
The service is coded in plain Java because no complex infrastructure is used
that would benefit from an application framework.

### Layers
The figure below explains how the layers are represented in the source code:

![UML class diagram](layers.png "Implementation of logical layers and their dependencies")

The implication of this design is that invocation from the command line is
completely decoupled from the domain, because the controller layer can only
interact with the domain through a service facade using DTO POJO classes.
Domain objects are never directly exposed to the controller layer.

The "interactor" classes provide the implementation of the service definitions
to import domain classes from persistent storage, invoke the appropriate
methods on domain classes, and persist the resulting domain objects.

Persistence provides all interfaces to the outside world:

- Reading the ORT result file.
- Interaction with the license scanner service.
- Exporting the bill-of-materials as an SPDX file.

### Command line handling
To provide a POSIX-compliant CLI, the [picocli](https://picocli.info) library
is used. It converts command line parameters into annotated property fields,
and generates usage instructions using these annotations.

### Bill-of-materials persistence
The `ConversionStore` provides a symmetric interface for reading and writing a
bill-of-materials in a specified flavor. The `ConversionPersistence` implements
only the currently relevant conversions, but could be extended to additional
formats for both input and output.

(End of document)
