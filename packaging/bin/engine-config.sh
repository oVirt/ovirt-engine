#!/bin/sh
#
# This script is designed to run the configuration tool.
# The tool's configuration should be under the /etc directory.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

usage () {
	cat << __EOF__
engine-config: get/set/list configuration
USAGE:
        engine-config ACTION [--cver=version] [-p | --properties=/path/to/alternate/property/file] [-c | --config=/path/to/alternate/config/file]
Where:
        ACTION              action to perform, see details below
        version             relevant configuration version to use.
        -p, --properties=   (optional) use the given alternate properties file.
        -c, --config=       (optional) use the given alternate configuration file.

        Available actions:
        -l, --list
                list available configuration keys.
        -a, --all
                get all available configuration values.
        -g key, --get=key [--cver=version]
                get the value of the given key for the given version. If a version is not given, the values of all existing versions are returned.
        -s key=val [--cver=version], --set key=val [--cver=version]
                set the value of the given key for the given version. The cver version is required for this action only when the version is not 'general'.
        -h, --help
                display this help and exit.

### Notes:
### 1. Passwords: password can be set in interactive mode ie:
###        engine-config -s PasswordEntry=interactive
###    or via file with one of the following options:
###        engine-config -s PasswordEntry --admin-pass-file=/tmp/mypass
###        engine-config -s PasswordEntry=/tmp/mypass
###    PasswordEntry varies between the different password options
###    See engine-config -h <OptionName> for more specific details
### 2. In order for your change(s) to take effect,
###    restart the oVirt engine service (using: 'service ovirt-engine restart').
################################################################################
__EOF__
	return 0
}

# TODO:
# why do we need CONF_FILE here?
# we do not use any vairable
CONF_FILE="${ENGINE_ETC}/engine-config/engine-config.conf"

parseArgs() {
	while [ -n "$1" ]; do
		local x="$1"
		local v="${x#*=}"
		shift
		case "${x}" in
			-c)
				CONF_FILE="$1"
				shift
			;;
			-configFile=*)
				CONF_FILE="${v}"
			;;
			-h|-help|--help)
				usage
				exit 0
			;;
		esac
	done
}
# do this in function so we do not lose $@
parseArgs "$@"

[ -s "${CONF_FILE}" ] || die "Configuration file '${CONF_FILE}' is either empty or does not exist"
. "${CONF_FILE}"

#
# Add this option to the java command line to enable remote debugging in
# all IP addresses and port 8787:
#
# -Xrunjdwp:transport=dt_socket,address=0.0.0.0:8787,server=y,suspend=y
#
# Note that the "suspend=y" options is needed to suspend the execution
# of the JVM till you connect with the debugger, otherwise it is
# not possible to debug the execution of the main method.
#

exec "${JAVA_HOME}/bin/java" \
	-Dlog4j.configuration="file:${ENGINE_ETC}/engine-config/log4j.xml" \
	-Djboss.modules.write-indexes=false \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.core.tools \
	-class org.ovirt.engine.core.config.EngineConfig \
	"$@"
