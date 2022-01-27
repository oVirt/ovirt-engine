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

from otopi import util

from ovirt_engine_setup import config as osetupconfig
from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.constants import classproperty
from ovirt_engine_setup.constants import osetupattrs
from ovirt_engine_setup.constants import osetupattrsclass
from ovirt_engine_setup.engine_common import constants as oengcommcons

from . import config

DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class FileLocations(object):
    SYSCONFDIR = '/etc'
    LOCALSTATEDIR = '/var'
    DATADIR = '/usr/share'

    OVIRT_ENGINE_SYSCONFDIR = config.ENGINE_SYSCONFDIR
    OVIRT_ENGINE_PKIDIR = config.ENGINE_PKIDIR
    OVIRT_ENGINE_DATADIR = config.ENGINE_DATADIR
    OVIRT_ENGINE_LOCALSTATEDIR = config.ENGINE_LOCALSTATEDIR
    OVIRT_ENGINE_LOGDIR = config.ENGINE_LOG
    OVIRT_ENGINE_SERVICE_CONFIG = config.ENGINE_SERVICE_CONFIG
    OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS = \
        config.ENGINE_SERVICE_CONFIG_DEFAULTS
    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG = \
        config.ENGINE_NOTIFIER_SERVICE_CONFIG

    OVIRT_ENGINE_BINDIR = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'bin',
    )

    OVIRT_ENGINE_DB_DIR = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'dbscripts',
    )
    OVIRT_ENGINE_DB_SCHMA_TOOL = os.path.join(
        OVIRT_ENGINE_DB_DIR,
        'schema.sh',
    )
    OVIRT_ENGINE_DEFAULT_DB_BACKUP_DIR = os.path.join(
        OVIRT_ENGINE_LOCALSTATEDIR,
        'backups',
    )

    OVIRT_ENGINE_DB_MD5_DIR = os.path.join(
        OVIRT_ENGINE_LOCALSTATEDIR,
        'dbmd5',
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
    OVIRT_ENGINE_UNLOCK_ENTITY = os.path.join(
        OVIRT_ENGINE_DB_UTILS_DIR,
        'unlock_entity.sh'
    )

    OVIRT_ENGINE_CRYPTO_TOOL = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'ovirt-engine-crypto-tool.sh',
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
    OVIRT_ENGINE_PKI_PKCS12_EXTRACT = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'pki-pkcs12-extract.sh',
    )
    OVIRT_ENGINE_PKI_SSH_ENROLL = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'pki-enroll-openssh-cert.sh',
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
    OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'websocket-proxy.p12',
    )
    OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'websocket-proxy.key.nopass',
    )
    OVIRT_ENGINE_PKI_REPORTS_KEY = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'reports.key.nopass',
    )
    OVIRT_ENGINE_PKI_JBOSS_STORE = os.path.join(
        OVIRT_ENGINE_PKIKEYSDIR,
        'jboss.p12',
    )
    OVIRT_ENGINE_PKI_JBOSS_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'jboss.cer',
    )
    OVIRT_ENGINE_PKI_ENGINE_CA_CERT = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'ca.pem',
    )
    OVIRT_ENGINE_PKI_ENGINE_QEMU_CA_CERT = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'qemu-ca.pem',
    )
    OVIRT_ENGINE_PKI_LOCAL_WEBSOCKET_PROXY_CERT = os.path.join(
        OVIRT_ENGINE_PKICERTSDIR,
        'websocket-proxy.cer',
    )
    OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        '.truststore',
    )
    OVIRT_ENGINE_PKI_CA_TEMPLATE_IN = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cacert.template.in',
    )
    OVIRT_ENGINE_PKI_CERT_TEMPLATE_IN = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cert.template.in',
    )
    OVIRT_ENGINE_PKI_CA_TEMPLATE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cacert.template',
    )
    OVIRT_ENGINE_PKI_QEMU_CA_TEMPLATE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'qemu-cacert.template',
    )
    OVIRT_ENGINE_PKI_CERT_TEMPLATE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cert.template',
    )
    OVIRT_ENGINE_PKI_QEMU_CERT_TEMPLATE = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'qemu-cert.template',
    )
    OVIRT_ENGINE_PKI_CA_CERT_CONF = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cacert.conf',
    )
    OVIRT_ENGINE_PKI_QEMU_CA_CERT_CONF = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'qemu-cacert.conf',
    )
    OVIRT_ENGINE_PKI_CERT_CONF = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'cert.conf',
    )
    OVIRT_ENGINE_PKI_QEMU_CERT_CONF = os.path.join(
        OVIRT_ENGINE_PKIDIR,
        'qemu-cert.conf',
    )
    OVIRT_ENGINE_PKI_ENGINE_CA_KEY = os.path.join(
        OVIRT_ENGINE_PKIPRIVATEDIR,
        'ca.pem',
    )
    OVIRT_ENGINE_PKI_ENGINE_QEMU_CA_KEY = os.path.join(
        OVIRT_ENGINE_PKIPRIVATEDIR,
        'qemu-ca.pem',
    )
    OVIRT_ENGINE_CRYPTO_TOOL = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'ovirt-engine-crypto-tool.sh',
    )

    NFS_RHEL_CONFIG = os.path.join(
        SYSCONFDIR,
        'sysconfig',
        'nfs',
    )
    NFS_EXPORT_FILE = os.path.join(
        SYSCONFDIR,
        'exports',
    )
    NFS_EXPORT_DIR = os.path.join(
        SYSCONFDIR,
        'exports.d',
    )

    NSS_DB_DIR = os.path.join(
        SYSCONFDIR,
        'pki',
        'nssdb',
    )

    OVIRT_NFS_EXPORT_FILE = os.path.join(
        NFS_EXPORT_DIR,
        'ovirt-engine-iso-domain.exports'
    )

    ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT = os.path.join(
        LOCALSTATEDIR,
        'lib',
        'exports',
        'iso',
    )

    ANSIBLE_RUNNER_SERVICE_SELINUX = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'selinux',
    )

    ANSIBLE_RUNNER_SERVICE_CONF = os.path.join(
        SYSCONFDIR,
        'ansible-runner-service',
        'config.yaml',
    )

    ANSIBLE_RUNNER_SERVICE_PROJECT = os.path.join(
        OVIRT_ENGINE_DATADIR,
        'ansible-runner-service-project',
    )

    DIR_HTTPD = os.path.join(
        SYSCONFDIR,
        'httpd',
    )

    DIR_WWW = os.path.join(
        LOCALSTATEDIR,
        'www',
    )

    HTTPD_RUNNER_WSGI_SCRIPT = os.path.join(
        DIR_WWW,
        'runnner',
        'runner.wsgi',
    )

    HTTPD_CONF_ANSIBLE_RUNNER_SERVICE = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'zz-ansible-runner-service.conf',
    )

    HTTPD_CONF_OVIRT_ENGINE = os.path.join(
        DIR_HTTPD,
        'conf.d',
        'z-ovirt-engine-proxy.conf',
    )

    HTTPD_CONF_OVIRT_ENGINE_TEMPLATE = os.path.join(
        osetupcons.FileLocations.OVIRT_SETUP_DATADIR,
        'conf',
        'ovirt-engine-proxy.conf.v2.in',
    )

    OVIRT_ENGINE_SERVICE_CONFIGD = '%s.d' % OVIRT_ENGINE_SERVICE_CONFIG
    OVIRT_ENGINE_SERVICE_CONFIG_DATABASE = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-database.conf',
    )
    # This keeps DWH database credentials, so that the engine
    # can access it.
    OVIRT_ENGINE_SERVICE_CONFIG_DWH_DATABASE = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-dwh-database.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_PROTOCOLS = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-protocols.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_JBOSS = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-jboss.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_PKI = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-pki.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_SSO = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '11-setup-sso.conf',
    )

    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIGD = (
        '%s.d' % OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG
    )
    OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JBOSS = os.path.join(
        OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIGD,
        '10-setup-jboss.conf',
    )
    OVIRT_ENGINE_SERVICE_CONFIG_JAVA = os.path.join(
        OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-java.conf',
    )
    OVIRT_ENGINE_EXTENSIONS_DIR = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'extensions.d'
    )

    AAA_JDBC_DB_SCHMA_TOOL = os.path.join(
        config.AAA_JDBC_DATADIR,
        'dbscripts',
        'schema.sh'
    )

    AAA_JDBC_TOOL = os.path.join(
        osetupconfig.BIR_DIR,
        'ovirt-aaa-jdbc-tool'
    )

    AAA_JDBC_CONFIG_DB = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'aaa',
        'internal.properties'
    )

    OVIRT_ENGINE_VACUUM_TOOL = os.path.join(
        OVIRT_ENGINE_BINDIR,
        'engine-vacuum.sh',
    )

    EXTERNAL_TRUSTSTORE = os.path.join(
        OVIRT_ENGINE_LOCALSTATEDIR,
        'external_truststore',
    )


