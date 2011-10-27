#!/bin/bash

# restore_db.sh
if [[ $1 = "-h" || $1 = "--help" ]]
then
    echo Usage : restore_db.sh [filename] [server] [dbname] [user] [password]  
    echo     "filename - the name of the restore file"
    echo     "server    - the sql server to access (default = .\sqlexpress)"
    echo     "dbname    - the database name to access/create (default = engine)"
    echo     "user      - the datbase user name (default = sa)"
    echo     "password	- the datbase user password (default = ENGINEadmin2009!)"
    exit
fi

restore_file=$1
if [[ ! -n $1 ]]
then
     restore_file="$dbname.bak"
fi

sqlServer=$2
if [[ ! -n $2 ]]
then
    sqlServer=.\sqlexpress
fi

dbname=$3
if [[ ! -n $3 ]]
then
     dbname=engine
fi

user=$4
if [[ ! -n $4 ]]
then
    user=sa
fi

password=$5
if [[ ! -n $5 ]]
then
     password=ENGINEadmin2009!
fi

echo restore_file - $restore_file
echo server - $sqlServer
echo dbname - $dbname
echo user - $user
echo password - $password


var='''$(dbname)='$dbname''' $(restore_file)='$restore_file
echo $var
echo  restoring the database ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../restore_db.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 1
fi

echo Done.

