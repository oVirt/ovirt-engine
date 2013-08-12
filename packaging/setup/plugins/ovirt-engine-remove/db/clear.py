#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Clear plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database
from ovirt_engine_setup import dialog


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
            osetupcons.RemoveEnv.REMOVE_DATABASE,
            None
        )
        self._bkpfile = None

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
    )
    def _customization(self):
        if self.environment[
            osetupcons.RemoveEnv.REMOVE_DATABASE
        ] is None:
            self.environment[
                osetupcons.RemoveEnv.REMOVE_DATABASE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_REMOVE_DATABASE',
                note=_(
                    'Do you want to remove Engine DB content? All data will '
                    'be lost (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[osetupcons.DBEnv.PASSWORD] is not None and
            self.environment[osetupcons.RemoveEnv.REMOVE_DATABASE]
        ),
        after=(
            osetupcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
        ),
    )
    def _misc(self):

        try:
            dbovirtutils = database.OvirtUtils(plugin=self)
            dbovirtutils.tryDatabaseConnect()
            self._bkpfile = dbovirtutils.backup()
            self.logger.info(
                _('Clearing database {database}').format(
                    database=self.environment[osetupcons.DBEnv.DATABASE],
                )
            )
            dbovirtutils.clearOvirtEngineDatabase()

        except RuntimeError as e:
            self.logger.debug('exception', exc_info=True)
            self.logger.warning(
                _(
                    'Cannot clear database: {error}'
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
                'A backup of the database is available at {path}'
            ).format(
                path=self._bkpfile
            ),
        )

# vim: expandtab tabstop=4 shiftwidth=4
