"""
provides all the predefined variables for engine-setup
"""
import os

DB_ADMIN="postgres"
DB_NAME="engine"
DB_POSTGRES = "postgres"
DB_HOST="localhost"
DB_USER="engine"
DB_PORT="5432"
DB_PASS_FILE="/root/.pgpass"
PGPASS_FILE_TEMPLATE="hostname:port:database:username:password"
JBOSS_SECURITY_DOMAIN="EncryptDBPassword"
JBOSS_KERB_AUTH="EngineKerberosAuth"
JBOSS_SERVICE_NAME="jboss-as"
NFS_SERVICE_NAME="nfs-server"

JBOSS_PROFILE_NAME="default"
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
DIR_JBOSS="/usr/share/%s" % (JBOSS_SERVICE_NAME)
DIR_PLUGINS="%s/ovirt-engine/scripts/plugins" % DIR_USR_SHARE

DIR_JBOSS_ROOT_WAR_IMAGES_SRC="%s/%s" % (DIR_JBOSS_RESOURCES, "images/")
DIR_JBOSS_ROOT_WAR="%s/standalone/deployments/ROOT.war" % DIR_JBOSS
DIR_ENGINE_EAR_SRC="%s/ovirt-engine/engine.ear" % DIR_USR_SHARE
DIR_ROOT_WAR_SRC="%s/ovirt-engine/resources/jboss/ROOT.war" % DIR_USR_SHARE
DIR_DB_SCRIPTS="%s/ovirt-engine/dbscripts/" % DIR_USR_SHARE
DIR_DB_BACKUPS="%s/ovirt-engine/db-backups" % DIR_USR_SHARE
DIR_ENGINE_CONFIG="%s/ovirt-engine/engine-config/" % DIR_USR_SHARE
DIR_RHEVM_CONFIG_CONF="/etc/ovirt-engine/engine-config/"
DIR_OVIRT_PKI="/etc/pki/ovirt-engine"
DIR_MODULES_SRC="%s/ovirt-engine/resources/jboss/modules" % DIR_USR_SHARE
DIR_MODULES_DEST="%s/modules" % DIR_JBOSS


FILE_INSTALLER_LOG="engine-setup.log"
FILE_JBOSS_HTTP_PARAMS="/etc/ovirt-engine/web-conf.js"
FILE_JBOSS_ROOT_WAR_CSS = "style.css"
FILE_JBOSS_ROOT_WAR_CSS_SRC="%s/%s" % (DIR_JBOSS_RESOURCES, FILE_JBOSS_ROOT_WAR_CSS)
FILE_JBOSS_WEB_XML_SRC="%s/standalone/deployments/ROOT.war/WEB-INF/web.xml" % DIR_JBOSS
FILE_EXTERNAL_CONFIG="%s/ovirt-engine/engine.ear/ovirtengine.war/ExternalConfig.txt" % DIR_USR_SHARE
FILE_DEFAULT_HTML="%s/ovirt-engine/engine.ear/ovirtengine.war/Default.htm" % DIR_USR_SHARE
FILE_SERVER_PARAMS_JS="%s/ovirt-engine/engine.ear/ovirtengine.war/ServerParameters.js" % DIR_USR_SHARE
FILE_KRB_CONF="%s/deployments/configuration/krb5.conf" % DIR_JBOSS
FILE_CA_CRT_SRC="/etc/pki/ovirt-engine/ca.pem"
FILE_CA_CRT_TEMPLATE="/etc/pki/ovirt-engine/cacert.template"
FILE_CERT_TEMPLATE="/etc/pki/ovirt-engine/cert.template"
FILE_JBOSSAS_CONF="/etc/%s/%s.conf" % (JBOSS_SERVICE_NAME, JBOSS_SERVICE_NAME)
FILE_JBOSS_STANDALONE="%s/standalone/configuration/standalone.xml" % DIR_JBOSS
FILE_JBOSS_ROOT_WAR_HTML="engine_index.html"
FILE_JBOSS_ROOT_WAR_HTML_DEST="%s/%s" % (DIR_JBOSS_ROOT_WAR, FILE_JBOSS_ROOT_WAR_HTML)
FILE_JBOSS_ROOT_WAR_HTML_SRC="%s/%s" % (DIR_JBOSS_RESOURCES, FILE_JBOSS_ROOT_WAR_HTML)
FILE_JBOSS_ROOT_WAR_JS_VERSION="engineVersion.js"
FILE_JBOSS_ROOT_WAR_CONTEXT_SRC="%s/ROOT_war-context.xml" % DIR_JBOSS_RESOURCES
FILE_JBOSS_ROOT_WAR_CONTEXT_DEST="%s/WEB-INF/context.xml" % DIR_JBOSS_ROOT_WAR
FILE_JBOSS_ROOT_WAR_FAVICON="favicon.ico"
FILE_JBOSS_ROOT_WAR_FAVICON_DEST="%s/%s" % (DIR_JBOSS_ROOT_WAR, FILE_JBOSS_ROOT_WAR_FAVICON)
FILE_JBOSS_ROOT_WAR_FAVICON_SRC="%s/%s" % (DIR_JBOSS_RESOURCES, FILE_JBOSS_ROOT_WAR_FAVICON)
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
FILE_PUBLIC_SSH_KEY="/etc/pki/ovirt-engine/keys/engine.ssh.key.txt"
FILE_YUM_VERSION_LOCK="/etc/yum/pluginconf.d/versionlock.list"
FILE_ISOUPLOADER_CONF="/etc/ovirt-engine/isouploader.conf"
FILE_LOGCOLLECTOR_CONF="/etc/ovirt-engine/logcollector.conf"
FILE_PSQL_CONF="/var/lib/pgsql/data/postgresql.conf"
FILE_LIMITS_CONF="/etc/security/limits.conf"
FILE_JDK_MODULE_XML="%s/modules/sun/jdk/main/module.xml" % DIR_JBOSS

