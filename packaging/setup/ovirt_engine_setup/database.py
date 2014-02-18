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


import atexit
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


from ovirt_engine import util as outil


from ovirt_engine_setup import constants as osetupcons


@util.export
class Statement(base.Base):

    @property
    def environment(self):
        return self._environment

    def __init__(
        self,
        dbenvkeys,
        environment,
    ):
        super(Statement, self).__init__()
        self._environment = environment
        self._dbenvkeys = dbenvkeys

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
        # autocommit member is available at >= 2.4.2
        def __backup_autocommit(connection):
            if hasattr(connection, 'autocommit'):
                return connection.autocommit
            else:
                return connection.isolation_level

        def __restore_autocommit(connection, v):
            if hasattr(connection, 'autocommit'):
                connection.autocommit = v
            else:
                connection.set_isolation_level(v)

        def __set_autocommit(connection, autocommit):
            if hasattr(connection, 'autocommit'):
                connection.autocommit = autocommit
            else:
                connection.set_isolation_level(
                    psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT
                    if autocommit
                    else
                    psycopg2.extensions.ISOLATION_LEVEL_READ_COMMITTED
                )

        ret = []
        if host is None:
            host = self.environment[self._dbenvkeys['host']]
        if port is None:
            port = self.environment[self._dbenvkeys['port']]
        if secured is None:
            secured = self.environment[self._dbenvkeys['secured']]
        if securedHostValidation is None:
            securedHostValidation = self.environment[
                self._dbenvkeys['hostValidation']
            ]
        if user is None:
            user = self.environment[self._dbenvkeys['user']]
        if password is None:
            password = self.environment[self._dbenvkeys['password']]
        if database is None:
            database = self.environment[self._dbenvkeys['database']]

        sslmode = 'allow'
        if secured:
            if securedHostValidation:
                sslmode = 'verify-full'
            else:
                sslmode = 'require'

        old_autocommit = None
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
                connection = self.environment[self._dbenvkeys['connection']]
            else:
                self.logger.debug('Creating own connection')

                #
                # old psycopg2 does not know how to ignore
                # uselss parameters
                #
                if not host:
                    _connection = connection = psycopg2.connect(
                        database=database,
                    )
                else:
                    _connection = connection = psycopg2.connect(
                        host=host,
                        port=port,
                        user=user,
                        password=password,
                        database=database,
                        sslmode=sslmode,
                    )

            if not transaction:
                old_autocommit = __backup_autocommit(connection)
                __set_autocommit(connection, True)

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
            if old_autocommit is not None and connection is not None:
                __restore_autocommit(connection, old_autocommit)
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
        type=str,
        ownConnection=False,
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
            ownConnection=ownConnection,
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
            name = option['name']
            value = option['value']
            version = option.get('version', 'general')

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

            res = self.execute(
                statement="""
                    select count(*) as count
                    from vdc_options
                    where
                        option_name=%(name)s and
                        version=%(version)s
                """,
                args=dict(
                    name=name,
                    version=version,
                ),
            )
            if res[0]['count'] == 0:
                self.execute(
                    statement="""
                        insert into vdc_options (
                            option_name,
                            option_value,
                            version
                        )
                        values (
                            %(name)s,
                            %(value)s,
                            %(version)s
                        )
                    """,
                    args=dict(
                        name=name,
                        version=version,
                        value=value,
                    ),
                )
            else:
                self.execute(
                    statement="""
                        update vdc_options
                        set
                            option_value=%(value)s
                        where
                            option_name=%(name)s and
                            version=%(version)s
                    """,
                    args=dict(
                        name=name,
                        version=version,
                        value=value,
                    ),
                )


