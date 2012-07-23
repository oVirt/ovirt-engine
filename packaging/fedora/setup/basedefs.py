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
DB_PASS_FILE="/root/.pgpass"
PGPASS_FILE_TEMPLATE="hostname:port:database:username:password"
JBOSS_SECURITY_DOMAIN="EncryptDBPassword"
JBOSS_KERB_AUTH="EngineKerberosAuth"
ENGINE_SERVICE_NAME="ovirt-engine"
ENGINE_USER_NAME="ovirt"
ENGINE_GROUP_NAME="ovirt"
HTTPD_SERVICE_NAME="httpd"
HTTP_PORT_POLICY="http_port_t"
NFS_SERVICE_NAME="nfs-server"
NOTIFIER_SERVICE_NAME = "engine-notifierd"
FREEIPA_RPM = "freeipa-server"
IPA_RPM = "ipa-server"
PGPASS_FILE_HEADER_LINE = "# This section was created during %s setup.\n\
# DO NOT CHANGE IT MANUALLY - OTHER UTILITIES AND TOOLS DEPEND ON ITS STRUCTURE." % APP_NAME
PGPASS_FILE_OPENING_LINE = "# Beginning of the oVirt Engine DB settings section"
PGPASS_FILE_USER_LINE = "DB USER credentials"
PGPASS_FILE_ADMIN_LINE = "DB ADMIN credentials"
PGPASS_FILE_CLOSING_LINE = "#####  End of %s DB settings section." % APP_NAME

JBOSS_HTTP_PORT="8080"
JBOSS_HTTPS_PORT="8443"
JBOSS_AJP_PORT="8009"
VDC_OPTION_CVER="general"
ENGINE_RPM_NAME="ovirt-engine"

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
DIR_DB_BACKUPS="%s/ovirt-engine/db-backups" % DIR_USR_SHARE
DIR_ENGINE_CONFIG="%s/ovirt-engine/engine-config/" % DIR_USR_SHARE
DIR_RHEVM_CONFIG_CONF="/etc/ovirt-engine/engine-config/"
DIR_OVIRT_PKI="/etc/pki/ovirt-engine"


FILE_INSTALLER_LOG="engine-setup.log"
FILE_JBOSS_HTTP_PARAMS="/etc/ovirt-engine/web-conf.js"
FILE_KRB_CONF="%s/deployments/configuration/krb5.conf" % DIR_ENGINE
FILE_CA_CRT_SRC="%s/ca.pem"%(DIR_OVIRT_PKI)
FILE_CA_CRT_TEMPLATE="%s/cacert.template"%(DIR_OVIRT_PKI)
FILE_CERT_TEMPLATE="%s/cert.template"%(DIR_OVIRT_PKI)
FILE_ENGINE_CERT="%s/certs/engine.cer"%(DIR_OVIRT_PKI)
FILE_JBOSSAS_CONF="/etc/%s/%s.conf" % (ENGINE_SERVICE_NAME, ENGINE_SERVICE_NAME)
FILE_ENGINE_SYSCONFIG="/etc/sysconfig/ovirt-engine"
FILE_DB_INSTALL_SCRIPT="engine-db-install.sh"
FILE_DB_UPGRADE_SCRIPT="upgrade.sh"
FILE_RHEVM_CONFIG_BIN=os.path.join(DIR_ENGINE_CONFIG, "engine-config")
FILE_RHEVM_CONFIG_PROPS="engine-config-install.properties"
FILE_RHEVM_EXTENDED_CONF = os.path.join(DIR_CONFIG, FILE_RHEVM_CONFIG_PROPS)
FILE_RESOLV_CONF="/etc/resolv.conf"
FILE_SLIMMING_PROFILE_CONF="/usr/share/ovirt-engine/conf/slimming.conf"
FILE_IPTABLES_DEFAULT="%s/ovirt-engine/conf/iptables.default" % DIR_USR_SHARE
FILE_IPTABLES_EXAMPLE="%s/ovirt-engine/conf/iptables.example" % DIR_USR_SHARE
FILE_IPTABLES_BACKUP="%s/ovirt-engine/conf/iptables.backup" % DIR_USR_SHARE
FILE_NFS_SYSCONFIG="%s/ovirt-engine/conf/nfs.sysconfig" % DIR_USR_SHARE
FILE_NFS_BACKUP="%s/ovirt-engine/conf/nfs.backup" % DIR_USR_SHARE
FILE_ETC_EXPORTS="/etc/exports"
FILE_PUBLIC_SSH_KEY="%s/keys/engine.ssh.key.txt"%(DIR_OVIRT_PKI)
FILE_PRIVATE_SSH_KEY="%s/keys/engine_id_rsa"%(DIR_OVIRT_PKI)
FILE_YUM_VERSION_LOCK="/etc/yum/pluginconf.d/versionlock.list"
FILE_ISOUPLOADER_CONF="/etc/ovirt-engine/isouploader.conf"
FILE_LOGCOLLECTOR_CONF="/etc/ovirt-engine/logcollector.conf"
FILE_PSQL_CONF="/var/lib/pgsql/data/postgresql.conf"
FILE_OVIRT_HTTPD_CONF="/etc/httpd/conf.d/ovirt-engine.conf"
FILE_HTTPD_SSL_CONFIG="/etc/httpd/conf.d/ssl.conf"
FILE_HTTPD_CONF="/etc/httpd/conf/httpd.conf"

