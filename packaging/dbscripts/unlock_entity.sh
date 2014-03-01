#!/bin/sh

cd "$(dirname "$0")"
. ./dbfunctions.sh
. ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
Usage: $0 [options] [ENTITIES]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})
    -s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})
    -p PORT       - The database port for the database        (def. ${PORT})
    -u USERNAME   - The username for the database             (def. engine)
    -d DATABASE   - The database name                         (def. ${DATABASE})
    -t TYPE       - The object type {vm | template | disk | snapshot}
    -r            - Recursive, unlocks all disks under the selected vm/template.
    -q            - Query db and display a list of the locked entites.
    ENTITIES      - The list of object names in case of vm/template, UUIDs in case of a disk

__EOF__
}

while getopts hvl:s:p:u:d:t:rq option; do
    case $option in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) VERBOSE=true;;
        l) LOGFILE=$OPTARG;;
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        t) TYPE=$OPTARG;;
        r) RECURSIVE=true;;
        q) QUERY=true;;
    esac
done

shift $(( $OPTIND - 1 ))
IDS="$@"

[ -n "${TYPE}" ] || die "Please specify type"
[ -z "${IDS}" -a -z "${QUERY}" ] && die "Please specify ids or query"
[ -n "${IDS}" -a -n "${QUERY}" ] && die "Please specify one ids or query"

if [ -n "${IDS}" ]; then
    echo "Caution, this operation may lead to data corruption and should be used with care. Please contact support prior to running this command"
    echo "Are you sure you want to proceed? [y/n]"
    read answer
    [ "${answer}" = "y" ] || die "Please contact support for further assistance."

    for ID in ${IDS} ; do
        unlock_entity "${TYPE}" "${ID}" "$(whoami)" ${RECURSIVE}
    done
elif [ -n "${QUERY}" ]; then
    query_locked_entities "${TYPE}"
fi
