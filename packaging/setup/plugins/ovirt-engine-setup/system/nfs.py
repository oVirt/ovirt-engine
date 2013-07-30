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


"""
NFS and RPCbind services configuration plugin.
"""


import platform
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """
    NFS and RPCbind services configuration plugin.
    """
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._distribution = platform.linux_distribution(
            full_distribution_name=0
        )[0]

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.SystemEnv.NFS_CONFIG_ENABLED,
            None
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.NFS_SERVICE_NAME,
            None
        )
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]
        if not self._distribution in ('redhat', 'fedora', 'centos'):
            self.logger.warning(
                _('Unsupported distribution disabling nfs export')
            )
            self._enabled = False

        if self.environment[
            osetupcons.SystemEnv.NFS_SERVICE_NAME
        ] is None:
            for service in ('nfs-server', 'nfs'):
                if self.services.exists(name=service):
                    self.environment[
                        osetupcons.SystemEnv.NFS_SERVICE_NAME
                    ] = service
                    break
            else:
                self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.CONFIG_APPLICATION_MODE_AVAILABLE,
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        """
        If the application mode is gluster don't ask to configure NFS.
        Else if not already configured, ask if you want to use NFS shares for
        ISO domain. If acknowledged, configure NFS related services.
        """
        if self.environment[
            osetupcons.ConfigEnv.APPLICATION_MODE
        ] == 'gluster':
            self.logger.info(
                _('NFS configuration skipped with application mode Gluster')
            )
            self._enabled = False
        else:
            enabled = self.environment[
                osetupcons.SystemEnv.NFS_CONFIG_ENABLED
            ]
            if enabled is None:
                self._enabled = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='NFS_CONFIG_ENABLED',
                    note=_(
                        'Configure NFS share on this server to be used '
                        'as an ISO Domain? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                )
            else:
                self._enabled = enabled

        # expose to other modules
        self.environment[
            osetupcons.SystemEnv.NFS_CONFIG_ENABLED
        ] = self._enabled

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.NET_FIREWALL_MANAGER_PROCESS_TEMPLATES,
        ),
        after=(
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
            osetupcons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        ),
        # must be run before firewall_manager plugin
        condition=lambda self: self._enabled
        # must be always enabled to create examples
    )
    def _firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].append(
            {
                'name': 'ovirt-nfs',
                'directory': 'base'
            }
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.NFS_RHEL_CONFIG,
                content=osetuputil.processTemplate(
                    osetupcons.FileLocations.OVIRT_NFS_RHEL_CONFIG,
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        self.logger.info(_('Restarting nfs services'))

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
                osetupcons.SystemEnv.NFS_SERVICE_NAME
            ],
            state=True,
        )
        for state in (False, True):
            self.services.state(
                name=self.environment[
                    osetupcons.SystemEnv.NFS_SERVICE_NAME
                ],
                state=state,
            )


# vim: expandtab tabstop=4 shiftwidth=4
