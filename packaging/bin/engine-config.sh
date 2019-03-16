#!/bin/sh
#
# This script is designed to run the configuration tool.
# The tool's configuration should be under the /etc directory.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

# logging configuration properties for tools
OVIRT_LOGGING_PROPERTIES="${OVIRT_LOGGING_PROPERTIES:-${ENGINE_USR}/conf/tools-logging.properties}"

usage () {
	cat << __EOF__
Usage: engine-config <action> [<args>]

AVAILABLE ACTIONS
       -l, --list
           List available configuration keys.

       -a, --all
           Get all available configuration values.

       -g KEY, --get=KEY
           Get the value of the given key for the given version. If a version is not given, the values of all existing versions are returned.

       -s KEY=VALUE, --set KEY=VALUE
           Set the value of the given key for the given version. The version is required for this action only when the version is not 'general'.

       -m KEY=VALUE, --merge KEY=VALUE
           Merge the value of the given key for the given version with the value in the database. The version is required for this action only when the version is not 'general'.

       -h, --help
           Show this help message and exit.

OPTIONS
       --cver=VERSION
           Relevant configuration version to use.

       -p PROP_FILE, --properties=PROP_FILE
           Use the given alternate properties file.

       -c CFG_FILE, --config=CFG_FILE
           Use the given alternate configuration file.

       --log-file=LOG_FILE
           Sets file to write logging into (if not set nothing is logged).

       --log-level=LOG_LEVEL
           Sets log level, one of FINE, INFO (default), WARNING, SEVERE (case insensitive).

SETTING PASSWORDS
       Passwords can be set in interactive mode:

           engine-config -s PasswordEntry=interactive

       or via file with one of the following options:

           engine-config -s PasswordEntry --admin-pass-file=/tmp/mypass
           engine-config -s PasswordEntry=/tmp/mypass

       PasswordEntry varies between the different password options.

CUSTOM LOGGING
       If you need custom logging setup, please create your own java.util.logging properties file,
       set a path to this file into OVIRT_LOGGING_PROPERTIES environment variable and execute engine-config.

NOTE
       In order for your change(s) to take effect, restart the oVirt engine.
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
# -agentlib:jdwp=transport=dt_socket,address=0.0.0.0:8787,server=y,suspend=y
#
# Note that the "suspend=y" options is needed to suspend the execution
# of the JVM till you connect with the debugger, otherwise it is
# not possible to debug the execution of the main method.
#

exec "${JAVA_HOME}/bin/java" \
	--add-modules java.se \
	--module-path "${ENGINE_USR}/logutils/logutils.jar" \
	-Djboss.modules.system.pkgs=org.jboss.byteman,org.ovirt.engine.core.logutils \
	-Djava.util.logging.config.file="${OVIRT_LOGGING_PROPERTIES}" \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.core.tools \
	-class org.ovirt.engine.core.config.EngineConfigExecutor \
	"$@"
