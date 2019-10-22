#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""sysctl plugin."""


import gettext
import os

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """sysctl plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        if (
            oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL in
            self.environment[osetupcons.RemoveEnv.FILES_TO_REMOVE]
        ):
            self.environment[
                osetupcons.RemoveEnv.FILES_TO_REMOVE
            ].remove(
                oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL
            )
            self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        if os.path.exists(
            oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL
        ):
            self.dialog.note(
                text=_(
                    'The file {filename} configuring sysctl kernel.shmmax has '
                    'been left in place in order to allow postgres to run at '
                    'next reboot.\n'
                    'If you want to restore default kernel.shmmax '
                    'value please remove the file and reboot.'
                ).format(
                    filename=oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL,
                ),
            )


# vim: expandtab tabstop=4 shiftwidth=4
