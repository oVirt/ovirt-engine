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
. ./dbfunc-custom.sh

cleanup() {
    dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database             (def. ${DBFUNC_DB_USER})
    -d DATABASE   - The database name                         (def. ${DBFUNC_DB_DATABASE})
    -m MD5DIR     - The directory for generated MD5 files     (def. ${DBFUNC_COMMON_MD5DIR})
    -c            - Force cleaning tasks and compensation info.

__EOF__
}

while getopts hvl:s:p:u:d:m:c option; do
    case "${option}" in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) DBFUNC_VERBOSE=1;;
        l) DBFUNC_LOGFILE="${OPTARG}";;
        s) DBFUNC_DB_HOST="${OPTARG}";;
        p) DBFUNC_DB_PORT="${OPTARG}";;
        u) DBFUNC_DB_USER="${OPTARG}";;
        d) DBFUNC_DB_DATABASE="${OPTARG}";;
        m) DBFUNC_COMMON_MD5DIR="${OPTARG}";;
        c) CLEAN_TASKS=1;;
    esac
done

dbfunc_common_upgrade
