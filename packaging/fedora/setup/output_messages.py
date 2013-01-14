'''
external text file to hold all user visible text.
info messages begins with INFO_ and error msg with ERR_

any text with %s inside it, has dynamic parameters inside.
please don't remove the %s from the text.
you can relocate %s position in the text as long as the context is kept.
\n means new line in the text
\ at the end of a line lets you continue the text in a new line

DONT CHANGE any of the params names (in UPPER-CASE)
they are used in the engine-setup.py
'''

import basedefs

#####################
####INFO MESSAGES####
#####################

#Global parameters
CONST_CLEANUP="engine-cleanup"
CONST_SETUP="engine-setup"
CONST_MANAGE_DOMAINS="engine-manage-domains"

#main
INFO_HEADER="Welcome to %s setup utility" % basedefs.APP_NAME
INFO_INSTALL_SUCCESS="\n **** Installation completed successfully ******\n\n     (Please allow %s a few moments to start up.....)\n" % basedefs.APP_NAME
INFO_INSTALL="Installing:"
INFO_SET_DB_SECURITY="Setting Database Security"
INFO_SET_DB_CONFIGURATION="Setting Database Configuration"
INFO_CONFIG_OVIRT_ENGINE="Configuring oVirt-engine"
INFO_CONFIG_HTTPD="Configuring HTTPD"
INFO_FIND_JAVA="Configuring JVM"
INFO_CREATE_CA="Creating CA"
INFO_CREATE_DB="Creating Database"
INFO_UPGRADE_DB="Upgrading Database Schema"
INFO_UPD_DC_TYPE="Updating the Default Data Center Storage Type"
INFO_UPD_ENGINE_CONF="Updating ovirt-engine service"
INFO_UPD_RHEVM_CONF="Editing %s Configuration" % basedefs.APP_NAME
INFO_UPD_CONF="Editing %s Configuration"
INFO_CFG_NFS="Configuring the Default ISO Domain"
INFO_START_ENGINE="Starting ovirt-engine Service"
INFO_START_HTTPD="Starting HTTPD Service"
INFO_CFG_IPTABLES="Configuring Firewall"
INFO_DSPLY_PARAMS="\n%s will be installed using the following configuration:" % basedefs.APP_NAME
INFO_USE_PARAMS="Proceed with the configuration listed above"
INFO_NEED_STOP_ENGINE="\nIn order to proceed the installer must stop the ovirt-engine service"
INFO_Q_STOP_ENGINE="Would you like to stop the ovirt-engine service"
INFO_PROCEED="Would you like to proceed"
INFO_STOP_ENGINE="Stopping ovirt-engine service..."
INFO_STOP_INSTALL_EXIT="Installation stopped, Goodbye."
INFO_CLOSE_PORTS="Please verify that the specified ports are not used by any service on this host and run setup again"
INFO_LOGIN_USER="Please use the user \"admin\" and password specified in order to login into %s" % basedefs.APP_NAME
INFO_ADD_USERS="To configure additional users, first configure authentication domains using the '%s' utility" % CONST_MANAGE_DOMAINS
WARN_SECOND_RUN="\nWARNING: %s setup has already been run on this host.\nTo remove all configuration and reset %s please run %s.\nPlease be advised that executing %s without cleanup is not supported." % (basedefs.APP_NAME, basedefs.APP_NAME, CONST_CLEANUP, CONST_SETUP)

# ASYNC TASKS AND COMPENSATIONS
INFO_RETRYING = "Retrying to clear system tasks. "
INFO_STOPPING_TASKS = "System will try to clear tasks during the next %s minutes.\n"
INFO_STOP_WITH_RUNNING_TASKS = "User decided not to stop running system tasks. Stopping upgrade.\n"
INFO_RESTORING_NORMAL_CONFIGURATION = "Restoring engine from maintenance mode"

#runFunction
INFO_DONE="DONE"
INFO_ERROR="ERROR"

