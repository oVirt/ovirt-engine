#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""engine_db plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """options plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._update_db = False
        self._new_value = None

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._old_fqdn = self.environment[osetupcons.ConfigEnv.FQDN]

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
    )
    def _validate_wsp_rename(self):
        self._new_value = '%s:%s' % (
            self.environment[osetupcons.RenameEnv.FQDN],
            osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT,
        )
        expected_value = '%s:%s' % (
            self._old_fqdn,
            osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT,
        )
        current_value = vdcoption.VdcOption(
            statement=database.Statement(
                dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                environment=self.environment,
            ),
        ).getVdcOption(
            name='WebSocketProxy',
            ownConnection=True,
        )
        if expected_value == current_value:
            self._update_db = True
        else:
            self.logger.warning(_('Not updating option WebSocketProxy'))
            self.dialog.note(
                _(
                    'WebSocketProxy option does not have the expected value:\n'
                    '    {expected_value}\n'
                    'but instead:\n'
                    '    {current_value}\n'
                    'To update it, please run:\n'
                    '    # engine-config -s WebSocketProxy={new_value}\n'
                ).format(
                    expected_value=expected_value,
                    current_value=current_value,
                    new_value=self._new_value,
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._update_db,
    )
    def _miscNewDatabase(self):
        self.logger.info(_('Update WebSocketProxy option'))
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'WebSocketProxy',
                    'value': self._new_value,
                },
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
