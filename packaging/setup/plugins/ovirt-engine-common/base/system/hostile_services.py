#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
hostile services handler plugin.
"""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    hostile services handler plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.SystemEnv.HOSTILE_SERVICES,
            'ovirt-engine-dwhd,ovirt-engine-notifier'
        )
        self._enabled = False
        self._toStart = []

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _setup(self):
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        name=osetupcons.Stages.SYSTEM_HOSTILE_SERVICES_DETECTION,
        condition=lambda self: self._enabled,
    )
    def _transaction_begin(self):
        for service in self.environment[
            osetupcons.SystemEnv.HOSTILE_SERVICES
        ].split(','):
            service = service.strip()
            if self.services.exists(service):
                if self.services.status(
                    name=service,
                ):
                    self.logger.info(_('Stopping service: %s'), service)
                    self._toStart.append(service)
                    self.services.state(
                        name=service,
                        state=False,
                    )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            self._enabled and
            self.environment[
                osetupcons.CoreEnv.ACTION
            ] != osetupcons.Const.ACTION_REMOVE
        ),
    )
    def _closeup(self):
        """
        Ensure that services we stopped while upgrading are
        restarted and will start at reboot.
        """
        for service in self._toStart:
            if self.services.exists(service):
                self.logger.info(_('Starting service: %s'), service)
                self.services.state(
                    name=service,
                    state=True
                )
                # See https://bugzilla.redhat.com/1083551
                self.services.startup(
                    name=service,
                    state=True
                )


# vim: expandtab tabstop=4 shiftwidth=4
