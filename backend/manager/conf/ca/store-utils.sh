#!/bin/bash

usage () {
        printf "store-utils.sh - A collection of encryption/decryption utilities using keystore.\n"
        printf "USAGE:\n"
        printf "Main mode:\n"
        printf "\tstore-utils {-enc|-dec|-pfx} {store} {pass} {alias} {string} {ear} [jboss home]\n"
        printf "Upgrade mode:\n"
        printf "\tstore-utils -jks {store} {pass} {alias} {PFX file} {CA PEM file}\n"
        printf "Where:\n"
        printf "\tStore        = path to keystore file.\n"
        printf "\tPass         = keystore password.\n"
        printf "\tAlias        = Certificate alias in keystore.\n"
        printf "\tString       = string to encrypt or decrypt. You may use double quotes (\" \") to encrypt a phrase.\n"
        printf "\tjboss home   = Path to jboss-as dir.\n"
        printf "\tear          = Path to engine ear dir.\n"
        printf "\tPFX file     = Path to exiting PFX file, with private key and a certificate.\n"
        printf "\tCA PEM file  = Path to a CA certificate file in PEM format.\n"

        return 0
}

if [ ! "$#" -ge 5 ]; then
        usage
        echo "Error: wrong argument number: $#.\n"
	exit 1
fi

JB_HOME=$7
if [ -z "$JB_HOME" ]; then
        JB_HOME=/usr/local/jboss-eap-5.0/jboss-as
fi

if [ -z "$6" ]; then
        EAR_LIB=$JB_HOME/standalone/deployments/engine.ear/lib
else
        EAR_LIB=$6
fi

CP=$EAR_LIB/engineencryptutils-3.0.0-0001.jar:$EAR_LIB/engine-compat.jar:$JB_HOME/common/lib/commons-logging.jar:$EAR_LIB/commons-codec-1.4.jar

if [ "$1" == "-pfx" ]; then
	PKEY_8=privatekey.pkcs8
	PKEY_64=privatekey.b64
	CERT_64=certificate.b64
	CERT_P12=engine.pfx
	keytool -export -rfc -keystore $2 -storepass $3 -alias $4 > $CERT_64
	java -cp $CP org.ovirt.engine.core.engineencryptutils.StoreUtils -pvk -store=$2 -pass=$3 -alias=$4 > $PKEY_8
	echo "-----BEGIN PRIVATE KEY-----" > $PKEY_64
	openssl enc -in $PKEY_8 -a >> $PKEY_64
	echo "-----END PRIVATE KEY-----" >> $PKEY_64
	openssl pkcs12 -inkey $PKEY_64 -in $CERT_64 -out $CERT_P12 -export -password pass:$3
	STAT=$?
	if [ ! $STAT -eq 0 ]; then
		rm -f $PKEY_8 $PKEY_64 $CERT_64 $CERT_P12
		echo error occured, exiting.
		exit $STAT
	fi
	rm -f $PKEY_8 $PKEY_64 $CERT_64
	#echo "Created new PFX file: $CERT_P12"
elif [ "$1" == "-jks" ]; then
	if [ ! -s $5 ]; then
		echo Unable to find the PFX file $5, exiting.
		exit 1
	fi
	if [ ! -s $6 ]; then
		echo Unable to find the CA certificate $6, exiting.
		exit 1
	fi
	#Import PFX
	keytool -importkeystore -srckeystore $5 -srcstoretype PKCS12 -srcstorepass $3 -deststoretype JKS -destkeystore $2 -deststorepass $3
	STAT=$?
	if [ ! $STAT -eq 0 ]; then
		rm -f $2
		echo Error trying to import the PFX file $5, exiting.
		exit $STAT
	fi

	#Add missing alias
	keytool -changealias -storepass $3 -keystore $2 -alias 1 -destalias $4
	STAT=$?
	if [ ! $STAT -eq 0 ]; then
		rm -f $2
		echo Error fixing alias. Exiting.
		exit $STAT
	fi

	openssl x509 -in $6 -out tmp.der -outform DER
	STAT=$?
	if [ ! $STAT -eq 0 ]; then
		rm -f $2
		echo Error converting CA certificate. Exiting.
		exit $STAT
	fi

	#Add the CA certificate
	keytool -import -noprompt -keystore $2 -storepass $3 -alias cacert -file tmp.der
	STAT=$?
	if [ ! $STAT -eq 0 ]; then
		rm -f $2 tmp.der
		echo Error importing CA certificate. Exiting.
		exit $STAT
	fi
	rm -f tmp.der
else
	java -cp $CP org.ovirt.engine.core.engineencryptutils.StoreUtils $1 -store=$2 -pass=$3 -string="$5" -alias=$4
fi

exit $?

