#!/bin/bash

# drop_vdc_db.sh
echo $1
if [[ $1 = "-h" || $1 = "--help" ]]
then
    echo Usage : drop_vdc_db.cmd [server] [dbname] [user] [password]
    echo     "server    - the sql server to access (default = .\sqlexpress)"
    echo     "dbname    - the database name to access/create (default = engine)"
    echo     "user      - the datbase user name (default = sa)"
    echo     "password	- the datbase user password (default = ENGINEadmin2009!)"
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

echo server - $sqlServer
echo dbname - $dbname
echo user - $user
echo password - $password

var='''$(dbname)='$dbname''''
echo $var

echo droping db $dbname ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -dmaster -i../drop_db.sql -v$var
if [[ $? != 0 ]]
then
    exit 1
fi

