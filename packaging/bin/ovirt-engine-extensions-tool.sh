#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

OVIRT_LOGGING_PROPERTIES="${OVIRT_LOGGING_PROPERTIES:-${ENGINE_USR}/conf/extensions-tool-logging.properties}"

exec "${JAVA_HOME}/bin/java" \
	--add-modules java.se \
	--module-path "${ENGINE_USR}/logutils/logutils.jar" \
	-Djava.security.auth.login.config="${ENGINE_USR}/conf/jaas.conf" \
	-Djava.util.logging.config.file="${OVIRT_LOGGING_PROPERTIES}" \
	-Dorg.ovirt.engine.exttool.core.programName="${0}" \
	-Dorg.ovirt.engine.exttool.core.packageName="${PACKAGE_NAME}" \
	-Dorg.ovirt.engine.exttool.core.packageVersion="${PACKAGE_VERSION}" \
	-Dorg.ovirt.engine.exttool.core.packageDisplayName="${PACKAGE_DISPLAY_NAME}" \
	-Dorg.ovirt.engine.exttool.core.engineEtc="${ENGINE_ETC}" \
	-Dorg.ovirt.engine.exttool.core.useTicketCache="${AAA_JAAS_USE_TICKET_CACHE}" \
	-Dorg.ovirt.engine.exttool.core.ticketCacheFile="${AAA_JAAS_TICKET_CACHE_FILE}" \
	-Dorg.ovirt.engine.exttool.core.useKeytab="${AAA_JAAS_USE_KEYTAB}" \
	-Dorg.ovirt.engine.exttool.core.keytabFile="${AAA_JAAS_KEYTAB_FILE}" \
	-Dorg.ovirt.engine.exttool.core.principalName="${AAA_JAAS_PRINCIPAL_NAME}" \
	-Dorg.ovirt.engine.exttool.core.debug="${AAA_JAAS_ENABLE_DEBUG}" \
	${JAVA_OPTS} \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.core.extensions-tool \
	-class org.ovirt.engine.exttool.core.ExtensionsToolExecutor \
	"$@"
