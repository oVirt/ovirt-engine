#!/bin/bash

#This script checks if the schema checksum as calculated from the scripts folder andstored in version.info
#equals to the schema checksum as calculated from the database

die () {
    printf >&2 "$@"
    exit 1
}

CURRENT_DIR=`dirname $(readlink -f "$0")`
VERSION_INFO_DIR=/etc/engine
DBSCRIPTS_DIR="$CURRENT_DIR"/..
DBNAME=engine
USERNAME=postgres
while getopts d:u:f: option; do
  case $option in
    d) DBNAME=$OPTARG;;
    u) USERNAME=$OPTARG;;
    f) VERSION_INFO_DIR=$OPTARG;;
  esac
done
VERSION_INFO_FILE="$VERSION_INFO_DIR"/version.info
if [ ! -e "$VERSION_INFO_FILE" ]; then
  echo "Version file $VERSION_INFO_FILE does not exist"
  exit 1
fi
EXPECTED_DB_SCHEMA_CHECKSUM=`grep DB_SCHEMA_CHECKSUM "$VERSION_INFO_DIR"/version.info | cut -d "=" -f2`
CALCULATED_DB_SCHEMA_CHECKSUM=`"$CURRENT_DIR"/calc_db_schema_checksum.sh -u $USERNAME -d $DBNAME` || die "could not calculate checksum of database schema"
echo "expected checksum is $EXPECTED_DB_SCHEMA_CHECKSUM"
echo "calculated checksum is $CALCULATED_DB_SCHEMA_CHECKSUM"
if [ "$EXPECTED_DB_SCHEMA_CHECKSUM" == "$CALCULATED_DB_SCHEMA_CHECKSUM" ]; then
  RETVAL=0
else
  RETVAL=1
fi

exit $RETVAL

