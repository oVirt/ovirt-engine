#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Hostname plugin."""


import gettext

from otopi import plugin
from otopi import util

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
        self.environment.setdefault(
            osetupcons.ConfigEnv.FQDN_IS_NEEDED,
            True
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
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.FQDN_IS_NEEDED
        ],
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
