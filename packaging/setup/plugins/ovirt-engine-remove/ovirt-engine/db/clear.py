#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Clear plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Clear plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.RemoveEnv.REMOVE_ENGINE_DATABASE,
            None
        )
        self._bkpfile = None

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            oenginecons.Stages.REMOVE_CUSTOMIZATION_ENGINE,
        ),
        condition=lambda self: (
            self.environment[oenginecons.RemoveEnv.REMOVE_ENGINE]
        ),
    )
    def _customization(self):
        if self.environment[
            oenginecons.RemoveEnv.REMOVE_ENGINE_DATABASE
        ] is None:
            self.environment[
                oenginecons.RemoveEnv.REMOVE_ENGINE_DATABASE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_ENGINE_DB_REMOVE',
                note=_(
                    'Do you want to remove Engine database content? '
                    'All data will be lost (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[oenginecons.EngineDBEnv.PASSWORD] is not None and
            self.environment[oenginecons.RemoveEnv.REMOVE_ENGINE_DATABASE]
        ),
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
        ),
    )
    def _misc(self):

        try:
            dbovirtutils = database.OvirtUtils(
                plugin=self,
                dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            )
            dbovirtutils.tryDatabaseConnect()
            self._bkpfile = dbovirtutils.backup(
                dir=self.environment[
                    oenginecons.ConfigEnv.OVIRT_ENGINE_DB_BACKUP_DIR
                ],
                prefix=oenginecons.Const.ENGINE_DB_BACKUP_PREFIX,
            )
            self.logger.info(
                _('Clearing Engine database {database}').format(
                    database=self.environment[
                        oenginecons.EngineDBEnv.DATABASE
                    ],
                )
            )
            dbovirtutils.clearDatabase()

        except RuntimeError as e:
            self.logger.debug('exception', exc_info=True)
            self.logger.warning(
                _(
                    'Cannot clear Engine database: {error}'
                ).format(
                    error=e,
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._bkpfile is not None,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'A backup of the Engine database is available at {path}'
            ).format(
                path=self._bkpfile
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
