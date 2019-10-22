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
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            oengcommcons.ConfigEnv.FENCE_KDUMP_LISTENER_STOP_NEEDED
        ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
        name=oenginecons.Stages.KDUMP_ALLOW,
    )
    def _customization_disable(self):
        if not self.environment[oenginecons.CoreEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            oenginecons.Stages.KDUMP_ALLOW,
        ),
    )
    def _customization_firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-fence-kdump-listener',
                'directory': 'ovirt-engine'
            },
        ])

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            self._enabled
        ),
    )
    def _closeup(self):
        for state in (False, True):
            self.services.state(
                name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=oenginecons.Const.FENCE_KDUMP_LISTENER_SERVICE_NAME,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
