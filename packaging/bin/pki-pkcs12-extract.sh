#!/bin/sh

extractkey() {
	local name="$1"
	local key="$2"
	local passin="$3"
	local passout="$4"

	local extra_args=""

	local pkcs12="${PKIDIR}/keys/${name}.p12"

	[ -z "${passout}" ] && extra_args="${extra_args} -nodes"

	if [ "${key}" = - ]; then
		key=/proc/self/fd/1
	else
		common_backup "${key}"
		touch "${key}"
		chmod go-rwx "${key}" || die "Cannot set key permissions"
	fi

	openssl \
		pkcs12 \
		-in "${pkcs12}" \
		-passin "pass:${passin}" \
		-passout "pass:${passout}" \
		-nocerts \
		-out "${key}" \
		${extra_args} \
		|| die "Cannot create key"

	return 0
}

extractcert() {
	local name="$1"
	local cert="$2"
	local passin="$3"

	local pkcs12="${PKIDIR}/keys/${name}.p12"

	if [ "${cert}" = - ]; then
		cert=/proc/self/fd/1
	else
		common_backup "${cert}"
		touch "${cert}"
		chmod a+r "${cert}" || die "Cannot set certificate permissions"
	fi
	openssl \
		pkcs12 \
		-in "${pkcs12}" \
		-passin "pass:${passin}" \
		-passout "pass:${passout}" \
		-nodes \
		-nokeys \
		-out "${cert}" \
		|| die "Cannot create cert"

	return 0
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Extract key/cert from a PKCS#12 store.

    --name=prefix         file name without prefix.
    --passin=password     password of PKCS#12.
    --passout=password    password of generated key file.
                          If not provided, key file will be cleartext.
    --key=keyfile         extract key to keyfile
    --cert=certfile       extract certificate to certfile
__EOF__
}

. "$(dirname "$(readlink -f "$0")")"/pki-common.sh

while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--name=*)
			NAME="${v}"
		;;
		--passin=*)
			PASSIN="${v}"
		;;
		--passout=*)
			PASSOUT="${v}"
		;;
		--key=*)
			KEY="${v}"
		;;
		--cert=*)
			CERT="${v}"
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
[ -n "${PASSIN}" ] || die "Please specify PKCS#12 password"
[ -n "${KEY}" -o -n "${CERT}" ] || \
	die "Please specify at least one of --key or --cert"

[ -n "${KEY}" ] && extractkey "${NAME}" "${KEY}" "${PASSIN}" "${PASSOUT}"
[ -n "${CERT}" ] && extractcert "${NAME}" "${CERT}" "${PASSIN}"

exit 0

