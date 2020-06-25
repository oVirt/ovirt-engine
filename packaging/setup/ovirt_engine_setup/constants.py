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
import sys

from otopi import util

from . import config


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


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
    reconfigurable=False,
    answerfile_condition=lambda env: True,
    summary_condition=lambda env: True,
    is_secret=False,
):
    class decorator(classproperty):
        def __init__(self, o):
            super(decorator, self).__init__(o)
            self.__osetup_attrs__ = dict(
                answerfile=answerfile,
                summary=summary,
                description=description,
                postinstallfile=postinstallfile,
                reconfigurable=reconfigurable,
                answerfile_condition=answerfile_condition,
                summary_condition=summary_condition,
                is_secret=is_secret,
            )
    return decorator


@util.export
class FileLocations(object):
    SYSCONFDIR = '/etc'
    LOCALSTATEDIR = '/var'
    DATADIR = '/usr/share'
    OVIRT_SETUP_DATADIR = config.SETUP_DATADIR
    OVIRT_SETUP_LOCALSTATEDIR = config.SETUP_LOCALSTATEDIR

    OVIRT_SETUP_BINDIR = os.path.join(
        OVIRT_SETUP_DATADIR,
        'bin',
    )

    OVIRT_SETUP_LOGDIR = os.path.join(
        config.SETUP_LOG,
        'setup',
    )
    OVIRT_ENGINE_SYSCONFDIR = config.ENGINE_SYSCONFDIR
    OVIRT_SETUP_DATADIR = config.SETUP_DATADIR
    OVIRT_OVIRT_SETUP_LOG_PREFIX = 'ovirt-engine-setup'
    OVIRT_OVIRT_REMOVE_LOG_PREFIX = 'ovirt-engine-remove'
    OVIRT_OVIRT_RENAME_LOG_PREFIX = 'ovirt-engine-rename'
    OVIRT_OVIRT_PROVISIONDB_LOG_PREFIX = 'ovirt-engine-provisiondb'
    OVIRT_OVIRT_HEALTH_CHECK_LOG_PREFIX = 'ovirt-engine-health-check'

    OVIRT_OVIRT_SETUP_CONFIG_FILE = config.ENGINE_SETUP_CONFIG

    OVIRT_IPTABLES_EXAMPLE = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'iptables.example'
    )

    OVIRT_IPTABLES_DEFAULT = os.path.join(
        OVIRT_SETUP_DATADIR,
        'conf',
        'iptables.default.in'
    )

    SYSCONFIG_IPTABLES = os.path.join(
        SYSCONFDIR,
        'sysconfig',
        'iptables',
    )

    OVIRT_FIREWALLD_CONFIG = os.path.join(
        OVIRT_SETUP_DATADIR,
        'firewalld',
    )

    OVIRT_FIREWALLD_EXAMPLE_DIR = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'firewalld'
    )

    FIREWALLD_SERVICES_DIR = os.path.join(
        SYSCONFDIR,
        'firewalld',
        'services',
    )

    OVIRT_SETUP_STATE_DIR = os.path.join(
        OVIRT_SETUP_LOCALSTATEDIR,
        'setup',
    )
    OVIRT_SETUP_ANSWERS_DIR = os.path.join(
        OVIRT_SETUP_STATE_DIR,
        'answers',
    )

    OVIRT_ENGINE_DNF_VERSIONLOCK = os.path.join(
        SYSCONFDIR,
        'dnf',
        'plugins',
        'versionlock.list',
    )

    OVIRT_ENGINE_YUM_VERSIONLOCK = os.path.join(
        SYSCONFDIR,
        'yum',
        'pluginconf.d',
        'versionlock.list',
    )

    OVIRT_ENGINE_YUM_VERSIONLOCK_CONF = os.path.join(
        SYSCONFDIR,
        'yum',
        'pluginconf.d',
        'versionlock.conf',
    )

    OVIRT_SETUP_POST_INSTALL_CONFIG = os.path.join(
        '%s.d' % OVIRT_OVIRT_SETUP_CONFIG_FILE,
        '20-setup-ovirt-post.conf'
    )

    VIRTIO_WIN_DIR = os.path.join(
        DATADIR,
        'virtio-win',
    )

    RHEV_GUEST_TOOLS_DIR = os.path.join(
        DATADIR,
        'rhev-guest-tools-iso',
    )

    RHV_GUEST_TOOLS_DIR = os.path.join(
        DATADIR,
        'rhv-guest-tools-iso',
    )

    OVIRT_GUEST_TOOLS_DIR = os.path.join(
        DATADIR,
        'ovirt-guest-tools-iso',
    )

    OVIRT_ENGINE_UNINSTALL_DIR = os.path.join(
        OVIRT_ENGINE_SYSCONFDIR,
        'uninstall.d'
    )


