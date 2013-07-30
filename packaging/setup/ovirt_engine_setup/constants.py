#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Constants."""


import os
import sys
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util


from . import config


def osetupattrsclass(o):
    sys.modules[o.__module__].__dict__.setdefault(
        '__osetup_attrs__', []
    ).append(o)
    return o


class classproperty(property):
    def __get__(self, cls, owner):
        return classmethod(self.fget).__get__(None, owner)()


def osetupattrs(
    answerfile=False,
    summary=False,
    description=None,
    postinstallfile=False,
):
    class decorator(classproperty):
        def __init__(self, o):
            super(decorator, self).__init__(o)
            self.__osetup_attrs__ = dict(
                answerfile=answerfile,
                summary=summary,
                description=description,
                postinstallfile=postinstallfile
            )
    return decorator


@util.export
class FileLocations(object):
    SYSCONFDIR = '/etc'
    LOCALSTATEDIR = '/var'
    DATADIR = '/usr/share'
    OVIRT_ENGINE_SYSCONFDIR = config.ENGINE_SYSCONFDIR
    OVIRT_ENGINE_PKIDIR = config.ENGINE_PKIDIR
    OVIRT_ENGINE_DATADIR = config.ENGINE_DATADIR
    OVIRT_ENGINE_LOCALSTATEDIR = config.ENGINE_LOCALSTATEDIR
    OVIRT_ENGINE_TMPDIR = config.ENGINE_TMP
    OVIRT_ENGINE_LOGDIR = config.ENGINE_LOG
    OVIRT_ENGINE_SERVICE_CONFIG = config.ENGINE_SERVICE_CONFIG
    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG = config.ENGINE_WEBSOCKET_PROXY_CONFIG
    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG = \
        config.ENGINE_NOTIFIER_SERVICE_CONFIG

    OVIRT_ENGINE_BINDIR = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'bin',
    )

    OVIRT_OVIRT_SETUP_CONFIG_FILE = config.ENGINE_SETUP_CONFIG

    OVIRT_SETUP_LOGDIR = os.path.join(
        OVIRT_ENGINE_LOGDIR,
        'setup',
    )
    OVIRT_OVIRT_SETUP_LOG_PREFIX = 'ovirt-engine-setup'
    OVIRT_OVIRT_REMOVE_LOG_PREFIX = 'ovirt-engine-remove'

    OVIRT_IPTABLES_EXAMPLE = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'iptables.example'
    )

    OVIRT_IPTABLES_DEFAULT = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'conf',
        'iptables.default.in'
    )

    OVIRT_FIREWALLD_CONFIG = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'firewalld',
    )

    OVIRT_FIREWALLD_EXAMPLE_DIR = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'firewalld'
    )

    OVIRT_ENGINE_SYSCTL = os.path.join(
        SYSCONFDIR,
        'sysctl.d',
        'ovirt-engine.conf',
    )

    LEGACY_OVIRT_ENGINE_SYSCONFIG = os.path.join(
        SYSCONFDIR,
        'sysconfig',
        'ovirt-engine',
    )

    LEGACY_PSQL_PASS_FILE = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        '.pgpass',
    )

    LEGACY_FIREWALLD_SERVICE_FILE = os.path.join(
        SYSCONFDIR,
        'firewalld',
        'services',
        'ovirt.xml'
    )

    OVIRT_ENGINE_DB_UTILS_DIR = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'setup',
        'dbutils'
    )
    OVIRT_ENGINE_DB_VALIDATOR = os.path.join(
        OVIRT_ENGINE_DB_UTILS_DIR,
        'validatedb.sh'
    )
    OVIRT_ENGINE_TASKCLEANER = os.path.join(
        OVIRT_ENGINE_DB_UTILS_DIR,
        'taskcleaner.sh'
    )

    OVIRT_ENGINE_DB_DIR = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'dbscripts',
    )
    OVIRT_ENGINE_DB_INSTALL = os.path.join(
        OVIRT_ENGINE_DB_DIR,
        'engine-db-install.sh',
    )
    OVIRT_ENGINE_DB_UPGRADE = os.path.join(
        OVIRT_ENGINE_DB_DIR,
        'upgrade.sh',
    )
    OVIRT_ENGINE_DB_BACKUP_DIR = os.path.join(
        OVIRT_ENGINE_LOCALSTATEDIR,
        'backups',
    )
    OVIRT_ENGINE_DB_MD5_DIR = os.path.join(
        OVIRT_ENGINE_LOCALSTATEDIR,
        'dbmd5',
    )
    OVIRT_ENGINE_DEPLOYMENTS_DIR = os.path.join(
        OVIRT_ENGINE_LOCALSTATEDIR,
        'deployments',
    )

    OVIRT_ENGINE_PKIKEYSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'keys',
    )
    OVIRT_ENGINE_PKICERTSDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'certs',
    )
    OVIRT_ENGINE_PKIPRIVATEDIR = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'private',
    )
    OVIRT_ENGINE_PKI_CA_CREATE = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'pki-create-ca.sh',
    )
    OVIRT_ENGINE_PKI_CA_ENROLL = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'pki-enroll-pkcs12.sh',
    )
    OVIRT_ENGINE_LOG_SETUP_EVENT = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'ovirt-engine-log-setup-event.sh',
    )
    OVIRT_ENGINE_PKI_ENGINE_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'engine.p12',
    )
    OVIRT_ENGINE_PKI_ENGINE_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'engine.cer',
    )
    OVIRT_ENGINE_PKI_ENGINE_SSH_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'engine_id_rsa',
    )
    OVIRT_ENGINE_PKI_APACHE_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'apache.p12',
    )
    OVIRT_ENGINE_PKI_APACHE_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'apache.key.nopass',
    )
    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'websocket-proxy.p12',
    )
    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'websocket-proxy.key.nopass',
    )
    OVIRT_ENGINE_PKI_JBOSS_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'jboss.p12',
    )
    OVIRT_ENGINE_PKI_ENGINE_CA_CERT = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'ca.pem',
    )
    OVIRT_ENGINE_PKI_APACHE_CA_CERT = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'apache-ca.pem',
    )
    OVIRT_ENGINE_PKI_APACHE_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'apache.cer',
    )
    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'websocket-proxy.cer',
    )
    OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        '.truststore',
    )
    OVIRT_ENGINE_PKI_CA_TEMPLATE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cacert.template.in',
    )
    OVIRT_ENGINE_PKI_CERT_TEMPLATE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cert.template.in',
    )
    OVIRT_ENGINE_PKI_CA_CERT_CONF = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cacert.conf',
    )
    OVIRT_ENGINE_PKI_CERT_CONF = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cert.conf',
    )
    OVIRT_ENGINE_PKI_ENGINE_CA_KEY = os.path.join(
        OVIRT_ENGINE_PKIPRIVATEDIR,
        'ca.pem',
    )

    OVIRT_ENGINE_YUM_VERSIONLOCK = os.path.join(
        SYSCONFDIR,
        'yum',
        'pluginconf.d',
        'versionlock.list',
    )
    OVIRT_ENGINE_SERVICE_CONFIGD = '%s.d' % OVIRT_ENGINE_SERVICE_CONFIG
    OVIRT_ENGINE_SERVICE_CONFIG_DATABASE = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-database.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_PROTOCOLS = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-protocols.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_JAVA = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-java.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_JBOSS = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-jboss.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_PKI = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-pki.conf',
    )

    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIGD = (
        '%s.d' % OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG
    )
    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG_SETUP = os.path.join(
        OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIGD,
        '10-setup.conf',
    )

    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIGD = (
        '%s.d' % OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG
    )
    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JAVA = os.path.join(
        OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIGD,
        '10-setup-java.conf',
    )
    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JBOSS = os.path.join(
        OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIGD,
        '10-setup-jboss.conf',
    )

    NFS_RHEL_CONFIG = os.path.join(
        SYSCONFDIR,
        'sysconfig',
        'nfs',
    )
    OVIRT_NFS_RHEL_CONFIG = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'conf',
        'nfs.sysconfig.in'
    )
    NFS_EXPORT_FILE = os.path.join(
        SYSCONFDIR,
        'exports',
    )
    NFS_EXPORT_DIR = os.path.join(
        SYSCONFDIR,
        'exports.d',
    )
    OVIRT_NFS_EXPORT_FILE = os.path.join(
        NFS_EXPORT_DIR,
        'ovirt-engine-iso-domain.exports'
    )
    OVIRT_ENGINE_UNINSTALL_DIR = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'uninstall.d'
    )
    OVIRT_SETUP_POST_INSTALL_CONFIG = os.path.join(
        '%s.d' % OVIRT_OVIRT_SETUP_CONFIG_FILE,
        '20-setup-ovirt-post.conf'
    )

    ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT = os.path.join(
        LOCALSTATEDIR,
        'lib',
        'exports',
        'iso',
    )

    JBOSS_HOME = os.path.join(
        DATADIR,
        'jboss-as',
    )

    VIRTIO_WIN_DIR = os.path.join(
        DATADIR,
        'virtio-win',
    )
    RHEV_GUEST_TOOLS_DIR = os.path.join(
        DATADIR,
        'rhev-guest-tools-iso',
    )

    DIR_HTTPD = os.path.join(
        SYSCONFDIR,
        'httpd',
    )
    HTTPD_CONF_OVIRT_ENGINE = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'z-ovirt-engine-proxy.conf',
    )
    HTTPD_CONF_OVIRT_ROOT = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'ovirt-engine-root-redirect.conf',
    )
    HTTPD_CONF_SSL = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'ssl.conf',
    )

    HTTPD_CONF_OVIRT_ENGINE_TEMPLATE = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'conf',
        'ovirt-engine-proxy.conf.v2.in',
    )

    HTTPD_CONF_OVIRT_ROOT_TEMPLATE = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'conf',
        'ovirt-engine-root-redirect.conf.in',
    )

    HTTPD_CONF_OVIRT_ENGINE_LEGACY = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'ovirt-engine.conf',
    )

    AIO_VDSM_PATH = os.path.join(
        DATADIR,
        'vdsm',
    )
    AIO_STORAGE_DOMAIN_DEFAULT_DIR = os.path.join(
        LOCALSTATEDIR,
        'lib',
        'images',
    )
    AIO_POST_INSTALL_CONFIG = os.path.join(
        '%s.d' % OVIRT_OVIRT_SETUP_CONFIG_FILE,
        '20-setup-aio.conf'
    )


