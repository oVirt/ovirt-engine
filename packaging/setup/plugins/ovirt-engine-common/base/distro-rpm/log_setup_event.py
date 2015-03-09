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


"""
Log setup event plugin
"""

import gettext
import platform

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Log setup event plugin
    """
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._distribution = platform.linux_distribution(
            full_distribution_name=0
        )[0]

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _setup(self):
        if self._distribution in ('redhat', 'fedora', 'centos'):
            self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
        priority=plugin.Stages.PRIORITY_LAST,
    )
    def _log_setup_event_setup(self):
        self.execute(
            (
                osetupcons.FileLocations.OVIRT_ENGINE_LOG_SETUP_EVENT,
                '--notes=Start of %s' % (
                    self.environment[osetupcons.CoreEnv.ACTION],
                ),
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._enabled,
        priority=plugin.Stages.PRIORITY_LAST,
    )
    def _log_setup_event_closeup(self):
        self.execute(
            (
                osetupcons.FileLocations.OVIRT_ENGINE_LOG_SETUP_EVENT,
                '--notes=End of %s' % self.environment[
                    osetupcons.CoreEnv.ACTION
                ],
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
