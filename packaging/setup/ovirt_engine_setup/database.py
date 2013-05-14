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


import os
import base64
import tempfile
import datetime
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


import psycopg2


from M2Crypto import X509
from M2Crypto import RSA


from otopi import base
from otopi import util


from ovirt_engine_setup import constants as osetupcons


@util.export
class Statement(base.Base):

    @property
    def environment(self):
        return self._environment

    def __init__(self, environment):
        super(Statement, self).__init__()
        self._environment = environment

    def execute(
        self,
        statement,
        args=dict(),
        host=None,
        port=None,
        secured=None,
        securedHostValidation=None,
        user=None,
        password=None,
        database=None,
        ownConnection=False,
        transaction=True,
    ):
        ret = []
        if host is None:
            host = self.environment[osetupcons.DBEnv.HOST]
        if port is None:
            port = self.environment[osetupcons.DBEnv.PORT]
        if secured is None:
            secured = self.environment[osetupcons.DBEnv.SECURED]
        if securedHostValidation is None:
            securedHostValidation = self.environment[
                osetupcons.DBEnv.SECURED_HOST_VALIDATION
            ]
        if user is None:
            user = self.environment[osetupcons.DBEnv.USER]
        if password is None:
            password = self.environment[osetupcons.DBEnv.PASSWORD]
        if database is None:
            database = self.environment[osetupcons.DBEnv.DATABASE]

        sslmode = 'allow'
        if secured:
            if securedHostValidation:
                sslmode = 'verify-full'
            else:
                sslmode = 'require'

        _connection = None
        cursor = None
        try:
            self.logger.debug(
                "Database: '%s', Statement: '%s', args: %s",
                database,
                statement,
                args,
            )
            if not ownConnection:
                connection = self.environment[osetupcons.DBEnv.CONNECTION]
            else:
                self.logger.debug('Creating own connection')
                _connection = connection = psycopg2.connect(
                    host=host,
                    port=port,
                    user=user,
                    password=password,
                    database=database,
                    sslmode=sslmode,
                )
                if not transaction:
                    connection.autocommit = True

            cursor = connection.cursor()
            cursor.execute(
                statement,
                args,
            )

            if cursor.description is not None:
                cols = [d[0] for d in cursor.description]
                while True:
                    entry = cursor.fetchone()
                    if entry is None:
                        break
                    ret.append(dict(zip(cols, entry)))

        except:
            if _connection is not None:
                _connection.rollback()
            raise
        else:
            if _connection is not None:
                _connection.commit()
        finally:
            if cursor is not None:
                cursor.close()
            if _connection is not None:
                _connection.close()

        self.logger.debug('Result: %s', ret)
        return ret

    def getVdcOption(
        self,
        name,
        version='general',
        type=str
    ):
        result = self.execute(
            statement="""
                select option_name, option_value
                from vdc_options
                where
                    option_name = %(name)s and
                    version = %(version)s
            """,
            args=dict(
                name=name,
                version=version,
            ),
        )
        if len(result) != 1:
            raise RuntimeError(
                _('Cannot locate application option {name}').format(
                    name=name,
                )
            )
        value = result[0]['option_value']
        if type == bool:
            value = value.lower() not in ('false', '0')

        return value

    def updateVdcOptions(
        self,
        options,
    ):
        for option in options:
            value = option['value']

            if option.get('encrypt', False):
                x509 = X509.load_cert(
                    file=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CERT
                    ),
                    format=X509.FORMAT_PEM,
                )
                value = base64.b64encode(
                    x509.get_pubkey().get_rsa().public_encrypt(
                        data=value,
                        padding=RSA.pkcs1_padding,
                    ),
                )

            if isinstance(value, bool):
                value = 'true' if value else 'false'

            self.execute(
                statement="""
                    update vdc_options
                    set
                        option_value=%(value)s,
                        version=%(version)s
                    where option_name=%(name)s
                """,
                args=dict(
                    name=option['name'],
                    value=value,
                    version=option.get('version', 'general'),
                ),
            )


