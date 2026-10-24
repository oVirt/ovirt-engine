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

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    # VDSStatus.Up
    _VDS_STATUS_UP = 3

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
        show_he_error = True
        host_up = True
        host_name = None
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )

        try:
            hosted_engine_vm_name = vdcoption.VdcOption(
                statement=statement,
            ).getVdcOption(
                'HostedEngineVmName',
                ownConnection=True,
            )
        except RuntimeError:
            hosted_engine_vm_name = 'HostedEngine'

        vds_id = statement.execute(
            statement="""
                SELECT vm_guid, run_on_vds
                FROM vms
                WHERE vm_name = %(HostedEngineVmName)s;
            """,
            args=dict(
                HostedEngineVmName=hosted_engine_vm_name,
            ),
            ownConnection=True,
            transaction=False,
        )

        try:
            if not vds_id[0]['vm_guid']:
                show_he_error = False
            elif vds_id[0]['run_on_vds']:
                ha_global_maintenance = statement.execute(
                    statement="""
                        SELECT s.vds_id, s.ha_global_maintenance, d.status,
                            st.vds_name
                        FROM vds_statistics s
                        JOIN vds_dynamic d ON d.vds_id = s.vds_id
                        JOIN vds_static st ON st.vds_id = s.vds_id
                        WHERE s.vds_id = %(VdsId)s;
                    """,
                    args=dict(
                        VdsId=vds_id[0]['run_on_vds'],
                    ),
                    ownConnection=True,
                    transaction=False,
                )

                try:
                    if ha_global_maintenance[0]['ha_global_maintenance']:
                        self.logger.info(_(
                            'Hosted Engine HA is in Global Maintenance mode.'
                        ))
                        show_he_error = False
                    elif (
                        ha_global_maintenance[0]['status'] !=
                        self._VDS_STATUS_UP
                    ):
                        host_up = False
                        host_name = ha_global_maintenance[0]['vds_name']
                except IndexError:
                    pass
        except IndexError:
            show_he_error = False

        if show_he_error and not host_up:
            self.logger.warning(_(
                'The host "{host}" running the hosted-engine VM is not Up, '
                'so the Global Maintenance state stored in the engine '
                'database may be stale (for example, if certificates have '
                'expired).\n'
                'Continue only if you are sure that the system is in '
                '"Global Maintenance" mode, e.g. after running '
                '"hosted-engine --set-maintenance --mode=global" on a '
                'hosted-engine host.'
            ).format(
                host=host_name,
            ))
            if self.environment[
                osetupcons.ConfigEnv.CONTINUE_SETUP_ON_HE_VM
            ] is None:
                self.environment[
                    osetupcons.ConfigEnv.CONTINUE_SETUP_ON_HE_VM
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_CONFIG_CONTINUE_SETUP_ON_HE_VM',
                    note=_(
                        'Is the system in Global Maintenance mode? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=False,
                )
            if self.environment[
                osetupcons.ConfigEnv.CONTINUE_SETUP_ON_HE_VM
            ]:
                show_he_error = False

        if show_he_error:
            self.logger.error(_(
                'It seems that you are running your engine inside of '
                'the hosted-engine VM and are not in "Global '
                'Maintenance" mode.\n'
                'In that case you should put the system into the "Global '
                'Maintenance" mode before running engine-setup, or the '
                'hosted-engine HA agent might kill the machine, which '
                'might corrupt your data.\n'
                'To skip this check, re-run with:\n'
                '--otopi-environment={key}=bool:True'
            ).format(
                key=osetupcons.ConfigEnv.CONTINUE_SETUP_ON_HE_VM,
            ))
            raise RuntimeError(_(
                'Hosted Engine setup detected, '
                'but Global Maintenance is not set.'
            ))


# vim: expandtab tabstop=4 shiftwidth=4
