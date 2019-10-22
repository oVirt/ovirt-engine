#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Engine plugin."""


import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Engine plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _filter_engine_sensitive_keys(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        sensitive_keys = [
            k.strip()
            for k in config.get('SENSITIVE_KEYS').split(',')
            if k.strip()
        ]
        for k in sensitive_keys:
            self.environment[
                otopicons.CoreEnv.LOG_FILTER
            ].append(config.get(k))

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.CoreEnv.ENGINE_SERVICE_STOP,
            None
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED,
            True
        )
        self._filter_engine_sensitive_keys()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
        ],
    )
    def _validation(self):
        if (
            self.services.exists(
                name=oenginecons.Const.ENGINE_SERVICE_NAME
            ) and self.services.status(
                name=oenginecons.Const.ENGINE_SERVICE_NAME
            )
        ):
            if self.environment[
                oenginecons.CoreEnv.ENGINE_SERVICE_STOP
            ] is None:
                self.environment[
                    oenginecons.CoreEnv.ENGINE_SERVICE_STOP
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_CORE_ENGINE_STOP',
                    note=_(
                        'During execution engine service will be stopped '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    true=_('OK'),
                    false=_('Cancel'),
                    default=True,
                )

            if not self.environment[oenginecons.CoreEnv.ENGINE_SERVICE_STOP]:
                raise RuntimeError(
                    _('Engine service is running, no approval to stop')
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and self.environment[
            oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
        ],
    )
    def _transactionBegin(self):
        if self.services.exists(name=oenginecons.Const.ENGINE_SERVICE_NAME):
            self.logger.info(_('Stopping engine service'))
            self.services.state(
                name=oenginecons.Const.ENGINE_SERVICE_NAME,
                state=False
            )


# vim: expandtab tabstop=4 shiftwidth=4
