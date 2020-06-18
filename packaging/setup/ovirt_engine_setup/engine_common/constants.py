#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Constants."""


import gettext
import os
import platform

from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.constants import classproperty
from ovirt_engine_setup.constants import osetupattrs
from ovirt_engine_setup.constants import osetupattrsclass

from . import config


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class FileLocations(object):
    SYSCONFDIR = '/etc'
    OVIRT_ENGINE_COMMON_DATADIR = config.ENGINE_COMMON_DATADIR
    OVIRT_ENGINE_PKIDIR = config.ENGINE_PKIDIR
    OVIRT_ENGINE_PKICERTSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'certs',
    )
    OVIRT_ENGINE_PKIKEYSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'keys',
    )

    DIR_HTTPD = os.path.join(
        osetupcons.FileLocations.SYSCONFDIR,
        'httpd',
    )
    HTTPD_CONF_OVIRT_ROOT = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'ovirt-engine-root-redirect.conf',
    )
    HTTPD_CONF_OVIRT_ROOT_TEMPLATE = os.path.join(
        osetupcons.FileLocations.OVIRT_SETUP_DATADIR,
        'conf',
        'ovirt-engine-root-redirect.conf.in',
    )
    HTTPD_CONF_SSL = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'ssl.conf',
    )
    JBOSS_HOME = config.JBOSS_HOME
    OVIRT_ENGINE_SYSCTL = os.path.join(
        SYSCONFDIR,
        'sysctl.d',
        'ovirt-engine.conf',
    )
    OVIRT_ENGINE_PKI_APACHE_CA_CERT = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'apache-ca.pem',
    )
    OVIRT_ENGINE_PKI_APACHE_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'apache.cer',
    )
    OVIRT_ENGINE_PKI_APACHE_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'apache.key.nopass',
    )


@util.export
class Defaults(object):
    DEFAULT_SYSTEM_USER_ROOT = 'root'
    DEFAULT_SYSTEM_USER_VDSM = 'vdsm'
    DEFAULT_SYSTEM_GROUP_KVM = 'kvm'
    DEFAULT_SYSTEM_USER_APACHE = 'apache'
    DEFAULT_SYSTEM_USER_POSTGRES = 'postgres'

    @classproperty
    def DEFAULT_SYSTEM_SHMMAX(self):
        SHMMAX = {
            'x86_64': 68719476736,
            'i686': 4294967295,
            'ppc64':  137438953472,
            'default': 4294967295,
        }
        return SHMMAX.get(platform.machine(), SHMMAX['default'])

    DEFAULT_PKI_COUNTRY = 'US'
    DEFAULT_PKI_STORE_PASS = 'mypass'

    DEFAULT_NETWORK_HTTP_PORT = 80
    DEFAULT_NETWORK_HTTPS_PORT = 443
    DEFAULT_NETWORK_JBOSS_HTTP_PORT = 8080
    DEFAULT_NETWORK_JBOSS_HTTPS_PORT = 8443
    DEFAULT_NETWORK_JBOSS_AJP_PORT = 8702
    DEFAULT_NETWORK_JBOSS_DEBUG_ADDRESS = '127.0.0.1:8787'

    DEFAULT_HTTPD_SERVICE = 'httpd'

    DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR = os.path.join(
        osetupcons.FileLocations.LOCALSTATEDIR,
        'lib',
        'pgsql',
        'data',
    )

    DEFAULT_POSTGRES_PROVISIONING_PG_CONF = os.path.join(
        DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR,
        'postgresql.conf',
    )

    DEFAULT_POSTGRES_PROVISIONING_PG_HBA = os.path.join(
        DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR,
        'pg_hba.conf',
    )

    DEFAULT_POSTGRES_PROVISIONING_PG_VERSION = os.path.join(
        DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR,
        'PG_VERSION',
    )

    DEFAULT_POSTGRES_PROVISIONING_SERVICE = 'postgresql'
    DEFAULT_POSTGRES_PROVISIONING_OLD_SERVICE = 'postgresql'
    DEFAULT_POSTGRES_PROVISIONING_MAX_CONN = 150
    DEFAULT_POSTGRES_PROVISIONING_LISTEN_ADDRESS = "'*'"
    DEFAULT_POSTGRES_PROVISIONING_LC_MESSAGES = "'en_US.UTF-8'"
    PG_PROV_AUTOVACUUM_VACUUM_SCALE_FACTOR = 0.01
    PG_PROV_AUTOVACUUM_ANALYZE_SCALE_FACTOR = 0.075
    PG_PROV_AUTOVACUUM_MAX_WORKERS = 6
    PG_PROV_AUTOVACUUM_MAINTENANCE_WORK_MEM = 65536
    PG_PROV_WORK_MEM_KB = 8192


