#!/bin/bash

usage () {
    printf "kerberos-util.sh - validate LDAP server\n"
    printf "USAGE:\n"
    printf "\tkerberos-utils [realm] [user] [pass] [jaas conf] [jboss home]\n"
    printf "Where:\n"
    printf "\trealm         = kerberos realm.\n"
    printf "\tuser         = user name.\n"
    printf "\tpass         = password.\n"
    printf "\tjaas conf    = jass conf path.\n"
    printf "\tear          = path to engine ear dir.\n"
    printf "\tjboss home   = path to jboss-as dir.\n"

    return 0
}

if [ ! "$#" -ge 5 ]; then
    usage
    echo "Error: wrong number of arguments"
    exit 1
fi

if [ $1 == '-?' ]; then
    usage
    exit 1
fi

JB_HOME=$6
if [ -z "$JB_HOME" ]; then
    JB_HOME=/usr/local/jboss-eap-5.0/jboss-as
fi

if [ -z "$5" ]; then
        EAR_LIB=$JB_HOME/server/engine-slimmed/deploy/engine.ear/lib
else
        EAR_LIB=$5
fi

CP=$EAR_LIB/utils-3.0.0-0001.jar:$EAR_LIB/engine-compat.jar:$JB_HOME/common/lib/commons-logging.jar

java -cp $CP org.ovirt.engine.core.utils.kerberos.KerberosUtil -realm="$1" -user="$2" -pass="$3" -conf="$4"  -Dsun.security.krb5.debug=true

exit $?
