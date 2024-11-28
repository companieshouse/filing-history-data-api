# `filing-history-data-api`

## Summary

The `filing-history-data-api` is a service that receives filing history deltas from
`filing-history-data-consumer`. It transforms these deltas to a standardised structure and then:

* stores or deletes documents within the `company_filing_history collection` in MongoDB, and
* enqueues a resource changed message that triggers further downstream processing.

The service is implemented in Java 21 using Spring Boot 3.2

## System requirements

* [Git](https://git-scm.com/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads)
* [Maven](https://maven.apache.org/download.cgi)
* [MongoDB](https://www.mongodb.com/)

## Building and Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and
   follow the steps in the
   README.
2. Enable the following services using the command `./bin/chs-dev services enable <service>`.
    * `filing-history-data-api`

3. Boot up the services' containers on docker using tilt `tilt up`.

## Environment variables

| Variable                           | Description                                                                           | Example (from docker-chs-development) |
|------------------------------------|---------------------------------------------------------------------------------------|---------------------------------------|
| FILING_HISTORY_API_SUB_DELTA_KEY   | The client ID of an API key, with internal app privileges, to call chs-kafka-api with | abc123def456ghi789                    |
| CHS_KAFKA_API_URL                  | The URL which the chs-kafka-api is hosted on                                          | http://api.chs.local:4001             |
| PORT                               | The port at which the service is hosted in ECS                                        | 8080                                  |
| LOGLEVEL                           | The level of log messages output to the logs                                          | debug                                 |
| HUMAN_LOG                          | A boolean value to enable more readable log messages                                  | 1                                     |
| RESOURCE_CHANGED_CALL_DISABLED     | Toggles whether resource-changed calls are posted.                                    | false                                 | 
| DELETE_CHILD_TRANSACTIONS_DISABLED | Toggles the deletion of child transactions feature.                                   | false                                 | 

## Building the docker image

```bash
mvn compile jib:dockerBuild
```

## To make local changes

Development mode is available for this service
in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

```bash
./bin/chs-dev development enable filing-history-data-api
```

This will clone the `filing-history-data-api` into the `./repositories` folder. Any changes to the
code, or resources will automatically trigger a rebuild and relaunch.

## Makefile Changes

The jacoco exec file that SonarQube uses on GitHub is incomplete and, therefore, produces incorrect test coverage
reporting when code is pushed up to the repo. This is because the `analyse-pull-request` job runs when we push code to
an open PR and this job runs `make test-unit`.
Therefore, the jacoco exec reporting only covers unit test coverage, not integration test coverage.

To remedy this, in the
short-term, we have decided to change the `make test-unit` command in the Makefile to run
`mvn clean verify -Dskip.unit.tests=false -Dskip.integration.tests=false` instead as this
will ensure unit AND integration tests are run and that coverage is added to the jacoco reporting and, therefore,
produce accurate SonarQube reporting on GitHub.

For a more in-depth explanation, please
see: https://companieshouse.atlassian.net/wiki/spaces/TEAM4/pages/4357128294/DSND-1990+Tech+Debt+Spike+-+Fix+SonarQube+within+Pom+of+Projects

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from
terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the
CICD platform 'Concourse'.

| Application specific attributes | Value                                                                                                                                                                                                                                                              | Description                                         |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| **ECS Cluster**                 | public-data                                                                                                                                                                                                                                                        | ECS cluster (stack) the service belongs to          |
| **Load balancer**               | {env}-chs-apichgovuk <br> {env}-chs-apichgovuk-private                                                                                                                                                                                                             | The load balancer that sits in front of the service |
| **Concourse pipeline**          | [Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/filing-history-data-api) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/filing-history-data-api) | Concourse pipeline link in shared services          |

### Contributing

- Please refer to
  the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated)
  for detailed information on the infrastructure being deployed.

### Testing

- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see
  the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** Slack channel.

### Vault Configuration Updates

- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please
  consult with the **#platform** team and submit a workflow request.

### Useful Links

- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
