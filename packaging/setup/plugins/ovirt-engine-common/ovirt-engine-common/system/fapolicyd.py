#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Fapolicyd misc plugin."""


import gettext
from otopi import plugin
from otopi import util
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Fapolicyd misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.FapolicydEnv.FAPOLICYD_SERVICE,
            oengcommcons.Defaults.DEFAULT_FAPOLICYD_SERVICE
        )
        self.environment.setdefault(
            oengcommcons.FapolicydEnv.NEED_RESTART,
            False,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=oengcommcons.Stages.FAPOLICYD_RESTART,
        before=(
                oengcommcons.Stages.CORE_ENGINE_START,
        ),
        condition=lambda self: (
                self._enabled and
                self.environment[
                    oengcommcons.FapolicydEnv.NEED_RESTART
                ]
        ),
    )
    def _closeup(self):
        fapolicyd_service = self.environment[
            oengcommcons.FapolicydEnv.FAPOLICYD_SERVICE
        ]
        service_status = self.services.status(
            fapolicyd_service
        )
        if service_status:
            self.logger.info(
                _('Restarting {service}').format(
                    service=fapolicyd_service,
                )
            )
            self.services.restart(fapolicyd_service)
        else:
            self.logger.info(
                _('No need to restart {service} because it is not running.'
                  ).format(
                    service=fapolicyd_service,
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
