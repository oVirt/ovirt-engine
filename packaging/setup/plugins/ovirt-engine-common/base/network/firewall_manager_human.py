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
Firewall human manager plugin.
"""

import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import firewall_manager_base

from . import process_firewalld_services


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall human manager plugin.
    """

    class _HumanManager(firewall_manager_base.FirewallManagerBase):

        def __init__(self, plugin):
            super(Plugin._HumanManager, self).__init__(plugin)
            self._output = []

        @property
        def name(self):
            return osetupcons.Const.FIREWALL_MANAGER_HUMAN

        def detect(self):
            return True

        def selectable(self):
            return False

        def print_manual_configuration_instructions(self):
            self.plugin.dialog.note(
                text=_(
                    'The following network ports should be opened:\n'
                    '{ports}'
                ).format(
                    ports='\n'.join(
                        sorted(
                            process_firewalld_services.Process.getInstance(
                                environment=self.environment,
                            ).parseFirewalld(
                                format='    {protocol}:{port}\n',
                            ).splitlines()
                        )
                    ) + '\n'
                ),
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        before=(
            osetupcons.Stages.KEEP_ONLY_VALID_FIREWALL_MANAGERS,
        ),
    )
    def _setup(self):
        self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGERS
        ].append(Plugin._HumanManager(self))


# vim: expandtab tabstop=4 shiftwidth=4
