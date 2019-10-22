#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall configuration plugin for Engine.
"""

import gettext

from otopi import plugin
from otopi import util

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
