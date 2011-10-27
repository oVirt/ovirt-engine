#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "CreateKStore.sh - Creates Certificate Authority certificate and keys\n"
        printf "USAGE:\n"
        printf "\tCreateCA [Path] [Country] [Organization] [Name] [startdate] [storepass] [keypass]\n"
        printf "Where:\n"
	printf "\tPath	       = path to keystore file\n"
        printf "\tCountry      = 2 Letters country code\n"
        printf "\tOrganization = Organization name string\n"
        printf "\tName         = Subject Name\n"
	printf "\tstorepass    = Keystore password\n"
	printf "\tkeypass      = Key pair password\n"
        return 0
}

if [ ! "$#" -eq 6 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi
if [ ! -d "$1" ]; then
	usage
	die "Directory $1 does not exist.\n"
fi

keytool -genkey -keyalg RSA -keystore $1/.keystore -storepass $5 -keypass $6 -alias engine -dname "CN=$4, O=$3, C=$2"

exit $?

