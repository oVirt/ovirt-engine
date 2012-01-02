#!/bin/bash
#
# Copyright 2009-2011 Red Hat, Inc. All rights reserved.
# Use is subject to license terms.
#
# Description:	  Installs and configs postgres db for engine
#
# note: this script should be run as root in order to have the permissions to create the nessesary files on the fs.
#
# internal script - run only by engine-installer.py after creating CA
#
# this script will create the engine db and set passwords for users postgres & engine
# the file /root/.pgpass must exsists (created by engine-setup) after this script finish,
# in order to access the db

# GLOBALS
HOST=`/bin/hostname`
SCRIPT_NAME="engine-db-install"
CUR_DATE=`date +"%Y_%m_%d_%H_%M_%S"`

#list of mandatory rpms - please add here any additional rpms required before configuring the postgresql server
REQUIRED_RPMS=(postgresql-server postgresql postgresql-libs postgresql-contrib uuid)

#postgresql data dir
PGDATA=/var/lib/pgsql/data

#location of engine db scripts
ENGINE_DB_SCRIPTS_DIR=/usr/share/ovirt-engine/dbscripts
ENGINE_DB_CREATE_SCRIPT=create_db.sh

#postresql service
PGSQL=postgresql
DB_ADMIN=postgres
DB_USER=engine
TABLE_NAME=vdc_options
DB_NAME=engine
SYSTEMCTL=/bin/systemctl
PGSQL_SERVICE="/sbin/service $PGSQL"
POSTGRESQL_SERVICE=postgresql.service

#auth security file path
PG_HBA_FILE=/var/lib/pgsql/data/pg_hba.conf

#uuid generate sql 
UUID_SQL=/usr/share/pgsql/contrib/uuid-ossp.sql
LOG_PATH=/var/log/ovirt-engine
USER=`/usr/bin/whoami`

#EXTERNAL ARGS
LOG_FILE=$1
DB_PASS=$2

# COMMANDS
CHKCONFIG=/sbin/chkconfig
COPY=/bin/cp
SED=/bin/sed
SHELL=/bin/sh
PSQL=/usr/bin/psql

verifyArgs()
{
	#if we dont have mandatory args, exit with 1
	if [[ "x${LOG_FILE}" == "x" ]]
	then
		echo "$SCRIPT_NAME must get log filename as argument 1"
		exit 1
	fi

	if [[ "x${DB_PASS}" == "x" ]]
	then
		echo "$SCRIPT_NAME must get db password as argument 2"
		exit 1
	fi
}

verifyRunPermissions()
{
    if [[ ! $USER == "root" ]]
    then
        echo "user $USER doesn't have permissions to run the script, please use root only."
        exit 1
    fi
}

_verifyRC()
{
    RC=$1
    STR=$2
    if [[ ! $RC -eq 0 ]]
    then
        echo "$2"
        exit 1
    fi
}

initLogFile()
{
    if [[ ! -d $LOG_PATH ]]
    then
        mkdir  $LOG_PATH > /dev/null
    fi
     _verifyRC $? "error, failed creating log dir $LOG_PATH"

    LOG_FILE="$LOG_PATH/$LOG_FILE"
    echo "#engine db installer log file on $HOST" > $LOG_FILE
}


#TODO: check if postgresql patch is installed
verifyPostgresPkgAreInstalled()
{
    echo "[$SCRIPT_NAME] verifying required rpms are installed." >> $LOG_FILE 
	for rpm in "${REQUIRED_RPMS[@]}"; do
		verifyPkgIsInstalled ${rpm}
	done
}

verifyPkgIsInstalled()
{
	RPM="$1"
	rpm -q $RPM >> $LOG_FILE 2>&1  
	_verifyRC $? "error, rpm $RPM is not installed"
}

verifyPostgresService()
{
   echo "[$SCRIPT_NAME] verifying postgres service exists." >> $LOG_FILE 
   if [ ! -f /lib/systemd/system/postgresql.service ]
   then
        echo "[$SCRIPT_NAME] postgresql service cannot be executed from $PGSQL_SERVICE" 
        exit 1 
   fi 
   if [ ! -x $PSQL ]
   then
        echo "[$SCRIPT_NAME] postgres psql command cannot be executed from $PSQL" 
        exit 1 
   fi 
}