@util.export
class Defaults(object):
    DEFAULT_SYSTEM_MEMCHECK_MINIMUM_MB = 4096
    DEFAULT_SYSTEM_MEMCHECK_RECOMMENDED_MB = 16384
    DEFAULT_SYSTEM_MEMCHECK_THRESHOLD = 90

    DEFAULT_CONFIG_APPLICATION_MODE = 'Both'
    DEFAULT_CONFIG_STORAGE_IS_LOCAL = False

    DEFAULT_ISO_DOMAIN_NAME = 'ISO_DOMAIN'

    DEFAULT_CLEAR_TASKS_WAIT_PERIOD = 20

    DEFAULT_ANSIBLE_RUNNER_SERVICE_PORT = '50001'

    DEFAULT_DB_HOST = 'localhost'
    DEFAULT_DB_PORT = 5432
    DEFAULT_DB_DATABASE = 'engine'
    DEFAULT_DB_USER = 'engine'
    DEFAULT_DB_PASSWORD = ''
    DEFAULT_DB_SECURED = False
    DEFAULT_DB_SECURED_HOST_VALIDATION = False
    DEFAULT_DB_DUMPER = 'pg_custom'
    DEFAULT_DB_RESTORE_JOBS = 2
    DEFAULT_DB_FILTER = None
    DEFAULT_PKI_RENEWAL_DOC_URL = (
        'https://www.ovirt.org/'
        'develop/release-management/features/infra/pki-renew/'
    )

    DEFAULT_ADDITIONAL_PACKAGES = (
        'ovirt-engine-ui-extensions'
        ',ovirt-web-ui'
    )

    DEFAULT_OVN_FIREWALLD_SERVICES = (
        'ovn-central-firewall-service'
        ',ovirt-provider-ovn'
    )


