#!/bin/sh

enroll() {
	local name="$1"
	local pass="$2"
	local subj="$3"
	local ovirt_ku="$4"
	local ovirt_eku="$5"
	local ovirt_san="$6"
	local keep_key="$7"
	local ca_file="$8"

	local req="${PKIDIR}/requests/${name}.req"
	local cert="${PKIDIR}/certs/${name}.cer"
	local pkcs12="${PKIDIR}/keys/${name}.p12"
	common_backup "${req}" "${pkcs12}"

	if [ -z "${keep_key}" ]; then
		openssl \
			genpkey \
			-algorithm RSA \
			-pkeyopt rsa_keygen_bits:2048 \
			-out "${TMPKEY}" \
			-aes256 \
			-pass "pass:${pass}" \
			|| die "Cannot create certificate request"
	else
		openssl \
			pkcs12 \
			-in "${pkcs12}" \
			-out "${TMPKEY}" \
			-passin "pass:${pass}" \
			-passout "pass:${pass}" \
			-nocerts \
			|| die "Cannot export '${pkcs12}'"
	fi

	openssl \
		req \
		-new \
		-days 365 \
		-key "${TMPKEY}" \
		-out "${req}" \
		-passin "pass:${pass}" \
		-passout "pass:${pass}" \
		-batch \
		-subj "/" \
		|| die "Cannot create certificate request"

	"${BINDIR}/pki-enroll-request.sh" \
		--name="${name}" \
		--subject="${subj}" \
		--ku="${ovirt_ku}" \
		--eku="${ovirt_eku}" \
		--san="${ovirt_san}" \
		--ca-file="${ca_file}" \
		|| die "Cannot sign request"

	touch "${pkcs12}"
	chmod go-rwx "${pkcs12}" || die "Cannot set PKCS#12 permissions"
	openssl \
		pkcs12 \
		-export \
		-in "${cert}" \
		-inkey "${TMPKEY}" \
		-out "${pkcs12}" \
		-passin "pass:${pass}" \
		-passout "pass:${pass}" \
		-aes256 \
		|| die "Cannot create PKCS#12"

	return 0
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Generate key, enroll certificate, store in PKCS#12 format.
Result will be at ${PKIDIR}/keys/PREFIX.p12

    --name=prefix         file name without prefix.
    --password=password   password of PKCS#12.
    --subject=subject     X.500 subject name.
    --ku=ku               optional custom key usage.
    --eku=ekus            optional custom extended key usage.
    --san=san             optional X.509 subject alternative name.
    --keep-key            reissue certificate based on previous request.
    --ca-file=file-name   CA base file name without extension.
__EOF__
}

. "$(dirname "$(readlink -f "$0")")"/pki-common.sh

TMPKEY="$(mktemp)"
cleanup() {
	rm -f "${TMPKEY}"
}
trap cleanup 0

OVIRT_KU=""
OVIRT_EKU=""
CA_FILE=ca
while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--name=*)
			NAME="${v}"
		;;
		--password=*)
			PASSWORD="${v}"
		;;
		--subject=*)
			SUBJECT="${v}"
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
		--keep-key)
			KEEP_KEY="1"
		;;
		--ca-file=*)
			CA_FILE="${v}"
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
[ -n "${PASSWORD}" ] || die "Please specify password"
[ -n "${SUBJECT}" ] || die "Please specify subject"

enroll "${NAME}" "${PASSWORD}" "${SUBJECT}" "${OVIRT_KU}" "${OVIRT_EKU}" "${OVIRT_SAN}" "${KEEP_KEY}" "${CA_FILE}"