@util.export
class Stages(object):
    ADMIN_PASSWORD_SET = 'osetup.admin.password.set'
    APACHE_RESTART = 'osetup.apache.core.restart'

    CORE_ENGINE_START = 'osetup.core.engine.start'

    DB_CONNECTION_SETUP = 'osetup.db.connection.setup'
    DB_CONNECTION_CUSTOMIZATION = 'osetup.db.connection.customization'
    DB_OWNERS_CONNECTIONS_CUSTOMIZED = \
        'osetup.db.owners.connections.customized'
    DB_CREDENTIALS_AVAILABLE_EARLY = 'osetup.db.connection.credentials.early'
    DB_CREDENTIALS_AVAILABLE_LATE = 'osetup.db.connection.credentials.late'
    DB_CREDENTIALS_WRITTEN = 'osetup.db.connection.credentials.written'
    DB_CONNECTION_AVAILABLE = 'osetup.db.connection.available'
    DB_SCHEMA = 'osetup.db.schema'
    DB_UPGRADEDBMS_ENGINE = 'osetup.db.upgrade.dbms.engine'
    DB_UPGRADEDBMS_DWH = 'osetup.db.upgrade.dbms.dwh'
    DB_CUST_UPGRADEDBMS_ENGINE = 'osetup.db.cust.upgrade.dbms.engine'
    DB_CUST_UPGRADEDBMS_DWH = 'osetup.db.cust.upgrade.dbms.dwh'

    CONFIG_DB_ENCRYPTION_AVAILABLE = 'osetup.config.encryption.available'

    NETWORK_OWNERS_CONFIG_CUSTOMIZED = \
        'osetup.network.owners.config.customized'

    DIALOG_TITLES_S_APACHE = 'osetup.dialog.titles.apache.start'
    DIALOG_TITLES_S_DATABASE = 'osetup.dialog.titles.database.start'
    DIALOG_TITLES_S_PKI = 'osetup.dialog.titles.pki.start'
    DIALOG_TITLES_E_APACHE = 'osetup.dialog.titles.apache.end'
    DIALOG_TITLES_E_DATABASE = 'osetup.dialog.titles.database.end'
    DIALOG_TITLES_E_PKI = 'osetup.dialog.titles.pki.end'

    DIALOG_TITLES_S_ENGINE = 'osetup.dialog.titles.engine.start'
    DIALOG_TITLES_E_ENGINE = 'osetup.dialog.titles.engine.end'

    DIALOG_TITLES_S_STORAGE = 'osetup.dialog.titles.storage.start'
    DIALOG_TITLES_E_STORAGE = 'osetup.dialog.titles.storage.end'

    RENAME_PKI_CONF_MISC = 'osetup.rename.pki.conf.misc'


@util.export
@util.codegen
class Const(object):
    # Enable only TLSv1.2 protocol. More information at
    # https://httpd.apache.org/docs/current/mod/mod_ssl.html#sslprotocol
    HTTPD_SSL_PROTOCOLS = '-all +TLSv1.2'


@util.export
@util.codegen
@osetupattrsclass
class DBEnvKeysConst(object):
    HOST = 'host'
    PORT = 'port'
    SECURED = 'secured'
    HOST_VALIDATION = 'hostValidation'
    USER = 'user'
    PASSWORD = 'password'
    DATABASE = 'database'
    CONNECTION = 'connection'
    PGPASSFILE = 'pgpassfile'
    NEW_DATABASE = 'newDatabase'
    NEED_DBMSUPGRADE = 'needDBMSUpgrade'
    DUMPER = 'dumper'
    FILTER = 'filter'
    RESTORE_JOBS = 'restoreJobs'
    INVALID_CONFIG_ITEMS = 'invalidConfigItems'

    REQUIRED_KEYS = (
        HOST,
        PORT,
        SECURED,
        HOST_VALIDATION,
        USER,
        PASSWORD,
        DATABASE,
        CONNECTION,
        PGPASSFILE,
        NEW_DATABASE,
        NEED_DBMSUPGRADE,
        DUMPER,
        FILTER,
        RESTORE_JOBS,
    )

    DEFAULTS_KEYS = (
        USER,
        DATABASE,
        PORT,
        SECURED,
        HOST_VALIDATION,
    )