@util.export
class Stages(object):

    SYSTEM_NFS_CONFIG_AVAILABLE = 'osetup.system.nfs.available'

    CONFIG_ISO_DOMAIN_AVAILABLE = 'osetup.config.iso_domain.available'

    CONFIG_EXTENSIONS_UPGRADE = 'osetup.config.extensions.upgrade'

    CONFIG_AAA_ADMIN_USER_SETUP = 'osetup.config.aaa.adminuser.setup'

    CORE_ENABLE = 'osetup.engine.core.enable'

    MEMORY_CHECK = 'osetup.memory.check'

    CA_AVAILABLE = 'osetup.pki.ca.available'
    QEMU_CA_AVAILABLE = 'osetup.pki.qemu.ca.available'

    POSTGRES_PROVISIONING_ALLOWED = 'osetup.engine.provisioning.pgsql.allow'
    NFS_CONFIG_ALLOWED = 'osetup.engine.system.nfs.allow'
    APPMODE_ALLOWED = 'osetup.engine.config.appmode.allow'
    KDUMP_ALLOW = 'osetup.engine.kdump.allow'
    CONNECTION_ALLOW = 'osetup.engine.db.connection.allow'

    REMOVE_CUSTOMIZATION_ENGINE = 'osetup.remove.customization.engine'

    OVN_SERVICES_RESTART = 'osetup.ovn.services.restart'
    OVN_PROVIDER_SERVICE_RESTART = 'osetup.ovn.provider.service.restart'
    OVN_PROVIDER_OVN_DB = 'osetup.ovn.provider.db'

    MAC_POOL_DB = 'osetup.macpool.db'


def _pki_ca_uri(uri, resource):
    return (
        '%s/services/pki-resource?'
        'resource=%s&'
        'format=X509-PEM-CA'
    ) % (
        uri, resource,
    )


