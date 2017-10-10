#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh
. "$(dirname "$(readlink -f "$0")")"/generate-pgpass.sh

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


generatePgPass
PGPASSFILE="${MYPGPASS}" psql -h "${ENGINE_DB_HOST}" -p "${ENGINE_DB_PORT}" -U "${ENGINE_DB_USER}" -c "
	select attach_user_to_role(
		'${USER_NAME}',
		'${AUTHZ_NAME}',
		'${PRINCIPAL_NAMESPACE}',
		'${PRINCIPAL_ID}',
		'${ROLE}'
	);
" > /dev/null
