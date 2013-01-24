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
REQUIRED_RPMS=(postgresql-server postgresql postgresql-libs)

#postgresql data dir
PGDATA=/var/lib/pgsql/data

#location of engine db scripts
ENGINE_DB_SCRIPTS_DIR=/usr/share/ovirt-engine/dbscripts
ENGINE_DB_CREATE_SCRIPT=create_db.sh

#EXTERNAL ARGS
LOGFILE=""
DB_PASS=""
DB_ADMIN=postgres
DB_HOST=localhost
DB_PORT="5432"
DB_USER=engine
DB_NAME=engine
TEMPLATE=template1
LOCAL_DB_SET=1
TABLE_NAME=vdc_options


#postresql service
PGSQL=postgresql
SYSTEMCTL=/bin/systemctl
PGSQL_SERVICE="/etc/init.d/postgresql"
SYSTEMD_PGSQL_SERVICE=postgresql.service

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-d DATABASE] [-u USERNAME] [-r 'remote'] -w PASSWORD -l LOGFILE\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${DB_HOST})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${DB_PORT})\n"
    printf "\t-d DATABASE   - The database name                         (def. ${DB_NAME})\n"
    printf "\t-u USERNAME   - The admin username for the database.      (def. ${DB_ADMIN})\n"
    printf "\t-w PASSWORD   - The admin password for the database.      (def. ${DB_PASS})\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-r REMOTE_INSTALL - The flag for peforming remote install (def. ${REMOTE_INSTALL})\n"
    printf "\t-h            - This help text.\n"
    printf "\n"

    exit 0
}

while getopts :s:p:d:u:w:l:r:h option; do
    case $option in
        s) DB_HOST=$OPTARG;;
        p) DB_PORT=$OPTARG;;
        d) DB_NAME=$OPTARG;;
        u) DB_ADMIN=$OPTARG;;
        w) DB_PASS=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        r) REMOTE_INSTALL=$OPTARG;;
        h) usage;;
    esac
done

#Figure out if we're support systemd
SYSTEMD_SUPPORT=0
if [ -d /lib/systemd ]; then
	SYSTEMD_SUPPORT=1
fi

#auth security file path
PG_HBA_FILE=/var/lib/pgsql/data/pg_hba.conf

LOG_PATH=/var/log/ovirt-engine
USER=`/usr/bin/whoami`

# COMMANDS
CHKCONFIG=/sbin/chkconfig
COPY=/bin/cp
SED=/bin/sed
SHELL=/bin/sh
PSQL_BIN=/usr/bin/psql

# Update PSQL BIN to include .pgpass environment variable, host and port values
ENGINE_PGPASS=/etc/ovirt-engine/.pgpass
PSQL="${PSQL_BIN} -h $DB_HOST -p $DB_PORT"

if [[ "x${REMOTE_INSTALL}" == "xremote" ]]
then
    LOCAL_DB_SET=0
fi

verifyArgs()
{
	#if we dont have mandatory args, exit with 1
	if [[ "x${LOGFILE}" == "x" ]]
	then
		echo "$SCRIPT_NAME must get a log filename as an argument"
		exit 1
	fi

	if [[ "x${DB_PASS}" == "x" ]]
	then
		echo "$SCRIPT_NAME must get a db password as an argument"
		exit 1
	fi
}

verifyRunPermissions()
{
    if [[ ! $EUID == 0 ]]
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
    # check if relative
    if [[ "${LOGFILE}" == "${LOGFILE#/}" ]]
    then
        if [[ ! -d $LOG_PATH ]]
        then
            mkdir  $LOG_PATH > /dev/null
        fi
         _verifyRC $? "error, failed creating log dir $LOG_PATH"

        LOGFILE="$LOG_PATH/$LOGFILE"
    fi
    echo "#engine db installer log file on $HOST" > $LOGFILE
}


#TODO: check if postgresql patch is installed
verifyPostgresPkgAreInstalled()
{
    echo "[$SCRIPT_NAME] verifying required rpms are installed." >> $LOGFILE
	for rpm in "${REQUIRED_RPMS[@]}"; do
		verifyPkgIsInstalled ${rpm}
	done
}

verifyPkgIsInstalled()
{
	RPM="$1"
	rpm -q $RPM >> $LOGFILE 2>&1
	_verifyRC $? "error, rpm $RPM is not installed"
}

