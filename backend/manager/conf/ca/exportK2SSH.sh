#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "exportK2SSH.sh- export public key from keystore\n"
        printf "USAGE:\n"
        printf "\texportK2SSH [KeyStore Filename] [alias] [Certificate] [Pass]\n"
        printf "Where:\n"
        printf "\tFilename	= KeyStore file name\n"
	printf "\talias		= Key alias name\n"
	printf "\tKey name	= Key file name\n"
	printf "\tPass          = KeyStore password\n"
        return 0
}

if [ ! "$#" -eq 4 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi

keytool -rfc -export -keystore $1 -storepass $4 -alias $2 -file keys/$3.tmp
openssl x509 -noout -in keys/$3.tmp -pubkey > keys/$3
rm -f keys/$3.tmp

exit $?

