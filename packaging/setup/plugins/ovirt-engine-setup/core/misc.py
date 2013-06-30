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


"""Misc plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=[
            otopicons.Stages.CORE_LOG_INIT,
        ],
    )
    def _preinit(self):
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_FILE_NAME_PREFIX,
            osetupcons.FileLocations.OVIRT_OVIRT_SETUP_LOG_PREFIX
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        priority=plugin.Stages.PRIORITY_LOW
    )
    def _init(self):
        if (
            self.environment[osetupcons.CoreEnv.UPGRADE_FROM_LEGACY] or
            os.path.exists(
                osetupcons.FileLocations.OVIRT_SETUP_POST_INSTALL_CONFIG
            )
        ):
            self.environment[
                osetupcons.CoreEnv.ACTION
            ] = osetupcons.Const.ACTION_UPGRADE
        else:
            self.environment[
                osetupcons.CoreEnv.ACTION
            ] = osetupcons.Const.ACTION_SETUP

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=osetupcons.Stages.CORE_ENGINE_START,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _closeup(self):
        self.logger.info(_('Starting engine service'))
        self.services.state(
            name=osetupcons.Const.ENGINE_SERVICE_NAME,
            state=True,
        )
        self.services.startup(
            name=osetupcons.Const.ENGINE_SERVICE_NAME,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
