#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Local Postgres upgrade plugin."""

import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.engine_common import postgres


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Local Postgres upgrade plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._upgrade_approved = False
        self._upgrade_approved_inplace = False
        self._upgrade_approved_cleanupold = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.EngineDBEnv.NEED_DBMSUPGRADE
            ]
        ),
        name=oengcommcons.Stages.DB_CUST_UPGRADEDBMS_ENGINE,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
        ),
        after=(
            oengcommcons.Stages.DB_CONNECTION_CUSTOMIZATION,
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
    )
    def _customization(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        )
        (
            self._upgrade_approved,
            self._upgrade_approved_inplace,
            self._upgrade_approved_cleanupold
        ) = dbovirtutils.DBMSUpgradeCustomizationHelper('engine')

    @plugin.event(
        stage=plugin.Stages.STAGE_EARLY_MISC,
        condition=lambda self: self._upgrade_approved,
        name=oengcommcons.Stages.DB_UPGRADEDBMS_ENGINE,
    )
    def _updateDBMS(self):
        self.logger.info(_('Upgrading PostgreSQL'))
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            postgres.DBMSUpgradeTransaction(
                parent=self,
                inplace=self._upgrade_approved_inplace,
                cleanupold=self._upgrade_approved_cleanupold,
                upgrade_from=self.environment[
                    oengcommcons.ProvisioningEnv.OLD_POSTGRES_SERVICE
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
