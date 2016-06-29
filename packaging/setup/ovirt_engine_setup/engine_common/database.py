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
import string
import tempfile

import psycopg2
from otopi import base, util
from ovirt_engine import util as outil

from ovirt_engine_setup import util as osetuputil
from ovirt_setup_lib import dialog
from ovirt_setup_lib import hostname as osetuphostname
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

    def _dropObjects(self, statement, objectType, objects):
        for name in [o['objectname'] for o in objects]:
            statement.execute(
                statement=(
                    """
                        DROP {type} IF EXISTS {name} CASCADE
                    """
                ).format(
                    type=objectType,
                    name=name,
                ),
                ownConnection=True,
                transaction=False,
            )

    def clearDatabase(self):
        statement = Statement(
            environment=self.environment,
            dbenvkeys=self._dbenvkeys,
        )

        objectsToDrop = {
            'VIEW': """
                SELECT table_schema || '.' || table_name AS objectname
                FROM information_schema.views
                WHERE table_schema = 'public'
            """,

            'TABLE': """
                SELECT table_schema || '.' || table_name AS objectname
                FROM information_schema.tables
                WHERE table_schema = 'public'
            """,

            'SEQUENCE': """
                SELECT
                    sequence_schema || '.' || sequence_name AS objectname
                FROM information_schema.sequences
                WHERE sequence_schema = 'public'
            """,

            'TYPE': """
                SELECT
                    c.relname::information_schema.sql_identifier
                    AS objectname
                FROM pg_namespace n, pg_class c, pg_type t
                WHERE
                    n.oid = c.relnamespace AND
                    t.typrelid = c.oid AND
                    c.relkind = 'c'::"char" AND
                    n.nspname = 'public'
            """,

            'FUNCTION': """
                SELECT
                    ns.nspname ||
                    '.' ||
                    proname ||
                    '(' || oidvectortypes(proargtypes) || ')'
                    AS objectname
                FROM
                    pg_proc INNER JOIN pg_namespace ns ON (
                        pg_proc.pronamespace=ns.oid
                    )
                WHERE ns.nspname = 'public'
            """,

            'SCHEMA': """
                SELECT schema_name AS objectname
                FROM information_schema.schemata
                WHERE schema_owner = %(username)s
            """,
        }

        objectsToDropArgs = dict(
            username=_ind_env(self, DEK.USER),
        )

        # it's important to drop object types in logical order
        for objectType in (
            'VIEW',
            'TABLE',
            'SEQUENCE',
            'FUNCTION',
            'TYPE',
            'SCHEMA'
        ):
            self._dropObjects(
                statement=statement,
                objectType=objectType,
                objects=statement.execute(
                    statement=objectsToDrop[objectType],
                    args=objectsToDropArgs,
                    ownConnection=True,
                    transaction=False,
                )
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
        if set(r['rc'] for r in res['result']) != set((0,)):
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
        ' *Command was: CREATE PROCEDURAL LANGUAGE plpgsql;',
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
        (
            'pg_restore: \[archiver \(db\)\] Error from TOC entry \d+'
            '; \d+ \d+ PROCEDURAL LANGUAGE plpgsql'
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
    def _lower_equal_no_dash(key, current, expected):
        return OvirtUtils._lower_equal(
            key,
            current.replace('-', ''),
            expected.replace('-', ''),
        )

    @staticmethod
    def _error_message(key, current, expected, format_str, name, pg_host):
        return format_str.format(
            key=key,
            current=current,
            expected=expected,
            name=name,
            pg_host=pg_host,
        )

    _PG_CONF_MSG = _(
        'Please fix {key} in postgresql.conf on {pg_host} before you '
        'continue. Its location is usually /var/lib/pgsql/data , or '
        'somewhere under /etc/postgresql* .'
    )

    def _pg_conf_info(self):
        return (
            {
                'key': 'server_encoding',
                'expected': 'UTF8',
                'ok': self._lower_equal_no_dash,
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
                'error_msg': '{specific}{pg_conf_msg}'.format(
                    specific=_(
                        '{name} requires {key} to be at least {expected}. '
                    ),
                    pg_conf_msg=self._PG_CONF_MSG,
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
                'ok': self._lower_equal_no_dash,
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': '{specific}{pg_conf_msg}'.format(
                    specific=_(
                        '{name} requires {key} to be {expected}. '
                    ),
                    pg_conf_msg=self._PG_CONF_MSG,
                ),
            },
            {
                'key': 'server_version',
                'expected': self._plugin.execute(
                    args=(
                        self.command.get('psql'),
                        '-V',
                    ),
                )[
                    1  # stdout
                ][
                    0  # first line. E.g. on Fedora 23: psql (PostgreSQL) 9.4.8
                ].split(
                    ' '
                )[
                    -1
                ],
                'ok': self._lower_equal,
                'check_on_use': True,
                'needed_on_create': False,
                'error_msg': _(
                    "Postgresql client version is '{expected}', whereas "
                    "the version on {pg_host} is '{current}'. "
                    "Please use a Postgresql server of version '{expected}'."
                ),
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
                self.logger.debug(
                    "Mismatch: key='%s', current='%s', expected='%s'",
                    key,
                    current,
                    expected,
                )
                raise RuntimeError(
                    self._error_message(
                        key=key,
                        current=current,
                        expected=expected,
                        format_str=item['error_msg'],
                        name=name,
                        pg_host=environment[self._dbenvkeys[DEK.HOST]]
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
            dbenv = {}
            for k in (
                DEK.HOST,
                DEK.PORT,
                DEK.SECURED,
                DEK.HOST_VALIDATION,
                DEK.DATABASE,
                DEK.USER,
                DEK.PASSWORD,
            ):
                dbenv[self._dbenvkeys[k]] = _ind_env(self, k)

            def query_dbenv(
                what,
                note,
                tests=None,
                **kwargs
            ):
                dialog.queryEnvKey(
                    name='{qpref}{what}'.format(
                        qpref=queryprefix,
                        what=string.upper(what),
                    ),
                    dialog=self.dialog,
                    logger=self.logger,
                    env=dbenv,
                    key=self._dbenvkeys[what],
                    note=note.format(
                        name=name,
                    ),
                    prompt=True,
                    default=defaultdbenvkeys[what],
                    tests=tests,
                    **kwargs
                )

            query_dbenv(
                what=DEK.HOST,
                note=_('{name} database host [@DEFAULT@]: '),
                tests=(
                    {
                        'test': osetuphostname.Hostname(
                            self._plugin,
                        ).getHostnameTester(),
                    },
                ),
            )

            query_dbenv(
                what=DEK.PORT,
                note=_('{name} database port [@DEFAULT@]: '),
                tests=({'test': osetuputil.getPortTester()},),
            )

            if dbenv[self._dbenvkeys[DEK.SECURED]] is None:
                dbenv[self._dbenvkeys[DEK.SECURED]] = dialog.queryBoolean(
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

            if not dbenv[self._dbenvkeys[DEK.SECURED]]:
                dbenv[self._dbenvkeys[DEK.HOST_VALIDATION]] = False

            if dbenv[self._dbenvkeys[DEK.HOST_VALIDATION]] is None:
                dbenv[
                    self._dbenvkeys[DEK.HOST_VALIDATION]
                ] = dialog.queryBoolean(
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

            query_dbenv(
                what=DEK.DATABASE,
                note=_('{name} database name [@DEFAULT@]: '),
            )

            query_dbenv(
                what=DEK.USER,
                note=_('{name} database user [@DEFAULT@]: '),
            )

            query_dbenv(
                what=DEK.PASSWORD,
                note=_('{name} database password: '),
                hidden=True,
            )

            self.logger.debug('dbenv: %s', dbenv)
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

    def replaced_localhost(self, replacement=None):
        return (
            replacement
            if (
                replacement and
                _ind_env(self, DEK.HOST) == 'localhost'
            )
            else _ind_env(self, DEK.HOST)
        )

    def getJdbcUrl(self, localhost_replacement=None):
        return (
            'jdbc:postgresql://{host}:{port}/{database}'
            '?{jdbcTlsOptions}'
        ).format(
            host=self.replaced_localhost(localhost_replacement),
            port=_ind_env(self, DEK.PORT),
            database=_ind_env(self, DEK.DATABASE),
            jdbcTlsOptions='&'.join(
                s for s in (
                    'ssl=true'
                    if _ind_env(self, DEK.SECURED)
                    else '',

                    (
                        'sslfactory='
                        'org.postgresql.ssl.NonValidatingFactory'
                    )
                    if not _ind_env(self, DEK.HOST_VALIDATION)
                    else ''
                ) if s
            ),
        )

    def getDBConfig(self, prefix, localhost_replacement=None):
        return (
            '{prefix}_DB_HOST="{host}"\n'
            '{prefix}_DB_PORT="{port}"\n'
            '{prefix}_DB_USER="{user}"\n'
            '{prefix}_DB_PASSWORD="{password}"\n'
            '{prefix}_DB_DATABASE="{database}"\n'
            '{prefix}_DB_SECURED="{secured}"\n'
            '{prefix}_DB_SECURED_VALIDATION="{hostValidation}"\n'
            '{prefix}_DB_DRIVER="org.postgresql.Driver"\n'
            '{prefix}_DB_URL="{jdbcUrl}"\n'
        ).format(
            prefix=prefix,
            host=self.replaced_localhost(localhost_replacement),
            port=_ind_env(self, DEK.PORT),
            user=_ind_env(self, DEK.USER),
            password=outil.escape(
                _ind_env(self, DEK.PASSWORD),
                ':\\',
            ),
            database=_ind_env(self, DEK.DATABASE),
            secured=_ind_env(self, DEK.SECURED),
            hostValidation=_ind_env(self, DEK.HOST_VALIDATION),
            jdbcUrl=self.getJdbcUrl(localhost_replacement),
        )


# vim: expandtab tabstop=4 shiftwidth=4
