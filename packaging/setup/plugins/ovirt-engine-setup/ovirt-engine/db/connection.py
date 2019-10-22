#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Connection plugin."""


import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    class DBTransaction(transaction.TransactionElement):
        """DB transaction element."""

        def __init__(self, parent):
            self._parent = parent

        def __str__(self):
            return _("Database Transaction")

        def prepare(self):
            pass

        def abort(self):
            connection = self._parent.environment[
                oenginecons.EngineDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.rollback()
                self._parent.environment[
                    oenginecons.EngineDBEnv.CONNECTION
                ] = None

        def commit(self):
            connection = self._parent.environment[
                oenginecons.EngineDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.commit()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.DBTransaction(self)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DB_CONNECTION_CUSTOMIZATION,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
        name=oenginecons.Stages.CONNECTION_ALLOW,
    )
    def _customization_enable(self):
        if not self.environment[oenginecons.CoreEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DB_CONNECTION_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DB_OWNERS_CONNECTIONS_CUSTOMIZED,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
            oenginecons.Stages.CONNECTION_ALLOW,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        ).getCredentials(
            name='Engine',
            queryprefix='OVESETUP_ENGINE_DB_',
            defaultdbenvkeys=oenginecons.Const.DEFAULT_ENGINE_DB_ENV_KEYS,
            show_create_msg=True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        after=(
            oengcommcons.Stages.DB_SCHEMA,
        ),
        condition=lambda self: self._enabled,
    )
    def _connection(self):
        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ] = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        # must be here as we do not have database at validation
        self.environment[
            oenginecons.EngineDBEnv.CONNECTION
        ] = self.environment[oenginecons.EngineDBEnv.STATEMENT].connect()


# vim: expandtab tabstop=4 shiftwidth=4
