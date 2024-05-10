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

    mvn compile jib:dockerBuild

## To make local changes

Development mode is available for this service
in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable filing-history-data-api

This will clone the `filing-history-data-api` into the `./repositories` folder. Any changes to the
code, or resources will automatically trigger a rebuild and relaunch.
