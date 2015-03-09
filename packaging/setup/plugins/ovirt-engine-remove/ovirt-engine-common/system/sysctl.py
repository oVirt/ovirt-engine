#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""sysctl plugin."""


import gettext
import os

from otopi import plugin, util

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
