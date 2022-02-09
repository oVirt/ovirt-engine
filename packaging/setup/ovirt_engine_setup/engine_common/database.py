#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import atexit
import datetime
import distutils.version
import gettext
import os
import re
import tempfile

import psycopg2

from otopi import base
from otopi import constants as otopicons
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog
from ovirt_setup_lib import hostname as osetuphostname

DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


AT_MOST_EXPECTED = _("It is required to be at most '{expected}'")
AT_LEAST_EXPECTED = _("It is required to be at least '{expected}'")
PG_CONF_MSG = _(
    "\nPlease note the following required changes in postgresql.conf on "
    "'{pg_host}':\n"
    "{keys}\n"
    "postgresql.conf is usually in /var/lib/pgsql/data, "
    " or "
    "somewhere under /etc/postgresql* . You have to restart PostgreSQL "
    "after making these changes."
)
RE_KEY_VALUE = re.compile(
    flags=re.VERBOSE,
    pattern=r"""
            ^
            \s*
            (?P<key>\w+)
            \s*
            =
            \s*
            (?P<value>\S+)
        """,
)
RE_KEY_VALUE_MULTIPLE = re.compile(
    flags=re.VERBOSE,
    pattern=r"""
            \s*
            (?P<key>\w+)
            \s*
            =
            \s*
            (?P<value>\S+)
        """,
)


def _ind_env(inst, keykey):
    return inst.environment[inst._dbenvkeys[keykey]]