@util.export
class Defaults(object):
    DEFAULT_SYSTEM_USER_ENGINE = 'ovirt'
    DEFAULT_SYSTEM_GROUP_ENGINE = 'ovirt'
    DEFAULT_WEBSOCKET_PROXY_PORT = 6100


@util.export
class Stages(object):

    SECRETS_FILTERED_FROM_SETUP_ATTRS_MODULES = \
        'osetup.secrets.filtered.from.setup.attrs.modules'
    NET_FIREWALL_MANAGER_AVAILABLE = 'osetup.net.firewallmanager.available'
    CONFIG_PROTOCOLS_CUSTOMIZATION = 'osetup.config.protocols.customization'
    CONFIG_APPLICATION_MODE_AVAILABLE = \
        'osetup.config.applicationMode.available'

    SSH_KEY_AVAILABLE = 'osetup.pki.ssh.available'

    SYSTEM_SYSCTL_CONFIG_AVAILABLE = 'osetup.system.sysctl.available'
    SYSTEM_HOSTILE_SERVICES_DETECTION = 'osetup.system.hostile.detection'
    DISTRO_RPM_PACKAGE_UPDATE_CHECK = 'osetup.distro-rpm.package.update.check'

    DIALOG_TITLES_S_FIREWALL = 'osetup.dialog.titles.firewall.start'
    DIALOG_TITLES_S_MISC = 'osetup.dialog.titles.misc.start'
    DIALOG_TITLES_S_NETWORK = 'osetup.dialog.titles.network.start'
    DIALOG_TITLES_S_PACKAGES = 'osetup.dialog.titles.packaging.start'
    DIALOG_TITLES_S_PRODUCT_OPTIONS = \
        'osetup.dialog.titles.productoptions.start'
    DIALOG_TITLES_S_SYSTEM = 'osetup.dialog.titles.system.start'

    DIALOG_TITLES_E_FIREWALL = 'osetup.dialog.titles.firewall.end'
    DIALOG_TITLES_E_MISC = 'osetup.dialog.titles.misc.end'
    DIALOG_TITLES_E_NETWORK = 'osetup.dialog.titles.network.end'
    DIALOG_TITLES_E_PACKAGES = 'osetup.dialog.titles.packages.end'
    DIALOG_TITLES_E_PRODUCT_OPTIONS = 'osetup.dialog.titles.productoptions.end'
    DIALOG_TITLES_E_SYSTEM = 'osetup.dialog.titles.system.end'

    DIALOG_TITLES_S_SUMMARY = 'osetup.dialog.titles.summary.start'
    DIALOG_TITLES_E_SUMMARY = 'osetup.dialog.titles.summary.end'

    REMOVE_CUSTOMIZATION_COMMON = 'osetup.remove.customization.common'
    REMOVE_CUSTOMIZATION_GROUPS = 'osetup.remove.customization.groups'
    REMOVE_FIREWALLD_SERVICES = 'osetup.remove.firewalld.services'

    KEEP_ONLY_VALID_FIREWALL_MANAGERS = \
        'osetup.keep.only.valid.firewall.managers'

    SETUP_SELINUX = 'osetup.setup.selinux'


