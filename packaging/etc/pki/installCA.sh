#!/bin/sh

ENGINE_KEY="/tmp/engine.$$.key"
cleanup() {
	rm -f "${ENGINE_KEY}"
}
trap cleanup 0

die() {
	local m="$1"
	echo "$m" >&2
	exit 1
}

usage() {
	DATE=`date --utc --date "now -1 days" +"%y%m%d%H%M%S%z"`
	cat << __EOF__
Usage:
    $0 [Subject] [Country] [Organization] [Alias] [Password] [ANSI Start Date] [Working Directory] [CA Subject]
e.g.:
    $0 hostname.fqdn US oVirt engine NoSoup4U $DATE
__EOF__

	exit 1
}

enroll_certificate() {
	local name="$1"
	local pass="$2"
	local subj="$3"

	echo " "
	echo "}} Creating Engine Key..."
	openssl req -newkey rsa:2048 -days 365 -out "requests/${name}.req" -keyout "${ENGINE_KEY}" -passout "pass:${pass}" -subj "${subj}" || die "Cannot create certificate request"
	chmod go-rwx "requests/${name}.req" "${ENGINE_KEY}"

	echo " "
	echo "}} Signing certificate request..."
	./SignReq.sh "${name}.req" "${name}.cer" 1800 "$(pwd)" "${DATE}" "${pass}"
	[ -s "certs/${name}.cer" ] || die "file 'certs/${name}.cer' does not exist!"

	echo " "
	echo "}} Creating PKCS#12 store..."
	openssl pkcs12 -export -in "certs/${name}.cer" -inkey "${ENGINE_KEY}" -passin "pass:${pass}" -out "keys/${name}.p12" -passout "pass:${pass}" || die "Cannot createPKCS#12"
	chmod go-rwx "keys/${name}.p12"
}

# Set var's
SUBJECT="$1"
COUNTRY="$2"
ORG="$3"
ALIAS="$4"
PASS="$5"
DATE="$6"
WORKDIR="$7"
CA_SUBJECT="$8"

[ -n "${CA_SUBJECT}" ] || usage

[ -d "$7" ] || die "Directory $7 does not exists"

echo " "
echo "} Creating CA..."

# Move to scripts location
cd "$WORKDIR"

# Create CA
./CreateCA.sh "$COUNTRY" "$ORG" "CA-$CA_SUBJECT" "$DATE" \
	|| die "CreateCA.sh exited with errors"
[ -s private/ca.pem ] || die "file private/ca.pem does not exist!"
[ -s ca.pem ] || die "file ca.pem does not exist!"
[ -s certs/ca.der ] || die "file certs/ca.der does not exist!"

# Import CA into keystore 
echo " "
echo "> Importing CA certificate..."
# Generate truststore
keytool -delete -noprompt -alias cacert -keystore ./.truststore -storepass "$PASS" > /dev/null 2>&1
keytool -import -noprompt -trustcacerts -alias cacert -keypass "$PASS" -file certs/ca.der -keystore ./.truststore -storepass "$PASS"
chmod a+r ./.truststore

echo " "
echo "} Creating client certificates for oVirt..."
enroll_certificate engine "$PASS" "/C=${COUNTRY}/O=${ORG}/CN=${SUBJECT}"
enroll_certificate apache "$PASS" "/C=${COUNTRY}/O=${ORG}/CN=${SUBJECT}"
enroll_certificate jboss "$PASS" "/C=${COUNTRY}/O=${ORG}/CN=${SUBJECT}"

exit 0

