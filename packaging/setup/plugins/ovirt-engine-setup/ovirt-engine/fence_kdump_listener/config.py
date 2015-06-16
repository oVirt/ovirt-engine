#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014-2015 Red Hat, Inc.
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


"""fence_kdump listener plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """fence_kdump listener plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            oengcommcons.ConfigEnv.FENCE_KDUMP_LISTENER_STOP_NEEDED
        ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            oengcommcons.Stages.DB_CONNECTION_STATUS,
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
        name=oenginecons.Stages.KDUMP_ALLOW,
    )
    def _customization_disable(self):
        if not self.environment[oenginecons.CoreEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            oengcommcons.Stages.DB_CONNECTION_STATUS,
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            oenginecons.Stages.KDUMP_ALLOW,
        ),
    )
    def _customization_firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-fence-kdump-listener',
                'directory': 'ovirt-engine'
            },
        ])

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            self._enabled
        ),
    )
    def _closeup(self):
        for state in (False, True):
            self.services.state(
                name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