@util.export
@util.codegen
class Const(object):
    PACKAGE_NAME = config.PACKAGE_NAME
    PACKAGE_VERSION = config.PACKAGE_VERSION
    DISPLAY_VERSION = config.DISPLAY_VERSION
    RPM_VERSION = config.RPM_VERSION
    RPM_RELEASE = config.RPM_RELEASE

    @classproperty
    def RPM_LOCK_LIST_SUFFIXES(self):
        return (
            '',
            '-backend',
            '-dbscripts',
            '-restapi',
            '-tools',
            '-tools-backup',
            '-webadmin-portal',
            '-ui-extensions',
        )

    FILE_GROUP_SECTION_PREFIX = 'file_group_'

    ACTION_SETUP = 'setup'
    ACTION_REMOVE = 'cleanup'
    ACTION_RENAME = 'rename'
    ACTION_PROVISIONDB = 'provisiondb'
    ACTION_HEALTHCHECK = 'health-check'
    FIREWALL_MANAGER_HUMAN = 'skip'
    FIREWALL_MANAGER_IPTABLES = 'iptables'
    FIREWALL_MANAGER_FIREWALLD = 'firewalld'
    ISO_DOMAIN_NFS_DEFAULT_ACL_FORMAT = '{fqdn}(rw)'

    REMOTE_ENGINE_SETUP_STYLE_AUTO_SSH = 'auto_ssh'
    REMOTE_ENGINE_SETUP_STYLE_MANUAL_FILES = 'manual_files'
    REMOTE_ENGINE_SETUP_STYLE_MANUAL_INLINE = 'manual_inline'
    EXIT_CODE_REMOVE_WITHOUT_SETUP = 11
    EXIT_CODE_PROVISIONING_NOT_SUPPORTED = 12
    EXIT_CODE_PROVISIONING_EXISTING_RESOURCES_FOUND = 13


@util.export
@util.codegen
@osetupattrsclass
class CoreEnv(object):
    OFFLINE_PACKAGER = 'OVESETUP_CORE/offlinePackager'
    ANSWER_FILE = 'OVESETUP_CORE/answerFile'
    DEVELOPER_MODE = 'OVESETUP_CORE/developerMode'
    UNINSTALL_UNREMOVABLE_FILES = 'OVESETUP_CORE/uninstallUnremovableFiles'

    # Information about files that were written by previous engine-setup runs
    # dict of filename: {
    #   hash type ('md5' or 'sha256'): hash value,
    #   'unremovable': True or False,
    #   'changed': True or False,
    #   'groups': set of: [group_id] . There is also a group 'unremovable'.
    #   if 'changed' is missing, it's because the file is missing. This can
    #   be either because we removed it at some point, or the user did.
    #   Perhaps we should somehow record removals in uninstall files, but
    #   currently we do not, so can't know what happened.
    # }
    UNINSTALL_FILES_INFO = 'OVESETUP_CORE/uninstallFilesInfo'

    # Information about files that were edited by previous engine-setup runs
    # dict of: filename: [ list of: { dict of: 'added' or 'removed': line } ]
    UNINSTALL_LINES_INFO = 'OVESETUP_CORE/uninstallLinesInfo'

    # List of the config files from which above information was parsed
    UNINSTALL_UNINSTALL_FILES = 'OVESETUP_CORE/uninstallUninstallFiles'

    # Descriptions of uninstall groups
    # dict of group_id: (localized) description
    UNINSTALL_GROUPS_DESCRIPTIONS = 'OVESETUP_CORE/uninstallGroupsDescriptions'

    GENERATE_POSTINSTALL = 'OVESETUP_CORE/generatePostInstall'
    GENERATE_STANDARD_ANSWERFILE = 'OVESETUP_CORE/generateStandardAnswerfile'
    FILE_GROUP_PREFIX = 'OVESETUP_CORE_MODIFIED_FILE_GROUP/'
    LINES_GROUP_PREFIX = 'OVESETUP_CORE_MODIFIED_LINES_GROUP/'
    REGISTER_UNINSTALL_GROUPS = 'OVESETUP_CORE/registerUninstallGroups'
    UPGRADE_SUPPORTED_VERSIONS = 'OVESETUP_CORE/upgradeSupportedVersions'
    ACTION = 'OVESETUP_CORE/action'
    FAIL_ON_DUPLICATED_CONSTANT = 'OVESETUP_CORE/failOnDulicatedConstant'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE(self):
        return 'OVESETUP_CORE/remove'

    @osetupattrs(
        postinstallfile=True,
    )
    def GENERATED_BY_VERSION(self):
        return 'OVESETUP_CORE/generatedByVersion'

    ORIGINAL_GENERATED_BY_VERSION = 'OVESETUP_CORE/originalGeneratedByVersion'

    SETUP_ATTRS_MODULES = 'OVESETUP_CORE/setupAttributesModules'

    REMOTE_ENGINE = 'OVESETUP_CORE/remoteEngine'

    RECONFIGURE_OPTIONAL_COMPONENTS = \
        'OVESETUP_CORE/reconfigureOptionalComponents'


