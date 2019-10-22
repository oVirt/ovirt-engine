#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""fence_kdump listener plugin."""


import gettext

from otopi import plugin
from otopi import util

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

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ConfigEnv.FENCE_KDUMP_LISTENER_STOP_NEEDED,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and self.environment[
            oengcommcons.ConfigEnv.FENCE_KDUMP_LISTENER_STOP_NEEDED
        ],
    )
    def _transactionBegin(self):
        if self.services.exists(
            name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
        ):
            self.logger.info(_('Stopping ovirt-fence-kdump-listener service'))
            self.services.state(
                name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
                state=False
            )


# vim: expandtab tabstop=4 shiftwidth=4
