#!/bin/sh 

die () {
        printf >&2 "$@"
        exit 1
}


usage () {
	printf "encryptpasswd.sh - Generate a an encrypted password from the plain-text password given.\n"
	printf "Usage: \n"
	printf "encryptpasswd [Plain-Text-Password]\n"
	printf "Where:\n"
	printf "Plain-Text-Password = The password to encrypt in plain text.\n"
	return 0
}


if [ ! "$#" -eq 1 ]; then
	usage 
	die "Error: wrong argument number: $#.\n"
fi

JAVA_BIN=java
if [[ "x${JAVA_HOME}" != "x" ]] ;then
    JAVA_BIN=$JAVA_HOME/bin/java
fi


pushd  $JBOSS_HOME
if [ -e "common/lib/jbosssx.jar" ]; then
    $JAVA_BIN -cp client/jboss-logging-spi.jar:common/lib/jbosssx.jar org.jboss.resource.security.SecureIdentityLoginModule $1
elif [ -e "lib/jbosssx.jar" ]; then
	$JAVA_BIN -cp client/jboss-logging-spi.jar:lib/jbosssx.jar org.jboss.resource.security.SecureIdentityLoginModule $1
else
        echo "unable to find jbosssx jar..."
        exit 1
fi

popd

exit $?
