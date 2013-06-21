#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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


"""Upgrade Apache configuration from legacy plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Upgrade Apache configuration from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _setup(self):
        self._enabled = (
            self.environment[osetupcons.CoreEnv.UPGRADE_FROM_LEGACY] and
            os.path.exists(
                osetupcons.FileLocations.HTTPD_CONF_OVIRT_ENGINE_LEGACY
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self.environment[osetupcons.ApacheEnv.NEED_RESTART] = True
        #creates a backup and allows rollback
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.
                    HTTPD_CONF_OVIRT_ENGINE_LEGACY
                ),
                content='',
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._enabled,
        before=[
            osetupcons.Stages.APACHE_RESTART,
        ],
    )
    def _closeup(self):
        self.environment[osetupcons.ApacheEnv.NEED_RESTART] = True
        os.remove(osetupcons.FileLocations.HTTPD_CONF_OVIRT_ENGINE_LEGACY)


# vim: expandtab tabstop=4 shiftwidth=4
