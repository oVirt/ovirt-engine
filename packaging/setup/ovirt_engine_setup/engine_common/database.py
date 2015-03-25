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


import atexit
import datetime
import gettext
import os
import re
import socket
import tempfile

import psycopg2
from otopi import base, util
from ovirt_engine import util as outil

from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup import dialog
from ovirt_engine_setup.engine_common import constants as oengcommcons
DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


def _ind_env(inst, keykey):
    return inst.environment[inst._dbenvkeys[keykey]]


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
        if not set(DEK.REQUIRED_KEYS) <= set(dbenvkeys.keys()):
            raise RuntimeError(
                _('Missing required db env keys: {keys}').format(
                    keys=list(set(DEK.REQUIRED_KEYS) - set(dbenvkeys.keys())),
                )
            )
        self._dbenvkeys = dbenvkeys

    def connect(
        self,
        host=None,
        port=None,
        secured=None,
        securedHostValidation=None,
        user=None,
        password=None,
        database=None,
    ):
        if host is None:
            host = _ind_env(self, DEK.HOST)
        if port is None:
            port = _ind_env(self, DEK.PORT)
        if secured is None:
            secured = _ind_env(self, DEK.SECURED)
        if securedHostValidation is None:
            securedHostValidation = _ind_env(self, DEK.HOST_VALIDATION)
        if user is None:
            user = _ind_env(self, DEK.USER)
        if password is None:
            password = _ind_env(self, DEK.PASSWORD)
        if database is None:
            database = _ind_env(self, DEK.DATABASE)

        sslmode = 'allow'
        if secured:
            if securedHostValidation:
                sslmode = 'verify-full'
            else:
                sslmode = 'require'

        #
        # old psycopg2 does not know how to ignore
        # uselss parameters
        #
        if not host:
            connection = psycopg2.connect(
                database=database,
            )
        else:
            #
            # port cast is required as old psycopg2
            # does not support unicode strings for port.
            # do not cast to int to avoid breaking usock.
            #
            connection = psycopg2.connect(
                host=host,
                port=str(port),
                user=user,
                password=password,
                database=database,
                sslmode=sslmode,
            )

        return connection

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
                connection = _ind_env(self, DEK.CONNECTION)
            else:
                self.logger.debug('Creating own connection')

                _connection = connection = self.connect(
                    host=host,
                    port=port,
                    secured=secured,
                    securedHostValidation=securedHostValidation,
                    user=user,
                    password=password,
                    database=database,
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


@util.export
class OvirtUtils(base.Base):

    _plainPassword = None

    @property
    def environment(self):
        return self._environment

    @property
    def command(self):
        return self._plugin.command

    @property
    def dialog(self):
        return self._plugin.dialog

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
        if not set(DEK.REQUIRED_KEYS) <= set(dbenvkeys.keys()):
            raise RuntimeError(
                _('Missing required db env keys: {keys}').format(
                    keys=list(set(DEK.REQUIRED_KEYS) - set(dbenvkeys.keys())),
                )
            )
        self._dbenvkeys = dbenvkeys

    def detectCommands(self):
        self.command.detect('pg_dump')
        self.command.detect('pg_restore')
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
                    host=_ind_env(self, DEK.HOST),
                    port=_ind_env(self, DEK.PORT),
                    database=_ind_env(self, DEK.DATABASE),
                    user=_ind_env(self, DEK.USER),
                    password=(
                        _ind_env(self, DEK.PASSWORD)
                        if type(self)._plainPassword
                        else outil.escape(
                            _ind_env(self, DEK.PASSWORD),
                            ':\\',
                        )
                    ),
                ),
            )
        self.environment[self._dbenvkeys[DEK.PGPASSFILE]] = pgpass

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
                from pg_catalog.pg_tables
                where schemaname = 'public';
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

    def _backup_restore_filters_info(self):
        return {
            'gzip': {
                'dump': ['gzip'],
                'restore': ['zcat'],
            },
            'bzip2': {
                'dump': ['bzip2'],
                'restore': ['bzcat'],
            },
            'xz': {
                'dump': ['xz'],
                'restore': ['xzcat'],
            },
        }

    def _dump_base_args(self):
        return [
            self.command.get('pg_dump'),
            '-E', 'UTF8',
            '--disable-dollar-quoting',
            '--disable-triggers',
            '-U', _ind_env(self, DEK.USER),
            '-h', _ind_env(self, DEK.HOST),
            '-p', str(_ind_env(self, DEK.PORT)),
        ]

    def _pg_restore_base_args(self):
        return [
            '-w',
            '-h', _ind_env(self, DEK.HOST),
            '-p', str(_ind_env(self, DEK.PORT)),
            '-U', _ind_env(self, DEK.USER),
            '-d', _ind_env(self, DEK.DATABASE),
        ]

    def _backup_restore_dumpers_info(self, backupfile, database):
        # if backupfile is not supplied, we write to stdout
        return {
            'pg_custom': {
                'dump_args': (
                    self._dump_base_args() +
                    [
                        '--format=custom',
                    ] +
                    (
                        ['--file=%s' % backupfile]
                        if backupfile else []
                    ) +
                    [database]
                ),
                'restore_args': (
                    [self.command.get('pg_restore')] +
                    self._pg_restore_base_args() +
                    (
                        ['--jobs=%s' % _ind_env(self, DEK.RESTORE_JOBS)]
                        if _ind_env(self, DEK.RESTORE_JOBS) and backupfile
                        else []
                    ) +
                    (
                        [backupfile]
                        if backupfile else []
                    )
                ),
            },
            'pg_plain': {
                'dump_args': (
                    self._dump_base_args() +
                    [
                        '--format=plain',
                    ] +
                    (
                        ['--file=%s' % backupfile]
                        if backupfile else []
                    ) +
                    [database]
                ),
                'restore_args': (
                    [self.command.get('psql')] +
                    self._pg_restore_base_args() +
                    (
                        ['--file=%s' % backupfile]
                        if backupfile else []
                    )
                ),
            },
        }

    def backup(
        self,
        dir,
        prefix,
    ):
        database = _ind_env(self, DEK.DATABASE)
        fd, backupFile = tempfile.mkstemp(
            prefix='%s-%s.' % (
                prefix,
                datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            ),
            suffix='.dump',
            dir=dir,
        )
        os.close(fd)

        self.logger.info(
            _("Backing up database {host}:{database} to '{file}'.").format(
                host=_ind_env(self, DEK.HOST),
                database=database,
                file=backupFile,
            )
        )

        filt = _ind_env(self, DEK.FILTER)
        f_infos = {}
        if filt is not None:
            f_infos = self._backup_restore_filters_info()
            if filt not in f_infos:
                raise RuntimeError(_('Unknown db filter {f}').format(f=filt))

        dumper = _ind_env(self, DEK.DUMPER)
        d_infos = self._backup_restore_dumpers_info(
            None if filt else backupFile,
            database
        )
        if dumper not in d_infos:
            raise RuntimeError(_('Unknown db dumper {d}').format(d=dumper))

        pipe = [
            {
                'args': d_infos[dumper]['dump_args'],
            }
        ]

        stdout = None
        if filt is not None:
            pipe.append(
                {
                    'args': f_infos[filt]['dump']
                }
            )
            stdout = open(backupFile, 'w')

        res = None
        try:
            res = self._plugin.executePipeRaw(
                pipe,
                envAppend={
                    'PGPASSWORD': '',
                    'PGPASSFILE': _ind_env(self, DEK.PGPASSFILE),
                },
                stdout=stdout,
            )
        finally:
            if stdout is not None:
                stdout.close()

        self.logger.debug('db backup res %s' % res)
        if {r['rc'] for r in res['result']} != {0}:
            raise RuntimeError(
                _(
                    'Failed to backup database, please check '
                    'the log file for details'
                )
            )
        return backupFile

    _IGNORED_ERRORS = (
        # TODO: verify and get rid of all the '.*'s

        '.*language "plpgsql" already exists',
        '.*must be owner of language plpgsql',
        # psql
        'ERROR:  must be owner of extension plpgsql',
        # pg_restore
        (
            'pg_restore: \[archiver \(db\)\] could not execute query: ERROR:  '
            'must be owner of extension plpgsql'
        ),

        # older versions of dwh used uuid-ossp, which requires
        # special privs, is not used anymore, and emits the following
        # errors for normal users.
        '.*permission denied for language c',
        '.*function public.uuid_generate_v1() does not exist',
        '.*function public.uuid_generate_v1mc() does not exist',
        '.*function public.uuid_generate_v3(uuid, text) does not exist',
        '.*function public.uuid_generate_v4() does not exist',
        '.*function public.uuid_generate_v5(uuid, text) does not exist',
        '.*function public.uuid_nil() does not exist',
        '.*function public.uuid_ns_dns() does not exist',
        '.*function public.uuid_ns_oid() does not exist',
        '.*function public.uuid_ns_url() does not exist',
        '.*function public.uuid_ns_x500() does not exist',

        # Other stuff, added because if we want to support other
        # formats etc we must explicitely filter all existing output
        # and not just ERRORs.
        'pg_restore: \[archiver \(db\)\] Error while PROCESSING TOC:',
        '    Command was: COMMENT ON EXTENSION',
        (
            'pg_restore: \[archiver \(db\)\] Error from TOC entry \d+'
            '; 0 0 COMMENT EXTENSION plpgsql'
        ),
        'pg_restore: WARNING:',
        'WARNING: ',
        'DETAIL:  ',
    )

    _RE_IGNORED_ERRORS = re.compile(
        pattern='|'.join(_IGNORED_ERRORS),
    )

    def restore(
        self,
        backupFile,
    ):
        database = _ind_env(self, DEK.DATABASE)

        self.logger.info(
            _("Restoring file '{file}' to database {host}:{database}.").format(
                host=_ind_env(self, DEK.HOST),
                database=database,
                file=backupFile,
            )
        )

        pipe = []

        filt = _ind_env(self, DEK.FILTER)
        f_infos = {}
        if filt is not None:
            f_infos = self._backup_restore_filters_info()
            if filt not in f_infos:
                raise RuntimeError(_('Unknown db filter {f}').format(f=filt))

        stdin = None
        if filt is not None:
            pipe.append(
                {
                    'args': f_infos[filt]['restore'],
                }
            )
            stdin = open(backupFile, 'r')

        dumper = _ind_env(self, DEK.DUMPER)
        d_infos = self._backup_restore_dumpers_info(
            None if filt else backupFile,
            database
        )
        if dumper not in d_infos:
            raise RuntimeError(_('Unknown db dumper {d}').format(d=dumper))

        pipe.append(
            {
                'args': d_infos[dumper]['restore_args'],
            }
        )

        try:
            res = self._plugin.executePipeRaw(
                pipe,
                envAppend={
                    'PGPASSWORD': '',
                    'PGPASSFILE': _ind_env(self, DEK.PGPASSFILE),
                },
                stdin=stdin,
                # raiseOnError=False,
            )
        finally:
            if stdin is not None:
                stdin.close()

        rc = res['result'][-1]['rc']
        stderr = res['result'][-1]['stderr'].splitlines()

        self.logger.debug('db restore rc %s stderr %s', rc, stderr)

        # if (rc != 0) and stderr:
        # Do something different for psql/pg_restore?
        if stderr:
            errors = [
                l for l in stderr
                if l and not self._RE_IGNORED_ERRORS.match(l)
            ]
            if errors:
                self.logger.error(
                    _(
                        'Errors while restoring {name} database, please check '
                        'the log file for details'
                    ).format(
                        name=database,
                    )
                )
                self.logger.debug(
                    'Errors unfiltered during restore:\n\n%s\n' %
                    '\n'.join(errors)
                )

    @staticmethod
    def _lower_equal(key, current, expected):
        return (
            current.strip(' \t"\'').lower() == expected.strip(' \t"\'').lower()
        )

    @staticmethod
    def _error_message(key, current, expected, format_str, name):
        return format_str.format(
            key=key,
            current=current,
            expected=expected,
            name=name,
        )

    def _pg_conf_info(self):
        return (
            {
                'key': 'server_encoding',
                'expected': 'UTF8',
                'ok': self._lower_equal,
                'check_on_use': True,
                'needed_on_create': False,
                'error_msg': _(
                    'Encoding of the {name} database is {current}. '
                    '{name} installation is only supported on servers '
                    'with default encoding set to {expected}. Please fix the '
                    'default DB encoding before you continue.'
                )
            },
            {
                'key': 'max_connections',
                'expected': self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_MAX_CONN
                ],
                'ok': lambda key, current, expected: (
                    int(current) >= int(expected)
                ),
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': _(
                    '{name} requires {key} to be at least {expected}. '
                    'Please fix {key} before you continue.'
                )
            },
            {
                'key': 'listen_addresses',
                'expected': self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_LISTEN_ADDRESS
                ],
                'ok': self._lower_equal,
                'check_on_use': False,
                'needed_on_create': True,
                'error_msg': None,
            },
            {
                'key': 'lc_messages',
                'expected': self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_LC_MESSAGES
                ],
                'ok': self._lower_equal,
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': _(
                    '{name} requires {key} to be {expected}. '
                    'Please fix {key} before you continue.'
                )
            },
        )

    _RE_KEY_VALUE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            \s*
            (?P<key>\w+)
            \s*
            =
            \s*
            (?P<value>\w+)
        """,
    )

    def _checkDbConf(self, environment, name):
        statement = Statement(
            environment=environment,
            dbenvkeys=self._dbenvkeys,
        )
        for item in [
            i for i in self._pg_conf_info() if i['check_on_use']
        ]:
            key = item['key']
            expected = item['expected']
            current = statement.execute(
                statement='show {key}'.format(key=key),
                ownConnection=True,
                transaction=False,
            )[0][key]
            if not item['ok'](key, current, expected):
                raise RuntimeError(
                    self._error_message(
                        key=key,
                        current=current,
                        expected=expected,
                        format_str=item['error_msg'],
                        name=name
                    )
                )

    def getUpdatedPGConf(self, content):
        needUpdate = True
        confs_ok = {}
        edit_params = {}
        for item in self._pg_conf_info():
            key = item['key']
            confs_ok[key] = False
            if item['needed_on_create']:
                edit_params[key] = item['expected']
        for l in content:
            m = self._RE_KEY_VALUE.match(l)
            if m is not None:
                for item in [
                    i for i in self._pg_conf_info()
                    if i['needed_on_create'] and m.group('key') == i['key']
                ]:
                    if item['ok'](
                        key=key,
                        current=m.group('value'),
                        expected=item['expected']
                    ):
                        confs_ok[item['key']] = True
                    else:
                        break
            if False not in confs_ok.values():
                needUpdate = False
                break

        if needUpdate:
            content = osetuputil.editConfigContent(
                content=content,
                params=edit_params,
            )
        return needUpdate, content

    def getCredentials(
        self,
        name,
        queryprefix,
        defaultdbenvkeys,
        show_create_msg=False,
        note=None,
        credsfile=None,
    ):
        interactive = None in (
            _ind_env(self, DEK.HOST),
            _ind_env(self, DEK.PORT),
            _ind_env(self, DEK.DATABASE),
            _ind_env(self, DEK.USER),
            _ind_env(self, DEK.PASSWORD),
        )

        if interactive:
            if note is None and credsfile:
                note = _(
                    "\nPlease provide the following credentials for the "
                    "{name} database.\nThey should be found on the {name} "
                    "server in '{credsfile}'.\n\n"
                ).format(
                    name=name,
                    credsfile=credsfile,
                )

            if note:
                self.dialog.note(text=note)

            if show_create_msg:
                self.dialog.note(
                    text=_(
                        "\n"
                        "ATTENTION\n"
                        "\n"
                        "Manual action required.\n"
                        "Please create database for ovirt-engine use. "
                        "Use the following commands as an example:\n"
                        "\n"
                        "create role {user} with login encrypted password "
                        "'{user}';\n"
                        "create database {database} owner {user}\n"
                        " template template0\n"
                        " encoding 'UTF8' lc_collate 'en_US.UTF-8'\n"
                        " lc_ctype 'en_US.UTF-8';\n"
                        "\n"
                        "Make sure that database can be accessed remotely.\n"
                        "\n"
                    ).format(
                        user=defaultdbenvkeys[DEK.USER],
                        database=defaultdbenvkeys[DEK.DATABASE],
                    ),
                )

        connectionValid = False
        while not connectionValid:
            host = _ind_env(self, DEK.HOST)
            port = _ind_env(self, DEK.PORT)
            secured = _ind_env(self, DEK.SECURED)
            securedHostValidation = _ind_env(self, DEK.HOST_VALIDATION)
            db = _ind_env(self, DEK.DATABASE)
            user = _ind_env(self, DEK.USER)
            password = _ind_env(self, DEK.PASSWORD)

            if host is None:
                while True:
                    host = self.dialog.queryString(
                        name='{qpref}HOST'.format(qpref=queryprefix),
                        note=_(
                            '{name} database host [@DEFAULT@]: '
                        ).format(
                            name=name,
                        ),
                        prompt=True,
                        default=defaultdbenvkeys[DEK.HOST],
                    )
                    try:
                        socket.getaddrinfo(host, None)
                        break  # do while missing in python
                    except socket.error as e:
                        self.logger.error(
                            _('Host is invalid: {error}').format(
                                error=e.strerror
                            )
                        )

            if port is None:
                while True:
                    try:
                        port = osetuputil.parsePort(
                            self.dialog.queryString(
                                name='{qpref}PORT'.format(qpref=queryprefix),
                                note=_(
                                    '{name} database port [@DEFAULT@]: '
                                ).format(
                                    name=name,
                                ),
                                prompt=True,
                                default=defaultdbenvkeys[DEK.PORT],
                            )
                        )
                        break  # do while missing in python
                    except ValueError:
                        pass

            if secured is None:
                secured = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='{qpref}SECURED'.format(qpref=queryprefix),
                    note=_(
                        '{name} database secured connection (@VALUES@) '
                        '[@DEFAULT@]: '
                    ).format(
                        name=name,
                    ),
                    prompt=True,
                    default=defaultdbenvkeys[DEK.SECURED],
                )

            if not secured:
                securedHostValidation = False

            if securedHostValidation is None:
                securedHostValidation = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='{qpref}SECURED_HOST_VALIDATION'.format(
                        qpref=queryprefix
                    ),
                    note=_(
                        '{name} database host name validation in secured '
                        'connection (@VALUES@) [@DEFAULT@]: '
                    ).format(
                        name=name,
                    ),
                    prompt=True,
                    default=True,
                ) == 'yes'

            if db is None:
                db = self.dialog.queryString(
                    name='{qpref}DATABASE'.format(qpref=queryprefix),
                    note=_(
                        '{name} database name [@DEFAULT@]: '
                    ).format(
                        name=name,
                    ),
                    prompt=True,
                    default=defaultdbenvkeys[DEK.DATABASE],
                )

            if user is None:
                user = self.dialog.queryString(
                    name='{qpref}USER'.format(qpref=queryprefix),
                    note=_(
                        '{name} database user [@DEFAULT@]: '
                    ).format(
                        name=name,
                    ),
                    prompt=True,
                    default=defaultdbenvkeys[DEK.USER],
                )

            if password is None:
                password = self.dialog.queryString(
                    name='{qpref}PASSWORD'.format(qpref=queryprefix),
                    note=_(
                        '{name} database password: '
                    ).format(
                        name=name,
                    ),
                    prompt=True,
                    hidden=True,
                )

            dbenv = {
                self._dbenvkeys[DEK.HOST]: host,
                self._dbenvkeys[DEK.PORT]: port,
                self._dbenvkeys[DEK.SECURED]: secured,
                self._dbenvkeys[DEK.HOST_VALIDATION]: securedHostValidation,
                self._dbenvkeys[DEK.USER]: user,
                self._dbenvkeys[DEK.PASSWORD]: password,
                self._dbenvkeys[DEK.DATABASE]: db,
            }

            if interactive:
                try:
                    self.tryDatabaseConnect(dbenv)
                    self._checkDbConf(environment=dbenv, name=name)
                    self.environment.update(dbenv)
                    connectionValid = True
                except RuntimeError as e:
                    self.logger.error(
                        _('Cannot connect to {name} database: {error}').format(
                            name=name,
                            error=e,
                        )
                    )
            else:
                # this is usally reached in provisioning
                # or if full ansewr file
                self.environment.update(dbenv)
                connectionValid = True

        try:
            self.environment[
                self._dbenvkeys[DEK.NEW_DATABASE]
            ] = self.isNewDatabase()
        except:
            self.logger.debug('database connection failed', exc_info=True)

        if not _ind_env(self, DEK.NEW_DATABASE):
            self._checkDbConf(environment=dbenv, name=name)


# vim: expandtab tabstop=4 shiftwidth=4
