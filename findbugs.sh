#!/bin/sh -ex

# This project needs to exclude certain findbugs checks. Some of these
# exclussions are common to all the modules of the projects, but some
# are specific to some modules. To handle this there is a global filters
# file and then one optional filters file per module. As the version of
# the Maven findbugs plugin that we use (3.0.2) doesn't support multiple
# filters files, we need to use a custom plugin that merges them before
# calling findbugs. To use this plugin, instead of the typical "mvn
# findbugs:findbugs" command you have to use the following:

mvn \
    org.ovirt.maven.plugins:ovirt-findbugs-maven-plugin:findbugs \
    -Pfindbugs-general

# The "-Pfindbugs-general" is a profile that enables the general
# filters, and it is optional.
