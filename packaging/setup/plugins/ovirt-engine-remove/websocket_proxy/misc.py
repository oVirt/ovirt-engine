#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Websocket-proxy plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Websocket-proxy plugin."""

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.RemoveEnv.REMOVE_WSP,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            osetupcons.Stages.REMOVE_CUSTOMIZATION_COMMON,
        ),
    )
    def _customization(self):
        if self.environment[osetupcons.RemoveEnv.REMOVE_ALL]:
            self.environment[owspcons.RemoveEnv.REMOVE_WSP] = True

        if self.environment[owspcons.RemoveEnv.REMOVE_WSP] is None:
            self.environment[
                owspcons.RemoveEnv.REMOVE_WSP
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_REMOVE_WEBSOCKET_PROXY',
                note=_(
                    'Do you want to remove the WebSocket proxy? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            )

        if self.environment[owspcons.RemoveEnv.REMOVE_WSP]:
            self.environment[osetupcons.RemoveEnv.REMOVE_OPTIONS].append(
                owspcons.Const.WEBSOCKET_PROXY_PACKAGE_NAME
            )
            self.environment[
                owspcons.ConfigEnv.WEBSOCKET_PROXY_STOP_NEEDED
            ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            (
                self.environment[osetupcons.RemoveEnv.REMOVE_ALL] or
                self.environment[owspcons.RemoveEnv.REMOVE_WSP]
            ) and not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        ),
    )
    def _misc(self):
        if self.services.exists(
            name=owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME
        ):
            self.services.startup(
                name=owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
                state=False,
            )


# vim: expandtab tabstop=4 shiftwidth=4
