"""
provides all the predefined variables for engine-setup
"""
import os

APP_NAME="oVirt Engine"
DB_ADMIN="postgres"
DB_NAME="engine"
DB_POSTGRES = "postgres"
DB_TEMPLATE = "template1"
DB_HOST="localhost"
DB_USER="engine"
DB_PORT="5432"
PGPASS_FILE_TEMPLATE="hostname:port:database:username:password"
JBOSS_SECURITY_DOMAIN="EncryptDBPassword"
JBOSS_KERB_AUTH="EngineKerberosAuth"
ENGINE_SERVICE_NAME="ovirt-engine"
ENGINE_USER_NAME="ovirt"
ENGINE_GROUP_NAME="ovirt"
HTTPD_SERVICE_NAME="httpd"
HTTP_PORT_POLICY="http_port_t"
NOTIFIER_SERVICE_NAME = "engine-notifierd"
ETL_SERVICE_NAME = "ovirt-engine-dwhd"
FREEIPA_RPM = "freeipa-server"
IPA_RPM = "ipa-server"
PGPASS_FILE_HEADER_LINE = "# This section was created during %s setup.\n\
# DO NOT CHANGE IT MANUALLY - OTHER UTILITIES AND TOOLS DEPEND ON ITS STRUCTURE." % APP_NAME
PGPASS_FILE_OPENING_LINE = "# Beginning of the oVirt Engine DB settings section"
PGPASS_FILE_USER_LINE = "DB USER credentials"
PGPASS_FILE_ADMIN_LINE = "DB ADMIN credentials"
PGPASS_FILE_CLOSING_LINE = "#####  End of %s DB settings section." % APP_NAME
DB_PASS_FILE="/etc/%s/.pgpass" % ENGINE_SERVICE_NAME
ORIG_PASS_FILE="/root/.pgpass"

JBOSS_HTTP_PORT="8700"
JBOSS_HTTPS_PORT="8701"
JBOSS_AJP_PORT="8702"
VDC_OPTION_CVER="general"
ENGINE_RPM_NAME="ovirt-engine"
ENGINE_YUM_GROUP="ovirt-engine"

INTERNAL_ADMIN="admin"
INTERNAL_DOMAIN="internal"

DIR_ETC_SYSCONFIG="/etc/sysconfig"
DIR_ETC_INITD="/etc/init.d"
DIR_USR_SHARE="/usr/share"
DIR_VAR_LOG="/var/log"
DIR_VAR_LIB="/var/lib"
DIR_LOG="%s/ovirt-engine/" % DIR_VAR_LOG
DIR_CONFIG="%s/ovirt-engine/conf" % DIR_USR_SHARE
DIR_JBOSS_RESOURCES="%s/ovirt-engine/resources/jboss" % DIR_USR_SHARE
DIR_KERBEROS="%s/ovirt-engine/kerberos" % DIR_USR_SHARE
DIR_ENGINE="/usr/share/%s" % (ENGINE_SERVICE_NAME)
DIR_PLUGINS="%s/ovirt-engine/scripts/plugins" % DIR_USR_SHARE

DIR_ENGINE_EAR="%s/ovirt-engine/engine.ear" % DIR_USR_SHARE
DIR_DB_SCRIPTS="%s/ovirt-engine/dbscripts/" % DIR_USR_SHARE
DIR_DB_BACKUPS="%s/ovirt-engine/backups" % DIR_VAR_LIB
DIR_ENGINE_CONFIG="%s/ovirt-engine/engine-config/" % DIR_USR_SHARE
DIR_ENGINE_CONFIG_CONF="/etc/ovirt-engine/engine-config/"
DIR_OVIRT_PKI="/etc/pki/ovirt-engine"

DIR_YUM_CACHE = "/var/cache/yum"
DIR_PKGS_INSTALL = "/usr/share"

