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


"""Schema plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database


@util.export
class Plugin(plugin.PluginBase):
    """Schema plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _checkSupportedVersionsPresent(self):
        # TODO: figure out a better way to do this for the future
        statement = database.Statement(environment=self.environment)
        dcVersions = statement.execute(
            statement="""
                SELECT compatibility_version FROM storage_pool;
            """,
            ownConnection=True,
            transaction=False,
        )
        clusterVersions = statement.execute(
            statement="""
                SELECT compatibility_version FROM vds_groups;;
            """,
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
            raise RuntimeError(
                _(
                    'Trying to upgrade from unsupported versions: {versions}'
                ).format(
                    versions=' '.join(versions - supported)
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.DB_SCHEMA,
        after=[
            osetupcons.Stages.CONFIG_DB_CREDENTIALS,
        ],
        condition=lambda self: self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ],
    )
    def _miscInstall(self):
        self.logger.info(_('Creating database schema'))
        args = [
            os.path.join(
                osetupcons.FileLocations.OVIRT_ENGINE_DB_DIR,
                'create_schema.sh',
            ),
            '-l', self.environment[otopicons.CoreEnv.LOG_FILE_NAME],
            '-s', self.environment[osetupcons.DBEnv.HOST],
            '-p', str(self.environment[osetupcons.DBEnv.PORT]),
            '-d', self.environment[osetupcons.DBEnv.DATABASE],
            '-u', self.environment[osetupcons.DBEnv.USER],
        ]
        if self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]:
            if not os.path.exists(
                osetupcons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
            ):
                os.makedirs(
                    osetupcons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
                )
            args.extend(
                [
                    '-m',
                    osetupcons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR,
                ]
            )
        else:
            args.extend(
                [
                    '-g',   # do not generate md5
                ]
            )
        self.execute(
            args=args,
            envAppend={
                'ENGINE_CERTIFICATE': (
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                ),
                'ENGINE_PGPASS': self.environment[
                    osetupcons.DBEnv.PGPASS_FILE
                ]
            },
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.DB_SCHEMA,
        after=[
            osetupcons.Stages.DB_CREDENTIALS_AVAILABLE,
        ],
        condition=lambda self: not self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ],
    )
    def _miscUpgrade(self):
        self._checkSupportedVersionsPresent()
        dbovirtutils = database.OvirtUtils(plugin=self)
        backupFile = dbovirtutils.backup()

        #
        # TODO
        # rename database
        # why do we need rename?
        # consider doing that via python
        #

        try:
            self.logger.info(_('Updating database schema'))
            args = [
                osetupcons.FileLocations.OVIRT_ENGINE_DB_UPGRADE,
                '-s', self.environment[osetupcons.DBEnv.HOST],
                '-p', str(self.environment[osetupcons.DBEnv.PORT]),
                '-u', self.environment[osetupcons.DBEnv.USER],
                '-d', self.environment[osetupcons.DBEnv.DATABASE],
            ]
            if self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ]:
                if not os.path.exists(
                    osetupcons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
                ):
                    os.makedirs(
                        osetupcons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
                    )
                args.extend(
                    [
                        '-m',
                        osetupcons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR,
                    ]
                )
            else:
                args.extend(
                    [
                        '-g',   # do not generate md5
                    ]
                )
            self.execute(
                args=args,
                envAppend={
                    'ENGINE_CERTIFICATE': (
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                    'ENGINE_PGPASS': self.environment[
                        osetupcons.DBEnv.PGPASS_FILE
                    ]
                },
            )
        except:
            self.logger.debug(
                'Exception during database upgrade',
                exc_info=True
            )
            self.logger.warning(_('Rolling back upgrade'))

            try:
                dbovirtutils.clearOvirtEngineDatabase()
                dbovirtutils.restore(backupFile=backupFile)
            except Exception as e:
                self.logger.debug(
                    'Exception during database restore',
                    exc_info=True,
                )
                self.logger.error(
                    _('Database rollback failed: {error}').format(
                        error=e,
                    )
                )

            raise


# vim: expandtab tabstop=4 shiftwidth=4