verifyPostgresService()
{
   echo "[$SCRIPT_NAME] verifying postgres service exists." >> $LOGFILE
   rc=0
   if [ $SYSTEMD_SUPPORT -eq 1 ]
   then
	   if [ ! -f /lib/systemd/system/postgresql.service ]
	   then
	   		rc=1
	   fi
   else
	   if [ ! -x $PGSQL_SERVICE ]
   	   then
   	   		rc=1
       fi
   fi

   if [ $rc != 0 ]
   then
        echo "[$SCRIPT_NAME] postgresql service cannot be executed from $PGSQL_SERVICE"
        exit 1
   fi

   if [ ! -x $PSQL_BIN ]
   then
        echo "[$SCRIPT_NAME] postgres psql command cannot be executed from $PSQL_BIN"
        exit 1
   fi
}

initPgsqlDB()
{
    echo "[$SCRIPT_NAME] init postgres db." >> $LOGFILE
    #verify is service postgres initdb has run already
    if [ -e "$PGDATA/PG_VERSION" ]
    then
        echo "[$SCRIPT_NAME] psgql db already been initialized." >> $LOGFILE
    else
		if [ $SYSTEMD_SUPPORT -eq 1 ]
		then
			/usr/bin/postgresql-setup initdb postgresql >> $LOGFILE 2>&1
			rc=$?
		else
			$PGSQL_SERVICE initdb >> $LOGFILE 2>&1
			rc=$?
		fi
	    _verifyRC $rc "error, failed initializing postgresql db"
    fi
}

startPgsqlService()
{
	USER=$1
	DB=$2
	echo "[$SCRIPT_NAME] stop postgres service." >> $LOGFILE
	if [ $SYSTEMD_SUPPORT -eq 1 ]
	then
		$SYSTEMCTL stop $SYSTEMD_PGSQL_SERVICE >> $LOGFILE 2>&1
	else
		$PGSQL_SERVICE stop >> $LOGFILE 2>&1
	fi

    echo "[$SCRIPT_NAME] starting postgres service." >> $LOGFILE
	if [ $SYSTEMD_SUPPORT -eq 1 ]
	then
		$SYSTEMCTL start $SYSTEMD_PGSQL_SERVICE >> $LOGFILE 2>&1
		rc=$?
	else
	    $PGSQL_SERVICE start >> $LOGFILE 2>&1
		rc=$?
	fi
	_verifyRC $rc "failed starting postgresql service"

    #verify that the postgres service is up before continuing
    SERVICE_UP=0
    for i in {1..20}
    do
       echo "[$SCRIPT_NAME] validating that postgres service is running...retry $i" >> $LOGFILE
       PGPASSFILE="${ENGINE_PGPASS}" $PSQL -U $USER -d $DB -c "select 1">> $LOGFILE 2>&1
       if [[ $? == 0 ]]
       then
            SERVICE_UP=1
            break
       fi
       sleep 1
    done

    if [[ $SERVICE_UP != 1 ]]
    then
        echo "[$SCRIPT_NAME] failed loading postgres service - timeout expired." >> $LOGFILE
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
    echo "[$SCRIPT_NAME] changing authentication scheme from $OLD to $NEW." >> $LOGFILE
    #backup original hba file
    BACKUP_HBA_FILE=$PG_HBA_FILE.orig
    if [ -r $PG_HBA_FILE ]
    then
        $COPY $PG_HBA_FILE $BACKUP_HBA_FILE
        _verifyRC $? "error, failed backing up auth file $PG_HBA_FILE"
    else
       echo "[$SCRIPT_NAME] can't find pgsql auto file $PG_HBA_FILE." >> $LOGFILE
       exit 1
    fi

	#if we dont have optional old, use old
        if [[ "x${OLD_OPTIONAL}" == "x" ]]
        then
               OLD_OPTIONAL=$OLD
        fi

	#sed will replace any OLD with NEW but will ignore comment and empty lines
	eval "$SED -i -e '/^[[:space:]]*#/!s/$OLD/$NEW/g' -e '/^[[:space:]]*#/!s/$OLD_OPTIONAL/$NEW/g' $PG_HBA_FILE" >> $LOGFILE 2>&1
	_verifyRC $? "error, failed updating hba auth file $PG_HBA_FILE"
}

#TODO: handle remote DB Installation
#TODO: handle history DB Installation
createDB()
{
    echo "[$SCRIPT_NAME] creating $DB_NAME db on postgres." >> $LOGFILE
    if [[ -d "$ENGINE_DB_SCRIPTS_DIR" && -e "$ENGINE_DB_SCRIPTS_DIR/$ENGINE_DB_CREATE_SCRIPT" ]]
    then
        pushd $ENGINE_DB_SCRIPTS_DIR >> $LOGFILE
        #TODO: to we need to verify if the db was already created? (we can create a new file and check if exists..)
        $SHELL $ENGINE_DB_CREATE_SCRIPT -s $DB_HOST -p $DB_PORT -u $DB_ADMIN >> $LOGFILE 2>&1
        _verifyRC $? "error, failed creating enginedb"
        popd >> $LOGFILE

    else
        echo "[$SCRIPT_NAME] error, can't find create_db script at $ENGINE_DB_SCRIPTS_DIR/$ENGINE_DB_CREATE_SCRIPT"
        exit 1
    fi
}

