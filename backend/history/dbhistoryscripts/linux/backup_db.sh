#!/bin/bash

# backup_db.sh
if [[ $1 = "-h" || $1 = "--help" ]]
then
    echo Usage : backup_db.cmd [filename] [server] [dbname] [user] [password] [debug]
    echo     "server    - the sql server to access (default = .\sqlexpress)"
    echo     "dbname    - the database name to access/create (default = engine)"
    echo     "user      - the datbase user name (default = sa)"
    echo     "password	- the datbase user password (default = ENGINEadmin2009!)"
    echo     "backup_file - the name of the backup file"
    exit
fi



sqlServer=$1
if [[ ! -n $1 ]]
then
    sqlServer=.\sqlexpress
fi

dbname=$2
if [[ ! -n $2 ]]
then
     dbname=engine
fi

user=$3
if [[ ! -n $3 ]]
then
    user=sa
fi

password=$4
if [[ ! -n $4 ]]
then
     password=ENGINEadmin2009!
fi


backup_file=$5
if [[ ! -n $5 ]]
then
     backup_file="$dbname.bak"
fi

echo server - $sqlServer
echo dbname - $dbname
echo user - $user
echo password - $password
echo backup_file - $backup_file

var='''$(dbname)='$dbname''' $(backup_file)='$backup_file
echo $var
echo  backing up database ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../backup_db.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 1
fi

echo Done.

