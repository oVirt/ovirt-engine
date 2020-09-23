#!/bin/sh

sign() {
	local name="$1"
	local subject="$2"
	local days="$3"
	local ovirt_ku="$4"
	local ovirt_eku="$5"
	local ovirt_san="$6"
	local ca_file="$7"

	local req="${REQ_DIR}/${name}.req"
	local cert="${CERT_DIR}/${name}.cer"

	common_backup "${cert}"

	local EXTRA_COMMAND
	if openssl x509 -text -in ${ca_file}.pem | grep "Subject Key Identifier" > /dev/null; then
		local extsection="v3_ca"
		[ -n "${ovirt_san}" ] && extsection="v3_ca_san"
		[ -n "${ovirt_ku}" -o -n "${ovirt_eku}" ] && extsection="custom"
		[ -e "${CERT_CONF}" ] || die "${CERT_CONF} is missing, Cannot sign certificate"
		EXTRA_COMMAND="-extfile ${CERT_CONF} -extensions ${extsection}"
	fi
	OVIRT_KU="${ovirt_ku}" OVIRT_EKU="${ovirt_eku}" OVIRT_SAN="${ovirt_san}" \
		openssl ca \
		-batch \
		-policy policy_match \
		-config openssl.conf \
		-cert ${ca_file}.pem \
		-keyfile private/${ca_file}.pem \
		-days "${days}" \
		-in "${req}" \
		-out "${cert}" \
		-startdate "$(date --utc --date "now -1 days" +"%y%m%d%H%M%SZ")" \
		${subject:+-subj "${subject}"} \
		-utf8 \
		${EXTRA_COMMAND} \
		|| die "Cannot sign certificate"
	chmod a+r "${cert}" || die "Cannot set certificate permissions"

	return 0
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Sign certificate request.
Certificate request should be put at: ${PKIDIR}/${requests}/PREFIX.req
Certificate will be available at:     ${PKIDIR}/${certs}/PREFIX.cer

    --name=prefix         file name.
    --subject=subject     X.500 subject name.
    --days=n              issue days.
    --ku=ku               optional custom key usage.
    --eku=ekus            optional custom extended key usage.
    --san=san             optional X.509 subject alternative name.
    --timeout=n           lock timeout, default=20
    --ca-file=file-name   CA base file name without extension.
    --cert-dir=directory  ${certs} directory, relative to the pki directory.
    --req-dir=directory   ${requests} directory, relative to the pki directory.
__EOF__
}

. "$(dirname "$(readlink -f "$0")")"/pki-common.sh

cleanup() {
	common_restore_perms "${PKIDIR}"
}
trap cleanup 0

NAME=""
SUBJECT=""
TIMEOUT="20"
DAYS="398"
OVIRT_KU=""
OVIRT_EKU=""
CA_FILE=ca
CERT_DIR="certs"
REQ_DIR="requests"
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
		--ku=*)
			OVIRT_KU="${v}"
		;;
		--eku=*)
			OVIRT_EKU="${v}"
		;;
		--san=*)
			OVIRT_SAN="${v}"
		;;
		--timeout=*)
			TIMEOUT="${v}"
		;;
		--ca-file=*)
			CA_FILE="${v}"
		;;
		--cert-dir=*)
			CERT_DIR="${v}"
		;;
		--req-dir=*)
			REQ_DIR="${v}"
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
common_set_conf_vars

# cannot use TMPDIR as we want the
# same file at any environment
# path must be local as remote filesystems
# do not [always] support flock.
LOCK="/tmp/ovirt-engine-pki.v2.lock"
LOCK_REF="${PKIDIR}/private"

lock_is_ok() {
	[ -e "${LOCK}" ] || return 1
	[ "$(stat --printf "%F-%u-%g-%a\n" "${LOCK}" "${LOCK_REF}" 2>&1 | uniq | wc -l)" = 1 ] || return 1
	return 0
}

retries=5
while ! lock_is_ok; do
	retries="$(($retries - 1))"
	[ "${retries}" -eq 0 ] && die "Cannot establish lock '${LOCK}'"

	#
	# Random sleep so multiple instances
	# will wakeup at different times.
	#
	sleep "$(($$ % 5))"

	if ! lock_is_ok; then
		rm -fr "${LOCK}"
		[ -e "${LOCK}" -o -L "${LOCK}" ] && die "Cannot remove '${LOCK}' please remove manually"

		if mkdir -m 700 "${LOCK}"; then
			chown -R --reference="${LOCK_REF}" "${LOCK}" || die "Cannot set ownership of lock '${LOCK}'"
			chmod -R --reference="${LOCK_REF}" "${LOCK}" || die "Cannot set permissions of lock '${LOCK}'"
		fi
	fi
done

# Wait for lock on fd 9
(
	flock -e -w "${TIMEOUT}" 9 || die "Timeout waiting for lock. Giving up"
	cd "${PKIDIR}"
	sign "${NAME}" "${SUBJECT}" "${DAYS}" "${OVIRT_KU}" "${OVIRT_EKU}" "${OVIRT_SAN}" "${CA_FILE}"
) 9< "${LOCK}"
result=$?

exit $result
