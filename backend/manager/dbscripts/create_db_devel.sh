#!/bin/bash
#include db general functions
source ./dbfunctions.sh

SERVERNAME="127.0.0.1"
DATABASE="engine"
USERNAME="postgres"

while getopts :hs:d:u:p:l:f:v option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        v) VERBOSE=true;;
        f) UUID=$OPTARG;;
        h) usage;;
    esac
done

printf "Running original create_db script...\n"
./create_db.sh   -s $SERVERNAME -d $DATABASE -u $USERNAME -f $UUID;
if [ $? -ne 0 ]
    then
      printf "Failed to create database ${DATABASE}\n"
      exit 1;
fi
printf "Setting development configuration values ...\n"
execute_file "fill_config_devel.sql" ${DATABASE} > /dev/null
ret=$?
printf "Development setting done.\n"
exit $?
