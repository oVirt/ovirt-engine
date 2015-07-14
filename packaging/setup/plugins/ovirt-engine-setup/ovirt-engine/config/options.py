#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


"""options plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """options plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _miscAlways(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'ProductRPMVersion',
                    'value': osetupcons.Const.DISPLAY_VERSION,
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _miscNewDatabase(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'SSLEnabled',
                    'value': 'true',
                },
                {
                    'name': 'EncryptHostCommunication',
                    'value': 'true',
                },
                {
                    'name': 'EncryptHostCommunication',
                    'value': 'true',
                },
                {
                    'name': 'ConfigDir',
                    'value': oenginecons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                },
                {
                    'name': 'DataDir',
                    'value': osetupcons.FileLocations.DATADIR,
                },
                {
                    'name': 'WebSocketProxy',
                    'value': '%s:%s' % (
                        self.environment[osetupcons.ConfigEnv.FQDN],
                        osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT,
                    ),
                },
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
