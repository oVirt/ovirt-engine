#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""websocket proxy plugin."""


import gettext

from otopi import plugin
from otopi import util

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
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            osetupcons.Stages.SECRETS_FILTERED_FROM_SETUP_ATTRS_MODULES,
        ),
    )
    def _boot(self):
        self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ].append(owspcons)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_STOP_NEEDED,
            False
        )

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
