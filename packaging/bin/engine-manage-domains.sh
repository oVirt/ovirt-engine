#!/bin/sh
#
# This script is designed to run the manage domains utility.
# The tool's configuration should be under the /etc directory.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

usage() {
	cat << __EOF__
engine-manage-domains: add/edit/delete/validate/list domains
USAGE:
        engine-manage-domains -action=ACTION [-domain=DOMAIN -provider=PROVIDER -user=USER -passwordFile=PASSWORD_FILE -interactive -configFile=PATH -addPermissions -forceDelete -ldapServers=LDAP_SERVERS] -report
Where:
        ACTION             action to perform (add/edit/delete/validate/list). See details below.
        DOMAIN             (mandatory for add, edit and delete) the domain you wish to perform the action on.
        PROVIDER           (mandatory for add, optional for edit) the LDAP provider type of server used for the domain. Among the supported providers IPA, RHDS, ITDS, ActiveDirectory and OpenLDAP.
        USER               (optional for edit, mandatory for add) the domain user.
        PASSWORD_FILE      (optional for edit, mandatory for add) a file containing the password in the first line.
        interactive        alternative for using -passwordFile - read the password interactively.
        PATH               (optional) use the given alternate configuration file.
        LDAP_SERVERS       (optional) a comma delimited list of LDAP servers to be set to the domain.

        Available actions:
        add
        Examples:
                -action=add -domain=example.com -user=admin -provider=IPA -passwordFile=/tmp/.pwd
                        Add a domain called example.com, using user admin with ldap server type IPA and read the password from /tmp/.pwd.
                -action=edit -domain=example.com -provider=ActiveDirectory -passwordFile=/tmp/.new_password
                        Edit the domain example.com, using another password file and updated the provider type to Active Directory.
                -action=delete -domain=example.com [-forceDelete]
                        Delete the domain example.com.
                -forceDelete Optional parameter used in combination with -action=delete to skip confirmation of operation.
                        Default behaviour is prompt for confirmation of delete.
                -action=validate
                        Validate the current configuration (go over all the domains, try to authenticate to each domain using the configured user/password.).
                -report In combination with -action=validate will report all validation error, if occured.
                        Default behaviour is to exit when a validation error occurs.
                -addPermissions In combination with -action=add/edit will add engine superuser permissions to the user.
                        Default behaviour is not to add permissions.
                -action=list
                        Lists the current configuration.
                -h
                        Show this help.
__EOF__
	return 0
}

# TODO:
# why do we need CONF_FILE here?
# we do not use any vairable
CONF_DIR="${ENGINE_ETC}/engine-manage-domains"
CONF_FILE="${CONF_DIR}/engine-manage-domains.conf"

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

# TODO:
# what is the benefit of creating this here
# and not at java code?
PROPERTIES_FILE="$(mktemp)" || die "Temporary properties file cannot be created"
cleanup() {
	rm -fr "${PROPERTIES_FILE}"
}
trap cleanup 0
cat << __EOF__ > "${PROPERTIES_FILE}"
AdUserName=
AdUserPassword.type=CompositePassword
LDAPSecurityAuthentication=
DomainName=
AdUserId=
LdapServers=
LDAPProviderTypes=
LDAPServerPort=
__EOF__

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
	-Dlog4j.configuration="file:${ENGINE_ETC}/engine-manage-domains/log4j.xml" \
	-Djboss.modules.write-indexes=false \
	-jar "${JBOSS_HOME}/jboss-modules.jar" \
	-dependencies org.ovirt.engine.core.tools \
	-class org.ovirt.engine.core.domains.ManageDomains \
	"$@" -propertiesFile="${PROPERTIES_FILE}"
