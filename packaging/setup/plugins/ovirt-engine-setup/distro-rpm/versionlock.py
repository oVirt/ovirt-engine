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
Yum versionlock configuration plugin.
"""

import platform
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """
    Yum versionlock configuration plugin.
    """
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._distribution = platform.linux_distribution(
            full_distribution_name=0
        )[0]

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _setup(self):
        if self._distribution in ('redhat', 'fedora', 'centos'):
            self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _configversionlock(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK,
                owner=self.environment[osetupcons.SystemEnv.USER_ROOT],
                mode=0o644,
                enforcePermissions=True,
                content=(
                    '\n'.join(
                        osetupcons.Const.RPM_LOCK_LIST
                    ).format(
                        version=osetupcons.Const.RPM_VERSION,
                        release=osetupcons.Const.RPM_RELEASE,
                    )
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(osetupcons.FileLocations.OVIRT_ENGINE_YUM_VERSIONLOCK)


# vim: expandtab tabstop=4 shiftwidth=4
