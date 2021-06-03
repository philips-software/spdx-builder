# Building a Bill-of-Materials from a Black Duck project version

_NOTE: This function requires access to the "Hierarchical BOM" REST API of
a [Synoptic Black Duck SCA server](https://www.synopsys.com/software-integrity/security-testing/software-composition-analysis.html)
. (See below for instructions on enabling this option on a self-managed
server.)_

## Usage 
SPDX-Builder can extract a bill-of-materials from a Black Duck server for a
specified project version.

A project version in Black Duck is exported to an SPDX file by:

```shell
spdx-builder blackduck -o <output_file> <project> <version> --url <server_url> --token <access_token>
```

_Note: If no "output_file" is specified, the output is written to a file named
`bom.spdx` in the current directory. If the file has no extension, `.spdx`
is automatically appended._

_Note: The "project" and "version" can be limited to the unique prefixes for a
project version._

_Note: The server URL and access token default to values found in
the `BLACKDUCK_URL` and `BLACKDUCK_API_TOKEN` environment variables._

## Enabling the "Hierarchical BOM API" on the server

To enable the Hierarchical BOM in the Black Duck server in case of a Docker
Swarm installation:

- Add the `HUB_HIERARCHICAL_BOM` environment variable to an `.env` file. Set the
  value to `true`.

or alternatively:

- Edit the webapp service in the `docker-compose.local-overrides.yml`
  file located in the docker-swarm directory: `webapp:environment:
  {HUB_HIERARCHICAL_BOM: "true"}`.

In case of a Kubernetes or OpenShift installation:

- Add the following to your environs
  flag: `--environs HUB_HIERARCHICAL_BOM:true`