@util.export
@util.codegen
@osetupattrsclass
class DocsEnv(object):
    DOCS_LOCAL = 'OVESETUP_DOCS/docsAreLocal'
    DWH_DOC_URL = 'OVESETUP_DOCS/dwhDocUrl'
    REPORTS_DOC_URL = 'OVESETUP_DOCS/reportsDocUrl'


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
class SystemEnv(object):
    USER_ENGINE = 'OVESETUP_SYSTEM/userEngine'
    GROUP_ENGINE = 'OVESETUP_SYSTEM/groupEngine'

    SELINUX_CONTEXTS = 'OVESETUP_SYSTEM/selinuxContexts'
    SELINUX_RESTORE_PATHS = 'OVESETUP_SYSTEM/selinuxRestorePaths'
    SELINUX_BOOLEANS = 'OVESETUP_SYSTEM/selinuxBooleans'
    SELINUX_PORTS = 'OVESETUP_SYSTEM/selinuxPorts'

    HOSTILE_SERVICES = 'OVESETUP_SYSTEM/hostileServices'
    RESERVED_PORTS = 'OVESETUP_SYSTEM/reservedPorts'


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):

    ADD_OVIRT_GLANCE_REPOSITORY = 'OVESETUP_CONFIG/addOvirtGlanceRepository'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Application mode'),
    )
    def APPLICATION_MODE(self):
        return 'OVESETUP_CONFIG/applicationMode'

    @osetupattrs(
        answerfile=True,
        summary=False,
    )
    def STORAGE_TYPE(self):
        return 'OVESETUP_CONFIG/storageType'

    @osetupattrs(
        answerfile=True,
        summary=False,
    )
    def STORAGE_IS_LOCAL(self):
        return 'OVESETUP_CONFIG/storageIsLocal'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Default SAN wipe after delete'),
        postinstallfile=True,
    )
    def SAN_WIPE_AFTER_DELETE(self):
        return 'OVESETUP_CONFIG/sanWipeAfterDelete'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Host FQDN'),
        postinstallfile=True,
    )
    def FQDN(self):
        return 'OVESETUP_CONFIG/fqdn'

    FQDN_IS_NEEDED = 'OVESETUP_CONFIG/fqdnIsNeeded'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Firewall manager'),
        postinstallfile=True,
    )
    def FIREWALL_MANAGER(self):
        return 'OVESETUP_CONFIG/firewallManager'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Update Firewall'),
    )
    def UPDATE_FIREWALL(self):
        return 'OVESETUP_CONFIG/updateFirewall'

    FIREWALL_MANAGERS = 'OVESETUP_CONFIG/firewallManagers'

    @osetupattrs(
        answerfile=True,
        summary=False,
    )
    def FIREWALL_CHANGES_REVIEW(self):
        return 'OVESETUP_CONFIG/firewallChangesReview'

    VALID_FIREWALL_MANAGERS = 'OVESETUP_CONFIG/validFirewallManagers'
    FQDN_REVERSE_VALIDATION = 'OVESETUP_CONFIG/fqdnReverseValidation'
    FQDN_NON_LOOPBACK_VALIDATION = 'OVESETUP_CONFIG/fqdnNonLoopback'

    REMOTE_ENGINE_SETUP_STYLES = 'OVESETUP_CONFIG/remoteEngineSetupStyles'

    @osetupattrs(
        answerfile=True,
    )
    def REMOTE_ENGINE_SETUP_STYLE(self):
        return 'OVESETUP_CONFIG/remoteEngineSetupStyle'

    @osetupattrs(
        answerfile=True,
    )
    def REMOTE_ENGINE_HOST_SSH_PORT(self):
        return 'OVESETUP_CONFIG/remoteEngineHostSshPort'

    # Optional, used if supplied
    REMOTE_ENGINE_HOST_CLIENT_KEY = 'OVESETUP_CONFIG/remoteEngineHostClientKey'

    # Optional, used if supplied, currently only log if not there
    REMOTE_ENGINE_HOST_KNOWN_HOSTS = \
        'OVESETUP_CONFIG/remoteEngineHostKnownHosts'

    @osetupattrs(
        answerfile=True,
        is_secret=True,
    )
    def REMOTE_ENGINE_HOST_ROOT_PASSWORD(self):
        return 'OVESETUP_CONFIG/remoteEngineHostRootPassword'

    ISO_PATHS_TO_UPLOAD = 'OVESETUP_CONFIG/isoPathsToUpload'
    TOTAL_MEMORY_MB = 'OVESETUP_CONFIG/totalMemoryMB'
    CONTINUE_SETUP_ON_HE_VM = 'OVESETUP_CONFIG/continueSetupOnHEVM'


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

    VERSION_LOCK_FILTER = 'OSETUP_RPMDISTRO/versionLockFilter'
    VERSION_LOCK_APPLY = 'OSETUP_RPMDISTRO/versionLockApply'

    PACKAGES_UPGRADE_LIST = 'OVESETUP_RPMDISTRO/packagesUpgradeList'
    PACKAGES_SETUP = 'OVESETUP_RPMDISTRO/packagesSetup'


