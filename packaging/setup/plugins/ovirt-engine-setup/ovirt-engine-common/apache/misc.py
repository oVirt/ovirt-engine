#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Apache misc plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ApacheEnv.CONFIGURED,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            oengcommcons.ApacheEnv.ENABLE
        ],
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):
        self.environment[oengcommcons.ApacheEnv.CONFIGURED] = True


# vim: expandtab tabstop=4 shiftwidth=4
