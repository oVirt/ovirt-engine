#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

OVIRT_LOGGING_PROPERTIES="${OVIRT_LOGGING_PROPERTIES:-${ENGINE_USR}/conf/ovirt-register-sso-client-tool-logging.properties}"

exec "${JAVA_HOME}/bin/java" \
	--add-modules java.se \
	--module-path "${ENGINE_USR}/logutils/logutils.jar" \
	-Djava.util.logging.config.file="${OVIRT_LOGGING_PROPERTIES}" \
	-Dorg.ovirt.engine.ssoreg.core.programName="${0}" \
	-Dorg.ovirt.engine.ssoreg.core.packageName="${PACKAGE_NAME}" \
	-Dorg.ovirt.engine.ssoreg.core.packageVersion="${PACKAGE_VERSION}" \
	-Dorg.ovirt.engine.ssoreg.core.packageDisplayName="${PACKAGE_DISPLAY_NAME}" \
	-Dorg.ovirt.engine.ssoreg.core.engineEtc="${ENGINE_ETC}" \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.core.sso-client-registration-tool \
	-class org.ovirt.engine.ssoreg.core.SsoRegistrationToolExecutor \
	"$@"