FILE_INSTALLER_LOG="engine-setup.log"
FILE_KRB_CONF="%s/deployments/configuration/krb5.conf" % DIR_ENGINE
FILE_CA_CRT_SRC="%s/ca.pem"%(DIR_OVIRT_PKI)
FILE_APACHE_CA_CRT_SRC="%s/apache-ca.pem"%(DIR_OVIRT_PKI)
FILE_CA_CRT_TEMPLATE="%s/cacert.template"%(DIR_OVIRT_PKI)
FILE_CERT_TEMPLATE="%s/cert.template"%(DIR_OVIRT_PKI)
FILE_ENGINE_CERT="%s/certs/engine.cer"%(DIR_OVIRT_PKI)
FILE_APACHE_CERT="%s/certs/apache.cer"%(DIR_OVIRT_PKI)
FILE_JBOSSAS_CONF="/etc/%s/%s.conf" % (ENGINE_SERVICE_NAME, ENGINE_SERVICE_NAME)
FILE_DB_INSTALL_SCRIPT="engine-db-install.sh"
FILE_DB_UPGRADE_SCRIPT="upgrade.sh"
FILE_DB_ASYNC_TASKS="%s/scripts/add_fn_db_get_async_tasks_function.sql" % DIR_ENGINE
FILE_ENGINE_CONFIG_BIN="/usr/bin/engine-config"
FILE_ENGINE_CONFIG_PROPS="engine-config-install.properties"
FILE_ENGINE_EXTENDED_CONF = os.path.join(DIR_CONFIG, FILE_ENGINE_CONFIG_PROPS)
FILE_RESOLV_CONF="/etc/resolv.conf"
FILE_SLIMMING_PROFILE_CONF="/usr/share/ovirt-engine/conf/slimming.conf"
FILE_IPTABLES_DEFAULT="%s/ovirt-engine/conf/iptables.default" % DIR_USR_SHARE
FILE_IPTABLES_EXAMPLE="/etc/ovirt-engine/iptables.example"
FILE_IPTABLES_BACKUP="%s/ovirt-engine/backups/iptables.backup" % DIR_VAR_LIB
FILE_FIREWALLD_SERVICE="/etc/firewalld/services/ovirt.xml"
FILE_NFS_SYSCONFIG="%s/ovirt-engine/conf/nfs.sysconfig" % DIR_USR_SHARE
FILE_NFS_BACKUP="%s/ovirt-engine/backups/nfs.backup" % DIR_VAR_LIB
FILE_ETC_EXPORTS="/etc/exports"
FILE_TRUSTSTORE="%s/.truststore"%(DIR_OVIRT_PKI)
FILE_ENGINE_KEYSTORE="%s/keys/engine.p12"%(DIR_OVIRT_PKI)
FILE_APACHE_KEYSTORE="%s/keys/apache.p12"%(DIR_OVIRT_PKI)
FILE_APACHE_PRIVATE_KEY="%s/keys/apache.key.nopass"%(DIR_OVIRT_PKI)
FILE_SSH_PRIVATE_KEY="%s/keys/engine_id_rsa"%(DIR_OVIRT_PKI)
FILE_YUM_VERSION_LOCK="/etc/yum/pluginconf.d/versionlock.list"
FILE_ISOUPLOADER_CONF="/etc/ovirt-engine/isouploader.conf"
FILE_LOGCOLLECTOR_CONF="/etc/ovirt-engine/logcollector.conf"
FILE_PSQL_CONF="/var/lib/pgsql/data/postgresql.conf"
FILE_OVIRT_HTTPD_CONF_TEMPLATE="%s/conf/ovirt-engine-proxy.conf.in" % DIR_ENGINE
FILE_OVIRT_HTTPD_CONF="/etc/httpd/conf.d/ovirt-engine.conf"
FILE_HTTPD_SSL_CONFIG="/etc/httpd/conf.d/ssl.conf"
FILE_HTTPD_CONF="/etc/httpd/conf/httpd.conf"
FILE_IMAGE_UPLOADER_CONF="/etc/ovirt-engine/imageuploader.conf"

# File containing the local configuration of the engine:
FILE_ENGINE_SYSCONFIG="/etc/sysconfig/ovirt-engine"

# This directory can also contain local configuration files for the
# engine that will be loaded in alphabetial order:
DIR_ENGINE_SYSCONFIG="%s.d" % FILE_ENGINE_SYSCONFIG

# This file will be automatically created when the engine goes into
# maintenance mode during upgrades and automatically removed when the
# engine goes back into normal mode once the upgrade is finished:
FILE_ENGINE_SYSCONFIG_MAINTENANCE="%s/99-maintenance.conf" % DIR_ENGINE_SYSCONFIG

# ISO FILES
FILE_VIRTIO_WIN_VFD="/usr/share/virtio-win/virtio-win.vfd"
FILE_VIRTIO_WIN_ISO="/usr/share/virtio-win/virtio-win.iso"
FILE_RHEV_GUEST_TOOLS_ISO="/usr/share/rhev-guest-tools-iso/rhev-tools-setup.iso"

#Locations of kernel configuration files
FILE_SYSCTL = "/etc/sysctl.conf"
DIR_SYSCTL = "/etc/sysctl.d"
FILE_ENGINE_SYSCTL = os.path.join(DIR_SYSCTL, "00-ovirt-engine.conf")

# ISO
ISO_DISPLAY_NAME = "ISO_DOMAIN"
DEFAULT_ISO_EXPORT_PATH = "/var/lib/exports/iso"

