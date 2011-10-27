#!/bin/sh
usage () {
        printf "$0\n"
        printf "USAGE:\n"
        printf "\t$0 [domains] [user] [pass] [mixed-mode] [jass conf] [jboss home] [jboss profile]\n"
        printf "Where:\n"
        printf "\tdomains       = comma separated domains list\n"
        printf "\tuser          = user name.\n"
        printf "\tpass          = password.\n"
        printf "\tmixed-mode    = y/n use AD in 2003 mixed mode.\n"
        printf "\tjaas conf     = jass conf path.\n"
        printf "\tjboss home    = path to jboss-as dir.\n"
        printf "\tjboss profile    = jboss profile.\n"
  return 0
}


if [ ! "$#" -ge 7 ]; then
  usage
  echo "Error: wrong number of arguments"
exit 1
fi

DOMAINS=$1
USER=$2
PASS=$3
MIXED_MODE=$4
KRB_CONF_FILE=krb5.conf
JASS=$5
JB_HOME=$6
JB_PROFILE=$7
EAR_LIB=server/$JB_PROFILE/deploy/engine.ear/lib

ENGINE_LIB_PATH=/usr/share/engine/engine.ear/lib

if [ -z "$JB_HOME" ]; then
        JB_HOME=/usr/local/jboss-eap-5.0/jboss-as
fi

# When DEVELOPER_MODE is empty, all jars are assumed to rest under the same folder. This is used during installation where
# Jboss is not extracted yet.
DEVELOPER_MODE=y
if [ -z "$DEVELOPER_MODE" ]; then
    CLASSPATH=$JB_HOME/utils-3.0.0-0001.jar:$JB_HOME/engine-compat.jar:$JB_HOME/commons-logging.jar
else
    echo -e "\n### running in developer mode ###"
	if [ ! -d $JB_HOME/$EAR_LIB ]; then
		#if the engine-slimmed profile is missing, use /usr/share/engine/engine.ear
		CLASSPATH=$ENGINE_LIB_PATH/utils-3.0.0-0001.jar:$ENGINE_LIB_PATH/engine-compat.jar:$JB_HOME/common/lib/commons-logging.jar		
	else
	    CLASSPATH=$JB_HOME/$EAR_LIB/utils-3.0.0-0001.jar:$JB_HOME/$EAR_LIB/engine-compat.jar:$JB_HOME/common/lib/commons-logging.jar
	fi
fi

printf "\ncreating the krb5.conf file...\n"
java -cp $CLASSPATH org.ovirt.engine.core.utils.kerberos.KrbConfCreator -mixed_mode=$MIXED_MODE -domains=$DOMAINS -krb5_conf_path=$KRB_CONF_FILE
res=$?
if [ "$res" -gt "0" ]; then
    echo Error: failed creating $KRB_CONF_FILE
    exit $res
fi

printf "\nchecking the file configuration...\n"
java -cp $CLASSPATH org.ovirt.engine.core.utils.kerberos.KerberosConfigCheck  -domains=$DOMAINS -user=$USER -password=$PASS -jaas_file=$JASS -krb5_conf_path=$KRB_CONF_FILE
res=$?
if [ "$res" -gt "0" ]; then
    echo Error: failed checking $KRB_CONF_FILE
    exit $res
fi

exit 0
