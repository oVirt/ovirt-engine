#!/bin/sh

KEYTOOL="${JAVA_HOME:-/usr}/bin/keytool"

clean_pki_dir() {
	for f in \
		"${PKIDIR}/cacert.conf" \
		"${PKIDIR}/cert.conf" \
		"${PKIDIR}/serial.txt" \
		"${PKIDIR}/serial.txt.old" \
		"${PKIDIR}/database.txt" \
		"${PKIDIR}/database.txt.old" \
		"${PKIDIR}/database.txt.attr" \
		"${PKIDIR}/database.txt.attr.old" \
		"${PKIDIR}/private/ca.pem" \
		"${PKIDIR}/ca.pem" \
		"${PKIDIR}/certs"/*.cer \
		"${PKIDIR}/certs"/*.pem \
		"${PKIDIR}/keys"/*.p12 \
		"${PKIDIR}/keys"/*.nopass \
		"${PKIDIR}/keys/engine_id_rsa" \
		"${PKIDIR}/requests"/*.req \
		"${PKIDIR}/requests"/*.csr \
	; do
		if [ -e "${f}" ]; then
			common_backup "${f}"
			rm "${f}" || \
				die "Failed to remove ${f}"
		fi
	done
}

config() {
	cp "${PKIDIR}/cacert.template" "${PKIDIR}/cacert.conf" || die "Cannot create cacert.conf"
	cp "${PKIDIR}/cert.template" "${PKIDIR}/cert.conf" || die "Cannot create cert.conf"
	chmod a+r "${PKIDIR}/cacert.conf" "${PKIDIR}/cert.conf" || die "Cannot set config files permissions"
}

enroll() {
	local subject="$1"

	#
	# openssl ca directory must
	# be writable for the user
	# as backup files are produced
	# so let's assume directory
	# is in correct permissions
	#

	echo 1000 > "${PKIDIR}/serial.txt" || die "Cannot write to serial.txt"

	touch "${PKIDIR}/database.txt" "${PKIDIR}/.rnd" || die "Cannot write to database.txt"

	touch "${PKIDIR}/private/ca.pem"
	chmod o-rwx "${PKIDIR}/private/ca.pem" || die "Cannot set CA permissions"
	openssl genrsa \
		-out "${PKIDIR}/private/ca.pem" \
		2048 \
		|| die "Cannot generate CA key"
	openssl req \
		-batch \
		-config "${PKIDIR}/cacert.conf" \
		-new \
		-key "${PKIDIR}/private/ca.pem" \
		-out "${PKIDIR}/requests/ca.csr" \
		-subj "/" \
		|| die "Cannot generate CA request"

	(
		cd "${PKIDIR}"
		openssl ca \
			-batch \
			-config openssl.conf \
			-extfile cacert.conf \
			-extensions v3_ca \
			-in requests/ca.csr \
			-out ca.pem \
			-keyfile private/ca.pem \
			-selfsign \
			-subj "${subject}" \
			-days 3650 \
			-startdate "$(date --utc --date "now -1 days" +"%y%m%d%H%M%SZ")"
	) || die "Cannot enroll CA certificate"

	openssl x509 -in "${PKIDIR}/ca.pem" -out "${PKIDIR}/certs/ca.der" || die "Cannot read CA certificate"

	chown --reference="${PKIDIR}/private" "${PKIDIR}/private/ca.pem" || die "Cannot set CA private key permissions"
	chmod a+r "${PKIDIR}/ca.pem" "${PKIDIR}/certs/ca.der" || die "Cannot set CA certificate permissions"

	return 0
}

keystore() {
	local password="$1"

	"${KEYTOOL}" \
		-delete \
		-noprompt \
		-alias cacert \
		-keystore "${PKIDIR}/.truststore" \
		-storepass "${password}" \
		> /dev/null 2>&1
	"${KEYTOOL}" \
		-import \
		-noprompt \
		-trustcacerts \
		-alias cacert \
		-keypass "${password}" \
		-file "${PKIDIR}/certs/ca.der" \
		-keystore "${PKIDIR}/.truststore" \
		-storepass "${password}" \
		|| die "Keystore import failed"
	chmod a+r "${PKIDIR}/.truststore"

	return 0
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Create certificate authority.

    --subject=subject              X.500 subject name.
    --keystore-password=password   Password for keystore.
__EOF__
}

. "$(dirname "$(readlink -f "$0")")"/pki-common.sh

cleanup() {
	common_restore_perms "${PKIDIR}"
}

trap cleanup 0
while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--subject=*)
			SUBJECT="${v}"
		;;
		--keystore-password=*)
			KEYSTORE_PASSWORD="${v}"
		;;
		--help)
			usage
			exit 0
		;;
		*)
			usage
			exit 1
		;;
	esac
done

[ -n "${SUBJECT}" ] || die "Please specify subject"
[ -n "${KEYSTORE_PASSWORD}" ] || die "Please specify keystore password"

clean_pki_dir
config
enroll "${SUBJECT}"
keystore "${KEYSTORE_PASSWORD}"
