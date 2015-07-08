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


"""Engine plugin."""


import gettext
import os

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


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
