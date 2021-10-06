#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""oVirt provider OVN plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants
from ovirt_engine_setup.engine.constants import OvnEnv


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """oVirt provider OVN plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[constants.RemoveEnv.REMOVE_ALL]
            and self.environment.get(OvnEnv.OVIRT_PROVIDER_OVN)
            and self.services.exists(name=OvnEnv.OVIRT_PROVIDER_OVN_SERVICE)
            and not self.environment[constants.CoreEnv.DEVELOPER_MODE]
        ),
    )
    def _misc(self):
        self.logger.info(
            _('Stopping and disabling ovirt-provider-ovn service')
        )
        for db in ['nb', 'sb']:
            self._execute_command(
                (
                    f'ovn-{db}ctl',
                    'del-ssl',
                ),
                _(f'Failed to un-configure {db} SSL'),
            )
            self._execute_command(
                (
                    f'ovn-{db}ctl',
                    'del-connection',
                ),
                _(f'Failed to un-configure {db} connection'),
            )
        self.services.state(
            name=OvnEnv.OVIRT_PROVIDER_OVN_SERVICE, state=False
        )
        self.services.startup(
            name=OvnEnv.OVIRT_PROVIDER_OVN_SERVICE,
            state=False,
        )

    def _execute_command(
        self,
        command,
        error_message,
    ):
        rc, _, stderr = self.execute(
            command,
            raiseOnError=False,
        )
        if rc != 0:
            self.logger.error(error_message)


# vim: expandtab tabstop=4 shiftwidth=4