@util.export
@util.codegen
class Const(object):
    ENGINE_PACKAGE_NAME = 'ovirt-engine'
    ENGINE_PACKAGE_SETUP_NAME = '%s-setup' % ENGINE_PACKAGE_NAME

    ENGINE_SERVICE_NAME = 'ovirt-engine'

    FENCE_KDUMP_LISTENER_SERVICE_NAME = 'ovirt-fence-kdump-listener'

    PKI_PASSWORD = 'mypass'
    MINIMUM_SPACE_ISODOMAIN_MB = 350
    ISO_DOMAIN_IMAGE_UID = '11111111-1111-1111-1111-111111111111'

    ENGINE_URI = '/ovirt-engine'
    ENGINE_PKI_CA_URI = _pki_ca_uri(ENGINE_URI, 'ca-certificate')
    ENGINE_PKI_QEMU_CA_URI = _pki_ca_uri(ENGINE_URI, 'qemu-ca-certificate')

    ENGINE_DB_BACKUP_PREFIX = 'engine'

    OVIRT_PROVIDER_OVN_CLIENT_ID_VALUE = 'ovirt-provider-ovn'

    @classproperty
    def ENGINE_DB_ENV_KEYS(self):
        return {
            DEK.HOST: EngineDBEnv.HOST,
            DEK.PORT: EngineDBEnv.PORT,
            DEK.SECURED: EngineDBEnv.SECURED,
            DEK.HOST_VALIDATION: EngineDBEnv.SECURED_HOST_VALIDATION,
            DEK.USER: EngineDBEnv.USER,
            DEK.PASSWORD: EngineDBEnv.PASSWORD,
            DEK.DATABASE: EngineDBEnv.DATABASE,
            DEK.CONNECTION: EngineDBEnv.CONNECTION,
            DEK.PGPASSFILE: EngineDBEnv.PGPASS_FILE,
            DEK.NEW_DATABASE: EngineDBEnv.NEW_DATABASE,
            DEK.NEED_DBMSUPGRADE: EngineDBEnv.NEED_DBMSUPGRADE,
            DEK.DUMPER: EngineDBEnv.DUMPER,
            DEK.FILTER: EngineDBEnv.FILTER,
            DEK.RESTORE_JOBS: EngineDBEnv.RESTORE_JOBS,
            DEK.INVALID_CONFIG_ITEMS: EngineDBEnv.INVALID_CONFIG_ITEMS,
        }

    @classproperty
    def DEFAULT_ENGINE_DB_ENV_KEYS(self):
        return {
            DEK.HOST: Defaults.DEFAULT_DB_HOST,
            DEK.PORT: Defaults.DEFAULT_DB_PORT,
            DEK.SECURED: Defaults.DEFAULT_DB_SECURED,
            DEK.HOST_VALIDATION: Defaults.DEFAULT_DB_SECURED_HOST_VALIDATION,
            DEK.USER: Defaults.DEFAULT_DB_USER,
            DEK.PASSWORD: Defaults.DEFAULT_DB_PASSWORD,
            DEK.DATABASE: Defaults.DEFAULT_DB_DATABASE,
            DEK.DUMPER: Defaults.DEFAULT_DB_DUMPER,
            DEK.FILTER: Defaults.DEFAULT_DB_FILTER,
            DEK.RESTORE_JOBS: Defaults.DEFAULT_DB_RESTORE_JOBS,
        }