@util.export
@util.codegen
@osetupattrsclass
class RenameEnv(object):
    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('New FQDN'),
    )
    def FQDN(self):
        return 'OSETUP_RENAME/fqdn'

    FORCE_OVERWRITE = 'OSETUP_RENAME/forceOverwrite'
    FORCE_IGNORE_AIA_IN_CA = 'OSETUP_RENAME/forceIgnoreAIAInCA'
    FILES_TO_BE_MODIFIED = 'OVESETUP_CORE/filesToBeModified'
    PKI_ENTITIES = 'OSETUP_RENAME/pkiEntities'


@util.export
@util.codegen
@osetupattrsclass
class RemoveEnv(object):
    @osetupattrs(
        answerfile=True,
    )
    def ASK_GROUPS(self):
        return 'OVESETUP_REMOVE/confirmUninstallGroups'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_GROUPS(self):
        return 'OVESETUP_REMOVE/enabledFileGroups'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_ALL(self):
        return 'OVESETUP_REMOVE/removeAll'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_CHANGED(self):
        return 'OVESETUP_REMOVE/removeChanged'

    @osetupattrs(
        answerfile=True,
    )
    def REMOVE_OPTIONS(self):
        return 'OVESETUP_REMOVE/removeOptions'

    FILES_TO_REMOVE = 'OVESETUP_REMOVE/filesToRemove'

    REMOVE_SPEC_OPTION_GROUP_LIST = 'OVESETUP_REMOVE/specOptionGroupList'


# vim: expandtab tabstop=4 shiftwidth=4
