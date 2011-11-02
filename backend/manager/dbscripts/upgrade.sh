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
# Each script must be re-entrant (i.e. each script checks if each step is needed
# to be executed or not, so we can run the same script many times without any
# problems)
################################################################################

#include db general functions
if [ -e ./dbfunctions.sh ]; then
    source ./dbfunctions.sh
    source ./dbcustomfunctions.sh
else
    printf "upgrade script should be run from database scripts directory\n"
    exit 1
fi

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-d DATABASE] [-u USERNAME] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-d DATABASE   - The database name                        (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database.\n"
    printf "\t-l LOGFILE    - The logfile for capturing output         (def. ${LOGFILE}\n"
    printf "\t-v            - Turn on verbosity (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"

    exit 0
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

while getopts hs:d:u:p:l:v option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        v) VERBOSE=true;;
        h) usage;;
    esac
done

#Dropping all views & sps
drop_views
drop_sps

# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} > /dev/null

printf "Upgrading database ...\n"
run_upgrade_files

#Refreshing  all views & sps
refresh_views
refresh_sps

printf "Done.\n"
ret=$?
exit $ret
