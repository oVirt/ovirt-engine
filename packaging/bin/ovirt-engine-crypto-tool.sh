#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

exec "${JAVA_HOME}/bin/java" \
	--add-modules java.se \
	-Dorg.ovirt.engine.cryptotool.core.programName="${0}" \
	-Dorg.ovirt.engine.cryptotool.core.packageName="${PACKAGE_NAME}" \
	-Dorg.ovirt.engine.cryptotool.core.packageVersion="${PACKAGE_VERSION}" \
	-Dorg.ovirt.engine.cryptotool.core.packageDisplayName="${PACKAGE_DISPLAY_NAME}" \
	-Dorg.ovirt.engine.cryptotool.core.engineEtc="${ENGINE_ETC}" \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.core.tools \
	-class org.ovirt.engine.core.cryptotool.Main \
	"$@"