EXEC_IPTABLES="/sbin/iptables"
EXEC_SLIMMING_PROFILE="%s/ovirt-engine/scripts/slimmingEAP51.sh" % DIR_USR_SHARE
EXEC_NSLOOKUP="/usr/bin/nslookup"
EXEC_IP="/sbin/ip"
EXEC_EXPORTFS="/usr/sbin/exportfs"
EXEC_SEMANAGE="/usr/sbin/semanage"
EXEC_RESTORECON="/sbin/restorecon"
EXEC_SERVICE="/sbin/service"
EXEC_CHKCONFIG="/sbin/chkconfig"
EXEC_LSOF="/usr/sbin/lsof"
EXEC_ENCRYPT_PASS="%s/ovirt-engine/bin/engine-encrypt-passwd.sh" % DIR_USR_SHARE
EXEC_RPM="/bin/rpm"
EXEC_FREE="/usr/bin/free"
EXEC_DF = "/bin/df"
EXEC_DATE="/bin/date"
EXEC_OPENSSL="/usr/bin/openssl"
EXEC_PGDUMP="/usr/bin/pg_dump"
EXEC_PSQL="/usr/bin/psql"
EXEC_PGRESTORE="/usr/bin/pg_restore"
EXEC_DROPDB="/usr/bin/dropdb"
EXEC_SHELL="/bin/sh"
EXEC_SSH_KEYGEN="/usr/bin/ssh-keygen"
EXEC_SYSCTL="/sbin/sysctl"
EXEC_SYSTEMCTL="/bin/systemctl"
EXEC_SETSEBOOL="/usr/sbin/setsebool"
EXEC_SEMANAGE="/usr/sbin/semanage"
EXEC_KEYTOOL="/usr/bin/keytool"
EXEC_FIREWALL_CMD = '/usr/bin/firewall-cmd'

CONST_BASE_MAC_ADDR="00:1A:4A"
CONST_DEFAULT_MAC_RANGE="00:1a:4a:16:84:02-00:1a:4a:16:84:fd"
CONST_MINIMUM_SPACE_ISODOMAIN=350
CONST_HTTP_BASE_PORT="8700"
CONST_HTTPS_BASE_PORT="8701"
CONST_AJP_BASE_PORT="8702"
CONST_CA_PASS="mypass"
CONST_KEY_PASS="mypass"
CONST_CA_COUNTRY="US"
CONST_CA_ALIAS="engine"
CONST_STORAGE_DOMAIN_NAME_SIZE_LIMIT=50
CONST_MIN_MEMORY_MB=2048
CONST_WARN_MEMORY_MB=4096
CONST_ORG_NAME_SIZE_LIMIT=64
CONST_VDSM_UID = 36
CONST_KVM_GID  = 36
CONST_MAX_PSQL_CONNS= 150
CONST_SHMMAX=35554432
CONST_CONFIG_EXTRA_IPTABLES_RULES="EXTRA_IPTABLES_RULES"
CONST_INSTALL_SIZE_MB=500
CONST_DOWNLOAD_SIZE_MB=500
CONST_DB_SIZE=500
CONST_DEFAULT_CLUSTER_ID="99408929-82CF-4DC7-A532-9D998063FA95"
CONST_DEFAULT_APPLICATION_MODE="both"

# If following option is True then ApplicationMode set option will not be
# shown/prompted to the user at the time of engine-setup. Default value will be
# CONST_DEFAULT_APPLICATION_MODE. If this definition is False then Installer
# prompts the user to select Application Mode option.
USE_DEFAULT_APPLICATION_MODE_WITHOUT_PROMPT=False

# This is needed for avoiding error in create_ca when supporting max cn length of 64.
# please DONT increase this size, any value over 55 will fail the setup.
# the truncated host-fqdn is concatenated with a random string to create a unique CN value.
CONST_MAX_HOST_FQDN_LEN=55

#text colors
RED="\033[0;31m"
GREEN="\033[32m"
YELLOW="\033[33m"
BLUE="\033[34m"
NO_COLOR="\033[0m"

COLORS = (RED, GREEN, BLUE, YELLOW, NO_COLOR)

#space len size for color print
SPACE_LEN=70

RPM_LOCK_LIST = """
{name}
{name}-backend
{name}-dbscripts
{name}-genericapi
{name}-restapi
{name}-tools
{name}-userportal
{name}-webadmin-portal
""".format(name=ENGINE_RPM_NAME)

# The list of directories where JVMs will be searched for:
JAVA_DIRS = [
    "/usr/lib/jvm",
]

# Accepted JVMs should give an string matching this when executed with
# the -version option:
JAVA_VERSION = "1.7.0"

# Random password default length
RANDOM_PASS_LENGTH = 12