@util.export
@util.codegen
@osetupattrsclass
class SystemEnv(object):

    USER_APACHE = 'OVESETUP_SYSTEM/userApache'
    USER_POSTGRES = 'OVESETUP_SYSTEM/userPostgres'
    USER_ROOT = 'OVESETUP_SYSTEM/userRoot'
    USER_VDSM = 'OVESETUP_SYSTEM/userVdsm'
    GROUP_KVM = 'OVESETUP_SYSTEM/groupKvm'

    SHMMAX = 'OVESETUP_SYSTEM/shmmax'


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):

    JAVA_HOME = 'OVESETUP_CONFIG/javaHome'
    JBOSS_HOME = 'OVESETUP_CONFIG/jbossHome'

    PUBLIC_HTTP_PORT = 'OVESETUP_CONFIG/publicHttpPort'  # internal use
    PUBLIC_HTTPS_PORT = 'OVESETUP_CONFIG/publicHttpsPort'  # internal use
    HTTP_PORT = 'OVESETUP_CONFIG/httpPort'
    HTTPS_PORT = 'OVESETUP_CONFIG/httpsPort'
    JBOSS_HTTP_PORT = 'OVESETUP_CONFIG/jbossHttpPort'
    JBOSS_HTTPS_PORT = 'OVESETUP_CONFIG/jbossHttpsPort'
    JBOSS_AJP_PORT = 'OVESETUP_CONFIG/jbossAjpPort'
    JBOSS_DIRECT_HTTP_PORT = 'OVESETUP_CONFIG/jbossDirectHttpPort'
    JBOSS_DIRECT_HTTPS_PORT = 'OVESETUP_CONFIG/jbossDirectHttpsPort'
    JBOSS_DEBUG_ADDRESS = 'OVESETUP_CONFIG/jbossDebugAddress'
    JBOSS_NEEDED = 'OVESETUP_CONFIG/jbossNeeded'
    JAVA_NEEDED = 'OVESETUP_CONFIG/javaNeeded'
    ENGINE_SERVICE_STOP_NEEDED = 'OVESETUP_CONFIG/engineServiceStopNeeded'
    FENCE_KDUMP_LISTENER_STOP_NEEDED = \
        'OVESETUP_CONFIG/fenceKdumpListenerStopNeeded'
    FORCE_INVALID_PG_CONF = 'OVESETUP_CONFIG/forceInvalidPGConf'
    NEED_COMMON_TITLES = 'OVESETUP_CONFIG/needCommonTitles'


@util.export
@util.codegen
@osetupattrsclass
class ProvisioningEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure local Engine database'),
    )
    def POSTGRES_PROVISIONING_ENABLED(self):
        return 'OVESETUP_PROVISIONING/postgresProvisioningEnabled'

    POSTGRES_CONF = 'OVESETUP_PROVISIONING/postgresConf'
    POSTGRES_PG_HBA = 'OVESETUP_PROVISIONING/postgresPgHba'
    POSTGRES_PG_VERSION = 'OVESETUP_PROVISIONING/postgresPgVersion'
    POSTGRES_SERVICE = 'OVESETUP_PROVISIONING/postgresService'
    OLD_POSTGRES_SERVICE = 'OVESETUP_PROVISIONING/oldPostgresService'
    POSTGRES_EXTRA_CONFIG_ITEMS =\
        'OVESETUP_PROVISIONING/postgresExtraConfigItems'
    POSTGRES_MAX_CONN = 'OVESETUP_PROVISIONING/postgresMaxConn'
    POSTGRES_LISTEN_ADDRESS = 'OVESETUP_PROVISIONING/postgresListenAddress'
    POSTGRES_LC_MESSAGES = 'OVESETUP_PROVISIONING/postgresLCMessages'
    PG_AUTOVACUUM_VACUUM_SCALE_FACTOR =\
        'OVESETUP_PROVISIONING/postgresAutovacuumVacuumScaleFactor'
    PG_AUTOVACUUM_ANALYZE_SCALE_FACTOR =\
        'OVESETUP_PROVISIONING/postgresAutovacuumAnalyzeScaleFactor'
    PG_AUTOVACUUM_MAX_WORKERS =\
        'OVESETUP_PROVISIONING/postgresAutovacuumMaxWorkers'
    PG_AUTOVACUUM_MAINTENANCE_WORK_MEM =\
        'OVESETUP_PROVISIONING/postgresAutovacuumMaintenanceWorkMem'
    PG_WORK_MEM_KB =\
        'OVESETUP_PROVISIONING/postgresWorkMemKb'
    PG_UPGRADE_INPLACE = 'OVESETUP_PROVISIONING/postgresUpgradeInplace'
    PG_UPGRADE_CLEANOLD = 'OVESETUP_PROVISIONING/postgresUpgradeCleanold'


@util.export
@util.codegen
@osetupattrsclass
class ApacheEnv(object):

    @osetupattrs(
        postinstallfile=True,
    )
    def CONFIGURED(self):
        return 'OVESETUP_APACHE/configured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Set application as default page'),
    )
    def CONFIGURE_ROOT_REDIRECTION(self):
        return 'OVESETUP_APACHE/configureRootRedirection'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure Apache SSL'),
    )
    def CONFIGURE_SSL(self):
        return 'OVESETUP_APACHE/configureSsl'

    CONFIGURE_ROOT_REDIRECTIOND_DEFAULT = \
        'OVESETUP_APACHE/configureRootRedirectionDefault'
    ENABLE = 'OVESETUP_APACHE/enable'
    HTTPD_CONF_OVIRT_ROOT = 'OVESETUP_APACHE/configFileOvirtRoot'
    HTTPD_CONF_SSL = 'OVESETUP_APACHE/configFileSsl'
    HTTPD_SERVICE = 'OVESETUP_APACHE/httpdService'
    NEED_RESTART = 'OVESETUP_APACHE/needRestart'


@util.export
@util.codegen
@osetupattrsclass
class RPMDistroEnv(object):
    OVIRT_JBOSS_PACKAGES = 'OVESETUP_RPMDISTRO/jbossPackages'


# vim: expandtab tabstop=4 shiftwidth=4