# Group descriptions
INFO_GRP_PORTS="Ports configuration"
INFO_GRP_ALL="General configuration parameters"
INFO_GRP_REMOTE_DB="Remote DB parameters"
INFO_GRP_LOCAL_DB="Local DB parameters"
INFO_GRP_ISO="ISO Domain parameters"
INFO_GRP_IPTABLES="Firewall related parameters"

#_addFinalInfoMsg
INFO_LOG_FILE_PATH="The installation log file is available at: %s"
INFO_RHEVM_URL="**** To access "+basedefs.APP_NAME+" browse to %s ****"

#_printAdditionalMessages
INFO_ADDTIONAL_MSG="Additional information:"
# the %s here is the msg
INFO_ADDTIONAL_MSG_BULLET=" * %s"

#addtional info about db restore
INFO_DB_RESTORED="Database upgrade failed. Previous database has been restored"

#config ip tables
INFO_IPTABLES_FILE="an example of the required configuration for iptables can be found at: %s"
INFO_FIREWALLD_INSTRUCTIONS="In order to configure firewalld, please execute the following command: firewall-cmd --add-service ovirt"
# the last 2 ports are http & https entered by the user
INFO_IPTABLES_PORTS=basedefs.APP_NAME + " requires the following TCP/IP Incoming ports to be opened on the firewall:\n\
22, %s, %s "
INFO_IPTABLES_BACKUP_FILE="The firewall has been updated, the old iptables configuration file was saved to %s"

#createca
INFO_CA_KEYSTORE_EXISTS="Keystore already exists, skipped certificates creation phase"
INFO_CA_SSL_FINGERPRINT=basedefs.APP_NAME + " CA SSL Certificate SHA1 fingerprint: %s"
INFO_CA_SSH_FINGERPRINT="SSH Public key fingerprint: %s"

#conf params
INFO_CONF_PARAMS_IPTABLES_USAGE="Should the installer configure the local firewall, overriding the current configuration"
INFO_CONF_PARAMS_IPTABLES_PROMPT="Firewall ports need to be opened.\n\
The installer can configure firewall automatically overriding the current configuration. The old configuration will be backed up.\n\
Alternately you can configure the firewall later using an example file. \n\
Which firewall do you wish to configure?"


INFO_CONF_PARAMS_OVERRIDE_HTTPD_CONF_USAGE="Should the installer configure the ports, overriding the current httpd configuration"
INFO_CONF_PARAMS_OVERRIDE_HTTPD_CONF_PROMPT="%s uses httpd to proxy requests to the application server.\n\
It looks like the httpd installed locally is being actively used.\n\
The installer can override current configuration .\n\
Alternatively you can use JBoss directly (on ports higher than 1024)\n\
Do you wish to override current httpd configuration and restart the service?" % basedefs.APP_NAME