@util.export
@util.codegen
@osetupattrsclass
class EngineDBEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database host'),
    )
    def HOST(self):
        return 'OVESETUP_DB/host'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database port'),
    )
    def PORT(self):
        return 'OVESETUP_DB/port'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database secured connection'),
    )
    def SECURED(self):
        return 'OVESETUP_DB/secured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database host name validation'),
    )
    def SECURED_HOST_VALIDATION(self):
        return 'OVESETUP_DB/securedHostValidation'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database name'),
    )
    def DATABASE(self):
        return 'OVESETUP_DB/database'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database user name'),
    )
    def USER(self):
        return 'OVESETUP_DB/user'

    @osetupattrs(
        answerfile=True,
        answerfile_condition=lambda env: not env.get(
            oengcommcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ),
        is_secret=True,
    )
    def PASSWORD(self):
        return 'OVESETUP_DB/password'

    CONNECTION = 'OVESETUP_DB/connection'
    STATEMENT = 'OVESETUP_DB/statement'
    PGPASS_FILE = 'OVESETUP_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_DB/newDatabase'
    NEED_DBMSUPGRADE = 'OVESETUP_DB/needDBMSUpgrade'
    JUST_RESTORED = 'OVESETUP_DB/justRestored'

    @osetupattrs(
        answerfile=True,
    )
    def DUMPER(self):
        return 'OVESETUP_DB/dumper'

    @osetupattrs(
        answerfile=True,
    )
    def FILTER(self):
        return 'OVESETUP_DB/filter'

    @osetupattrs(
        answerfile=True,
    )
    def RESTORE_JOBS(self):
        return 'OVESETUP_DB/restoreJobs'

    @osetupattrs(
        answerfile=True,
    )
    def FIX_DB_VIOLATIONS(self):
        return 'OVESETUP_DB/fixDbViolations'

    @osetupattrs(
        answerfile=True,
    )
    def ENGINE_VACUUM_FULL(self):
        return 'OVESETUP_DB/engineVacuumFull'

    @osetupattrs(
        answerfile=True,
    )
    def FIX_DB_CONFIGURATION(self):
        return 'OVESETUP_DB/fixDbConfiguration'

    @osetupattrs(
        answerfile=False,
    )
    def INVALID_CONFIG_ITEMS(self):
        return 'OVESETUP_DB/invalidConfigItems'


@util.export
@util.codegen
@osetupattrsclass
class CoreEnv(object):

    @osetupattrs(
        answerfile=True,
    )
    def ENGINE_SERVICE_STOP(self):
        return 'OVESETUP_CORE/engineStop'

    @osetupattrs(
        answerfile=True,
        postinstallfile=True,
        summary=True,
        description=_('Engine installation'),
    )
    def ENABLE(self):
        return 'OVESETUP_ENGINE_CORE/enable'


@util.export
@util.codegen
@osetupattrsclass
class SystemEnv(object):

    @osetupattrs(
        answerfile=True,
    )
    def MEMCHECK_ENABLED(self):
        return 'OVESETUP_SYSTEM/memCheckEnabled'

    MEMCHECK_MINIMUM_MB = 'OVESETUP_SYSTEM/memCheckMinimumMB'
    MEMCHECK_RECOMMENDED_MB = 'OVESETUP_SYSTEM/memCheckRecommendedMB'
    MEMCHECK_THRESHOLD = 'OVESETUP_SYSTEM/memCheckThreshold'

    NFS_SERVICE_NAME = 'OVESETUP_SYSTEM/nfsServiceName'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('NFS setup'),
        summary_condition=lambda env: env.get(
            SystemEnv.NFS_CONFIG_ENABLED
        ),
    )
    def NFS_CONFIG_ENABLED(self):
        return 'OVESETUP_SYSTEM/nfsConfigEnabled'

    #
    # In 3.3/3.4.0 the NFS_CONFIG_ENABLED was in postinstall file
    # and now removed.
    # At first upgrade from these versions we should not consider
    # its value from environment.
    # This variable will not be available at these versions, and
    # will set to False in future runs to allow us to
    # consider the value of NFS_CONFIG_ENABLED in later setups.
    #
    @osetupattrs(
        postinstallfile=True,
    )
    def NFS_CONFIG_ENABLED_LEGACY_IN_POSTINSTALL(self):
        return 'OVESETUP_SYSTEM/nfsConfigEnabled_legacyInPostInstall'