@util.export
class OvirtUtils(base.Base):

    _plainPassword = None

    @property
    def environment(self):
        return self._environment

    @property
    def command(self):
        return self._plugin.command

    def __init__(
        self,
        plugin,
        dbenvkeys,
        environment=None,
    ):
        super(OvirtUtils, self).__init__()
        self._plugin = plugin
        self._environment = (
            self._plugin.environment
            if environment is None
            else environment
        )
        self._dbenvkeys = dbenvkeys

    def detectCommands(self):
        self.command.detect('pg_dump')
        self.command.detect('psql')

    def createPgPass(self):

        #
        # we need client side psql library
        # version as at least in rhel for 8.4
        # the password within pgpassfile is
        # not escaped.
        # the simplest way is to checkout psql
        # utility version.
        #
        if type(self)._plainPassword is None:
            rc, stdout, stderr = self._plugin.execute(
                args=(
                    self.command.get('psql'),
                    '-V',
                ),
            )
            type(self)._plainPassword = ' 8.' in stdout[0]

        fd, pgpass = tempfile.mkstemp()
        atexit.register(os.unlink, pgpass)
        with os.fdopen(fd, 'w') as f:
            f.write(
                (
                    '# DB USER credentials.\n'
                    '{host}:{port}:{database}:{user}:{password}\n'
                ).format(
                    host=self.environment[self._dbenvkeys['host']],
                    port=self.environment[self._dbenvkeys['port']],
                    database=self.environment[self._dbenvkeys['database']],
                    user=self.environment[self._dbenvkeys['user']],
                    password=(
                        self.environment[self._dbenvkeys['password']]
                        if type(self)._plainPassword
                        else outil.escape(
                            self.environment[self._dbenvkeys['password']],
                            ':\\',
                        )
                    ),
                ),
            )
        self.environment[self._dbenvkeys['pgpassfile']] = pgpass

    def tryDatabaseConnect(self, environment=None):

        if environment is None:
            environment = self.environment

        try:
            statement = Statement(
                environment=environment,
                dbenvkeys=self._dbenvkeys,
            )
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
            dbenvkeys=self._dbenvkeys,
        )
        ret = statement.execute(
            statement="""
                select count(*) as count
                from information_schema.tables
                where table_schema = 'public';
            """,
            args=dict(),
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

    def createLanguage(self, language):
        statement = Statement(
            environment=self.environment,
            dbenvkeys=self._dbenvkeys,
        )

        if statement.execute(
            statement="""
                select count(*)
                from pg_language
                where lanname=%(language)s;
            """,
            args=dict(
                language=language,
            ),
            ownConnection=True,
            transaction=False,
        )[0]['count'] == 0:
            statement.execute(
                statement=(
                    """
                        create language {language};
                    """
                ).format(
                    language=language,
                ),
                args=dict(),
                ownConnection=True,
                transaction=False,
            )

    def clearDatabase(self):

        self.createLanguage('plpgsql')

        statement = Statement(
            environment=self.environment,
            dbenvkeys=self._dbenvkeys,
        )

        statement.execute(
            statement="""
                create or replace
                function
                    oesetup_generate_drop_all_syntax()
                    returns setof text
                AS $procedure$ begin
                    return query
                        select
                            'drop function if exists ' ||
                            ns.nspname ||
                            '.' ||
                            proname ||
                            '(' ||
                                oidvectortypes(proargtypes) ||
                            ') cascade;'
                        from
                            pg_proc inner join pg_namespace ns on (
                                pg_proc.pronamespace=ns.oid
                            )
                        where
                            ns.nspname = 'public'
                        union
                        select
                            'drop type if exists ' ||
                            c.relname::information_schema.sql_identifier ||
                            ' ' ||
                            'cascade;'
                        from
                            pg_namespace n, pg_class c, pg_type t
                        where
                            n.oid = c.relnamespace and t.typrelid = c.oid and
                            c.relkind = 'c'::"char" and n.nspname = 'public';
                end; $procedure$
                language plpgsql;
            """,
            args=dict(),
            ownConnection=True,
            transaction=False,
        )

        spdrops = statement.execute(
            statement="""
                select oesetup_generate_drop_all_syntax as drop
                from oesetup_generate_drop_all_syntax()
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
        dir,
        prefix,
    ):
        fd, backupFile = tempfile.mkstemp(
            prefix='%s-%s.' % (
                prefix,
                datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            ),
            suffix='.sql',
            dir=dir,
        )
        os.close(fd)

        self.logger.info(
            _("Backing up database {host}:{database} to '{file}'.").format(
                host=self.environment[self._dbenvkeys['host']],
                database=self.environment[self._dbenvkeys['database']],
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
                '-U', self.environment[self._dbenvkeys['user']],
                '-h', self.environment[self._dbenvkeys['host']],
                '-p', str(self.environment[self._dbenvkeys['port']]),
                '-f', backupFile,
                self.environment[self._dbenvkeys['database']],
            ),
            envAppend={
                'PGPASSWORD': '',
                'PGPASSFILE': self.environment[self._dbenvkeys['pgpassfile']],
            },
        )

        return backupFile

    def restore(
        self,
        backupFile,
    ):
        self._plugin.execute(
            (
                self.command.get('psql'),
                '-w',
                '-h', self.environment[self._dbenvkeys['host']],
                '-p', str(self.environment[self._dbenvkeys['port']]),
                '-U', self.environment[self._dbenvkeys['user']],
                '-d', self.environment[self._dbenvkeys['database']],
                '-f', backupFile,
            ),
            envAppend={
                'PGPASSWORD': '',
                'PGPASSFILE': self.environment[self._dbenvkeys['pgpassfile']],
            },
        )


# vim: expandtab tabstop=4 shiftwidth=4
