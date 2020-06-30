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

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self.environment.get(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ),
    )
    def _setup(self):
        self.environment[
            osetupcons.RenameEnv.PKI_ENTITIES
        ].append(
            {
                'name': 'websocket-proxy',
                'display_name': 'WebSocket Proxy',
                'ca_cert': None,
                'extract_key': True,
                'extra_action': None,
            }
        )


# vim: expandtab tabstop=4 shiftwidth=4