@util.export
class Defaults(object):
    DEFAULT_SYSTEM_USER_ROOT = 'root'
    DEFAULT_SYSTEM_USER_ENGINE = 'ovirt'
    DEFAULT_SYSTEM_USER_APACHE = 'apache'
    DEFAULT_SYSTEM_USER_VDSM = 'vdsm'
    DEFAULT_SYSTEM_GROUP_KVM = 'kvm'
    DEFAULT_SYSTEM_GROUP_ENGINE = 'ovirt'
    DEFAULT_SYSTEM_USER_POSTGRES = 'postgres'

    DEFAULT_SYSTEM_SHMMAX = 35554432

    DEFAULT_SYSTEM_MEMCHECK_MINIMUM_MB = 4096
    DEFAULT_SYSTEM_MEMCHECK_RECOMMENDED_MB = 16384

    DEFAULT_DB_NAME = 'engine'
    DEFAULT_DB_HOST = 'localhost'
    DEFAULT_DB_PORT = 5432
    DEFAULT_DB_DATABASE = 'engine'
    DEFAULT_DB_USER = 'engine'
    DEFAULT_DB_PASSWORD = ''
    DEFAULT_DB_SECURED = False
    DEFAULT_DB_SECURED_HOST_VALIDATION = False
    DEFAULT_CLEAR_TASKS_WAIT_PERIOD = 20

    DEFAULT_PKI_COUNTRY = 'US'
    DEFAULT_PKI_STORE_PASS = 'mypass'

    DEFAULT_NETWORK_HTTP_PORT = 80
    DEFAULT_NETWORK_HTTPS_PORT = 443
    DEFAULT_NETWORK_JBOSS_HTTP_PORT = 8080
    DEFAULT_NETWORK_JBOSS_HTTPS_PORT = 8443
    DEFAULT_NETWORK_JBOSS_AJP_PORT = 8702
    DEFAULT_NETWORK_JBOSS_DEBUG_ADDRESS = '127.0.0.1:8787'
    DEFAULT_WEBSOCKET_PROXY_PORT = 6100

    DEFAULT_CONFIG_APPLICATION_MODE = 'Both'
    DEFAULT_CONFIG_STORAGE_TYPE = 'NFS'

    DEFAULT_ISO_DOMAIN_NAME = 'ISO_DOMAIN'

    DEFAULT_HTTPD_SERVICE = 'httpd'

    DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR = os.path.join(
        FileLocations.LOCALSTATEDIR,
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
    DEFAULT_POSTGRES_PROVISIONING_MAX_CONN = 150


@util.export
class Stages(object):
    CORE_ENGINE_START = 'osetup.core.engine.start'
    DB_CONNECTION_SETUP = 'osetup.db.connection.setup'
    DB_CONNECTION_CUSTOMIZATION = 'osetup.db.connection.customization'
    DB_CONNECTION_STATUS = 'osetup.db.connection.status'
    DB_HOST_LOCATION_CUSTOMIZATION = 'osetup.db.hostlocation.customization'
    DB_CREDENTIALS_AVAILABLE_EARLY = 'osetup.db.connection.credentials.early'
    DB_CREDENTIALS_AVAILABLE_LATE = 'osetup.db.connection.credentials.late'
    DB_CONNECTION_AVAILABLE = 'osetup.db.connection.available'
    DB_SCHEMA = 'osetup.db.schema'
    NET_FIREWALL_MANAGER_AVAILABLE = 'osetup.net.firewallmanager.available'
    NET_FIREWALL_MANAGER_PROCESS_TEMPLATES = \
        'osetup.net.firewallmanager.templates.available'
    CONFIG_DB_CREDENTIALS = 'osetup.config.database.credentials'
    CONFIG_PROTOCOLS_CUSTOMIZATION = 'osetup.config.protocols.customization'
    CONFIG_DB_ENCRYPTION_AVAILABLE = 'osetup.config.encryption.available'
    CONFIG_APPLICATION_MODE_AVAILABLE = \
        'osetup.config.applicationMode.available'
    CA_AVAILABLE = 'osetup.pki.ca.available'
    SYSTEM_NFS_CONFIG_AVAILABLE = 'osetup.system.nfs.available'
    SYSTEM_SYSCTL_CONFIG_AVAILABLE = 'osetup.system.sysctl.available'
    CONFIG_ISO_DOMAIN_AVAILABLE = 'osetup.config.iso_domain.available'
    APACHE_RESTART = 'osetup.apache.core.restart'

    DIALOG_TITLES_S_APACHE = 'osetup.dialog.titles.apache.start'
    DIALOG_TITLES_S_DATABASE = 'osetup.dialog.titles.database.start'
    DIALOG_TITLES_S_ENGINE = 'osetup.dialog.titles.engine.start'
    DIALOG_TITLES_S_NETWORK = 'osetup.dialog.titles.network.start'
    DIALOG_TITLES_S_FIREWALL = 'osetup.dialog.titles.firewall.start'
    DIALOG_TITLES_S_PACKAGES = 'osetup.dialog.titles.packaging.start'
    DIALOG_TITLES_S_PKI = 'osetup.dialog.titles.pki.start'
    DIALOG_TITLES_S_SYSTEM = 'osetup.dialog.titles.system.start'
    DIALOG_TITLES_E_APACHE = 'osetup.dialog.titles.apache.end'
    DIALOG_TITLES_E_DATABASE = 'osetup.dialog.titles.database.end'
    DIALOG_TITLES_E_ENGINE = 'osetup.dialog.titles.engine.end'
    DIALOG_TITLES_E_NETWORK = 'osetup.dialog.titles.network.end'
    DIALOG_TITLES_E_FIREWALL = 'osetup.dialog.titles.firewall.end'
    DIALOG_TITLES_E_PACKAGES = 'osetup.dialog.titles.packages.end'
    DIALOG_TITLES_E_PKI = 'osetup.dialog.titles.pki.end'
    DIALOG_TITLES_E_SYSTEM = 'osetup.dialog.titles.system.end'

    DIALOG_TITLES_S_SUMMARY = 'osetup.dialog.titles.summary.start'
    DIALOG_TITLES_E_SUMMARY = 'osetup.dialog.titles.summary.end'

    AIO_CONFIG_AVAILABLE = 'osetup.aio.config.available'
    AIO_CONFIG_ROOT_PASSWORD = 'osetup.aio.config.rootpassword'
    AIO_CONFIG_STORAGE = 'osetup.aio.config.storage'

    UPGRADE_FROM_LEGACY_CONFIG = 'osetup.legacy.upgrade'


@util.export
@util.codegen
class Const(object):
    PACKAGE_NAME = config.PACKAGE_NAME
    PACKAGE_VERSION = config.PACKAGE_VERSION
    DISPLAY_VERSION = config.DISPLAY_VERSION
    RPM_VERSION = config.RPM_VERSION
    RPM_RELEASE = config.RPM_RELEASE

    USER_ADMIN = 'admin'
    DOMAIN_INTERNAL = 'internal'
    ENGINE_SERVICE_NAME = 'ovirt-engine'
    WEBSOCKET_PROXY_SERVICE_NAME = 'ovirt-websocket-proxy'
    PKI_PASSWORD = 'mypass'
    DEFAULT_CLUSTER_ID = '99408929-82CF-4DC7-A532-9D998063FA95'
    MINIMUM_SPACE_ISODOMAIN_MB = 350
    ISO_DOMAIN_IMAGE_UID = '11111111-1111-1111-1111-111111111111'
    MAC_RANGE_BASE = '00:1a:4a'

    ENGINE_URI = '/ovirt-engine'

    ENGINE_PACKAGE_NAME = 'ovirt-engine'
    ENGINE_PACKAGE_SETUP_NAME = '%s-setup' % ENGINE_PACKAGE_NAME
    UPGRADE_YUM_GROUP = 'ovirt-engine-3.3'

    @classproperty
    def RPM_LOCK_LIST(self):
        return tuple([
            '{name}%s'.format(
                name=self.ENGINE_PACKAGE_NAME,
            ) % package for package in (
                '',
                '-backend',
                '-dbscripts',
                '-restapi',
                '-tools',
                '-userportal',
                '-webadmin-portal',
            )
        ])

    FILE_GROUP_SECTION_PREFIX = 'file_group_'

    ACTION_SETUP = 'setup'
    ACTION_REMOVE = 'cleanup'
    ACTION_UPGRADE = 'upgrade'


@util.export
@util.codegen
@osetupattrsclass
class CoreEnv(object):
    OFFLINE_PACKAGER = 'OVESETUP_CORE/offlinePackager'
    ANSWER_FILE = 'OVESETUP_CORE/answerFile'
    DEVELOPER_MODE = 'OVESETUP_CORE/developerMode'
    UNINSTALL_UNREMOVABLE_FILES = 'OVESETUP_CORE/uninstallUnremovableFiles'
    FILE_GROUP_PREFIX = 'OVESETUP_CORE_MODIFIED_FILE_GROUP/'
    LINES_GROUP_PREFIX = 'OVESETUP_CORE_MODIFIED_LINES_GROUP/'
    UNINSTALL_ENABLED_FILE_GROUPS = 'OVESETUP_CORE/uninstallEnabledFileGroups'
    REGISTER_UNINSTALL_GROUPS = 'OVESETUP_CORE/registerUninstallGroups'
    CONFIRM_UNINSTALL_GROUPS = 'OVESETUP_CORE/confirmUninstallGroups'
    UPGRADE_SUPPORTED_VERSIONS = 'OVESETUP_CORE/upgradeSupportedVersions'
    UPGRADE_FROM_LEGACY = 'OVESETUP_CORE/upgradeFromLegacy'
    ACTION = 'OVESETUP_CORE/action'

    @osetupattrs(
        answerfile=True,
    )
    def ENGINE_SERVICE_STOP(self):
        return 'OVESETUP_CORE/engineStop'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE(self):
        return 'OVESETUP_CORE/remove'


@util.export
@util.codegen
@osetupattrsclass
class DialogEnv(object):
    @osetupattrs(
        answerfile=True,
    )
    def CONFIRM_SETTINGS(self):
        return 'OVESETUP_DIALOG/confirmSettings'


@util.export
@util.codegen
class NetEnv(object):
    FIREWALLD_SERVICES = 'OVESETUP_NETWORK/firewalldServices'
    FIREWALLD_SUBST = 'OVESETUP_NETWORK/firewalldSubst'


@util.export
@util.codegen
@osetupattrsclass
class DBEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Database host'),
    )
    def HOST(self):
        return 'OVESETUP_DB/host'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Datbase port'),
    )
    def PORT(self):
        return 'OVESETUP_DB/port'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Database secured connection'),
    )
    def SECURED(self):
        return 'OVESETUP_DB/secured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Database host name validation'),
    )
    def SECURED_HOST_VALIDATION(self):
        return 'OVESETUP_DB/securedHostValidation'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Database name'),
    )
    def DATABASE(self):
        return 'OVESETUP_DB/database'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Database user name'),
    )
    def USER(self):
        return 'OVESETUP_DB/user'

    @osetupattrs(
        answerfile=True,
    )
    def PASSWORD(self):
        return 'OVESETUP_DB/password'

    CONNECTION = 'OVESETUP_DB/connection'
    STATEMENT = 'OVESETUP_DB/statement'
    PGPASS_FILE = 'OVESETUP_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_DB/newDatabase'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_EMPTY_DATABASE(self):
        return 'OVESETUP_DB/cleanupRemove'

    @osetupattrs(
        answerfile=True,
    )
    def FIX_DB_VIOLATIONS(self):
        return 'OVESETUP_DB/fixDbViolations'


