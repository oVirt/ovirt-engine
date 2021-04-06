#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Apache selinux plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache selinux plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment.setdefault(
            osetupcons.SystemEnv.SELINUX_BOOLEANS,
            []
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            osetupcons.Stages.SETUP_SELINUX,
        ),
    )
    def _misc(self):
        self.environment[
            osetupcons.SystemEnv.SELINUX_BOOLEANS
        ].append({
            'boolean': 'httpd_can_network_connect',
            'state': 'on'
        })


# vim: expandtab tabstop=4 shiftwidth=4
