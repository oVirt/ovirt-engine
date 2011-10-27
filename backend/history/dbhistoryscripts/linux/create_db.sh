#!/bin/bash

# create_db.sh
if [[ $1 = "-h" || $1 = "--help" ]]
then
    echo Usage : create_db.cmd [server] [dbname] [user] [password] [debug]
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

var='''$(db)='$dbname''' $(debug)='$debug' $(dbname)='$dbname

echo Creating DB ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -dmaster -i../create_db.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 1
fi

echo create tables ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../create_tables.sql
if [[ $? != 0 ]]
then
    exit 2
fi

echo create views ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../create_views.sql
if [[ $? != 0 ]]
then
    exit 3
fi

echo creating the Stored Procs .....
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../create_sp.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 3
fi

echo running insert enum script ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../insert_enum_values.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 4
fi

echo running insert period script ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../insert_period_table_values.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 5
fi


echo Done.

