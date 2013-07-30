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


"""
sshd service handler plugin.
"""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """
    sshd service handler plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self._enabled and
            self.environment[osetupcons.AIOEnv.CONFIGURE]
        ),
        after=(
            osetupcons.Stages.AIO_CONFIG_AVAILABLE,
        ),
        before=(
            osetupcons.Stages.AIO_CONFIG_ROOT_PASSWORD,
        ),
    )
    def _customization(self):
        if not self.services.exists(name='sshd'):
            raise RuntimeError('sshd service is required')
        if not self.services.status(name='sshd'):
            self.services.state(
                name='sshd',
                state=True,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            self._enabled and
            self.environment[osetupcons.AIOEnv.CONFIGURE]
        ),
    )
    def _closeup(self):
        self.services.startup(
            name='sshd',
            state=True
        )



# vim: expandtab tabstop=4 shiftwidth=4