initPgsqlDB()
{
    echo "[$SCRIPT_NAME] init postgres db." >> $LOG_FILE 
    #verify is service postgres initdb has run already
    if [ -e "$PGDATA/PG_VERSION" ]
    then
        echo "[$SCRIPT_NAME] psgql db already been initialized." >> $LOG_FILE
    else
		#This is how it is handled in RHEL6, im leaving this remark in case
		#We'll need to revert
        #$PGSQL_SERVICE initdb >> $LOG_FILE 2>&1
		/usr/bin/postgresql-setup initdb postgresql >> $LOG_FILE 2>&1
	    _verifyRC $? "error, failed initializing postgresql db"
    fi
}

startPgsqlService()
{
	USER=$1
	DB=$2
	echo "[$SCRIPT_NAME] stop postgres service." >> $LOG_FILE
	$PGSQL_SERVICE stop >> $LOG_FILE 2>&1

    echo "[$SCRIPT_NAME] starting postgres service." >> $LOG_FILE
    $PGSQL_SERVICE start >> $LOG_FILE 2>&1
	_verifyRC $? "failed starting postgresql service"

    #verify that the postgres service is up before continuing
    SERVICE_UP=0
    for i in {1..20}
    do
       echo "[$SCRIPT_NAME] validating that postgres service is running...retry $i" >> $LOG_FILE
       $PSQL -U $USER -d $DB -c "select 1">> $LOG_FILE 2>&1
       if [[ $? == 0 ]]
       then
            SERVICE_UP=1
            break
       fi
       sleep 1
    done

    if [[ $SERVICE_UP != 1 ]]
    then
        echo "[$SCRIPT_NAME] failed loading postgres service - timeout expired." >> $LOG_FILE
        exit 1
    fi
}

#change auth from default ident to trust
#TODO: Handle more auth types in the future
changePgAuthScheme()
{
	OLD=$1
	NEW=$2
	OLD_OPTIONAL=$3
    echo "[$SCRIPT_NAME] changing authentication scheme from $OLD to $NEW." >> $LOG_FILE
    #backup original hba file
    BACKUP_HBA_FILE=$PG_HBA_FILE.orig
    if [ -r $PG_HBA_FILE ]
    then
        $COPY $PG_HBA_FILE $BACKUP_HBA_FILE
        _verifyRC $? "error, failed backing up auth file $PG_HBA_FILE"
    else
       echo "[$SCRIPT_NAME] can't find pgsql auto file $PG_HBA_FILE." >> $LOG_FILE
       exit 1
    fi

	#if we dont have optional old, use old
        if [[ "x${OLD_OPTIONAL}" == "x" ]]
        then
               OLD_OPTIONAL=$OLD
        fi

	#sed will replace any OLD with NEW but will ignore comment and empty lines
	eval "$SED -i -e '/^[[:space:]]*#/!s/$OLD/$NEW/g' -e '/^[[:space:]]*#/!s/$OLD_OPTIONAL/$NEW/g' $PG_HBA_FILE" >> $LOG_FILE 2>&1
	_verifyRC $? "error, failed updating hba auth file $PG_HBA_FILE"
}

#TODO: handle remote DB Installation
#TODO: handle history DB Installation
createDB()
{
    echo "[$SCRIPT_NAME] creating $DB_NAME db on postgres." >> $LOG_FILE
    if [[ -d "$ENGINE_DB_SCRIPTS_DIR" && -e "$ENGINE_DB_SCRIPTS_DIR/$ENGINE_DB_CREATE_SCRIPT" ]]
    then
        pushd $ENGINE_DB_SCRIPTS_DIR >> $LOG_FILE
        #TODO: to we need to verify if the db was already created? (we can create a new file and check if exists..)
        $SHELL $ENGINE_DB_CREATE_SCRIPT -u $DB_ADMIN >> $LOG_FILE 2>&1
        _verifyRC $? "error, failed creating enginedb"
        popd >> $LOG_FILE

    else
        echo "[$SCRIPT_NAME] error, can't find create_db script at $ENGINE_DB_SCRIPTS_DIR/$ENGINE_DB_CREATE_SCRIPT"
        exit 1
    fi
}

