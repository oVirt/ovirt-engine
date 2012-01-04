#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
STORE_FILE=$DIR/../../../../ear/target/version.info
UPGRADE_SCRIPTS_DIR=$DIR/../../dbscripts/upgrade
echo "Storing schema checksum"
touch $STORE_FILE
sed -i "/DB_SCHEMA_CHECKSUM/d" $STORE_FILE

rm -rf /tmp/md5_values
for file in $UPGRADE_SCRIPTS_DIR/??_??_????*.sql; do
   ADD_SCHEMA_VERSION_SCRIPT=$(basename $file)
#For the first script 03_00_0000_add_schema_version.sql - there is a special entry created in db installation with checksum 0
   if [ "$ADD_SCHEMA_VERSION_SCRIPT" == "03_00_0000_add_schema_version.sql" ]; then
      checksum="0"
   else
      checksum=`echo $(md5sum $file) | cut -d " " -f1`
   fi
   echo $checksum >>/tmp/md5_values
done
# The following line prints the md5 values file, each line is in format of "checksum - filename" so for each line it takes only the checksum
# into the calculation of the md5
CALCULATED_DB_SCHEMA_CHECKSUM=`cat /tmp/md5_values | md5sum | cut -d " " -f1`
rm -rf /tmp/md5_values
echo DB_SCHEMA_CHECKSUM=$CALCULATED_DB_SCHEMA_CHECKSUM>>$STORE_FILE


