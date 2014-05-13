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


"""Connection plugin."""


import socket
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import transaction
from otopi import plugin


from ovirt_engine_setup.engine_common \
    import enginecommonconstants as oengcommcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    class DBTransaction(transaction.TransactionElement):
        """yum transaction element."""

        def __init__(self, parent):
            self._parent = parent

        def __str__(self):
            return _("Database Transaction")

        def prepare(self):
            pass

        def abort(self):
            connection = self._parent.environment[
                oengcommcons.EngineDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.rollback()
                self._parent.environment[
                    oengcommcons.EngineDBEnv.CONNECTION
                ] = None

        def commit(self):
            connection = self._parent.environment[
                oengcommcons.EngineDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.commit()

    def _checkDbEncoding(self, environment):

        statement = database.Statement(
            dbenvkeys=oengcommcons.Const.ENGINE_DB_ENV_KEYS,
            environment=environment,
        )
        encoding = statement.execute(
            statement="""
                show server_encoding
            """,
            ownConnection=True,
            transaction=False,
        )[0]['server_encoding']
        if encoding.lower() != 'utf8':
            raise RuntimeError(
                _(
                    'Encoding of the Engine database is {encoding}. '
                    'Engine installation is only supported on servers '
                    'with default encoding set to UTF8. Please fix the '
                    'default DB encoding before you continue'
                ).format(
                    encoding=encoding,
                )
            )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.DBTransaction(self)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oengcommcons.Stages.DB_CONNECTION_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
    )
    def _customization(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oengcommcons.Const.ENGINE_DB_ENV_KEYS,
        )

        interactive = None in (
            self.environment[oengcommcons.EngineDBEnv.HOST],
            self.environment[oengcommcons.EngineDBEnv.PORT],
            self.environment[oengcommcons.EngineDBEnv.DATABASE],
            self.environment[oengcommcons.EngineDBEnv.USER],
            self.environment[oengcommcons.EngineDBEnv.PASSWORD],
        )

        if interactive:
            self.dialog.note(
                text=_(
                    "\n"
                    "ATTENTION\n"
                    "\n"
                    "Manual action required.\n"
                    "Please create database for ovirt-engine use. "
                    "Use the following commands as an example:\n"
                    "\n"
                    "create role {user} with login encrypted password '{user}'"
                    ";\n"
                    "create database {database} owner {user}\n"
                    " template template0\n"
                    " encoding 'UTF8' lc_collate 'en_US.UTF-8'\n"
                    " lc_ctype 'en_US.UTF-8';\n"
                    "\n"
                    "Make sure that database can be accessed remotely.\n"
                    "\n"
                ).format(
                    user=oengcommcons.Defaults.DEFAULT_DB_USER,
                    database=oengcommcons.Defaults.DEFAULT_DB_DATABASE,
                ),
            )

        connectionValid = False
        while not connectionValid:
            host = self.environment[oengcommcons.EngineDBEnv.HOST]
            port = self.environment[oengcommcons.EngineDBEnv.PORT]
            secured = self.environment[oengcommcons.EngineDBEnv.SECURED]
            securedHostValidation = self.environment[
                oengcommcons.EngineDBEnv.SECURED_HOST_VALIDATION
            ]
            db = self.environment[oengcommcons.EngineDBEnv.DATABASE]
            user = self.environment[oengcommcons.EngineDBEnv.USER]
            password = self.environment[oengcommcons.EngineDBEnv.PASSWORD]

            if host is None:
                while True:
                    host = self.dialog.queryString(
                        name='OVESETUP_ENGINE_DB_HOST',
                        note=_('Engine database host [@DEFAULT@]: '),
                        prompt=True,
                        default=oengcommcons.Defaults.DEFAULT_DB_HOST,
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
                                name='OVESETUP_ENGINE_DB_PORT',
                                note=_('Engine database port [@DEFAULT@]: '),
                                prompt=True,
                                default=oengcommcons.Defaults.DEFAULT_DB_PORT,
                            )
                        )
                        break  # do while missing in python
                    except ValueError:
                        pass

            if secured is None:
                secured = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_ENGINE_DB_SECURED',
                    note=_(
                        'Engine database secured connection (@VALUES@) '
                        '[@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=oengcommcons.Defaults.DEFAULT_DB_SECURED,
                )

            if not secured:
                securedHostValidation = False

            if securedHostValidation is None:
                securedHostValidation = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_ENGINE_DB_SECURED_HOST_VALIDATION',
                    note=_(
                        'Engine database host name validation in secured '
                        'connection (@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                ) == 'yes'

            if db is None:
                db = self.dialog.queryString(
                    name='OVESETUP_ENGINE_DB_DATABASE',
                    note=_('Engine database name [@DEFAULT@]: '),
                    prompt=True,
                    default=oengcommcons.Defaults.DEFAULT_DB_DATABASE,
                )

            if user is None:
                user = self.dialog.queryString(
                    name='OVESETUP_ENGINE_DB_USER',
                    note=_('Engine database user [@DEFAULT@]: '),
                    prompt=True,
                    default=oengcommcons.Defaults.DEFAULT_DB_USER,
                )

            if password is None:
                password = self.dialog.queryString(
                    name='OVESETUP_ENGINE_DB_PASSWORD',
                    note=_('Engine database password: '),
                    prompt=True,
                    hidden=True,
                )

            dbenv = {
                oengcommcons.EngineDBEnv.HOST: host,
                oengcommcons.EngineDBEnv.PORT: port,
                oengcommcons.EngineDBEnv.SECURED: secured,
                oengcommcons.EngineDBEnv.SECURED_HOST_VALIDATION: (
                    securedHostValidation
                ),
                oengcommcons.EngineDBEnv.USER: user,
                oengcommcons.EngineDBEnv.PASSWORD: password,
                oengcommcons.EngineDBEnv.DATABASE: db,
            }

            if interactive:
                try:
                    dbovirtutils.tryDatabaseConnect(dbenv)
                    self._checkDbEncoding(dbenv)
                    self.environment.update(dbenv)
                    connectionValid = True
                except RuntimeError as e:
                    self.logger.error(
                        _('Cannot connect to Engine database: {error}').format(
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
                oengcommcons.EngineDBEnv.NEW_DATABASE
            ] = dbovirtutils.isNewDatabase()
        except:
            self.logger.debug('database connection failed', exc_info=True)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        after=(
            oengcommcons.Stages.DB_SCHEMA,
        ),
    )
    def _connection(self):
        self.environment[
            oengcommcons.EngineDBEnv.STATEMENT
        ] = database.Statement(
            dbenvkeys=oengcommcons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        # must be here as we do not have database at validation
        self.environment[
            oengcommcons.EngineDBEnv.CONNECTION
        ] = self.environment[oengcommcons.EngineDBEnv.STATEMENT].connect()


# vim: expandtab tabstop=4 shiftwidth=4