INFO_CONF_PARAMS_HTTP_PORT_USAGE="Configures HTTP service port"
INFO_CONF_PARAMS_HTTP_PORT_PROMPT="HTTP Port"
INFO_CONF_PARAMS_HTTPS_PORT_USAGE="Configures HTTPS service port"
INFO_CONF_PARAMS_HTTPS_PORT_PROMPT="HTTPS Port"
INFO_CONF_PARAMS_AJP_PORT_USAGE="Configures the AJP port"
INFO_CONF_PARAMS_AJP_PORT_PROMPT="AJP Port (The default is recommended)"
INFO_CONF_PARAMS_FQDN_USAGE="The Host's fully qualified domain name"
INFO_CONF_PARAMS_FQDN_PROMPT="Host fully qualified domain name. Note: this name should be fully resolvable"
INFO_CONF_PARAMS_CA_PASS_USAGE="The password for the CA private key"
INFO_CONF_PARAMS_CA_PASS_PROMPT="Password for the CA private key"
INFO_CONF_PARAMS_AUTH_PASS_USAGE="Password for internal admin user"
INFO_CONF_PARAMS_AUTH_PASS_PROMPT="Enter a password for an internal %s administrator user (admin@internal)" % basedefs.APP_NAME
INFO_CONF_PARAMS_ORG_NAME_USAGE="Organization Name for the Certificate"
INFO_CONF_PARAMS_ORG_NAME_PROMPT="Organization Name for the Certificate"
INFO_CONF_PARAMS_DC_TYPE_USAGE="Default Data Center Storage Type"
INFO_CONF_PARAMS_DC_TYPE_PROMPT="The default storage type you will be using "
INFO_CONF_PARAMS_CONFIG_NFS_USAGE="Whether to configure NFS share on this server to be used as an ISO domain"
INFO_CONF_PARAMS_CONFIG_NFS_PROMPT="Configure NFS share on this server to be used as an ISO Domain?"
INFO_CONF_PARAMS_NFS_MP_USAGE="NFS mount point"
INFO_CONF_PARAMS_NFS_MP_PROMPT="Local ISO domain path"
INFO_CONF_PARAMS_NFS_DESC_USAGE="ISO Domain name"
INFO_CONF_PARAMS_NFS_DESC_PROMPT="Display name for the ISO Domain"
INFO_CONF_PARAMS_MAC_RANGE_USAGE="MAC range for the virtual machines, e.g. 00:11:22:33:44:00-00:11:22:33:44:FF"
INFO_CONF_PARAMS_MAC_RANG_PROMPT="MAC range for the virtual machines"
INFO_CONF_PARAMS_RANDOM_PASSWORDS_USAGE="Override passwords with random"
INFO_CONF_PARAMS_RANDOM_PASSWORDS_PROMPT="Override passwords with random"
INFO_CONF_PARAMS_DB_PASSWD_USAGE="Password for the local database administrator"
INFO_CONF_PARAMS_DB_PASSWD_PROMPT="Enter a password for a local %s DB admin user (%s)" % (basedefs.APP_NAME, basedefs.DB_USER)
INFO_CONF_PARAMS_PASSWD_CONFIRM_PROMPT="Confirm password"

#Remote DB interaction
INFO_CONF_PARAMS_REMOTE_DB_USAGE="Select local or remote DB server"
INFO_CONF_PARAMS_REMOTE_DB_PROMPT="Enter DB type for installation"
INFO_CONF_PARAMS_USE_DB_HOST_USAGE="Hostname or IP address of the DB server."
INFO_CONF_PARAMS_USE_DB_HOST_PROMPT="\nEntering a remote %s DB configuration section. All the configuration parameters should be provided by the remote DB administrator.\
\n\nEnter the host IP or host name where %s DB is running" % (basedefs.APP_NAME, basedefs.APP_NAME)
INFO_CONF_PARAMS_USE_DB_PORT_USAGE="Connection port for the remote DB"
INFO_CONF_PARAMS_USE_DB_PORT_PROMPT="Enter the DB port number"
INFO_CONF_PARAMS_DB_ADMIN_USAGE="Remote DB admin user"
INFO_CONF_PARAMS_DB_ADMIN_PROMPT="Enter the remote DB user name"
INFO_CONF_PARAMS_REMOTE_DB_PASSWD_USAGE="Password for the remote database user"
INFO_CONF_PARAMS_REMOTE_DB_PASSWD_PROMPT="Enter the password for the remote DB user"
INFO_CONF_PARAMS_DB_SECURE_CONNECTION_USAGE="Should the connection to the DB be secure? (The support must be configured on the remote DB server)"
INFO_CONF_PARAMS_DB_SECURE_CONNECTION_PROMPT="Configure a secure connection? (make sure SSL is configured on the remote DB server)"


