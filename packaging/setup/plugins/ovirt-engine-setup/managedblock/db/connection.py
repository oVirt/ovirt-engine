#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Managed block connection plugin."""


import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.managedblock import constants as ombcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Managed block connection plugin."""

    class DBTransaction(transaction.TransactionElement):
        """DB transaction element."""

        def __init__(self, parent):
            self._parent = parent

        def __str__(self):
            return _("Managed Block Database Transaction")

        def prepare(self):
            pass

        def abort(self):
            connection = self._parent.environment[
                ombcons.ManagedBlockDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.rollback()
                self._parent.environment[
                    ombcons.ManagedBlockDBEnv.CONNECTION
                ] = None

        def commit(self):
            connection = self._parent.environment[
                ombcons.ManagedBlockDBEnv.CONNECTION
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
            ombcons.Stages.DB_MB_CONNECTION_CUSTOMIZATION,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
        name=ombcons.Stages.MB_CONNECTION_ALLOW,
    )
    def _customization_enable(self):
        if not self.environment[oenginecons.CoreEnv.ENABLE] or not\
                self.environment[ombcons.CoreEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=ombcons.Stages.DB_MB_CONNECTION_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DB_OWNERS_CONNECTIONS_CUSTOMIZED,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
            ombcons.Stages.MB_CONNECTION_ALLOW,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=ombcons.Const.MANAGEDBLOCK_DB_ENV_KEYS,
        ).getCredentials(
            name='Managed Block',
            defaultdbenvkeys=ombcons.Const.DEFAULT_MANAGEDBLOCK_DB_ENV_KEYS,
            show_create_msg=True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=ombcons.Stages.DB_MB_SCHEMA,
        after=(
            ombcons.Stages.DB_MB_CREDENTIALS_AVAILABLE_LATE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc_managedblock_schema(self):
        # Do nothing for now, needed for scheduling only.
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=ombcons.Stages.DB_MB_CONNECTION_AVAILABLE,
        after=(
            ombcons.Stages.DB_MB_SCHEMA,
        ),
        condition=lambda self: self._enabled,
    )
    def _connection(self):
        self.environment[
            ombcons.ManagedBlockDBEnv.STATEMENT
        ] = database.Statement(
            dbenvkeys=ombcons.Const.MANAGEDBLOCK_DB_ENV_KEYS,
            environment=self.environment,
        )
        # must be here as we do not have database at validation
        self.environment[
            ombcons.ManagedBlockDBEnv.CONNECTION
        ] = self.environment[ombcons.ManagedBlockDBEnv.STATEMENT].connect()


# vim: expandtab tabstop=4 shiftwidth=4
