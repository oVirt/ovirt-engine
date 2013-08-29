#!/bin/sh
################################################################
# This script change the ownership of objects in the ${DATABASE}
# database from ${FROM_USER} to ${TO_USER}
################################################################

#include db general functions
cd "$(dirname "$0")"
source ./common.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
    Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-d DATABASE] -f FROM_USER -t TO_USER
    -s SERVERNAME - The database servername for the database (def. ${SERVERNAME})
    -p PORT       - The database port for the database (def. ${PORT})
    -d DATABASE   - The database name (def. ${DATABASE})
    -f FROM_USER  - The current owner for the database
    -f TO_USER    - The new owner for the database
    -h            - This help text.\n"
__EOF__
    exit $ret
}


while getopts hs:p:d:f:t: option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        f) FROM_USER=$OPTARG;;
        t) TO_USER=$OPTARG;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

if [[ ! -n "${FROM_USER}" || ! -n "${TO_USER}" ]]; then
   usage
fi

# Change all schema objects ownership
echo "Changing database ${DATABASE} objects ownership"
pg_dump -s -h "${SERVERNAME}" -p ${PORT} -U ${FROM_USER} "${DATABASE}" | grep -i 'owner to' | sed "s/OWNER TO ${FROM_USER};/OWNER TO ${TO_USER};/i" | psql -h "${SERVERNAME}" -p ${PORT} -U ${FROM_USER} "${DATABASE}"

if [[ $PIPESTATUS -ne 0 ]]; then
    echo "Failed to change DB ${DATABASE} objects ownership."
    exit 1
fi

#change the DB ownership
echo "Changing database ${DATABASE} ownership"
cmd="ALTER DATABASE ${DATABASE} OWNER TO ${TO_USER};"
psql -w -h "${SERVERNAME}" -p ${PORT} --pset=tuples_only=on --set ON_ERROR_STOP=1 -c "${cmd}" -U ${FROM_USER} -d "${DATABASE}"

if [ $? -ne 0 ]; then
    echo "Failed to change DB ${DATABASE} ownership."
    exit 2
fi

echo "Changing database ${DATABASE} ownership from ${FROM_USER} to ${TO_USER} completed successfully."
exit 0
