#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


import gettext
import os

from otopi import constants as otopicons
from otopi import plugin, transaction, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Schema plugin."""

    class SchemaTransaction(transaction.TransactionElement):
        """yum transaction element."""

        def __init__(self, parent, backup=None):
            self._parent = parent
            self._backup = backup

        def __str__(self):
            return _("Engine schema Transaction")

        def prepare(self):
            pass

        def abort(self):
            self._parent.logger.info(_('Rolling back database schema'))
            try:
                dbovirtutils = database.OvirtUtils(
                    plugin=self._parent,
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                )
                self._parent.logger.info(
                    _('Clearing Engine database {database}').format(
                        database=self._parent.environment[
                            oenginecons.EngineDBEnv.DATABASE
                        ],
                    )
                )
                dbovirtutils.clearDatabase()
                if self._backup is not None and os.path.exists(self._backup):
                    self._parent.logger.info(
                        _('Restoring Engine database {database}').format(
                            database=self._parent.environment[
                                oenginecons.EngineDBEnv.DATABASE
                            ],
                        )
                    )
                    dbovirtutils.restore(backupFile=self._backup)
            except Exception as e:
                self._parent.logger.debug(
                    'Error during Engine database restore',
                    exc_info=True,
                )
                self._parent.logger.error(
                    _('Engine database rollback failed: {error}').format(
                        error=e,
                    )
                )

        def commit(self):
            pass

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _checkDatabaseOwnership(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        result = statement.execute(
            statement="""
                select
                    nsp.nspname as object_schema,
                    cls.relname as object_name,
                    rol.rolname as owner,
                    case cls.relkind
                        when 'r' then 'TABLE'
                        when 'i' then 'INDEX'
                        when 'S' then 'SEQUENCE'
                        when 'v' then 'VIEW'
                        when 'c' then 'TYPE'
                    else
                        cls.relkind::text
                    end as object_type
                from
                    pg_class cls join
                    pg_roles rol on rol.oid = cls.relowner join
                    pg_namespace nsp on nsp.oid = cls.relnamespace
                where
                    nsp.nspname not in ('information_schema', 'pg_catalog') and
                    nsp.nspname not like 'pg_%%' and
                    rol.rolname != %(user)s
                order by
                    nsp.nspname,
                    cls.relname
            """,
            args=dict(
                user=self.environment[oenginecons.EngineDBEnv.USER],
            ),
            ownConnection=True,
            transaction=False,
        )
        if len(result) > 0:
            raise RuntimeError(
                _(
                    'Cannot upgrade the Engine database schema due to wrong '
                    'ownership of some database entities.\n'
                    'Please execute: {command}\n'
                    'Using the password of the "postgres" user.'
                ).format(
                    command=(
                        '{cmd} '
                        '-s {server} '
                        '-p {port} '
                        '-d {db} '
                        '-f postgres '
                        '-t {user}'
                    ).format(
                        cmd=(
                            oenginecons.FileLocations.
                            OVIRT_ENGINE_DB_CHANGE_OWNER
                        ),
                        server=self.environment[oenginecons.EngineDBEnv.HOST],
                        port=self.environment[oenginecons.EngineDBEnv.PORT],
                        db=self.environment[oenginecons.EngineDBEnv.DATABASE],
                        user=self.environment[oenginecons.EngineDBEnv.USER],
                    ),
                )
            )

    def _checkSupportedVersionsPresent(self):
        # TODO: figure out a better way to do this for the future
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        dcVersions = statement.execute(
            statement="""
                SELECT compatibility_version FROM storage_pool;
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
            'SELECT compatibility_version FROM {table};'
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
        self._checkDatabaseOwnership()
        self._checkSupportedVersionsPresent()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_SCHEMA,
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        backupFile = None

        if not self.environment[
            oenginecons.EngineDBEnv.NEW_DATABASE
        ]:
            dbovirtutils = database.OvirtUtils(
                plugin=self,
                dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            )
            backupFile = dbovirtutils.backup(
                dir=self.environment[
                    oenginecons.ConfigEnv.OVIRT_ENGINE_DB_BACKUP_DIR
                ],
                prefix=oenginecons.Const.ENGINE_DB_BACKUP_PREFIX,
            )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.SchemaTransaction(
                parent=self,
                backup=backupFile,
            )
        )

        self.logger.info(_('Creating/refreshing Engine database schema'))
        args = [
            oenginecons.FileLocations.OVIRT_ENGINE_DB_SCHMA_TOOL,
            '-s', self.environment[oenginecons.EngineDBEnv.HOST],
            '-p', str(self.environment[oenginecons.EngineDBEnv.PORT]),
            '-u', self.environment[oenginecons.EngineDBEnv.USER],
            '-d', self.environment[oenginecons.EngineDBEnv.DATABASE],
            '-l', self.environment[otopicons.CoreEnv.LOG_FILE_NAME],
            '-c', 'apply',
        ]
        if self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]:
            if not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
            ):
                os.makedirs(
                    oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
                )
            args.extend(
                [
                    '-m',
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR,
                        '%s-%s.scripts.md5' % (
                            self.environment[
                                oenginecons.EngineDBEnv.HOST
                            ],
                            self.environment[
                                oenginecons.EngineDBEnv.DATABASE
                            ],
                        ),
                    ),
                ]
            )
        rc, stdout, stderr = self.execute(
            args=args,
            envAppend={
                'DBFUNC_DB_PGPASSFILE': self.environment[
                    oenginecons.EngineDBEnv.PGPASS_FILE
                ]
            },
            raiseOnError=False,
        )
        if rc:
            self.logger.error(
                '%s: %s',
                os.path.basename(
                    oenginecons.FileLocations.OVIRT_ENGINE_DB_SCHMA_TOOL
                ),
                stderr[-1]
            )
            raise RuntimeError(_('Engine schema refresh failed'))


# vim: expandtab tabstop=4 shiftwidth=4
