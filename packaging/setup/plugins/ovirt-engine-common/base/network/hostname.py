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


"""Hostname plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import hostname as osetuphostname


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Protocols plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.FQDN,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION,
            False
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('ip')
        self.command.detect('dig')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.CONFIG_PROTOCOLS_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ),
    )
    def _customization(self):
        osetuphostname.Hostname(
            plugin=self,
        ).getHostname(
            envkey=osetupcons.ConfigEnv.FQDN,
            whichhost=_('this'),
            supply_default=True,
            validate_syntax=True,
            system=True,
            dns=True,
            local_non_loopback=self.environment[
                osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION
            ],
            reverse_dns=self.environment[
                osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION
            ],
        )


# vim: expandtab tabstop=4 shiftwidth=4
