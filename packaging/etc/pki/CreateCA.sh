#!/bin/sh

usage() {
	cat << __EOF__
CreateCA.sh - Creates Certificate Authority certificate and keys
USAGE:
    $0 [Country] [Organization] [Name] [startdate]
Where:
    Country      = 2 Letters country code
    Organization = Organization name string
    Name         = CA Subject Name
    startdate    = in YYMMDDHHMMSSZ ASN1 format
__EOF__
	exit 1
}

[ "$#" -eq 4 ] || usage

cp cacert.template cacert.conf
echo "C = $1" >> cacert.conf
echo "O = $2" >> cacert.conf
echo "CN = $3" >> cacert.conf
cp cert.template cert.conf

#
# openssl ca directory must
# be writable for the user
# as backup files are produced
# so let's assume directory
# is in correct permissions
#
echo 1000 > serial.txt
rm -f database.txt*
touch database.txt .rnd
chown --reference=. serial.txt* database.txt* .rnd*

openssl genrsa -out private/ca.pem 2048 && \
	openssl req -new -key private/ca.pem \
		-config cacert.conf -out requests/ca.csr && \
	openssl ca -selfsign -out ca.pem -in requests/ca.csr \
		-keyfile private/ca.pem -days 3650 -startdate "$4" \
		-config openssl.conf -extfile cacert.conf \
		-extensions v3_ca -batch && \
	openssl x509 -in ca.pem -out certs/ca.der
