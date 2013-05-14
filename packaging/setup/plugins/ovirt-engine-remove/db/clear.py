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
            osetupcons.DBEnv.REMOVE_EMPTY_DATABASE,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
    )
    def _customization(self):
        if self.environment[
            osetupcons.DBEnv.REMOVE_EMPTY_DATABASE
        ] is None:
            self.environment[
                osetupcons.DBEnv.REMOVE_EMPTY_DATABASE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_DB_REMOVE',
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
            self.environment[osetupcons.DBEnv.REMOVE_EMPTY_DATABASE]
        ),
    )
    def _misc(self):

        try:
            dbovirtutils = database.OvirtUtils(plugin=self)
            dbovirtutils.tryDatabaseConnect()
            dbovirtutils.backup()
            self.logger.info(
                _("Clearing database '{database}'").format(
                    database=self.environment[
                        osetupcons.DBEnv.DATABASE
                    ],
                ),
            )
            dbovirtutils.clearOvirtEngineDatabase()

        except RuntimeError as e:
            self.logger.debug('exception', exc_info=True)
            self.logger.warning(
                _(
                    'Cannot connect to database: {error}'
                ).format(
                    error=e,
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