# ISO FILES
FILE_VIRTIO_WIN_VFD="/usr/share/virtio-win/virtio-win.vfd"
FILE_VIRTIO_WIN_ISO="/usr/share/virtio-win/virtio-win.iso"
FILE_RHEV_GUEST_TOOLS_ISO="/usr/share/rhev-guest-tools-iso/rhev-tools-setup.iso"
FILE_SYSCTL="/etc/sysctl.conf"

# ISO
ISO_DISPLAY_NAME = "ISO_DOMAIN"
DEFAULT_ISO_EXPORT_PATH = "/usr/local/exports/iso"

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
EXEC_ENCRYPT_PASS="%s/encryptpasswd.sh"%(DIR_OVIRT_PKI)
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
EXEC_SETSEBOOL="/usr/sbin/setsebool"
EXEC_SEMANAGE="/usr/sbin/semanage"

CONST_BASE_MAC_ADDR="00:1A:4A"
CONST_DEFAULT_MAC_RANGE="00:1a:4a:16:84:02-00:1a:4a:16:84:fd"
CONST_MINIMUM_SPACE_ISODOMAIN=350
CONST_HTTP_BASE_PORT="8080"
CONST_HTTPS_BASE_PORT="8443"
CONST_AJP_BASE_PORT="8009"
CONST_CA_PASS="mypass"
CONST_CA_COUNTRY="US"
CONST_CA_ALIAS="engine"
CONST_STORAGE_DOMAIN_NAME_SIZE_LIMIT=50
CONST_MIN_MEMORY_GB=2
CONST_WARN_MEMORY_GB=4
CONST_ORG_NAME_SIZE_LIMIT=64
CONST_VDSM_UID = 36
CONST_KVM_GID  = 36
CONST_MAX_PSQL_CONNS= 150
CONST_SHMMAX=35554432

# This is needed for avoiding error in create_ca when supporting max cn length of 64.
# please DONT increase this size, any value over 55 will fail the setup.
# the truncated host-fqdn is concatenated with a random string to create a unique CN value.
CONST_MAX_HOST_FQDN_LEN=55

#text colors
RED="\033[0;31m"
GREEN="\033[92m"
BLUE="\033[94m"
YELLOW="\033[93m"
NO_COLOR="\033[0m"

COLORS = (RED, GREEN, BLUE, YELLOW, NO_COLOR)

#space len size for color print
SPACE_LEN=70

RPM_LOCK_LIST = """
ovirt-engine
ovirt-engine-backend
ovirt-engine-config
ovirt-engine-genericapi
ovirt-engine-notification-service
ovirt-engine-restapi
ovirt-engine-tools-common
ovirt-engine-userportal
ovirt-engine-webadmin-portal
"""