@util.export
@util.codegen
@osetupattrsclass
class PKIEnv(object):
    @osetupattrs(
        is_secret=True,
    )
    def STORE_PASS(self):
        return 'OVESETUP_PKI/storePassword'

    @osetupattrs(
        postinstallfile=True,
    )
    def COUNTRY(self):
        return 'OVESETUP_PKI/country'

    @osetupattrs(
        answerfile=True,
        summary=True,
        postinstallfile=True,
        description=_('PKI organization'),
    )
    def ORG(self):
        return 'OVESETUP_PKI/organization'

    ENGINE_SSH_PUBLIC_KEY = 'OVESETUP_PKI/sshPublicKey'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Renew PKI'),
    )
    def RENEW(self):
        return 'OVESETUP_PKI/renew'

    ENTITIES = 'OVESETUP_PKI/entities'


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):

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
        description=_('NFS mount point'),
        postinstallfile=True,
    )
    def ISO_DOMAIN_NFS_MOUNT_POINT(self):
        return 'OVESETUP_CONFIG/isoDomainMountPoint'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('NFS export ACL'),
    )
    def ISO_DOMAIN_NFS_ACL(self):
        return 'OVESETUP_CONFIG/isoDomainACL'

    @osetupattrs(
        answerfile=True,
        postinstallfile=True
    )
    def ISO_DOMAIN_NAME(self):
        return 'OVESETUP_CONFIG/isoDomainName'

    ISO_DOMAIN_DEFAULT_NFS_MOUNT_POINT = \
        'OVESETUP_CONFIG/isoDomainDefaultMountPoint'

    @osetupattrs(
        answerfile=True,
        postinstallfile=True,
        summary=True,
        description=_('Engine Host FQDN'),
        summary_condition=lambda env: not env.get(
            CoreEnv.ENABLE
        ),
    )
    def ENGINE_FQDN(self):
        return 'OVESETUP_ENGINE_CONFIG/fqdn'

    @osetupattrs(
        answerfile=True,
    )
    def ENGINE_HEAP_MIN(self):
        return 'OVESETUP_CONFIG/engineHeapMin'

    @osetupattrs(
        answerfile=True,
    )
    def ENGINE_HEAP_MAX(self):
        return 'OVESETUP_CONFIG/engineHeapMax'

    @osetupattrs(
        answerfile=True,
    )
    def OVIRT_ENGINE_DB_BACKUP_DIR(self):
        return 'OVESETUP_CONFIG/engineDbBackupDir'

    @osetupattrs(
        postinstallfile=True,
    )
    def ADMIN_USER(self):
        return 'OVESETUP_CONFIG/adminUser'

    @osetupattrs(
        postinstallfile=True,
    )
    def ADMIN_USER_AUTHZ_TYPE(self):
        return 'OVESETUP_CONFIG/adminUserAuthzType'

    @osetupattrs(
        postinstallfile=True,
    )
    def ADMIN_USER_AUTHZ_NAME(self):
        return 'OVESETUP_CONFIG/adminUserAuthzName'

    @osetupattrs(
        postinstallfile=True,
    )
    def ADMIN_USER_NAMESPACE(self):
        return 'OVESETUP_CONFIG/adminUserNamespace'

    @osetupattrs(
        postinstallfile=True,
    )
    def ADMIN_USER_ID(self):
        return 'OVESETUP_CONFIG/adminUserId'

    @osetupattrs(
        answerfile=True,
        is_secret=True,
    )
    def ADMIN_PASSWORD(self):
        return 'OVESETUP_CONFIG/adminPassword'

    PKI_RENEWAL_DOC_URL = 'OVESETUP_CONFIG/pkiRenewalDocUrl'

    @osetupattrs(
        answerfile=True,
    )
    def IGNORE_VDS_GROUP_IN_NOTIFIER(self):
        return 'OVESETUP_CONFIG/ignoreVdsgroupInNotifier'


@util.export
@util.codegen
@osetupattrsclass
class RPMDistroEnv(object):
    ENGINE_PACKAGES = 'OVESETUP_RPMDISTRO/enginePackages'
    ENGINE_SETUP_PACKAGES = 'OVESETUP_RPMDISTRO/engineSetupPackages'
    ADDITIONAL_PACKAGES = 'OVESETUP_RPMDISTRO/additionalPackages'

    ENGINE_AAA_JDBC_PACKAGE = 'OVESETUP_RPMDISTRO/aaaJdbcPackage'


@util.export
@util.codegen
@osetupattrsclass
class ApacheEnv(object):

    HTTPD_CONF_OVIRT_ENGINE = 'OVESETUP_APACHE/configFileOvirtEngine'


@util.export
@util.codegen
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


@util.export
@util.codegen
@osetupattrsclass
class RemoveEnv(object):
    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_ENGINE(self):
        return 'OVESETUP_REMOVE/removeEngine'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_ENGINE_DATABASE(self):
        return 'OVESETUP_REMOVE/engineDatabase'


