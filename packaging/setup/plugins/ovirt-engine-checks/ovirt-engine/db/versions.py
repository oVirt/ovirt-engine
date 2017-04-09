#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2017 Red Hat, Inc.
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


"""Versions plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Versions plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _checkSupportedVersionsPresent(self):
        # TODO: figure out a better way to do this for the future
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        dcVersions = statement.execute(
            statement="""
                SELECT name, compatibility_version FROM storage_pool;
            """,
            ownConnection=True,
            transaction=False,
        )
        clusterTable = statement.execute(
            statement="""
                SELECT table_name FROM information_schema.tables
                WHERE table_name IN ('vds_groups', 'cluster');
            """,
            ownConnection=True,
            transaction=False,
        )
        sql = _(
            'SELECT name, compatibility_version FROM {table};'
        ).format(
            table=clusterTable[0]['table_name']
        )
        clusterVersions = statement.execute(
            statement=sql,
            ownConnection=True,
            transaction=False,
        )

        versions = set([
            x['compatibility_version']
            for x in dcVersions + clusterVersions
        ])
        supported = set([
            x.strip()
            for x in self.environment[
                osetupcons.CoreEnv.UPGRADE_SUPPORTED_VERSIONS
            ].split(',')
            if x.strip()
        ])

        if versions - supported:
            for (queryres, errmsg) in (
                (
                    dcVersions,
                    _(
                        'The following Data Centers have a too old '
                        'compatibility level, please upgrade them:'
                    )
                ),
                (
                    clusterVersions,
                    _(
                        'The following Clusters have a too old '
                        'compatibility level, please upgrade them:'
                    )
                ),
            ):
                objs = [
                    x['name']
                    for x in queryres
                    if x['compatibility_version'] not in supported
                ]
                if objs:
                    self.logger.error(errmsg)
                    self.dialog.note('\n'.join(objs))

            raise RuntimeError(
                _(
                    'Trying to upgrade from unsupported versions: {versions}'
                ).format(
                    versions=' '.join(versions - supported)
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _validation(self):
        self._checkSupportedVersionsPresent()


# vim: expandtab tabstop=4 shiftwidth=4
