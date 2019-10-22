#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.CONTINUE_SETUP_ON_HE_VM,
            None,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        before=(
            oenginecons.Stages.MEMORY_CHECK,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[oenginecons.EngineDBEnv.NEW_DATABASE] and
            not self.environment[oenginecons.EngineDBEnv.JUST_RESTORED] and
            not self.environment[
                osetupcons.ConfigEnv.CONTINUE_SETUP_ON_HE_VM
            ]
        ),
    )
    def _validate(self):
        ShowHEError = True
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )

        try:
            HostedEngineVmName = vdcoption.VdcOption(
                statement=statement,
            ).getVdcOption(
                'HostedEngineVmName',
                ownConnection=True,
            )
        except RuntimeError:
            HostedEngineVmName = 'HostedEngine'

        VdsId = statement.execute(
            statement="""
                SELECT vm_guid, run_on_vds
                FROM vms
                WHERE vm_name = %(HostedEngineVmName)s;
            """,
            args=dict(
                HostedEngineVmName=HostedEngineVmName,
            ),
            ownConnection=True,
            transaction=False,
        )

        try:
            if not VdsId[0]['vm_guid']:
                ShowHEError = False
            elif VdsId[0]['run_on_vds']:
                HAGlobalMaintenance = statement.execute(
                    statement="""
                        SELECT vds_id, ha_global_maintenance
                        FROM vds_statistics
                        WHERE vds_id = %(VdsId)s;
                    """,
                    args=dict(
                        VdsId=VdsId[0]['run_on_vds'],
                    ),
                    ownConnection=True,
                    transaction=False,
                )

                try:
                    if HAGlobalMaintenance[0]['ha_global_maintenance']:
                        self.logger.info(_(
                            'Hosted Engine HA is in Global Maintenance mode.'
                        ))
                        ShowHEError = False
                except IndexError:
                    pass
        except IndexError:
            ShowHEError = False

        if ShowHEError:
            self.logger.error(_(
                'It seems that you are running your engine inside of '
                'the hosted-engine VM and are not in "Global '
                'Maintenance" mode.\n'
                'In that case you should put the system into the "Global '
                'Maintenance" mode before running engine-setup, or the '
                'hosted-engine HA agent might kill the machine, which '
                'might corrupt your data.\n'
            ))
            raise RuntimeError(_(
                'Hosted Engine setup detected, '
                'but Global Maintenance is not set.'
            ))


# vim: expandtab tabstop=4 shiftwidth=4