def getInvalidConfigItemsMessage(invalid_config_items):
    return '\n'.join(
        [
            e['format_str'].format(**e)
            for e in invalid_config_items
            if not e['needed_on_create'] and e['format_str']
        ]
    ) + PG_CONF_MSG.format(
        keys='\n'.join(
            [
                (
                    _("  '{key}' is currently '{current}'.") + (
                        ' {s}.'.format(s=e['format_str'])
                        if e['format_str']
                        else _(" .'{key}' needs to be '{expected}'.")
                    )
                ).format(**e)
                for e in invalid_config_items
                if e['needed_on_create']
                # This is a hack. Using needed_on_create to check if the param
                # applies to a postgresql.conf param or something else.
            ]
        ),
        pg_host=invalid_config_items[0]['pg_host'],
    )


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
                dbname=database,
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
                dbname=database,
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
        logResult=True,
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

        except Exception:
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

        self.logger.debug('Result: %s', ret if logResult else 'Not logged')
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
        self.command.detect('semanage')

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

    def checkServerVersion(
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
            statement="SHOW server_version",
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
        server_v = ret[0]['server_version']
        self.logger.debug(
            "PostgreSQL server version: {v}".format(
                v=server_v,
            )
        )
        return server_v

    def checkClientVersion(self):
        rc, stdout, stderr = self._plugin.execute(
            (
                self.command.get('psql'),
                '--version',
            ),
            raiseOnError=True,
        )
        client_v = stdout[0].split()[-1]
        self.logger.debug(
            "PostgreSQL client version: {v}".format(
                v=client_v,
            )
        )
        return client_v

    def checkDBMSUpgrade(
        self,
        host=None,
        port=None,
        secured=None,
        user=None,
        password=None,
        database=None,
    ):
        server_v = distutils.version.LooseVersion(
            self.checkServerVersion(
                host,
                port,
                secured,
                user,
                password,
                database,
            )
        ).version[:2]
        client_v = distutils.version.LooseVersion(
            self.checkClientVersion()
        ).version[:2]
        return server_v < client_v

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
                AND proname NOT LIKE 'uuid_%%'
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
        'language "plpgsql" already exists',
        'must be owner of language plpgsql',
        'must be owner of extension plpgsql',
        'must be owner of extension uuid-ossp',
        # older versions of dwh used uuid-ossp, which requires
        # special privs, is not used anymore, and emits the following
        # errors for normal users.
        'permission denied for language c',
        'function public.uuid_generate_v1() does not exist',
        'function public.uuid_generate_v1mc() does not exist',
        'function public.uuid_generate_v3(uuid, text) does not exist',
        'function public.uuid_generate_v4() does not exist',
        'function public.uuid_generate_v5(uuid, text) does not exist',
        'function public.uuid_nil() does not exist',
        'function public.uuid_ns_dns() does not exist',
        'function public.uuid_ns_oid() does not exist',
        'function public.uuid_ns_url() does not exist',
        'function public.uuid_ns_x500() does not exist',
        # This happens because we do some GRANTs for grafana's user on dwh db,
        # but dwh db user has no right to do them itself:
        'must be member of role "postgres"',
    )

    _RE_IGNORED_ERRORS = re.compile(
        pattern='|'.join([
            '.*ERROR: *{}'.format(err)
            for err in _IGNORED_ERRORS
        ])
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
        stderr = res[
            'result'
        ][
            -1
        ][
            'stderr'
        ].decode(
            'utf-8',
            'replace'
        ).splitlines()

        self.logger.debug('db restore rc %s stderr %s', rc, stderr)

        # if (rc != 0) and stderr:
        # Do something different for psql/pg_restore?
        if stderr:
            errors = [
                line for line in stderr
                if line and
                'ERROR:' in line and
                not self._RE_IGNORED_ERRORS.match(line)
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
    def _pg_versions_match(key, current, expected):
        return (
            distutils.version.LooseVersion(current).version[:2] ==
            distutils.version.LooseVersion(expected).version[:2]
        )

    @staticmethod
    def _lower_equal_no_dash(key, current, expected):
        return OvirtUtils._lower_equal(
            key,
            current.replace('-', ''),
            expected.replace('-', ''),
        )

    def _pg_conf_info(self):
        return self.environment.get(
            oengcommcons.ProvisioningEnv.POSTGRES_EXTRA_CONFIG_ITEMS,
            ()
        ) + (
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
                'error_msg': '{specific}'.format(
                    specific=AT_LEAST_EXPECTED,
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
                'error_msg': '{specific}'.format(
                    specific=_(
                        '{name} requires {key} to be {expected}'
                    ),
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
                'ok': self._pg_versions_match,
                'check_on_use': True,
                'skip_on_dbmsupgrade': True,
                'needed_on_create': False,
                'error_msg': _(
                    "Postgresql client version is '{expected}', whereas "
                    "the version on '{pg_host}' is '{current}'. "
                    "Please use a PostgreSQL server of version '{expected}'."
                ),
            },
            {
                'key': 'log_line_prefix',
                'expected': "'%m '",  # timestamp with milliseconds
                'ok': self._lower_equal,
                'check_on_use': False,
                'needed_on_create': True,
                'error_msg': None,
            },
            {

                'key': 'log_filename',
                'expected': "'postgresql-%m.log'",  # month as a decimal number
                'ok': self._lower_equal,
                'check_on_use': False,
                'needed_on_create': True,
                'error_msg': None,
            },
            {

                'key': 'log_timezone',
                'expected': "'UTC'",
                'ok': self._lower_equal,
                'check_on_use': False,
                'needed_on_create': True,
                'error_msg': None,
            },
        )

    def validateDbConf(self, name, environment=None):
        '''

        :param environment: db environment
        :param name: db name
        :return: A set of invalid config items. i.e an empty set implies the db
                 settings are valid.
        '''
        if environment is None:
            environment = self._environment
        statement = Statement(
            environment=environment,
            dbenvkeys=self._dbenvkeys,
        )
        invalid_config_items = []
        for item in [
            i for i in self._pg_conf_info() if i['check_on_use']
        ]:
            if (
                self._environment[self._dbenvkeys[DEK.NEED_DBMSUPGRADE]] and
                item.get('skip_on_dbmsupgrade', False)
            ):
                continue
            key = item['key']
            expected = item['expected']
            # When using 'show some_setting', the returned value is prettified
            # e.g for memory values you'd get '64MB' and not 64. When a number
            # is needed, prefer a query to the pg_settings table instead.
            if item.get('useQueryForValue', False):
                get_statement = 'select setting {key} from pg_settings' \
                                ' where name = \'{key}\''.format(key=key)
            else:
                get_statement = 'show {key}'.format(key=key)
            current = statement.execute(
                statement=get_statement,
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
                item_data = {
                    'key': key,
                    'current': current,
                    'expected': expected,
                    'format_str': item['error_msg'],
                    'name': name,
                    'pg_host': environment[self._dbenvkeys[DEK.HOST]],
                    'needed_on_create': item['needed_on_create'],
                }
                if self._environment.get(
                    oengcommcons.ConfigEnv.FORCE_INVALID_PG_CONF,
                    False
                ):
                    formatted_msg = ''
                    if item['error_msg']:
                        formatted_msg = item['error_msg'].format(**item_data)
                    self.logger.warn(
                        formatted_msg
                        if formatted_msg and not item['needed_on_create']
                        else _(
                            "Ignoring invalid PostgreSql configuration for "
                            "{name}: "
                            "host = '{pg_host}', "
                            "key = '{key}', "
                            "current value = '{current}'."
                            "{error_msg}"
                        ).format(
                            name=name,
                            pg_host=environment[self._dbenvkeys[DEK.HOST]],
                            key=key,
                            current=current,
                            error_msg=(
                                ' {m}.'.format(m=formatted_msg)
                                if formatted_msg
                                else ''
                            ),
                        )
                    )
                else:
                    invalid_config_items.append(item_data)
        return invalid_config_items

    def getUpdatedPGConf(self, content):
        edit_params = {}
        for item in self._pg_conf_info():
            key = item['key']
            if item['needed_on_create']:
                edit_params[key] = item['expected']
        for line in content:
            m = RE_KEY_VALUE.match(line)
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
                        del(edit_params[item['key']])

        needUpdate = len(edit_params) > 0
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
        validateconf=True,
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
                        "Please create the database for the {name} to use.\n"
                        "To create a user:\n"
                        "postgres=# create role <user_name> with "
                        "login encrypted password '<password>';\n"
                        "To create a database:\n"
                        "postgres=# create database <database_name> "
                        "owner <user_name> template template0 "
                        "encoding 'UTF8' lc_collate 'en_US.UTF-8' "
                        "lc_ctype 'en_US.UTF-8';\n"
                        "\n"
                        "If you plan for a remote application "
                        "to use this database, "
                        "make sure it can be accessed remotely.\n"
                        "\n"
                    ).format(
                        name=name,
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
                        what=what.upper(),
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
                if kwargs.get('hidden'):
                    self.environment[
                        otopicons.CoreEnv.LOG_FILTER
                    ].append(
                        dbenv[self._dbenvkeys[what]]
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
                    invalid_config_items = self.validateDbConf(name, dbenv)
                    if invalid_config_items:
                        self.logger.error(
                            getInvalidConfigItemsMessage(
                                invalid_config_items
                            )
                        )
                        continue
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
        except Exception:
            self.logger.debug('database connection failed', exc_info=True)

        try:
            self.environment[
                self._dbenvkeys[DEK.NEED_DBMSUPGRADE]
            ] = self.checkDBMSUpgrade()
        except Exception:
            self.logger.debug('database version check failed', exc_info=True)

        if not _ind_env(self, DEK.NEW_DATABASE) and validateconf:
            invalid_config_items = self.validateDbConf(name, dbenv)
            if (
                invalid_config_items and
                DEK.INVALID_CONFIG_ITEMS in self._dbenvkeys
            ):
                # If DEK.INVALID_CONFIG_ITEMS is not in self._dbenvkeys,
                # it probably means that this component is not interested
                # in invalid items. This can be removed once all components
                # add it, currently dwh.
                self.environment[
                    self._dbenvkeys[DEK.INVALID_CONFIG_ITEMS]
                ] = invalid_config_items

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

    def getInstanceSize(
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
            statement=(
                'SELECT '
                'SUM(pg_database_size(datname)) '
                'As dbms_size FROM pg_database'
            ),
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
        dbms_human_size = int(ret[0]['dbms_size'])
        return dbms_human_size

    def getPGDATA(
        self,
    ):
        rc, stdout, stderr = self._plugin.execute(
            (
                self.command.get('systemctl'),
                'show',
                '-p',
                'Environment',
                self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
                ]
            ),
            raiseOnError=False,
        )
        if rc == 0:
            for line in stdout:
                for k, v in RE_KEY_VALUE_MULTIPLE.findall(line):
                    if k == 'PGDATA':
                        return v
        raise RuntimeError(_('Unable to detect PGDATA location'))

    def getPGDATAAvailableSpace(
        self,
        pgdata,
    ):
        found = False
        pd = pgdata
        while not found:
            if not os.path.exists(pd):
                pd = os.path.dirname(pd)
            else:
                found = True
        statvfs = os.statvfs(pd)
        return statvfs.f_frsize * statvfs.f_bavail

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

    def setupOwnsDB(self):
        # FIXME localhost is inappropriate in case of docker e.g
        # we need a deterministic notion of local/remote pg_host in sense of
        # 'we own postgres' or not.
        return (
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] or
            _ind_env(self, DEK.HOST) == 'localhost'
        )

    def _HumanReadableSize(self, bytes):
        size_in_mb = bytes / pow(2, 20)
        return (
            _('{size} MB').format(size=size_in_mb)
            if size_in_mb < 1024
            else _('{size:1.1f} GB').format(
                size=size_in_mb/1024.0,
            )
        )

    def _verify_selinux_file_contexts(self):
        # This is a hack. See also https://bugzilla.redhat.com/1518599 .
        # Under certain conditions, something which should add file context
        # equivalency rules to scl postgres, does not, and files are then
        # created later with wrong labels, eventually causing the postgres
        # service to fail.

        # TODO:
        # 1. Make this optional? So that if selinux is disabled or not
        # enforcing, we do not fail?
        # 2. Make the version (9.5) and location (/var/opt...) parametric
        self.logger.info(_('Verifying PostgreSQL SELinux file context rules'))
        rc, stdout, stderr = self._plugin.execute(
            (
                self.command.get('semanage'),
                'fcontext',
                '--list',
            ),
            raiseOnError=False,
        )
        # Only fail if the command succeded but didn't include the expected
        # line. This way it should also work in developer mode, also probably
        # if selinux is disabled or permissive.
        if rc == 0 and '/var/lib/pgsql = /var' not in stdout:
            self.logger.error(_(
                'SELinux file context rules for PostgreSQL are missing\n'
            ))
            raise RuntimeError(_(
                'SELinux file context rules for PostgreSQL are missing'
            ))

    def DBMSUpgradeCustomizationHelper(self, which_db):
        upgrade_approved_inplace = False
        upgrade_approved_cleanupold = False

        client_v = self.checkClientVersion()
        server_v = self.checkServerVersion()

        self.logger.warning(
            _(
                'This release requires PostgreSQL server {cv} but the '
                '{db} database is currently hosted on PostgreSQL server {sv}.'
            ).format(
                cv=client_v,
                sv=server_v,
                db=which_db,
            )
        )

        if not self.setupOwnsDB():
            self.logger.error(_(
                'Please upgrade the PostgreSQL instance that serves the {db}'
                'database to {v} and retry.\n'
                'If the remote DBMS is on an EL7 system, install '
                'PostgreSQL and the scl utility, and use\n'
                '    postgresql-setup upgrade\n'
                'to upgrade it on the EL7 system.\n'
                'Otherwise please consult the documentation shipped with your '
                'PostgreSQL distribution.'
            ).format(
                v=client_v,
                db=which_db,
            ))
            raise RuntimeError(
                _(
                    'Please upgrade {db} PostgreSQL '
                    'server to {v} and retry.'
                ).format(
                    v=client_v,
                    db=which_db,
                )
            )

        self._verify_selinux_file_contexts()
        newpath = os.path.dirname(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
            ]
        )
        if (
            os.path.exists(newpath) and
            os.listdir(newpath)
        ) or os.path.islink(newpath):
            self.logger.error(
                _(
                    'A data directory for the new PostgreSQL instance '
                    'already exists, although the engine uses an older '
                    'version.\n'
                    'Please verify that it is not needed, remove it, and '
                    'then run Setup again.\n'
                    'Alternatively, you can upgrade PostgreSQL manually.\n'
                    'Location of the new data directory is:\n'
                    '{newpath}\n'
                ).format(
                    newpath=newpath,
                )
            )
            raise RuntimeError(
                _(
                    'Cannot upgrade PostgreSQL automatically - directory '
                    'of the new version already exists'
                )
            )
        instance_size = self.getInstanceSize()
        pgdata = self.getPGDATA()
        available_space = self.getPGDATAAvailableSpace(pgdata)
        upgrade_approved = dialog.queryBoolean(
            dialog=self.dialog,
            name='UPGRADE_DBMS',
            note=_(
                'This tool can automatically upgrade PostgreSQL. '
                'Automatically upgrade? (@VALUES@) [@DEFAULT@]: '
            ),
            prompt=True,
            default=True,
        )
        if not upgrade_approved:
            raise RuntimeError(
                _(
                    'Please upgrade {db} PostgreSQL '
                    'server to {v} and retry.'
                ).format(
                    v=client_v,
                    db=which_db,
                )
            )
        # Do not allow in-place upgrade, it's too risky. See also:
        # https://bugzilla.redhat.com/1492138
        upgrade_approved_inplace = False
        self.environment[
            oengcommcons.ProvisioningEnv.PG_UPGRADE_INPLACE
        ] = upgrade_approved_inplace
        if upgrade_approved_inplace:
            upgrade_approved_cleanupold = False
            self.logger.warning(_(
                'PostgreSQL will be upgraded in place, '
                'automatic rollback on failure will not be possible.'
            ))
        else:
            if instance_size > available_space:
                raise RuntimeError(_(
                    "Insufficient free space to migrate PostgreSQL: "
                    "required {r}, available {a}"
                ).format(
                    r=self._HumanReadableSize(instance_size),
                    a=self._HumanReadableSize(available_space),
                ))
            upgrade_approved_cleanupold = dialog.queryBoolean(
                dialog=self.dialog,
                name='UPGRADE_DBMS_CLEANUPOLD',
                note=_(
                    'PostgreSQL will be upgraded by copying its data '
                    'to a new directory.\n'
                    'Do you want to automatically clean up the old data '
                    'directory on success to reclaim its space ({s})? '
                    '(@VALUES@) [@DEFAULT@]: '
                ).format(
                    s=self._HumanReadableSize(instance_size),
                ),
                prompt=True,
                default=True,
            )

        self.environment[
            oengcommcons.ProvisioningEnv.PG_UPGRADE_CLEANOLD
        ] = upgrade_approved_cleanupold
        self.logger.info(_(
            'Any further action on the DB will be performed only '
            'after PostgreSQL has been successfully upgraded to 9.5.'
        ))
        return (
            upgrade_approved,
            upgrade_approved_inplace,
            upgrade_approved_cleanupold,
        )

    def setDefaultPrivilegesReadOnlyForUser(self, user):
        # TODO: Reconsider grantReadOnlyAccessToUser:
        # Unite them? Keep only one?
        # grantReadOnlyAccessToUser was buggy, bz 2026358.
        # It's ran during provisioning, requires postgres.
        # Current can work also with remote db.
        # grantReadOnlyAccessToUser is used by both dwh/grafana and
        # provisiondb, latter only for restoring grafana. Perhaps it's
        # not needed there either, so can be completely removed.
        owner = _ind_env(self, DEK.USER)
        db = _ind_env(self, DEK.DATABASE)
        statement = Statement(
            environment=self.environment,
            dbenvkeys=self._dbenvkeys,
        )
        res = statement.execute(
            statement=('SELECT defaclacl FROM pg_default_acl'),
            ownConnection=True,
        )
        # In current 4.4, this returns:
        # [{'defaclacl': '{ovirt_engine_history_grafana=r/postgres}'}]
        expected = f'{{{user}=r/{owner}}}'
        if expected in (x['defaclacl'] for x in res):
            self.logger.debug(f'User {user} already has default privileges')
            return
        self.logger.info(_(f'Allowing {user} to read data on {db}'))
        statement.execute(
            statement=(
                'ALTER DEFAULT PRIVILEGES '
                f'FOR USER {owner} '
                'IN SCHEMA public '
                f'GRANT SELECT ON TABLES TO {user}'
            ),
            ownConnection=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