checkIfDBExists()
{
    echo "[$SCRIPT_NAME] checking if $DB_NAME db exists already.." >> $LOG_FILE
    $PSQL -U $DB_ADMIN -d $DB_NAME -c "select 1">> $LOG_FILE 2>&1
    if [[ $? -eq 0 ]]
    then
        echo "[$SCRIPT_NAME] $DB_NAME db already exists on $HOST." >> $LOG_FILE
        echo " [$SCRIPT_NAME] verifying $TABLE_NAME table exists..." >> $LOG_FILE
        RES=`echo "SELECT count(*) FROM pg_tables WHERE tablename='$TABLE_NAME'" | $PSQL -U $DB_ADMIN -d $DB_NAME -t`
        if [[ $RES -eq 1 ]]
        then
            echo "[$SCRIPT_NAME] $TABLE_NAME table exists in $DB_NAME" >> $LOG_FILE
            #rc 1 means - no actions is needed
            return 1
        else
            echo "[$SCRIPT_NAME] $TABLE_NAME table doesn't exists in $DB_NAME" >> $LOG_FILE
            #rc 2 means - something is wrong, db exists but table doesnt!
            return 2
        fi
    else
        echo "[$SCRIPT_NAME] $DB_NAME not installed." >> $LOG_FILE
        return 0
    fi
}

escapeDBPassword()
{
	echo "[$SCRIPT_NAME] escaping db password that contains '(quote)" >> $LOG_FILE
	# Need to escape ' in db values
	DB_PASS=$(echo $DB_PASS|sed "s/'/''/g")
}

updateDBUsers()
{
	echo "[$SCRIPT_NAME] updating postgres users credentials" >> $LOG_FILE

	#update user postgres password
	$PSQL -U $DB_ADMIN -c "ALTER ROLE $DB_ADMIN WITH ENCRYPTED PASSWORD '$DB_PASS'" >> /dev/null  2>&1
	_verifyRC $? "failed updating user $DB_ADMIN password"

	#drop engine ROLE if exists
	$PSQL -U $DB_ADMIN -c "DROP ROLE IF EXISTS $DB_USER" >> $LOG_FILE 2>&1
	_verifyRC $? "failed updating user $DB_USER password"

	#create user engine + password
	$PSQL -U $DB_ADMIN -c "CREATE ROLE $DB_USER WITH LOGIN SUPERUSER ENCRYPTED PASSWORD '$DB_PASS'" >> /dev/null 2>&1
	_verifyRC $? "failed updating user $DB_USER password"

	#grant all permissions to user engine to db engine
	$PSQL -U $DB_ADMIN -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME to $DB_USER " >> $LOG_FILE 2>&1
	_verifyRC $? "failed updating user $DB_USER privileges"
}

turnPgsqlOnStartup()
{
    #turn on the postgres service on startup
    $CHKCONFIG $PGSQL on >> $LOG_FILE 2>&1
    _verifyRC $? "failed adding postgresql to startup scripts"
}

# Main

verifyArgs
verifyRunPermissions
initLogFile
verifyPostgresPkgAreInstalled
verifyPostgresService
initPgsqlDB
#change auth to trust so we can create the db and the users
#adding md5 to support re-entrance install
#since the user might change the db password so the .pgpass wont work
#till we change the password again
changePgAuthScheme peer ident
changePgAuthScheme ident trust md5
#update postgres user pass and create engine user
turnPgsqlOnStartup
startPgsqlService postgres postgres
checkIfDBExists

#get return value from checkIfDBExists function
DB_EXISTS=$?
if [[ $DB_EXISTS -eq 0 ]]
then
	createDB
	escapeDBPassword
	updateDBUsers
	#change auth to md5, now that we have users with passwords
	changePgAuthScheme trust md5
	startPgsqlService engine engine
elif [[ $DB_EXISTS -eq 2 ]]
then
   echo "[$SCRIPT_NAME] error, $TABLE_NAME doesnt exists on DB $DB_NAME" >> $LOG_FILE
   exit 1
fi


echo "[$SCRIPT_NAME] finished installing postgres db on $HOST." >> $LOG_FILE
exit 0
