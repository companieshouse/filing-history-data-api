#!/bin/bash

PORT=8080

exec java -jar -Dserver.port="${PORT}" "filing-history-data-api.jar"