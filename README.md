# SPDX-Builder

## Introduction
This command line tool converts bill-of-materials information from various 
sources into SPDX 2.1 formats.

Currently supported inputs are:
- [OSS Review Toolkit](https://github.com/oss-review-toolkit/ort) Analyzer 
YAML files
- [License Service](https://github.com/philips-labs/license-scanner) REST API

## Usage
See the command line help:

`java -jar convert2spdx.jar --help`

## TO DO List
- Read ORT analyzer file into model
- Retrieve licenses from License Service
- Support non-SPDX licenses



