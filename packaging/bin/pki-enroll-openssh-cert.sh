#!/bin/sh

sign() {
	local name="$1"
	local id="$2"
	local host="$3"
	local principals="$4"
	local options="$5"
	local days="$6"

	local cert="${PKIDIR}/certs/${name}.cer"
	local sshpub="${PKIDIR}/certs/${name}.pub"
	local sshcert="${PKIDIR}/certs/${name}-cert.pub"

	common_backup "${sshpub}" "${sshcert}"

	#
	# TODO: replace when el-6 supports -m PKCS8
	#
	openssl x509 -in "${cert}" -noout -pubkey | \
		ssh-keygen -i -m PKCS8 -f /proc/self/fd/0 \
		> "${sshpub}" \
		|| die "Cannot generate ssh pubkey out of certificate"

	#
	# TODO: modify when CA key mode will be better (PKI rewrite)
	#
	(
		TMPCA="$(mktemp)"
		cleanup() {
			rm -fr "${TMPCA}"
		}
		trap cleanup 0
		cat "${PKIDIR}/private/ca.pem" > "${TMPCA}"
		ssh-keygen \
			-s "${TMPCA}" \
			-P "" \
			-I "${id}" \
			${host:+-h} \
			-V "-1h:+${days}d" \
			${principals:+-n "${principals}"} \
			$(printf "${options}" | xargs -ix -d',' echo -O x) \
			"${sshpub}" \
			|| die "ssh-keygen failed"
	) || die "Cannot sign ssh certificate"
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Enroll OpenSSH certificate out of X.509 certificate.
Certificate is available at:                   ${PKIDIR}/certs/PREFIX.cer
Open SSH Certificate will be available at:     ${PKIDIR}/certs/PREFIX-cert.pub

    --name=prefix           file name.
    --id=id                 certificate id.
    --host                  generate host certificate.
    --principals=principals certificate principals.
    --options=options       certificate options.
    --days=n                issue days.
__EOF__
}

. "$(dirname "$(readlink -f "$0")")"/pki-common.sh

NAME=""
ID=""
HOST=""
PRINCIPALS=""
OPTIONS="clear,permit-pty"
DAYS="398"
while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--name=*)
			NAME="${v}"
		;;
		--id=*)
			ID="${v}"
		;;
		--host)
			HOST="1"
		;;
		--principals=*)
			PRINCIPALS="${v}"
		;;
		--options=*)
			OPTIONS="${v}"
		;;
		--days=*)
			DAYS="${v}"
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
[ -n "${ID}" ] || die "Please specify id"
[ -n "${HOST}" ] && OPTIONS=""

sign "${NAME}" "${ID}" "${HOST}" "${PRINCIPALS}" "${OPTIONS}" "${DAYS}"
