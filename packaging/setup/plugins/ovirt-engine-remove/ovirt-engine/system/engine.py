#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Engine plugin."""


import gettext
import os

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Engine plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        if not os.path.exists(
            osetupcons.FileLocations.OVIRT_SETUP_POST_INSTALL_CONFIG
        ):
            if os.path.exists(
                osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            ):
                self.dialog.note(
                    text=_(
                        'If you want to cleanup after setup of a previous '
                        'version, you should use the setup package of that '
                        'version.'
                    )
                )
            raise RuntimeError(
                _('Could not detect product setup')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oenginecons.Stages.REMOVE_CUSTOMIZATION_ENGINE,
        after=(
            osetupcons.Stages.REMOVE_CUSTOMIZATION_COMMON,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _customization(self):
        if self.environment[osetupcons.RemoveEnv.REMOVE_ALL]:
            self.environment[oenginecons.RemoveEnv.REMOVE_ENGINE] = True
            self.environment[
                oenginecons.RemoveEnv.REMOVE_ENGINE_DATABASE
            ] = True

        if self.environment[oenginecons.RemoveEnv.REMOVE_ENGINE]:
            self.environment[
                oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
            ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[oenginecons.RemoveEnv.REMOVE_ENGINE] and
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        ),
    )
    def _misc(self):
        self.services.startup(
            name=oenginecons.Const.ENGINE_SERVICE_NAME,
            state=False,
        )


# vim: expandtab tabstop=4 shiftwidth=4