#Auth domain
INFO_VAL_PATH_NAME_INVALID="Error: mount point is not a valid path"
INFO_VAL_PATH_NAME_IN_EXPORTS="Error: mount point already exists in %s" % (basedefs.FILE_ETC_EXPORTS)
INFO_VAL_PATH_NOT_WRITEABLE="Error: mount point is not writeable"
INFO_VAR_PATH_NOT_EMPTY="Error: directory %s is not empty"
INFO_VAL_PATH_SPACE="Error: mount point %s contains only %s of available space while a minimum of %s is required"
INFO_VAL_NOT_INTEGER="Error: value is not an integer"
INFO_VAL_PORT_NOT_RANGE="Error: port is outside the range of %i - 65535"
INFO_VAL_CHOOSE_PORT = "Please choose a different port."
INFO_VAL_FAILED_ADD_PORT_TO_HTTP_POLICY="Error: failed adding port %d to " + basedefs.HTTP_PORT_POLICY + " because it is \
already preallocated in selinux."
INFO_VAL_STRING_EMPTY="Error: can't accept an empty answer for param"
INFO_VAL_NOT_IN_OPTIONS="Error: response is not part of the following accepted answers: %s"
INFO_VAL_NOT_DOMAIN="Error: domain is not a valid domain name"
INFO_VAL_NOT_USER="Error: user name contains illegal characters"
INFO_VAL_PORT_OCCUPIED="Error: TCP Port %s is already open by %s (pid: %s)"
INFO_VAL_PORT_OCCUPIED_BY_JBOSS="Error: TCP Port %s is used by JBoss"
INFO_VAL_PASSWORD_DONT_MATCH="Error: passwords don't match"
INFO_VAL_ISO_DOMAIN_TOO_LONG="Error: ISO domain name length is limited to %s characters"%(basedefs.CONST_STORAGE_DOMAIN_NAME_SIZE_LIMIT)
INFO_VAL_ISO_DOMAIN_ILLEGAL_CHARS="Error: ISO domain name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters"
INFO_VAL_ORG_NAME_TOO_LONG="Error: organization name length is limited to %s characters"%(basedefs.CONST_ORG_NAME_SIZE_LIMIT)
INFO_VAL_ORG_NAME_ILLEGAL_CHARS="Error: organization name can't contain ',', '%', '$', '@', '&', '!', '*', '#', '+' characters"

#iso domain warning
WARN_ISO_DOMAIN_SECURITY="A default ISO share has been created on this host.\n\
   If IP based access restrictions are required, please edit %s entry in /etc/exports"
WARN_LOW_MEMORY="There are less than %s MBs of available free memory on this machine.\nIt is  recommended to have at least %s MBs of available memory to run the %s." % (basedefs.CONST_WARN_MEMORY_MB, basedefs.CONST_WARN_MEMORY_MB, basedefs.APP_NAME)

WARN_WEAK_PASS="Warning: Weak Password."

# General error input messages
INFO_STRING_LEN_LESS_THAN_MIN="String length is less than the minimum allowed: %s"
INFO_STRING_EXCEEDS_MAX_LENGTH="String length exceeds the maximum length allowed: %s"
INFO_STRING_CONTAINS_ILLEGAL_CHARS="String contains illegal characters"

# Yum
WARN_INSTALL_GPG_KEY = (
    "\nA package is being installed. The package requires installation of a new GPG key.\n"
    "GPG key details: userid=%s hexkeyid=%s\n"
)

#####################
#####ERR MESSAGES####
#####################

# Ping failed
ERR_PING = "Error: the provided hostname is unreachable"

# Errors with pgpass file
ERR_BACKUP_PGPASS = "Error: Failed to backup pgpass to temp file %s. Check file permissions."
ERR_PGPASS = (
    "Error: DB password file was not found on this system. Verify "
    "that this system was previously installed and that there's a "
    "password file at %s or %s" % (
        basedefs.DB_PASS_FILE,
        basedefs.ORIG_PASS_FILE
    )
)

#runFunction
ERR_EXP_RUN_FUNCTION="Internal error, Please report this issue"

#MAIN
ERR_CHECK_LOG_FILE_FOR_MORE_INFO="Please check log file %s for more information"
ERR_YUM_LOCK="Internal Error: Can't edit versionlock "
ERR_RPM_QUERY="Internal Error: Can't query rpm versions"

