#!/bin/bash
#
# This script is designed to run the configuration tool.
# The tool's configuration should be under the /etc directory.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

usage () {
        printf "engine-config: get/set/list configuration\n"
        printf "USAGE:\n"
        printf "\tengine-config ACTION [--cver=version] [-p | --properties=/path/to/alternate/property/file] [-c | --config=/path/to/alternate/config/file]\n"
        printf "Where:\n"
        printf "\tACTION              action to perform, see details below\n"
        printf "\tversion             relevant configuration version to use.\n"
        printf "\t-p, --properties=   (optional) use the given alternate properties file.\n"
        printf "\t-c, --config=       (optional) use the given alternate configuration file.\n"
        printf "\n"
        printf "\tAvailable actions:\n"
        printf "\t-l, --list\n"
        printf "\t\tlist available configuration keys.\n"
        printf "\t-a, --all\n"
        printf "\t\tget all available configuration values.\n"
        printf "\t-g key, --get=key [--cver=version]\n"
        printf "\t\tget the value of the given key for the given version. If a version is not given, the values of all existing versions are returned.\n"
        printf "\t-s key=val [--cver=version], --set key=val [--cver=version]\n"
        printf "\t\tset the value of the given key for the given version. The cver version is required for this action only when the version is not 'general'.\n"
        printf "\t-h, --help\n"
        printf "\t\tdisplay this help and exit.\n"
        printf "\n"
        printf "### Notes: \n"
        printf "### 1. Passwords: password can be set in interactive mode ie:\n"
        printf "###        engine-config -s PasswordEntry=interactive\n"
        printf "###    or via file with one of the following options:\n"
        printf "###        engine-config -s PasswordEntry --admin-pass-file=/tmp/mypass\n"
        printf "###        engine-config -s PasswordEntry=/tmp/mypass\n"
        printf "###    PasswordEntry varies between the different password options\n"
        printf "###    See engine-config -h <OptionName> for more specific details\n"
        printf "### 2. In order for your change(s) to take effect,\n"
        printf "###    restart the oVirt engine service (using: 'service ovirt-engine restart').\n"
        printf "################################################################################\n"

        return 0
}

# Support alternate configuration file
CONF_FILE="${ENGINE_ETC}/engine-config/engine-config.conf"

found=0
for ((i=1; i<=$# && ! found; i++))
do
        var="${!i}"
        next=$[$i+1]
        next="${!next}"

        if [ "-c" == "${var}" ]; then
                CONF_FILE="${next}"
                found=1
        elif [ `echo "${var}" | grep -i '\-\-config\='` ]; then
                candidate=${var#--config=}
                if [ -s $candidate ]; then
                        CONF_FILE=$candidate
                else
                        CONF_FILE=
                fi
                found=1
        fi
done

if [ ${found} -eq 1 -a "x" == "x$CONF_FILE" ]; then
        die "Error! Alternate conf file '$candidate' is empty or does not exist!\n"
fi

if [ ! -s $CONF_FILE ]; then
        CONF_FILE=./engine-config.conf
fi
. $CONF_FILE

# Check basic argument constraints
if [ "$#" -gt 8 -o "$#" -lt 1 ]; then
        usage
        die "Error: wrong argument number: $#.\n"
fi


if [ "$1" == "--help" -o "$1" == "-h" -a "$#" -eq 1 ]; then
        usage
        exit 0
fi

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

# Run!
exec "${JAVA_HOME}/bin/java" \
  -Dlog4j.configuration="file:${ENGINE_ETC}/engine-config/log4j.xml" \
  -Djboss.modules.write-indexes=false \
  -jar "${JBOSS_HOME}/jboss-modules.jar" \
  -dependencies org.ovirt.engine.core.tools \
  -class org.ovirt.engine.core.config.EngineConfig \
  "$@"
