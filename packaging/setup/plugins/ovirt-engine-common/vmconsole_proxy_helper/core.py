#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""vmconsole proxy plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """vmconsole proxy configuration plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_STOP_NEEDED,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ].append(ovmpcons)

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_STOP_NEEDED
        ],
    )
    def _transactionBegin(self):
        if self.services.exists(
            name=ovmpcons.Const.VMCONSOLE_PROXY_SERVICE_NAME,
        ):
            self.logger.info(_('Stopping vmconsole-proxy service'))
            self.services.state(
                name=ovmpcons.Const.VMCONSOLE_PROXY_SERVICE_NAME,
                state=False
            )


# vim: expandtab tabstop=4 shiftwidth=4
