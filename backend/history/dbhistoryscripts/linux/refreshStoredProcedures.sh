#!/bin/bash

# refreshStoredProcedures.sh
echo $1
if [[ $1 = "-h" || $1 = "--help" ]]
then
    echo Usage : refreshStoredProcedures [server] [dbname] [user] [password] [debug]
    echo     "server    - the sql server to access (default = .\sqlexpress)"
    echo     "dbname    - the database name to access/create (default = engine)"
    echo     "user      - the datbase user name (default = sa)"
    echo     "password	- the datbase user password (default = ENGINEadmin2009!)"
    echo     debug - true/false enables storing of exception data in DB
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

debug=$5
if [[ ! -n $5 ]]
then
     debug=false
fi

echo server - $sqlServer
echo dbname - $dbname
echo user - $user
echo password - $password
echo debug - $debug

var='''$(dbname)='$dbname' ''$(db)=engine'

echo create views ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../create_views.sql
if [[ $? != 0 ]]
then
    exit 1
fi

echo refreshing the Stored Procs ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../create_sp.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 2
fi
echo Done.