@util.export
class OvirtUtils(base.Base):

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def command(self):
        return self._plugin.command

    def __init__(self, plugin):
        super(OvirtUtils, self).__init__()
        self._plugin = plugin

    def detectCommands(self):
        self.command.detect('pg_dump')
        self.command.detect('psql')

    def tryDatabaseConnect(self, environment=None):

        if environment is None:
            environment = self.environment

        try:
            statement = Statement(environment=environment)
            statement.execute(
                statement="""
                    select 1
                """,
                ownConnection=True,
                transaction=False,
            )
            self.logger.debug('Connection succeeded')
        except psycopg2.OperationalError as e:
            self.logger.debug('Connection failed', exc_info=True)
            raise RuntimeError(
                _('Cannot connect to database: {error}').format(
                    error=e,
                )
            )

    def isNewDatabase(
        self,
        host=None,
        port=None,
        secured=None,
        user=None,
        password=None,
        database=None,
    ):
        statement = Statement(
            environment=self.environment,
        )
        ret = statement.execute(
            statement="""
                select count(*) as count
                from information_schema.tables
                where table_name=%(table)s
            """,
            args=dict(
                table='schema_version',
            ),
            host=host,
            port=port,
            secured=secured,
            user=user,
            password=password,
            database=database,
            ownConnection=True,
            transaction=False,
        )
        return ret[0]['count'] == 0

    def clearOvirtEngineDatabase(self):
        statement = Statement(
            environment=self.environment,
        )
        try:
            spdrops = statement.execute(
                statement="""
                    select generate_drop_all_functions_syntax as drop
                    from generate_drop_all_functions_syntax()
                """,
                ownConnection=True,
                transaction=False,
            )
            for spdrop in [t['drop'] for t in spdrops]:
                statement.execute(
                    statement=spdrop,
                    ownConnection=True,
                    transaction=False,
                )
        except (psycopg2.OperationalError, psycopg2.ProgrammingError):
            self.logger.debug(
                'generate_drop_all_functions_syntax failed',
                exc_info=True,
            )
        tables = statement.execute(
            statement="""
                select table_name
                from information_schema.views
                where table_schema = %(schemaname)s
            """,
            args=dict(
                schemaname='public',
            ),
            ownConnection=True,
            transaction=False,
        )
        for view in [t['table_name'] for t in tables]:
            statement.execute(
                statement=(
                    """
                        drop view if exists {view} cascade
                    """
                ).format(
                    view=view,
                ),
                ownConnection=True,
                transaction=False,
            )

        seqs = statement.execute(
            statement="""
                select relname as seqname
                from pg_class
                where relkind=%(relkind)s
            """,
            args=dict(
                relkind='S',
            ),
            ownConnection=True,
            transaction=False,
        )
        for seq in [t['seqname'] for t in seqs]:
            statement.execute(
                statement=(
                    """
                        drop sequence if exists {sequence} cascade
                    """
                ).format(
                    sequence=seq,
                ),
                ownConnection=True,
                transaction=False,
            )
        tables = statement.execute(
            statement="""
                select tablename
                from pg_tables
                where schemaname = %(schemaname)s
            """,
            args=dict(
                schemaname='public',
            ),
            ownConnection=True,
            transaction=False,
        )
        for table in [t['tablename'] for t in tables]:
            statement.execute(
                statement=(
                    """
                        drop table if exists {table} cascade
                    """
                ).format(
                    table=table,
                ),
                ownConnection=True,
                transaction=False,
            )

    def backup(
        self,
        prefix='engine',
    ):
        fd, backupFile = tempfile.mkstemp(
            prefix='%s-%s.' % (
                prefix,
                datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            ),
            suffix='.sql',
            dir=osetupcons.FileLocations.OVIRT_ENGINE_DB_BACKUP_DIR,
        )
        os.close(fd)

        self.logger.info(
            _("Backing up database to '{file}'.").format(
                file=backupFile,
            )
        )
        self._plugin.execute(
            (
                self.command.get('pg_dump'),
                '-E', 'UTF8',
                '--disable-dollar-quoting',
                '--disable-triggers',
                '--format=p',
                '-U', self.environment[osetupcons.DBEnv.USER],
                '-h', self.environment[osetupcons.DBEnv.HOST],
                '-p', str(self.environment[osetupcons.DBEnv.PORT]),
                '-f', backupFile,
                self.environment[osetupcons.DBEnv.DATABASE],
            ),
            envAppend={
                'PGPASSFILE': self.environment[
                    osetupcons.DBEnv.PGPASS_FILE
                ]
            },
        )

        return backupFile

    def restore(
        self,
        backupFile,
    ):
        self.clearOvirtEngineDatabase()
        self._plugin.execute(
            (
                self.command.get('psql'),
                '-w',
                '-h', self.environment[osetupcons.DBEnv.HOST],
                '-p', str(self.environment[osetupcons.DBEnv.PORT]),
                '-U', self.environment[osetupcons.DBEnv.USER],
                '-d', self.environment[osetupcons.DBEnv.DATABASE],
                '-f', backupFile,
            ),
            envAppend={
                'PGPASSFILE': self.environment[
                    osetupcons.DBEnv.PGPASS_FILE
                ]
            },
        )

# vim: expandtab tabstop=4 shiftwidth=4
