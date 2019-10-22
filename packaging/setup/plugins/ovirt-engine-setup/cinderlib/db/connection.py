#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Cinderlib connection plugin."""


import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup.cinderlib import constants as oclcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Cinderlib connection plugin."""

    class DBTransaction(transaction.TransactionElement):
        """DB transaction element."""

        def __init__(self, parent):
            self._parent = parent

        def __str__(self):
            return _("CinderLib Database Transaction")

        def prepare(self):
            pass

        def abort(self):
            connection = self._parent.environment[
                oclcons.CinderlibDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.rollback()
                self._parent.environment[
                    oclcons.CinderlibDBEnv.CONNECTION
                ] = None

        def commit(self):
            connection = self._parent.environment[
                oclcons.CinderlibDBEnv.CONNECTION
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
            oclcons.Stages.DB_CL_CONNECTION_CUSTOMIZATION,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
        name=oclcons.Stages.CL_CONNECTION_ALLOW,
    )
    def _customization_enable(self):
        if not self.environment[oenginecons.CoreEnv.ENABLE] or not\
                self.environment[oclcons.CoreEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oclcons.Stages.DB_CL_CONNECTION_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DB_OWNERS_CONNECTIONS_CUSTOMIZED,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
            oclcons.Stages.CL_CONNECTION_ALLOW,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=oclcons.Const.CINDERLIB_DB_ENV_KEYS,
        ).getCredentials(
            name='Cinderlib',
            queryprefix='OVESETUP_CINDERLIB_DB_',
            defaultdbenvkeys=oclcons.Const.DEFAULT_CINDERLIB_DB_ENV_KEYS,
            show_create_msg=True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oclcons.Stages.DB_CL_SCHEMA,
        after=(
            oclcons.Stages.DB_CL_CREDENTIALS_AVAILABLE_LATE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc_cinderlib_schema(self):
        # Do nothing for now, needed for scheduling only.
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oclcons.Stages.DB_CL_CONNECTION_AVAILABLE,
        after=(
            oclcons.Stages.DB_CL_SCHEMA,
        ),
        condition=lambda self: self._enabled,
    )
    def _connection(self):
        self.environment[
            oclcons.CinderlibDBEnv.STATEMENT
        ] = database.Statement(
            dbenvkeys=oclcons.Const.CINDERLIB_DB_ENV_KEYS,
            environment=self.environment,
        )
        # must be here as we do not have database at validation
        self.environment[
            oclcons.CinderlibDBEnv.CONNECTION
        ] = self.environment[oclcons.CinderlibDBEnv.STATEMENT].connect()


# vim: expandtab tabstop=4 shiftwidth=4