#_DB Errors
ERR_DB_CREATE_FAILED="Database creation failed"
ERR_DB_UPGRADE_FAILED="Database upgrade failed. Previous database has been restored"
ERR_DB_BACKUP="Database backup failed"
ERR_DB_RESTORE="Database restore failed"
ERR_DB_DROP="Database drop failed"
ERR_DB_RENAME = "Failed to rename DB '%s' to '%s'. Check that there are no active connections and try again."
ERR_DB_GET_SPACE = "Error: Failed to get %s database size."
ERR_DB_CONNECTION = "Could not connect to host %s with provided credentials. Check that your settings are correct."
ERR_DB_CREATE_PRIV = "Couldn't create temp database on server %s. Check provided credentials."
ERR_DB_DROP_PRIV = "Couldn't drop temp database on server %s. Check provided credentials."
ERR_DB_CONNECTIONS_BLOCK = "Error: failed to block new DB connections"
ERR_DB_CONNECTIONS_CLEAR = "Error: failed to clear active DB connections"
ERR_DB_TEMP_LIST = "Error: failed fetch temp remote DB list. Verify that DB server is up and accessible."
ERR_DB_UUID = "Error: uuid-ossp extension is not loaded into the DB.\n\
Verify with the DB admin that uuid-ossp extension is loaded into newly created databases and \
rerun the setup."

#_updateVdcOptions
ERR_CANT_FIND_VDC_OPTION_FILE="Unexpected error, Cannot find %s"
ERR_CANT_FIND_RHEVM_CONFIG_FILE="Unexpected error, Cannot find %s"
ERR_FAILED_UPD_VDC_OPTIONS="Failed updating configuration parameter: %s"
ERR_FAILED_GET_VDC_OPTIONS="Failed to retrieve configuration parameter: %s"
#start_jboss
ERR_FAILED_CHKCFG_JBOSS="Failed to configure JBoss service to start on boot"
ERR_FAILED_STP_JBOSS_SERVICE="Failed stopping JBoss service"
ERR_FAILED_START_JBOSS_SERVICE="Failed starting JBoss service"
ERR_FAILED_STATUS_JBOSS_SERVICE="Failed getting the status of the JBoss service"
ERR_FAILED_START_SERVICE = "Error: Can't start the %s service"
ERR_FAILED_STOP_SERVICE = "Error: Can't stop the %s service"
ERR_FAILED_TO_RESTART_JBOSS_SERVICE = "Failed restarting JBoss service"
ERR_FAILED_TO_CONFIGURE_ENGINE_MAINTENANCE = "Error: failed to configure the engine\
for the maintenance"

#START NFS SERVICE
ERR_FAILED_TO_START_NFS_SERVICE="Failed to start the NFS services"
ERR_RESTARTING_NFS_SERVICE="Failed starting the %s service"
ERR_FAILED_CHKCFG_NFS="Failed to configure %s service to start on boot"

#add isodomain to db
ERR_FAILED_INSERT_ISO_DOMAIN="Failed inserting ISO domain into %s db"

#config nfs share
ERR_FAILED_CFG_NFS_SHARE="Failed to configure NFS share on this host"
ERR_SET_SELINUX_NFS_SHARE="Failed to set SELINUX policy for NFS share"
ERR_REFRESH_SELINUX_CONTEXT="Failed to refresh SELINUX context for NFS share"

#HANDLE PARAMS
ERR_EXP_HANDLE_PARAMS="Failed handling user parameters input"
ERR_EXP_HANDLE_ANSWER_FILE="Failed handling answer file: %s"
ERR_EXP_KEYBOARD_INTERRUPT="Keyboard interrupt caught."
ERR_EXP_MISSING_PARAM_FROM_ANS_FILE="Param %s is missing from answer file"
ERR_EXP_GROUP_VALIDATION_ANS_FILE="validation of group %s returned: %s while expecting %s"

#VALIDATE param value
ERR_EXP_VALIDATE_PARAM="Error reading parameter %s from answer file"

#VALIDATE USER PERMISSIONS
ERR_EXP_INVALID_PERM="Error: insufficient permissions for user %s, you must run with user root."

#update vdcoption
ERR_EXP_UPD_VDC_OPTION="Error: failed updating configuration field %s to %s"
ERR_EXP_GET_VDC_OPTION="Error: failed fetching configuration field %s"
#update default dc type
ERR_EXP_UPD_DC_TYPE="Failed updating default Data Center Storage Type in %s db"