checkIfDBExists()
{
    echo "[$SCRIPT_NAME] checking if $DB_NAME db exists already.." >> $LOGFILE
    PGPASSFILE="${ENGINE_PGPASS}" $PSQL -U $DB_ADMIN -d $DB_NAME -c "select 1">> $LOGFILE 2>&1
    if [[ $? -eq 0 ]]
    then
        echo "[$SCRIPT_NAME] $DB_NAME db already exists on $DB_HOST." >> $LOGFILE
        echo " [$SCRIPT_NAME] verifying $TABLE_NAME table exists..." >> $LOGFILE
        RES=`echo "SELECT count(*) FROM pg_tables WHERE tablename='$TABLE_NAME'" | PGPASSFILE="${ENGINE_PGPASS}" $PSQL -U $DB_ADMIN -d $DB_NAME -t`
        if [[ $RES -eq 1 ]]
        then
            echo "[$SCRIPT_NAME] $TABLE_NAME table exists in $DB_NAME" >> $LOGFILE
            #rc 1 means - no actions is needed
            return 1
        else
            echo "[$SCRIPT_NAME] $TABLE_NAME table doesn't exists in $DB_NAME" >> $LOGFILE
            #rc 2 means - something is wrong, db exists but table doesnt!
            return 2
        fi
    else
        echo "[$SCRIPT_NAME] $DB_NAME not installed." >> $LOGFILE
        return 0
    fi
}

escapeDBPassword()
{
	echo "[$SCRIPT_NAME] escaping db password that contains '(quote)" >> $LOGFILE
	# Need to escape ' in db values
	DB_PASS=$(echo $DB_PASS|sed "s/'/''/g")
}

updateDBUsers()
{
	echo "[$SCRIPT_NAME] updating db admin credentials" >> $LOGFILE

	# update admin user password
	PGPASSFILE="${ENGINE_PGPASS}" $PSQL -U $DB_ADMIN -d $TEMPLATE -c "ALTER ROLE $DB_ADMIN WITH ENCRYPTED PASSWORD '$DB_PASS'" >> /dev/null  2>&1
	_verifyRC $? "failed updating user $DB_ADMIN password"

    if [[ $LOCAL_DB_SET -eq 1 ]]
    then
        # Drop engine ROLE if exists
        PGPASSFILE="${ENGINE_PGPASS}" $PSQL -U $DB_ADMIN -c "DROP ROLE IF EXISTS $DB_USER" >> $LOGFILE 2>&1
        _verifyRC $? "failed dropping user $DB_USER"

        # Create user $DB_USER + password
        PGPASSFILE="${ENGINE_PGPASS}" $PSQL -U $DB_ADMIN -c "CREATE ROLE $DB_USER WITH CREATEDB LOGIN ENCRYPTED PASSWORD '$DB_PASS'" >> $LOGFILE 2>&1
        _verifyRC $? "failed creating user $DB_USER with encrypted password"

        DB_ADMIN=$DB_USER
    fi
}

turnPgsqlOnStartup()
{
    #turn on the postgres service on startup
    $CHKCONFIG $PGSQL on >> $LOGFILE 2>&1
    _verifyRC $? "failed adding postgresql to startup scripts"
}

# Main

verifyArgs
verifyRunPermissions
initLogFile
verifyPostgresPkgAreInstalled

#### Only if running locally
if [[ $LOCAL_DB_SET -eq 1 ]]
then
    echo "Running local installation"
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
fi
checkIfDBExists

#get return value from checkIfDBExists function
DB_EXISTS=$?
if [[ $DB_EXISTS -eq 0 ]]
then

	updateDBUsers
	createDB
	escapeDBPassword

	# if we run locally,
	# change auth to md5, now that we have users with passwords
    if [[ $LOCAL_DB_SET -eq 1 ]]
    then
	    changePgAuthScheme trust md5
	    startPgsqlService engine engine
	fi
elif [[ $DB_EXISTS -eq 2 ]]
then
   echo "[$SCRIPT_NAME] error, $TABLE_NAME doesnt exists on DB $DB_NAME" >> $LOGFILE
   exit 1
fi

echo "[$SCRIPT_NAME] finished installing postgres db on $DB_HOST." >> $LOGFILE
exit 0