@util.export
@util.codegen
@osetupattrsclass
class SystemEnv(object):

    USER_ROOT = 'OVESETUP_SYSTEM/userRoot'
    USER_ENGINE = 'OVESETUP_SYSTEM/userEngine'
    USER_APACHE = 'OVESETUP_SYSTEM/userApache'
    USER_VDSM = 'OVESETUP_SYSTEM/userVdsm'
    GROUP_KVM = 'OVESETUP_SYSTEM/groupKvm'
    GROUP_ENGINE = 'OVESETUP_SYSTEM/groupEngine'
    USER_POSTGRES = 'OVESETUP_SYSTEM/userPostgres'

    SHMMAX = 'OVESETUP_SYSTEM/shmmax'

    MEMCHECK_ENABLED = 'OVESETUP_SYSTEM/memCheckEnabled'
    MEMCHECK_MINIMUM_MB = 'OVESETUP_SYSTEM/memCheckMinimumMB'
    MEMCHECK_RECOMMENDED_MB = 'OVESETUP_SYSTEM/memCheckRecommendedMB'

    SELINUX_CONTEXTS = 'OVESETUP_SYSTEM/selinuxContexts'
    SELINUX_RESTORE_PATHS = 'OVESETUP_SYSTEM/selinuxRestorePaths'

    NFS_SERVICE_NAME = 'OVESETUP_SYSTEM/nfsServiceName'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('NFS setup'),
        postinstallfile=True,
    )
    def NFS_CONFIG_ENABLED(self):
        return 'OVESETUP_SYSTEM/nfsConfigEnabled'

    HOSTILE_SERVICES = 'OVESETUP_SYSTEM/hostileServices'


