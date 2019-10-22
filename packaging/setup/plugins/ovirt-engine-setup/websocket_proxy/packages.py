#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Package upgrade plugin.
"""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Package upgrade plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.RPMDistroEnv.PACKAGES,
            owspcons.Const.WEBSOCKET_PROXY_PACKAGE_NAME
        )
        self.environment.setdefault(
            owspcons.RPMDistroEnv.PACKAGES_SETUP,
            owspcons.Const.WEBSOCKET_PROXY_SETUP_PACKAGE_NAME
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            owspcons.Stages.CONFIG_WEBSOCKET_PROXY_CUSTOMIZATION,
        ),
        before=(
            osetupcons.Stages.DISTRO_RPM_PACKAGE_UPDATE_CHECK,
        )
    )
    def _customization(self):
        def tolist(s):
            if not s:
                return []
            return [e.strip() for e in s.split(',')]

        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_SETUP
        ].extend(
            tolist(self.environment[owspcons.RPMDistroEnv.PACKAGES_SETUP])
        )

        if self.environment[owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG]:
            packages = tolist(
                self.environment[
                    owspcons.RPMDistroEnv.PACKAGES
                ]
            )
            self.environment[
                osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
            ].append(
                {
                    'packages': packages,
                },
            )


# vim: expandtab tabstop=4 shiftwidth=4
