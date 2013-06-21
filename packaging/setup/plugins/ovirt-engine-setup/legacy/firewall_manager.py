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


"""Upgrade firewall configuration from legacy plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Upgrade firewall configuration from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
        before=[
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
        ],
    )
    def _customization(self):
        managers = []
        if self.services.exists('firewalld'):
            managers.append('firewalld')
        if self.services.exists('iptables'):
            managers.append('iptables')
        for manager in managers:
            if (
                self.services.exists(manager) and
                self.services.status(manager)
            ):
                self.environment[
                    osetupcons.ConfigEnv.FIREWALL_MANAGER
                ] = manager


# vim: expandtab tabstop=4 shiftwidth=4
