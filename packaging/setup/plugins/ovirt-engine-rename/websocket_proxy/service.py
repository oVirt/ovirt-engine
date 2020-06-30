#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""websocket-proxy plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """websocket-proxy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._service_was_up = False
        self._service = owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
        condition=lambda self: self.environment.get(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ) and not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and self.services.status(self._service),
    )
    def _late_setup(self):
        self._service_was_up = True
        self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_STOP_NEEDED
        ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._service_was_up,
    )
    def _closeup_wsp(self):
        self.logger.info("Starting {}".format(self._service))
        self.services.state(
            name=self._service,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
