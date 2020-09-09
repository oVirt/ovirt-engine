#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Vacuum plugin."""

import datetime
import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Vacuum plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.EngineDBEnv.ENGINE_VACUUM_FULL,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
        after=(
            oengcommcons.Stages.DB_CUST_UPGRADEDBMS_ENGINE,
            oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
        ),
    )
    def _customization(self):
        if self.environment[
            oenginecons.EngineDBEnv.ENGINE_VACUUM_FULL
        ] is not None:
            return

        self.environment[
            oenginecons.EngineDBEnv.ENGINE_VACUUM_FULL
        ] = dialog.queryBoolean(
            dialog=self.dialog,
            name='ENGINE_VACUUM_FULL',
            # TODO try to supply some estimation on the amount
            # of space we will need to read/write/remove if possible.
            # some projects like check_postgres may supply that report
            # already. See https://github.com/bucardo/check_postgres
            note=_(
                'Perform full vacuum on the engine database {db}@{host}?'
                '\nThis operation may take a while'
                ' depending on this setup health and the'
                '\nconfiguration of the db vacuum process.'
                '\nSee'
                ' https://www.postgresql.org/docs/12/sql-vacuum.html'
                '\n(@VALUES@) [@DEFAULT@]: '
            ).format(
                db=self.environment[
                    oenginecons.EngineDBEnv.DATABASE
                ],
                host=self.environment[
                    oenginecons.EngineDBEnv.HOST
                ],
            ),
            prompt=True,
            default=False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            oenginecons.EngineDBEnv.ENGINE_VACUUM_FULL
        ],
    )
    def _vacuum(self):
        self.logger.info(
            _("Running vacuum full on the engine schema")
        )
        start = datetime.datetime.now()
        args = [
            oenginecons.FileLocations.OVIRT_ENGINE_VACUUM_TOOL,
            '-f',
            '-v'
        ]
        self.execute(args=args)
        self.logger.info(
            _("Running vacuum full elapsed {secs}").format(
                secs=datetime.datetime.now() - start,
            )
        )
