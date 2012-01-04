#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
EXPECTED_DB_SCHEMA_CHECKSUM=`grep DB_SCHEMA_CHECKSUM /etc/engine/version.info | cut -d "=" -f2`
DBNAME=engine
USERNAME=postgres

while getopts d:u: option; do
    case $option in
	d) DBNAME=$OPTARG;;
	u) USERNAME=$OPTARG;;
    esac
done

CALCULATED_DB_SCHEMA_CHECKSUM=`$DIR/calc_db_schema_checksum.sh -u $USERNAME -d $DBNAME`

echo "expected checksum is "$EXPECTED_DB_SCHEMA_CHECKSUM
echo "calculated checksum is "$CALCULATED_DB_SCHEMA_CHECKSUM

if [ "$EXPECTED_DB_SCHEMA_CHECKSUM" == "$CALCULATED_DB_SCHEMA_CHECKSUM" ]; then
	RETVAL=0
else
	RETVAL=1
fi

exit $RETVAL

