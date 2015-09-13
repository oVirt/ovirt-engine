#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

generatePgPass() {
	local password="${ENGINE_DB_PASSWORD}"

	#
	# we need client side psql library
	# version as at least in rhel for 8.4
	# the password within pgpassfile is
	# not escaped.
	# the simplest way is to checkout psql
	# utility version.
	#
	if ! psql -V | grep -q ' 8\.'; then
		password="$(echo "${password}" | sed -e 's/\\/\\\\/g' -e 's/:/\\:/g')"
	fi

	export PGPASSFILE="${MYTEMP}/.pgpass"
	touch "${PGPASSFILE}" || die "Can't create ${PGPASSFILE}"
	chmod 0600 "${PGPASSFILE}" || die "Can't chmod ${PGPASSFILE}"

	cat > "${PGPASSFILE}" << __EOF__
${ENGINE_DB_HOST}:${ENGINE_DB_PORT}:${ENGINE_DB_DATABASE}:${ENGINE_DB_USER}:${password}
__EOF__
}

usage() {
	cat << __EOF__
Usage: $0 [OPTIONS]
Manage user roles.

    --command=command        Command.
        add                      Add role.
    --user-name=name         User name.
    --authz-name=name        Name of authorization provider instace.
    --principal-namespace=ns Namespace within provider.
    --principal-id=id        Unique user id within provider.
    --role=role              Role name.

Interesting roles:

    SuperUser
        Role of administrator.
__EOF__
}

cleanup() {
	[ -n "${MYTEMP}" ] && rm -fr "${MYTEMP}" ]
}
trap cleanup 0

COMMAND=
USER_NAME=
AUTHZ_NAME=
PRINCIPAL_NAMESPACE=
PRINCIPAL_ID=
ROLE=

while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--command=*)
			COMMAND="${v}"
			case "${COMMAND}" in
				add) ;;
				*) die "Invalid command '${COMMAND}'" ;;
			esac
		;;
		--user-name=*)
			USER_NAME="${v}"
		;;
		--authz-name=*)
			AUTHZ_NAME="${v}"
		;;
		--principal-namespace=*)
			PRINCIPAL_NAMESPACE="${v}"
		;;
		--principal-id=*)
			PRINCIPAL_ID="${v}"
		;;
		--role=*)
			ROLE="${v}"
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

[ -n "${COMMAND}" ] || die "Please specify command"
[ -n "${USER_NAME}" ] || die "Please specify user name"
[ -n "${AUTHZ_NAME}" ] || die "Please specify provider"
[ -n "${PRINCIPAL_NAMESPACE}" ] || die "Please specify provider namespace"
[ -n "${PRINCIPAL_ID}" ] || die "Please specify provider id"
[ -n "${ROLE}" ] || die "Please specify role"

MYTEMP="$(mktemp -d)"
generatePgPass
psql -h "${ENGINE_DB_HOST}" -p "${ENGINE_DB_PORT}" -U "${ENGINE_DB_USER}" -c "
	select attach_user_to_role(
		'${USER_NAME}',
		'${AUTHZ_NAME}',
		'${PRINCIPAL_NAMESPACE}',
		'${PRINCIPAL_ID}',
		'${ROLE}'
	);
" > /dev/null
