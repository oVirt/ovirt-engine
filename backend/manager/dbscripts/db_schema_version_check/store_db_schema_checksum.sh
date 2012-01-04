#!/bin/bash


#This script stores calculates the md5 sum of all scripts by
#traversing the dbscript folder, and for each script calculating the md5,
#and then calculating an md5 for the list of md5 values. The result is
#stored in a file named version.info which is by default located under ear/version.info

die () {
    printf >&2 "$@"
    exit 1
}

CURRENT_DIR=`dirname $(readlink -f "$0")`
STORE_FILE="$CURRENT_DIR"/../../../../ear/target/version.info
UPGRADE_SCRIPTS_DIR="$CURRENT_DIR"/../upgrade
echo "Storing schema checksum"
touch $STORE_FILE
sed -i "/DB_SCHEMA_CHECKSUM/d" "$STORE_FILE" || die "error in manipulating version.info file"

CHECKSUMS_FILE=`mktemp`
for SCRIPT_FILE in "$UPGRADE_SCRIPTS_DIR"/??_??_????*.sql; do
  ADD_SCHEMA_VERSION_SCRIPT=$(basename $SCRIPT_FILE)
  CHECKSUM_VALUE=`md5sum $SCRIPT_FILE| sed 's/ .*//g'`
#For the first script 03_00_0000_add_schema_version.sql - 
#there is a special entry created  in db installation 
#with checksum 0
  if [ "$ADD_SCHEMA_VERSION_SCRIPT" == "03_00_0000_add_schema_version.sql" ]; then
    CHECKSUM_VALUE="0"
  fi
  echo $CHECKSUM_VALUE >>$CHECKSUMS_FILE
done

# The following lines calculate the MD5 of the checksums file, remove the part
# after the space in the checksum value, and store the result in the store file
CALCULATED_DB_SCHEMA_CHECKSUM=`md5sum "$CHECKSUMS_FILE" | sed 's/ .*//g'`
echo "DB_SCHEMA_CHECKSUM=$CALCULATED_DB_SCHEMA_CHECKSUM">>"$STORE_FILE" || die "error in storing checksum to $STORE_FILE"
exit 0


