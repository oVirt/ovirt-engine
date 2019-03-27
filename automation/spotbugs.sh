#!/bin/bash
mvn -v
mvn clean install com.github.spotbugs:spotbugs-maven-plugin:spotbugs -U -DskipTests -s ${MAVEN_SETTINGS}
