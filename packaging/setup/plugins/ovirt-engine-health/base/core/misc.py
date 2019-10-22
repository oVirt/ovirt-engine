#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Misc plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
    )
    def _preinit(self):
        # We do not set otopicons.CoreEnv.LOG_DIR for health-check - otopi's
        # default is /tmp, which is ok for us
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_FILE_NAME_PREFIX,
            osetupcons.FileLocations.OVIRT_OVIRT_HEALTH_CHECK_LOG_PREFIX
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[
            osetupcons.CoreEnv.ACTION
        ] = osetupcons.Const.ACTION_HEALTHCHECK
        self.environment[
            oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
        ] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: not os.path.exists(
            osetupcons.FileLocations.OVIRT_SETUP_POST_INSTALL_CONFIG
        ),
    )
    def _exit_if_engine_is_not_set_up(self):
        self.dialog.note(
            text=_(
                'Please use the health check utility only on an engine '
                'machine, after it is set up, with version 3.3 or later'
            )
        )
        raise RuntimeError(
            _('Could not detect engine')
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _no_postinstall(self):
        self.environment[osetupcons.CoreEnv.GENERATE_POSTINSTALL] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _no_standard_answerfile(self):
        self.environment[
            osetupcons.CoreEnv.GENERATE_STANDARD_ANSWERFILE
        ] = False


# vim: expandtab tabstop=4 shiftwidth=4