@util.export
@util.codegen
@osetupattrsclass
class PKIEnv(object):
    STORE_PASS = 'OVESETUP_PKI/storePassword'

    COUNTRY = 'OVESETUP_PKI/country'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('PKI organization'),
    )
    def ORG(self):
        return 'OVESETUP_PKI/organization'


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
    WEBSOCKET_PROXY_PORT = 'OVESETUP_CONFIG/websocketProxyPort'
    JBOSS_DEBUG_ADDRESS = 'OVESETUP_CONFIG/jbossDebugAddress'

    MAC_RANGE_POOL = 'OVESETUP_CONFIG/macRangePool'

    @osetupattrs(
        postinstallfile=True,
    )
    def ISO_DOMAIN_EXISTS(self):
        return 'OVESETUP_CONFIG/isoDomainExists'

    @osetupattrs(
        postinstallfile=True,
    )
    def ISO_DOMAIN_SD_UUID(self):
        return 'OVESETUP_CONFIG/isoDomainSdUuid'

    @osetupattrs(
        postinstallfile=True,
    )
    def ISO_DOMAIN_STORAGE_DIR(self):
        return 'OVESETUP_CONFIG/isoDomainStorageDir'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Application mode'),
    )
    def APPLICATION_MODE(self):
        return 'OVESETUP_CONFIG/applicationMode'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Datacenter storage type'),
    )
    def STORAGE_TYPE(self):
        return 'OVESETUP_CONFIG/storageType'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Host fqdn'),
        postinstallfile=True,
    )
    def FQDN(self):
        return 'OVESETUP_CONFIG/fqdn'

    @osetupattrs(
        answerfile=True,
    )
    def ADMIN_PASSWORD(self):
        return 'OVESETUP_CONFIG/adminPassword'

    ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT = \
        'OVESETUP_CONFIG/isoDomainDefaultMountPoint'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('NFS mount point'),
        postinstallfile=True,
    )
    def ISO_DOMAIN_NFS_MOUNT_POINT(self):
        return 'OVESETUP_CONFIG/isoDomainMountPoint'

    @osetupattrs(
        answerfile=True,
        postinstallfile=True
    )
    def ISO_DOMAIN_NAME(self):
        return 'OVESETUP_CONFIG/isoDomainName'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Firewall manager'),
        postinstallfile=True,
    )
    def FIREWALL_MANAGER(self):
        return 'OVESETUP_CONFIG/firewallManager'

    FQDN_REVERSE_VALIDATION = 'OVESETUP_CONFIG/fqdnReverseValidation'
    FQDN_NON_LOOPBACK_VALIDATION = 'OVESETUP_CONFIG/fqdnNonLoopback'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure WebSocket Proxy'),
    )
    def WEBSOCKET_PROXY_CONFIG(self):
        return 'OVESETUP_CONFIG/websocketProxyConfig'