#attach to su role
ERR_EXP_ATTACH_SU_ROLE="Failed to configure Super User role for %s"

#UPDATE LOG4J
ERR_EXP_FAILED_UPD_LOG4J="Failed to configure application logging (could not edit %s)"

#config iptables
ERR_EXP_FAILED_CFG_IPTABLES="Failed to configure firewall"

#_create ca
ERR_EXP_CREATE_CA="Failed to create certificate authority"


#edit login config
ERR_EXP_UPD_LOGIN_CONFIG_FILE="Error while editing %s"

#edit html file
ERR_EXP_UPD_HTML_FILE="Error while editing default html path in %s"

#edit external config
ERR_EXP_EXTERNAL_CFG="Error while editing %s"

#update jboss beans
ERR_EXP_UPD_JBOSS_BEANS="Error while editing %s"

#edit xml
ERR_EXP_UPD_XML_FILE="Error while editing %s"

#edit root war
ERR_EXP_PARSE_WEB_CONF = "Cannot find '%s' from %s, please verify that ovirt-engine is configured"
ERR_EXP_CANT_FIND_CA_FILE="Can't find file %s"
ERR_EXP_CPY_FILE="Failed copying %s to %s"
ERR_READ_RPM_VER="Error reading version number for package %s"

#edit jboss conf
ERR_EXP_UPD_JBOSS_CONF="Error while editing %s"

#find and replace
ERR_EXP_FIND_AND_REPLACE="Failed to replace content in %s"

#set xml content & get node
ERR_EXP_UPD_XML_CONTENT="Unexpected error: XML query %s returned %s results"
ERR_EXP_UNKN_XML_OBJ="Unexpected error: given XML is neither string nor instance"

ERR_EXP_FAILED_CONFIG_ENGINE="Failed updating ovirt-engine configuration"

#edit transaction timeout
ERR_EXP_UPD_TRANS_TIMEOUT="Failed updating JBoss transaction timeout in %s"

#copy and link config
ERR_EXP_CPY_RHEVM_CFG="Failed to copy %s configuration files to %s"
ERR_EXP_LINK_EXISTS="%s is already linked to %s"
ERR_EXP_FAILED_CREATE_RHEVM_CONFIG_DIR="Failed to create %s directory"

#input param
ERR_EXP_READ_INPUT_PARAM="Error while trying to read parameter %s from user."

#xml config file handler
ERR_EXP_ILLG_PARAM_TYPE="Internal error: Illegal parameter type - paramsDict should be a dictionary, please report this issue"

#get configured ips
ERR_EXP_GET_CFG_IPS="Could not get list of available IP addresses on this host"
ERR_EXP_GET_CFG_IPS_CODES="Failed to get list of IP addresses"

#validate fqdn
ERR_EXP_CANT_FIND_IP="Could not find any configured IP address"
ERR_DIDNT_RESOLVED_IP="%s did not resolve into an IP address"
ERR_IPS_NOT_CONFIGED="Some or all of the IP addresses: (%s) which were resolved from the FQDN %s are not configured on any interface on this host"
ERR_IPS_NOT_CONFIGED_ON_INT="The IP (%s) which was resolved from the FQDN %s is not configured on any interface on this host"
ERR_IPS_HAS_NO_PTR="None of the IP addresses on this host(%s) holds a PTR record for the FQDN: %s"
ERR_IP_HAS_NO_PTR="The IP %s does not hold a PTR record for the FQDN: %s"

#init logging
ERR_EXP_FAILED_INIT_LOGGER="Unexpected error: Failed to initiate logger, please check file system permission"

#enum
ERR_EXP_VALUE_ERR="Internal error: Value '%s' is not in the Enum."

#update pgpass
ERR_UPD_DB_PASS="Internal Error: Can't set DB password"
ERR_CANT_FIND_PGPASS_FILE="Could not find DB password file %s" % (basedefs.DB_PASS_FILE)

#general errors
ERR_RC_CODE="Return Code is not zero"
ERR_SQL_CODE="Failed running sql query"
ERR_FAILURE="General failure"

