#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Apache misc plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ApacheEnv.HTTPD_SERVICE,
            oengcommcons.Defaults.DEFAULT_HTTPD_SERVICE
        )
        self.environment.setdefault(
            oengcommcons.ApacheEnv.NEED_RESTART,
            False,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=oengcommcons.Stages.APACHE_RESTART,
        after=(
            oengcommcons.Stages.CORE_ENGINE_START,
        ),
        condition=lambda self: (
            self._enabled and
            self.environment[
                oengcommcons.ApacheEnv.NEED_RESTART
            ]
        ),
    )
    def _closeup(self):
        self.logger.info(
            _('Restarting {service}').format(
                service=self.environment[
                    oengcommcons.ApacheEnv.HTTPD_SERVICE
                ],
            )
        )
        self.services.startup(
            name=self.environment[
                oengcommcons.ApacheEnv.HTTPD_SERVICE
            ],
            state=True,
        )
        for state in (False, True):
            self.services.state(
                name=self.environment[
                    oengcommcons.ApacheEnv.HTTPD_SERVICE
                ],
                state=state,
            )


# vim: expandtab tabstop=4 shiftwidth=4
