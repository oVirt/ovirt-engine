#!/bin/bash

# upgradeToEAP4_6.sh
if [[ $1 = "-h" || $1 = "--help" ]]
then
    echo Usage : upgradeToEAP4_6.cmd [filename] [server] [dbname] [user] [password] [script_path]
    echo     "server    - the sql server to access (default = .\sqlexpress)"
    echo     "dbname    - the database name to access/create (default = engine)"
    echo     "user      - the datbase user name (default = sa)"
    echo     "password	- the datbase user password (default = ENGINEadmin2009!)"
    echo     "script_path - the path to the db scripts directory"
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


script_path=$5
if [[ ! -n $5 ]]
then
     script_path=$(pwd)
fi

echo server - $sqlServer
echo dbname - $dbname
echo user - $user
echo password - $password
echo script_path - $script_path

pushd $script_path

# -------------------------------------------------------------------------------
# Upgrading VDS,Vds_group,VM_pool,Tag & Bokkmark ids from INT to UUID
# -------------------------------------------------------------------------------
var='''$(db)='$dbname''' $(debug)='$debug

echo check if need to upgrade from INT to UUID...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../DropConstrains.sql
if [[ $? != 0 ]]
then
    exit 1
fi
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../INT2UUIDUpgrade.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 2
fi
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../CreateConstrains.sql
if [[ $? != 0 ]]
then
    exit 3
fi
echo running upgrade script ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../upgradeToEAP4_6.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 4
fi
echo running insert enum script ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../insert_enum_values.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 5
fi
echo running insert period script ...
../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../insert_period_table_values.sql -v"$var"
if [[ $? != 0 ]]
then
    exit 6
fi

echo refreshing Views and SPs...
./refreshStoredProcedures.sh $sqlServer $dbname $user $password
# Cleanup : for future use
# ../../../manager/dbscripts/sqlcmd.py -U$user -P$password -S$sqlServer -d$dbname -i../Cleanup.sql

echo Done.

popd


