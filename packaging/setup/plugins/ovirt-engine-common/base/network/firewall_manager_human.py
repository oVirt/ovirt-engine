#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall human manager plugin.
"""

import gettext

from otopi import plugin
from otopi import util

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
