#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""options plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """options plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _miscAlways(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'ProductRPMVersion',
                    'value': osetupcons.Const.DISPLAY_VERSION,
                },
            ),
        )
        # Restoring DbJustRestored because the DB has been "imported"
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'DbJustRestored',
                    'value': '0',
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _miscNewDatabase(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'SSLEnabled',
                    'value': 'true',
                },
                {
                    'name': 'EncryptHostCommunication',
                    'value': 'true',
                },
                {
                    'name': 'EncryptHostCommunication',
                    'value': 'true',
                },
                {
                    'name': 'ConfigDir',
                    'value': oenginecons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                },
                {
                    'name': 'DataDir',
                    'value': osetupcons.FileLocations.DATADIR,
                },
                {
                    'name': 'WebSocketProxy',
                    'value': '%s:%s' % (
                        self.environment[osetupcons.ConfigEnv.FQDN],
                        osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT,
                    ),
                },
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
