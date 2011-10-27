#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "CreateReq.sh - Generate a new keypair and certificate request\n"
        printf "USAGE:\n"
        printf "\tCreateReq [useKeyStore] [Country] [Organization] [Name] [Filename] [Alias]\n"
        printf "Where:\n"
	printf "\tKeyStore     = Store keys in keystore: 0 or 1.\n"
        printf "\tCountry      = 2 Letters country code\n"
        printf "\tOrganization = Organization name string\n"
        printf "\tName         = Certificate Subject Name\n"
        printf "\tFilename     = name of keys and request files (must match, script add .pem and .req respectivly)\n"
	printf "\tPass         = Keystore password\n"
	printf "\tAlias        = Certificate alias in keystore\n"
        return 0
}

createConf() {
	cp cert.template cert.conf
	echo C = $2 >> cert.conf
	echo O = $3 >> cert.conf
	echo CN = $4 >> cert.conf
}

PASS=$6
if [ -z "$PASS" ]; then
	PASS=change_it
fi

ALIAS=$7
if [ -z "$ALIAS" ]; then
	ALIAS=engine
fi

if [ ! "$#" -ge 5 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi

createConf

if [ "$1" -eq 1 ]; then

	keytool -certreq -keystore ./.keystore -storepass $PASS -alias $ALIAS -file requests/$5.req
else
	openssl req -newkey rsa:1024 -config cert.conf -out requests/$5.req -keyout keys/$5.pem
fi

exit $?

