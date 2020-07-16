# SPDX-Builder

## Introduction
This command line tool converts package information with license scan results 
into SPDX 2.2 bill-of-material files.

Package information is read from 
[OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) Analyzer YAML 
files.

License scanning is delegated via the 
[License Scanning Service](https://github.com/philips-labs/license-scanner) 
REST API.

## Usage
See the command line help:

`java -jar convert2spdx.jar --help`

## TO DO List
- Support a list of licenses scanned from files
- Support non-SPDX licenses (in input and output)
- Support SPDX RDF/XML output format
