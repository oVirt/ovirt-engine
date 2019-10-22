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

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            oengcommcons.ConfigEnv.FENCE_KDUMP_LISTENER_STOP_NEEDED
        ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            (
                self.environment[
                    osetupcons.RemoveEnv.REMOVE_ALL
                ] or
                self.environment[
                    oenginecons.RemoveEnv.REMOVE_ENGINE
                ]
            ) and
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ]
        ),
    )
    def _misc(self):
        self.services.startup(
            name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
            state=False,
        )


# vim: expandtab tabstop=4 shiftwidth=4
