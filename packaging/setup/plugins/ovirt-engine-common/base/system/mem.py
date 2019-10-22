#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import mem

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self.environment.setdefault(
            osetupcons.ConfigEnv.TOTAL_MEMORY_MB,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.logger.debug('Checking total memory')
        if self.environment[osetupcons.ConfigEnv.TOTAL_MEMORY_MB] is None:
            self.environment[
                osetupcons.ConfigEnv.TOTAL_MEMORY_MB
            ] = mem.get_total_mb()


# vim: expandtab tabstop=4 shiftwidth=4
