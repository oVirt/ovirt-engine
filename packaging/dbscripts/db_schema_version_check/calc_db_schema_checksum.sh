#!/bin/bash
#This scripts calculate the value of the database schema checksum by performing an MD5 on all the MD5 values of
#The installed scripts, as specified in the schema_version table
die () {
    printf >&2 "$@"
    exit 1
}

RETVAL=-1
DBSCRIPTS_DIR=`dirname $(readlink -f "$0")`/..
service postgresql status>/dev/null 2>&1  || die "postgresql is not running"

source $DBSCRIPTS_DIR/dbfunctions.sh  || die "dbfunctions.sh script does not exist"
source $DBSCRIPTS_DIR/dbcustomfunctions.sh || die "dbcustomfunctions.sh script does not exist"

set_defaults
while getopts d:u: option; do
  case $option in
    d) DATABASE=$OPTARG;;
    u) USERNAME=$OPTARG;;
  esac
done

LOGFILE=/tmp/calc_db_schema_checksum.log
CMD="select trim(checksum) from schema_version where state='INSTALLED' order by script"
MD5_FILE=`mktemp`
execute_command "$CMD" ${DATABASE}>"$MD5_FILE" || die "execution of SQL to get checksum from DB has failed"

#following two lines make sure the format of the result from the db does not contain a space 
#before each checksum, and a newline in order for it to match to the file that is obtained 
#from the folder scanning. This is required as the selection returns for each row a space before
#the values and generates a new line
RETVAL=`sed '$d' "$MD5_FILE" | sed 's/ //g' | md5sum | sed 's/ .*//g'`
echo $RETVAL
exit 0


