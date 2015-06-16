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


"""websocket proxy plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """websocket proxy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_STOP_NEEDED,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ].append(owspcons)

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_STOP_NEEDED
        ],
    )
    def _transactionBegin(self):
        if self.services.exists(
            name=owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
        ):
            self.logger.info(_('Stopping websocket-proxy service'))
            self.services.state(
                name=owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
                state=False
            )


# vim: expandtab tabstop=4 shiftwidth=4
