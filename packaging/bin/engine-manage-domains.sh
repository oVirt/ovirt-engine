#!/bin/sh
#
# This script is designed to run the manage domains utility.
# The tool's configuration should be under the /etc directory.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

# logging configuration properties for tools
OVIRT_LOGGING_PROPERTIES="${OVIRT_LOGGING_PROPERTIES:-${ENGINE_USR}/conf/tools-logging.properties}"

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
	-Djava.util.logging.config.file="${OVIRT_LOGGING_PROPERTIES}" \
	-Djboss.modules.write-indexes=false \
	-Dorg.ovirt.engine.extensions.aaa.builtin.tools.programName="${0}" \
	-Dorg.ovirt.engine.extensions.aaa.builtin.tools.packageName="${PACKAGE_NAME}" \
	-Dorg.ovirt.engine.extensions.aaa.builtin.tools.packageVersion="${PACKAGE_VERSION}" \
	-Dorg.ovirt.engine.extensions.aaa.builtin.tools.packageDisplayName="${PACKAGE_DISPLAY_NAME}" \
	-Dorg.ovirt.engine.extensions.aaa.builtin.tools.engineEtc="${ENGINE_ETC}" \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.extensions.builtin \
	-class org.ovirt.engine.extensions.aaa.builtin.tools.ManageDomainsExecutor \
	"$@"
