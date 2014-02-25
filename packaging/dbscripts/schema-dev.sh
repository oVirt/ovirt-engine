#!/bin/sh

DBFUNC_COMMON_DBSCRIPTS_DIR="$(dirname "$0")"
. "${DBFUNC_COMMON_DBSCRIPTS_DIR}/dbfunc-custom.sh"

args() {
	while getopts :s:d: option; do
		case "${option}" in
			s) DBFUNC_DB_HOST="${OPTARG}";;
			d) DBFUNC_DB_DATABASE="${OPTARG}";;
			c) COMMAND="${OPTARG}";;
		esac
	done
}

COMMAND="apply"
args "$@"

exec "${DBFUNC_COMMON_DBSCRIPTS_DIR}/schema.sh" \
	-m "${DBFUNC_COMMON_DBSCRIPTS_DIR}/.${DBFUNC_DB_HOST}-${DBFUNC_DB_DATABASE}.scripts.md5" \
	-l "$0.${DBFUNC_DB_HOST}-${DBFUNC_DB_DATABASE}.log" \
	-c "${COMMAND}" \
	"$@"
