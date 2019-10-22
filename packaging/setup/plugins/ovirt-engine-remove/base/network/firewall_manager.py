#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall manager selection plugin.
"""

import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall manager selection plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        before=(
            otopicons.Stages.FIREWALLD_VALIDATION,
        ),
    )
    def _validation(self):
        for m in self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGERS]:
            m.remove()


# vim: expandtab tabstop=4 shiftwidth=4
