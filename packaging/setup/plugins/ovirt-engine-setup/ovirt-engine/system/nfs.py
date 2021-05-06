#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
NFS and RPCbind services configuration plugin.
"""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    NFS and RPCbind services configuration plugin.
    """

    SYSCONFIG_NFS_PARAMS = {
        'RPCNFSDCOUNT':     '8',
        'LOCKD_TCPPORT':    '32803',
        'LOCKD_UDPPORT':    '32769',
        'RPCMOUNTDOPTS':    '"-p 892"',
        'RPCRQUOTADOPTS':   '"-p 875"',
        'STATDARG':         '"-p 662 -o 2020"',
    }

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.SystemEnv.NFS_CONFIG_ENABLED,
            None
        )
        self.environment.setdefault(
            oenginecons.SystemEnv.NFS_SERVICE_NAME,
            None
        )

        #
        # Assume we have the flag in post install
        # on non new installation.
        #
        self.environment.setdefault(
            oenginecons.SystemEnv.NFS_CONFIG_ENABLED_LEGACY_IN_POSTINSTALL,
            self.environment[
                osetupcons.CoreEnv.ORIGINAL_GENERATED_BY_VERSION
            ] is not None
        )

        self._enabled = True
        self._foundpreextnfs = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_PROGRAMS,
        condition=lambda self: self._enabled,
    )
    def _programs(self):
        self.command.detect('exportfs')

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _late_setup(self):
        if not osetuputil.is_ovirt_packaging_supported_distro():
            self.logger.warning(
                _('Unsupported distribution disabling nfs export')
            )
            self._enabled = False

        if self.environment[
            oenginecons.SystemEnv.NFS_SERVICE_NAME
        ] is None:
            for service in ('nfs-server', 'nfs'):
                if self.services.exists(name=service):
                    self.environment[
                        oenginecons.SystemEnv.NFS_SERVICE_NAME
                    ] = service
                    break
            else:
                self._enabled = False
        if (
            self.environment[
                oenginecons.SystemEnv.NFS_CONFIG_ENABLED
            ] is None or
            self.environment[
                oenginecons.SystemEnv.NFS_CONFIG_ENABLED_LEGACY_IN_POSTINSTALL
            ]
        ):
            if not self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]:
                self.environment[
                    oenginecons.SystemEnv.NFS_CONFIG_ENABLED
                ] = False

        self.environment[
            oenginecons.SystemEnv.NFS_CONFIG_ENABLED_LEGACY_IN_POSTINSTALL
        ] = False

        exportfs = self.command.get('exportfs', optional=True)
        if exportfs:
            # We used to always have it, because we required nfs-utils.
            # This broke SCAP, so we removed the requirement.
            rc, stdout, stderr = self.execute(
                (
                    exportfs,
                ),
                raiseOnError=False,
            )
            if rc == 0:
                for line in stdout:
                    if line[0] == '/':
                        self._foundpreextnfs = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oenginecons.Stages.NFS_CONFIG_ALLOWED
    )
    def _customization_enable(self):
        if not self.environment[oenginecons.CoreEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oenginecons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        after=(
            oenginecons.Stages.NFS_CONFIG_ALLOWED,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        # Do not ask about this anymore, as it's deprecated, see also:
        # https://bugzilla.redhat.com/1332813
        # Still allow setting in the answerfile.
        self._enabled = self.environment[
            oenginecons.SystemEnv.NFS_CONFIG_ENABLED
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
            oenginecons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        ),
        # must be run before firewall_manager plugin
        condition=lambda self: (
            self._enabled or self._foundpreextnfs
        ),
        # must be always enabled to create examples
    )
    def _firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].append(
            {
                'name': 'ovirt-nfs',
                'directory': 'ovirt-engine'
            }
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        changed_lines = []
        content = []
        if os.path.exists(oenginecons.FileLocations.NFS_RHEL_CONFIG):
            with open(oenginecons.FileLocations.NFS_RHEL_CONFIG, 'r') as f:
                content = f.read().splitlines()
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=oenginecons.FileLocations.NFS_RHEL_CONFIG,
                content=osetuputil.editConfigContent(
                    content=content,
                    params=self.SYSCONFIG_NFS_PARAMS,
                    changed_lines=changed_lines,
                    new_line_tpl='{spaces}{param}={value}',
                )
            )
        )
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='nfs_config',
            description='NFS Configuration',
            optional=True
        ).addChanges(
            'nfs_config',
            oenginecons.FileLocations.NFS_RHEL_CONFIG,
            changed_lines,
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(oenginecons.FileLocations.NFS_RHEL_CONFIG)

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        self.logger.info(_('Restarting nfs services'))

        try:
            # Fedora 23 adds nfs-config, which *is* a dependency, but
            # systemd does not know it needs to be restarted after nfs
            # config is changed. It's enough to only stop it, starting
            # nfs will start it as a dependency.
            if self.services.exists(name='nfs-config'):
                self.services.state(
                    name='nfs-config',
                    state=False,
                )

            if not self.services.supportsDependency:
                self.services.startup(
                    name='rpcbind',
                    state=True,
                )
                self.services.state(
                    name='rpcbind',
                    state=True,
                )

            self.services.startup(
                name=self.environment[
                    oenginecons.SystemEnv.NFS_SERVICE_NAME
                ],
                state=True,
            )
            for state in (False, True):
                self.services.state(
                    name=self.environment[
                        oenginecons.SystemEnv.NFS_SERVICE_NAME
                    ],
                    state=state,
                )
        except RuntimeError as ex:
            self.logger.debug('exception', exc_info=True)
            msg = _(
                'Unable to start NFS service or its dependencies: {error}\n'
                'Please check their configuration and manually restart'
            ).format(
                error=ex,
            )
            self.logger.warning(msg)


# vim: expandtab tabstop=4 shiftwidth=4
