#!/bin/sh

die () {
    printf >&2 "$@"
    exit 1
}

usage () {
    printf "CreateReq.sh - Generate a new keypair and certificate request\n"
    printf "USAGE:\n"
    printf "\tCreateReq [Country] [Organization] [Name] [Filename] [Pass] [Alias]\n"
    printf "Where:\n"
    printf "\tCountry      = 2 Letters country code\n"
    printf "\tOrganization = Organization name string\n"
    printf "\tName         = Certificate Subject Name\n"
    printf "\tFilename     = name of keys and request files (must match, script add .pem and .req respectively)\n"
    printf "\tPass         = Keystore password\n"
    printf "\tAlias        = Certificate alias in keystore\n"
    return 0
}

createConf() {
	cp cert.template cert.conf
	echo C = $1 >> cert.conf
	echo O = $2 >> cert.conf
	echo CN = $3 >> cert.conf
}

PASS=$5
if [ -z "$PASS" ]; then
	PASS=change_it
fi

ALIAS=$6
if [ -z "$ALIAS" ]; then
	ALIAS=engine
fi

if [ ! "$#" -ge 4 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi

createConf

keytool -certreq -keystore ./.keystore -storepass $PASS -alias $ALIAS -file requests/$4.req

exit $?