# ISO FILES
FILE_VIRTIO_WIN_VFD="/usr/share/virtio-win/virtio-win.vfd"
FILE_VIRTIO_WIN_ISO="/usr/share/virtio-win/virtio-win.iso"
FILE_RHEV_GUEST_TOOLS_ISO="/usr/share/rhev-guest-tools-iso/rhev-tools-setup.iso"
FILE_SYSCTL="/etc/sysctl.conf"

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
EXEC_ENCRYPT_PASS="/etc/pki/ovirt-engine/encryptpasswd.sh"
EXEC_RPM="/bin/rpm"
EXEC_FREE="/usr/bin/free"
EXEC_DATE="/bin/date"
EXEC_OPENSSL="/usr/bin/openssl"
EXEC_PGDUMP="/usr/bin/pg_dump"
EXEC_PSQL="/usr/bin/psql"
EXEC_PGRESTORE="/usr/bin/pg_restore"
EXEC_DROPDB="/usr/bin/dropdb"
EXEC_SHELL="/bin/sh"
EXEC_SSH_KEYGEN="/usr/bin/ssh-keygen"
EXEC_SYSCTL="/sbin/sysctl"

CONST_BASE_MAC_ADDR="00:1A:4A"
CONST_DEFAULT_MAC_RANGE="00:1a:4a:16:84:02-00:1a:4a:16:84:fd"
CONST_MINIMUM_SPACE_ISODOMAIN=350
CONST_HTTP_BASE_PORT="8080"
CONST_HTTPS_BASE_PORT="8443"
CONST_AJP_BASE_PORT="8009"
CONST_JBOSS_TRANS_TIMEOUT="600"
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
CONST_FD_OPEN = 65535
CONST_FD_LINE = "jboss           %s    nofile          %s"
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

#space len size for color print
SPACE_LEN=70

RPM_LOCK_LIST = "ovirt-engine-genericapi ovirt-engine ovirt-engine-backend \
ovirt-engine-jboss-deps ovirt-engine-webadmin-portal ovirt-engine-userportal \
ovirt-engine-restapi ovirt-engine-config ovirt-engine-tools-common ovirt-engine-notification-service"
