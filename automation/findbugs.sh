#!/bin/bash

mvn clean install findbugs:findbugs -U -DskipTests \
    -s ${MAVEN_SETTINGS}
