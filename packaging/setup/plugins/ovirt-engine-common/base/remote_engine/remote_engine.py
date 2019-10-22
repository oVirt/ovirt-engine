#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Remote engine plugin."""


from otopi import plugin
from otopi import util

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