@util.export
@util.codegen
@osetupattrsclass
class ProvisioningEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure local database'),
    )
    def POSTGRES_PROVISIONING_ENABLED(self):
        return 'OVESETUP_PROVISIONING/postgresProvisioningEnabled'

    POSTGRES_CONF = 'OVESETUP_PROVISIONING/postgresConf'
    POSTGRES_PG_HBA = 'OVESETUP_PROVISIONING/postgresPgHba'
    POSTGRES_PG_VERSION = 'OVESETUP_PROVISIONING/postgresPgVersion'
    POSTGRES_SERVICE = 'OVESETUP_PROVISIONING/postgresService'
    POSTGRES_MAX_CONN = 'OVESETUP_PROVISIONING/postgresMaxConn'


@util.export
@util.codegen
@osetupattrsclass
class ApacheEnv(object):

    HTTPD_SERVICE = 'OVESETUP_APACHE/httpdService'

    HTTPD_CONF_SSL = 'OVESETUP_APACHE/configFileSsl'
    HTTPD_CONF_OVIRT_ENGINE = 'OVESETUP_APACHE/configFileOvirtEngine'
    HTTPD_CONF_OVIRT_ROOT = 'OVESETUP_APACHE/configFileOvirtRoot'

    CONFIGURE_ROOT_REDIRECTIOND_DEFAULT = \
        'OVESETUP_APACHE/configureRootRedirectionDefault'

    NEED_RESTART = 'OVESETUP_APACHE/needRestart'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure Apache SSL'),
        postinstallfile=True,
    )
    def CONFIGURE_SSL(self):
        return 'OVESETUP_APACHE/configureSsl'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Set application as default page'),
        postinstallfile=True,
    )
    def CONFIGURE_ROOT_REDIRECTION(self):
        return 'OVESETUP_APACHE/configureRootRedirection'


