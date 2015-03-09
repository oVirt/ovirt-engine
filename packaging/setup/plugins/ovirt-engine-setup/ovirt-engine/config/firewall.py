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
Firewall configuration plugin for Engine.
"""

import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall configuration plugin for Engine
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
        ),
    )
    def _configuration(self):
        self.environment[
            osetupcons.NetEnv.FIREWALLD_SUBST
        ].update({
            '@JBOSS_HTTP_PORT@': self.environment[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
            ],
            '@JBOSS_HTTPS_PORT@': self.environment[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
            ],
        })
        if self.environment[
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
        ] is not None:
            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
                {
                    'name': 'ovirt-jboss-http',
                    'directory': 'ovirt-engine'
                },
                {
                    'name': 'ovirt-jboss-https',
                    'directory': 'ovirt-engine'
                },
            ])


# vim: expandtab tabstop=4 shiftwidth=4
