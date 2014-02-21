#!/bin/sh

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
cd "$(dirname "$0")"
. ./dbfunctions.sh
. ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output         (def. ${LOGFILE}
    -s SERVERNAME - The database servername for the database (def. ${SERVERNAME})
    -p PORT       - The database port for the database       (def. ${PORT})
    -u USERNAME   - The username for the database            (def. engine)
    -d DATABASE   - The database name                        (def. ${DATABASE})
    -m MD5DIR     - The directory for generated MD5 files    (def. ${MD5DIR}
    -g NOMD5      - Do not generate MD55 for files           (def. ${NOMD5}
    -c            - Force cleaning tasks and compensation info.

__EOF__
}

while getopts hvl:s:p:u:d:m:gc option; do
    case "${option}" in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) VERBOSE=true;;
        l) LOGFILE="${OPTARG}";;
        s) SERVERNAME="${OPTARG}";;
        p) PORT="${OPTARG}";;
        u) USERNAME="${OPTARG}";;
        d) DATABASE="${OPTARG}";;
        m) MD5DIR="${OPTARG}";;
        g) NOMD5=true;;
        c) CLEAN_TASKS=true;;
    esac
done

run_upgrade_files
