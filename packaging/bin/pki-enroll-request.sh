#!/bin/sh

sign() {
	local name="$1"
	local subject="$2"
	local days="$3"

	local req="requests/${name}.req"
	local cert="certs/${name}.cer"

	common_backup "${cert}"

	local EXTRA_COMMAND
	if openssl x509 -text -in ca.pem | grep "Subject Key Identifier" > /dev/null; then
		EXTRA_COMMAND="-extfile cert.conf -extensions v3_ca"
	fi
	openssl ca \
		-batch \
		-policy policy_match \
		-config openssl.conf \
		-cert ca.pem \
		-keyfile private/ca.pem \
		-days "${days}" \
		-in "${req}" \
		-out "${cert}" \
		-startdate "$(date --utc --date "now -1 days" +"%y%m%d%H%M%S%z")" \
		${subject:+-subj "${subject}"} \
		${EXTRA_COMMAND} \
		|| die "Cannot sign certificate"
	chmod a+r "${cert}" || die "Cannot set certificate permissions"

	return 0
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Sign certificate request.
Certificate request should be put at: ${PKIDIR}/requests/PREFIX.req
Certificate will be available at:     ${PKIDIR}/certs/PREFIX.cer

    --name=prefix         file name.
    --subject=subject     X.500 subject name.
    --days=n              issue days.
    --timeout=n           lock timeout, default=20
__EOF__
}

. "$(dirname "$(readlink -f "$0")")"/pki-common.sh

cleanup() {
	common_restore_perms "${PKIDIR}"
}
trap cleanup 0

TIMEOUT="20"
DAYS="1800"
while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--name=*)
			NAME="${v}"
		;;
		--subject=*)
			SUBJECT="${v}"
		;;
		--days=*)
			DAYS="${v}"
		;;
		--timeout=*)
			TIMEOUT="${v}"
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

[ -n "${NAME}" ] || die "Please specify name"

# Wait for lock on fd 9
(
	flock -e -w "${TIMEOUT}" 9 || die "Timeout waiting for lock. Giving up"
	cd "${PKIDIR}"
	sign "${NAME}" "${SUBJECT}" "${DAYS}"
) 9< "${PKIDIR}/ca.pem"
result=$?

exit $result