@util.export
@util.codegen
@osetupattrsclass
class OvnEnv(object):

    @osetupattrs(
        answerfile=True,
        description=_('Set up ovirt-provider-ovn'),
        postinstallfile=True,
        reconfigurable=True,
        summary=True,
    )
    def OVIRT_PROVIDER_OVN(self):
        return 'OVESETUP_OVN/ovirtProviderOvn'

    @osetupattrs(
        description=_('ovirt-provider-ovn id'),
        postinstallfile=True,
    )
    def OVIRT_PROVIDER_ID(self):
        return 'OVESETUP_OVN/ovirtProviderOvnId'

    @osetupattrs(
        answerfile=True,
        description=_('oVirt OVN provider user name'),
    )
    def OVIRT_PROVIDER_OVN_USER(self):
        return 'OVESETUP_OVN/ovirtProviderOvnUser'

    @osetupattrs(
        answerfile=True,
        description=_('oVirt OVN provider password'),
        is_secret=True,
    )
    def OVIRT_PROVIDER_OVN_PASSWORD(self):
        return 'OVESETUP_OVN/ovirtProviderOvnPassword'

    OVIRT_PROVIDER_OVN_SERVICE = 'ovirt-provider-ovn'
    OPENVSWITCH_SERVICE = 'openvswitch'
    OVN_NORTHD_SERVICE = 'ovn-northd'

    ENGINE_MACHINE_OVN_SERVICES = (
        OPENVSWITCH_SERVICE,
        OVN_NORTHD_SERVICE,
    )

    PROVIDER_NAME = 'ovirt-provider-ovn'

    FIREWALLD_SERVICES_DIR = 'OVESETUP_OVN/firewalldServicesDir'
    OVN_FIREWALLD_SERVICES = 'OVESETUP_OVN/firewalldServices'

    @osetupattrs(
        is_secret=True,
    )
    def OVIRT_PROVIDER_OVN_SECRET(self):
        return 'OVESETUP_OVN/ovirtProviderOvnSecret'


@util.export
class OvnFileLocations(object):

    DEFAULT_FIREWALLD_SERVICES_DIR = '/usr/lib/firewalld/services'

    OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE = (
        '/etc/ovirt-provider-ovn/conf.d/10-setup-ovirt-provider-ovn.conf'
    )

    OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_EXAMPLE = os.path.join(
        FileLocations.OVIRT_ENGINE_SYSCONFDIR,
        'ovirt-provider-ovn-conf.example'
    )

    OVIRT_PROVIDER_OVN_NDB = 'ovn-ndb'
    OVIRT_PROVIDER_OVN_SDB = 'ovn-sdb'
    OVIRT_PROVIDER_OVN_HTTPS = 'ovirt-provider-ovn'

    OVIRT_PROVIDER_OVN_NDB_KEY = os.path.join(
        FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
        OVIRT_PROVIDER_OVN_NDB + '.key.nopass'
    )
    OVIRT_PROVIDER_OVN_SDB_KEY = os.path.join(
        FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
        OVIRT_PROVIDER_OVN_SDB + '.key.nopass'
    )
    OVIRT_PROVIDER_OVN_HTTPS_KEY = os.path.join(
        FileLocations.OVIRT_ENGINE_PKIKEYSDIR,
        OVIRT_PROVIDER_OVN_HTTPS + '.key.nopass'
    )

    OVIRT_PROVIDER_OVN_NDB_CERT = os.path.join(
        FileLocations.OVIRT_ENGINE_PKICERTSDIR,
        OVIRT_PROVIDER_OVN_NDB + '.cer'
    )
    OVIRT_PROVIDER_OVN_SDB_CERT = os.path.join(
        FileLocations.OVIRT_ENGINE_PKICERTSDIR,
        OVIRT_PROVIDER_OVN_SDB + '.cer'
    )
    OVIRT_PROVIDER_OVN_HTTPS_CERT = os.path.join(
        FileLocations.OVIRT_ENGINE_PKICERTSDIR,
        OVIRT_PROVIDER_OVN_HTTPS + '.cer'
    )


# vim: expandtab tabstop=4 shiftwidth=4
