#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014-2015 Red Hat, Inc.
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


"""Remote engine plugin."""


from otopi import constants as otopicons
from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import remote_engine


@util.export
class Plugin(plugin.PluginBase):
    """Remote engine plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.REMOTE_ENGINE,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.REMOTE_ENGINE_SETUP_STYLE,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_SSH_PORT,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_CLIENT_KEY,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_KNOWN_HOSTS,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_ROOT_PASSWORD,
            None
        )
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            osetupcons.ConfigEnv.REMOTE_ENGINE_HOST_ROOT_PASSWORD
        )
        self.environment[
            osetupcons.ConfigEnv.REMOTE_ENGINE_SETUP_STYLES
        ] = []

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.CoreEnv.REMOTE_ENGINE
        ] = remote_engine.RemoteEngine(plugin=self)

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
    )
    def _cleanup(self):
        self.environment[
            osetupcons.CoreEnv.REMOTE_ENGINE
        ].cleanup()


# vim: expandtab tabstop=4 shiftwidth=4