#encrypt password
ERR_EXP_PARSING_ENCRYPT_PASS="Error while parsing encrypted jboss password"
ERR_EXP_ENCRYPT_PASS="Internal Error: Can't encrypt database password"
ERR_ENCRYPT_TOOL_NOT_FOUND="Internal Error: Encryption tool not found at %s " % (basedefs.EXEC_ENCRYPT_PASS)

#update postgres-ds.xml
#ERR_SYM_LINK_JBOSS_PSSQL_DS_FILE="Internal Error: can't add symbolic link to file %s" % (basedefs.FILE_JBOSS_PGSQL_DS_XML_DEST)

ERR_EXP_LSOF="Error while trying to determine the list of 'open TCP ports' on the host"
ERR_EXP_UNKWN_ERROR="Unknown error occurred during validation"

#error running free
ERR_EXP_FREE_MEM="Internal error occurred when trying to determine the amount of available memory.\nPlease make that there is at least %s MB of memory on the Host" % basedefs.CONST_MIN_MEMORY_MB
ERR_EXP_NOT_EMOUGH_MEMORY="Error: Not enough available memory on the Host\n(the minimum requirement is %s MB and the recommended is %s MB)." % (basedefs.CONST_MIN_MEMORY_MB, basedefs.CONST_WARN_MEMORY_MB)
ERR_EXP_NO_SPACE="Not enough available space on the Host\n(Current available space at %s is %s MB and %s MB is needed)."

#import iso files
ERR_FAILED_TO_COPY_FILE_TO_ISO_DOMAIN="Failed to copy files to iso domain"

#copy file
ERR_SOURCE_DIR_NOT_SUPPORTED="Error: function supports copy of files only and not directories"

# Command line parsing errors:
ERR_ONLY_1_FLAG="Error: The %s flag is mutually exclusive to all other command line options"
ERR_NO_ANSWER_FILE="Error: Could not find file %s"

ERR_EXP_EDIT_PSQL_CONF="Error: failed editing %s" % basedefs.FILE_PSQL_CONF

ERR_EXP_FAILED_KERNEL_PARAMS="Error: failed setting the kernel parameters"

# Prerequisites Packages
ERR_HTTPD_NOT_INSTALLED="Error: Httpd is not installed in the system"
ERR_MOD_SSL_NOT_INSTALLED="Error: mod_ssl is not installed in the system"

# edit ssl.conf
ERR_EXP_UPD_HTTPD_SSL_CONFIG="Failed updating ssl configuration file %s"

# Search of JVM:
ERR_EXP_CANT_FIND_SUPPORTED_JAVA="Error: Can't find any supported JVM"

# create ovirt-engine.conf
ERR_CREATE_OVIRT_HTTPD_CONF="Failed creating ovirt-engine.conf file %s"

# start httpd
ERR_FAILED_CHKCFG_HTTPD="Failed to configure httpd service to start on boot"
ERR_RESTARTING_HTTPD_SERVICE="Failed to restart httpd service"
ERR_FAILED_TO_START_HTTPD_SERVICE="Error: Can't start the httpd service"

# enable selinux boolean
ERR_FAILED_UPDATING_SELINUX_BOOLEAN="Failed to enable SELinux boolean"

# update listen ports
ERR_EXP_UPD_HTTP_LISTEN_PORT="Error: can't update http listen port in file %s"
ERR_EXP_UPD_HTTPS_LISTEN_PORT="Error: can't update https listen port in file %s"
WARN_IPA_INSTALLED="Warning! IPA Installation detected. Support for port 80/443 will be disabled."

# passwords errors
ERR_SPACES_IN_PASS = "Error: White spaces are not allowed in passwords"
ERR_NOT_ALLOWED_CHAR = "Error: The %s chars are not allowed in passwords"

MSG_ERROR_SPACE = "Not enough free space available."
MSG_STOP_UPGRADE_SPACE = "Not enough free space available for the upgrade operation.\
Stopping upgrade.\nIf you would like to perform an upgrade and ignore the space check,\n\
run the upgrade with --no-space-check option"
