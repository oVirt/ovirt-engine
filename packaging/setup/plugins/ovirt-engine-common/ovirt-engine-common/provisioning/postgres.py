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
from otopi import util

from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Local Postgres plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_CONF,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_CONF
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_HBA
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_PG_VERSION,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_VERSION
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_SERVICE,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_SERVICE
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_MAX_CONN,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_MAX_CONN
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_LISTEN_ADDRESS,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_LISTEN_ADDRESS
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_LC_MESSAGES,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_LC_MESSAGES
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_AUTOVACUUM_VACUUM_SCALE_FACTOR,
            oengcommcons.Defaults.PG_PROV_AUTOVACUUM_VACUUM_SCALE_FACTOR
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_AUTOVACUUM_ANALYZE_SCALE_FACTOR,
            oengcommcons.Defaults.PG_PROV_AUTOVACUUM_ANALYZE_SCALE_FACTOR
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_AUTOVACUUM_MAX_WORKERS,
            oengcommcons.Defaults.PG_PROV_AUTOVACUUM_MAX_WORKERS
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_AUTOVACUUM_MAINTENANCE_WORK_MEM,
            oengcommcons.Defaults.PG_PROV_AUTOVACUUM_MAINTENANCE_WORK_MEM
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_WORK_MEM_KB,
            oengcommcons.Defaults.PG_PROV_WORK_MEM_KB
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.OLD_POSTGRES_SERVICE,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_SERVICE
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_UPGRADE_INPLACE,
            False
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.PG_UPGRADE_CLEANOLD,
            False
        )

# vim: expandtab tabstop=4 shiftwidth=4
