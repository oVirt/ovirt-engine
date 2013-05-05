#!/bin/bash

################################################################################
# Upgrade script wrapper for handling each Schema or data change.
# Each upgrade change should be in a separate file formatted  by MM_mm_nnnn_[Name].sql
# where:
#   MM  indicates Major Version number
#   mm indicates Minor Version number
#   nnnn are numbers starting from 0010, each having an offset of 10 from previous script
#   (i.e 0010 0020 ....)
#   [Name] is a short descriptive name for the script.
#   All changes should be located under the upgrade/ directory
#
#   When using -f <version> all scripts with version greater than the given one will
#   be re-executed , for example -f 0300100 will execute all scripts from 03000110
#   and forth
# Since all views $ SP are dropped before upgrade and restored after all upgrade
# script were executed. We may have cases in which we need to run some helper
# functions before the upgrade script runs.
# In such a case and when those functions can not be put in the common_sp.sql
# because they are dependant on objects created by an upgrade script, we can put
# one or several lines at the begining of our upgrade file:
# --#source <sql_file_name>_sp.sql
# for example , putting in an upgrade script
# --#source myfunctions_sp.sql
# will run myfunctions_sp.sql before the upgrade script is executed.
#
# Each script must be re-entrant (i.e. each script checks if each step is needed
# to be executed or not, so we can run the same script many times without any
# problems)
################################################################################

#include db general functions
pushd $(dirname ${0})>/dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] [-d DATABASE] [-u USERNAME] [-f VERSION] [-c] [-g] [-m MD5DIR] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database       (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                        (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database            (def. engine)\n"
    printf "\t-l LOGFILE    - The logfile for capturing output         (def. ${LOGFILE}\n"
    printf "\t-f VERSION    - Force upgrading from specified version   (def. ${VERSION}\n"
    printf "\t-c            - Force cleaning tasks and compensation info.\n"
    printf "\t-g NOMD5      - Do not generate MD55 for files (generated in dev env only) (def. ${NOMD5}\n"
    printf "\t-m MD5DIR     - The directory for generated MD5 files (generated in dev env only) (def. ${MD5DIR}\n"
    printf "\t-v            - Turn on verbosity (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    popd>/dev/null
    exit $ret
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

while getopts hs:d:u:p:l:f:m:gcv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        f) VERSION=$OPTARG;;
        c) CLEAN_TASKS=true;;
        m) MD5DIR=$OPTARG;;
        g) NOMD5=true;;
        v) VERBOSE=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

run_upgrade_files

ret=$?
printf "Done.\n"
popd>/dev/null
exit $ret
