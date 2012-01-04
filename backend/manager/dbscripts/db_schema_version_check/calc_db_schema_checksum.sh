#!/bin/bash
# selects all current checksums for all db scripts and performs md5sum on them (md5 on the list of md5 values)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DBSCRIPTS_DIR=$DIR/../../dbscripts
service postgresql status>/dev/null 2>&1

source $DBSCRIPTS_DIR/dbfunctions.sh
source $DBSCRIPTS_DIR/dbcustomfunctions.sh
set_defaults
while getopts d:u: option; do
    case $option in
	d) DATABASE=$OPTARG;;
	u) USERNAME=$OPTARG;;
    esac
done
if [ $?  -eq 0 ]; then
  LOGFILE=target/calc_db_schema_checksum.log
  CMD="select trim(checksum) from schema_version where state='INSTALLED' order by script"
  execute_command "$CMD" ${DATABASE}> /tmp/md5_values_db
  #following two lines make sure the format of the result from the db does not contain a space before each checksum, and a newline
  #in order for it to match to the file that is obtained from the folder scanning. This is required as the selection returns for each row a space before
  #the values and generates a new line
  cat /tmp/md5_values_db | cut -d " " -f2 >/tmp/md5_values_db_2
  sed -i '$d' /tmp/md5_values_db_2
  RETVAL=`cat /tmp/md5_values_db_2 | md5sum | cut -d " " -f1`
  rm -rf /tmp/md5_values_db*
else
  RETVAL=-1
fi
echo $RETVAL


