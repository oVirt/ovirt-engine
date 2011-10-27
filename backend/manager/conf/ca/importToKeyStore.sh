#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "importToKeyStore.sh- import CA certificate into keystore\n"
        printf "USAGE:\n"
        printf "\timportToKeyStore [KeyStore Filename] [alias] [Certificate] [Pass]\n"
        printf "Where:\n"
        printf "\tFilename	= KeyStore file name\n"
	printf "\talias		= certificate alias name\n"
	printf "\tCertificate	= Certificate file name\n"
	printf "\tPass          = KeyStore password\n"
        return 0
}

if [ ! "$#" -eq 4 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi

keytool -import -noprompt -keystore $1 -storepass $4 -alias $2 -file $3

exit $?

