#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Local Postgres plugin."""


import gettext

from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import postgres
from ovirt_engine_setup.provisiondb import constants as oprovisioncons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Local Postgres plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._renamedDBResources = False
        self._provisioning = postgres.Provisioning(
            plugin=self,
            dbenvkeys=oprovisioncons.Const.PROVISION_DB_ENV_KEYS,
            defaults=oprovisioncons.Const.DEFAULT_PROVISION_DB_ENV_KEYS,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._provisioning.detectCommands()
        if not self._provisioning.supported():
            osetuputil.addExitCode(
                environment=self.environment,
                code=(
                    osetupcons.Const.
                    EXIT_CODE_PROVISIONING_NOT_SUPPORTED
                )
            )
            raise RuntimeError('Provisioning not supported')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
    )
    def _customization(self):
        self.environment[oprovisioncons.ProvDBEnv.HOST] = 'localhost'
        self.environment[
            oprovisioncons.ProvDBEnv.PORT
        ] = oprovisioncons.Defaults.DEFAULT_DB_PORT
        self._provisioning.applyEnvironment()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        self._provisioning.validate()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        if self.environment.get(oprovisioncons.ConfigEnv.PROVISION_DB):
            self._provisioning.provision()
        if self.environment.get(oprovisioncons.ConfigEnv.PROVISION_USER):
            self._provisioning.createUser()
        if self._provisioning.databaseRenamed:
            osetuputil.addExitCode(
                environment=self.environment,
                code=(
                    osetupcons.Const.
                    EXIT_CODE_PROVISIONING_EXISTING_RESOURCES_FOUND
                )
            )
            raise RuntimeError(
                _(
                    'Existing resources found, new ones created: \n'
                    'database {database} user {user}'
                ).format(
                    database=self.environment[
                        oprovisioncons.ProvDBEnv.DATABASE
                    ],
                    user=self.environment[
                        oprovisioncons.ProvDBEnv.USER
                    ],
                )
            )
        if self.environment.get(oprovisioncons.ConfigEnv.ADD_TO_PG_HBA):
            with transaction.Transaction() as localtransaction:
                self._provisioning.addPgHbaDatabaseAccess(
                    transaction=localtransaction,
                )
        if self.environment.get(oprovisioncons.ConfigEnv.GRANT_READONLY):
            self._provisioning.grantReadOnlyAccessToUser()
        self._provisioning.restartPG()


# vim: expandtab tabstop=4 shiftwidth=4