@util.export
@util.codegen
@osetupattrsclass
class RPMDistroEnv(object):
    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Upgrade packages'),
    )
    def ENABLE_UPGRADE(self):
        return 'OSETUP_RPMDISTRO/enableUpgrade'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Require packages rollback'),
    )
    def REQUIRE_ROLLBACK(self):
        return 'OSETUP_RPMDISTRO/requireRollback'


@util.export
@util.codegen
@osetupattrsclass
class AIOEnv(object):
    ENABLE = 'OVESETUP_AIO/enable'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure VDSM on this host'),
    )
    def CONFIGURE(self):
        return 'OVESETUP_AIO/configure'

    @osetupattrs(
        answerfile=True,
        summary=False,
    )
    def ROOT_PASSWORD(self):
        return 'OVESETUP_AIO/rootPassword'

    LOCAL_DATA_CENTER = 'OVESETUP_AIO/localDataCenter'
    LOCAL_CLUSTER = 'OVESETUP_AIO/localCluster'
    LOCAL_HOST = 'OVESETUP_AIO/localHost'
    VDSM_CPU = 'OVESETUP_AIO/vdsmCpu'

    STORAGE_DOMAIN_SD_UUID = 'OVESETUP_AIO/storageDomainSdUuid'
    STORAGE_DOMAIN_DEFAULT_DIR = 'OVESETUP_AIO/storageDomainDefaultDir'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Local storage domain directory'),
    )
    def STORAGE_DOMAIN_DIR(self):
        return 'OVESETUP_AIO/storageDomainDir'

    STORAGE_DOMAIN_NAME = 'OVESETUP_AIO/storageDomainName'


@util.export
class AIODefaults(object):
    DEFAULT_LOCAL_DATA_CENTER = 'local_datacenter'
    DEFAULT_LOCAL_CLUSTER = 'local_cluster'
    DEFAULT_LOCAL_HOST = 'local_host'
    DEFAULT_STORAGE_DOMAIN_NAME = 'local_storage'


@util.export
@util.codegen
class AIOConst(object):
    MINIMUM_SPACE_STORAGEDOMAIN_MB = 10240


class AsyncTasksEnv(object):

    @osetupattrs(
        answerfile=True,
    )
    def CLEAR_TASKS(self):
        return 'OVESETUP_ASYNC/clearTasks'

    @osetupattrs(
        answerfile=True,
    )
    def CLEAR_TASKS_WAIT_PERIOD(self):
        return 'OVESETUP_ASYNC/clearTasksWait'


# vim: expandtab tabstop=4 shiftwidth=4
