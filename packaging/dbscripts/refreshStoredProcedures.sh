#!/bin/sh

cd "$(dirname "$0")"
. ./dbfunctions.sh
. ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})
    -s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})
    -p PORT       - The database port for the database        (def. ${PORT})
    -u USERNAME   - The username for the database             (def. engine)
    -d DATABASE   - The database name                         (def. ${DATABASE})

__EOF__
}

while getopts hvl:s:p:u:d: option; do
    case "${option}" in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) VERBOSE=true;;
        l) LOGFILE="${OPTARG}";;
        s) SERVERNAME="${OPTARG}";;
        p) PORT="${OPTARG}";;
        u) USERNAME="${OPTARG}";;
        d) DATABASE="${OPTARG}";;
    esac
done

#Dropping all views & sps
drop_views
drop_sps

#Refreshing  all views & sps
refresh_views
refresh_sps
