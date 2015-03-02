#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015 Red Hat, Inc.
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


"""Advertise DWH and Reports plugin."""


import gettext
import os.path


from otopi import util
from otopi import plugin


from ovirt_engine import configfile


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import dwh_history_timekeeping
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Advertise DWH and Reports plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._dwhHost = None
        self._engine_manual = None
        self._statement = None

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG,
        ])
        if config.get('ENGINE_MANUAL'):
            self._engine_manual = config.get('ENGINE_MANUAL')

        if os.path.isdir(self._engine_manual):
            self.environment.setdefault(
                osetupcons.DocsEnv.DOCS_LOCAL,
                True
            )
            self.environment.setdefault(
                osetupcons.DocsEnv.DWH_DOC_URL,
                osetupcons.Const.DWH_DOC_URI
            )
            self.environment.setdefault(
                osetupcons.DocsEnv.REPORTS_DOC_URL,
                osetupcons.Const.REPORTS_DOC_URI
            )
        else:
            self.environment.setdefault(
                osetupcons.DocsEnv.DOCS_LOCAL,
                False
            )
            self.environment.setdefault(
                osetupcons.DocsEnv.DWH_DOC_URL,
                osetupcons.Const.DWH_DOC_URL
            )
            self.environment.setdefault(
                osetupcons.DocsEnv.REPORTS_DOC_URL,
                osetupcons.Const.REPORTS_DOC_URL
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oengcommcons.Stages.DB_SCHEMA,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]
        ),
    )
    def _get_dwh_host(self):
        self._statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )

        self._dwhHost = dwh_history_timekeeping.getValueFromTimekeeping(
            statement=self._statement,
            name=dwh_history_timekeeping.DB_KEY_HOSTNAME
        )

        self.logger.debug(
            _(
                'DWH host is {dwhHost}.'
            ).format(
                dwhHost=self._dwhHost,
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment.get(
                oenginecons.DWHCoreEnv.ENABLE
            ) and not self.environment.get(
                oenginecons.ReportsCoreEnv.ENABLE
            ) and not self._dwhHost
        ),
    )
    def _closeup(self):
        noteText = _(
            'Note! If you want to gather statistical'
            ' information you can install Reports and/or DWH:\n'
        )

        if not self.environment.get(osetupcons.DocsEnv.DOCS_LOCAL):
            self.dialog.note(
                text=(
                    noteText +
                    '    {dwhURL}\n'
                    '    {reportsURL}\n'
                ).format(
                    dwhURL=self.environment[
                        osetupcons.DocsEnv.DWH_DOC_URL
                    ],
                    reportsURL=self.environment[
                        osetupcons.DocsEnv.REPORTS_DOC_URL
                    ],
                )
            )
        else:
            if self.environment[oengcommcons.ConfigEnv.JBOSS_AJP_PORT]:
                engineURI = oenginecons.Const.ENGINE_URI
            else:
                engineURI = '/'

            self.dialog.note(
                text=(
                    noteText +
                    '    http://{fqdn}:{httpPort}{engineURI}{dwhURL}\n'
                ).format(
                    fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                    httpPort=self.environment[
                        oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
                    ],
                    engineURI=engineURI,
                    dwhURL=self.environment[
                        osetupcons.DocsEnv.DWH_DOC_URL
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
