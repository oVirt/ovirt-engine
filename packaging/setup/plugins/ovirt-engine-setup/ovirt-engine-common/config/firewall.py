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
            '@HTTP_PORT@': self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
            ],
            '@HTTPS_PORT@': self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
            ],
        })
        if self.environment[
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT
        ] is not None:
            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
                {
                    'name': 'ovirt-http',
                    'directory': 'ovirt-common'
                },
                {
                    'name': 'ovirt-https',
                    'directory': 'ovirt-common'
                },
            ])


# vim: expandtab tabstop=4 shiftwidth=4
