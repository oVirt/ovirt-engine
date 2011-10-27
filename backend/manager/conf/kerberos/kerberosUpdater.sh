#!/bin/sh
usage () {
        printf "$0\n"
        printf "USAGE:\n"
        printf "\t$0  [DBuser] [DBpass] [conection_string] [jass conf] [jboss home] [jboss profile] [mixed-mode]\n"
        printf "Where:\n"
        printf "\tDBuser          = DB username.\n"
        printf "\tDBpass          = DB password.\n"
        printf "\tconnection_string = DB connection string.\n"
        printf "\tjaas conf     = jass conf path.\n"
        printf "\tjboss home    = path to jboss-as dir.\n"
        printf "\tjboss profile    = jboss profile.\n"
        printf "\tmixed-mode    = y/n use AD in 2003/2008 mixed mode.\n"
  return 0
}


if [ ! "$#" -ge 6 ]; then
  usage
  echo "Error: wrong number of arguments"
exit 1
fi

USER=$1
PASS=$2
CONNECTION_STRING=$3
KRB_CONF_FILE=krb5.conf
JAAS=$4
JB_HOME=$5
JB_PROFILE=$6
MIXED_MODE=$7
EAR_LIB=server/$JB_PROFILE/deploy/engine.ear/lib


if [ -z "$JB_HOME" ]; then
        JB_HOME=/usr/local/jboss-eap-5.0/jboss-as
fi

# When DEVELOPER_MODE is empty, all jars are assumed to rest under the same folder.
# This is used during installation where Jboss is not extracted yet.
 DEVELOPER_MODE=y
if [ -z $DEVELOPER_MODE ]; then
    CLASSPATH=$JB_HOME/utils-3.0.0-0001.jar:$JB_HOME/engine-compat.jar:$JB_HOME/commons-logging.jar:$JB_HOME/sqljdbc4.jar:$JB_HOME/engineencryptutils-3.0.0-0001.jar:$JB_HOME/commons-codec-1.4.jar
else
    CLASSPATH=$JB_HOME/$EAR_LIB/utils-3.0.0-0001.jar:$JB_HOME/$EAR_LIB/engine-compat.jar:$JB_HOME/common/lib/commons-logging.jar:$JB_HOME/common/lib/sqljdbc4.jar:$JB_HOME/$EAR_LIB/engineencryptutils-3.0.0-0001.jar:$JB_HOME/$EAR_LIB/commons-codec-1.4.jar
    echo ### running in developer mode ###
fi

if [ -z $MIXED_MODE ]; then
    echo detecting mixed mode
    RESULT=`java -cp $CLASSPATH org.ovirt.engine.core.utils.kerberos.DetectMixedMode $JB_HOME/server/$JB_PROFILE/conf/krb5.conf`
    MIXED_MODE=$RESULT
    echo mixed-mode is $MIXED_MODE
fi

java -cp $CLASSPATH org.ovirt.engine.core.utils.kerberos.KerberosUpgrade -user=$USER -password=$PASS -connection_string=$CONNECTION_STRING -jaas_file=$JAAS  -mixed_mode=$MIXED_MODE  -krb5_conf_path=$KRB_CONF_FILE

res=$?
if [ "$res" -gt "0" ]; then
    echo Error: failed creating $KRB_CONF_FILE
    exit $res
fi

