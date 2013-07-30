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


import psycopg2


from otopi import constants as otopicons
from otopi import util
from otopi import transaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup import database
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
            connection = self._parent.environment[osetupcons.DBEnv.CONNECTION]
            if connection is not None:
                connection.rollback()
                self._parent.environment[osetupcons.DBEnv.CONNECTION] = None

        def commit(self):
            connection = self._parent.environment[osetupcons.DBEnv.CONNECTION]
            if connection is not None:
                connection.commit()

    def _checkDbEncoding(self, environment):

        statement = database.Statement(environment=environment)
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
                    'Encoding of the engine database is {encoding}. '
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
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.DBTransaction(self)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DB_CONNECTION_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_DATABASE,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
    )
    def _customization(self):
        dbovirtutils = database.OvirtUtils(plugin=self)

        interactive = None in (
            self.environment[osetupcons.DBEnv.HOST],
            self.environment[osetupcons.DBEnv.PORT],
            self.environment[osetupcons.DBEnv.DATABASE],
            self.environment[osetupcons.DBEnv.USER],
            self.environment[osetupcons.DBEnv.PASSWORD],
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
                    "create user engine password 'engine';\n"
                    "create database engine owner engine;\n"
                    "\n"
                    "Make sure that database can be accessed remotely.\n"
                    "\n"
                ),
            )
        else:
            self.dialog.note(
                text=_('Using existing credentials'),
            )

        connectionValid = False
        while not connectionValid:
            host = self.environment[osetupcons.DBEnv.HOST]
            port = self.environment[osetupcons.DBEnv.PORT]
            secured = self.environment[osetupcons.DBEnv.SECURED]
            securedHostValidation = self.environment[
                osetupcons.DBEnv.SECURED_HOST_VALIDATION
            ]
            db = self.environment[osetupcons.DBEnv.DATABASE]
            user = self.environment[osetupcons.DBEnv.USER]
            password = self.environment[osetupcons.DBEnv.PASSWORD]

            if host is None:
                while True:
                    host = self.dialog.queryString(
                        name='OVESETUP_DB_HOST',
                        note=_('Database host [@DEFAULT@]: '),
                        prompt=True,
                        default=osetupcons.Defaults.DEFAULT_DB_HOST,
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
                                name='OVESETUP_DB_PORT',
                                note=_('Database port [@DEFAULT@]: '),
                                prompt=True,
                                default=osetupcons.Defaults.DEFAULT_DB_PORT,
                            )
                        )
                        break  # do while missing in python
                    except ValueError:
                        pass

            if secured is None:
                secured = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_DB_SECURED',
                    note=_(
                        'Database secured connection (@VALUES@) '
                        '[@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=osetupcons.Defaults.DEFAULT_DB_SECURED,
                )

            if not secured:
                securedHostValidation = False

            if securedHostValidation is None:
                securedHostValidation = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_DB_SECURED_HOST_VALIDATION',
                    note=_(
                        'Validate host name in secured connection (@VALUES@) '
                        '[@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                ) == 'yes'

            if db is None:
                db = self.dialog.queryString(
                    name='OVESETUP_DB_DATABASE',
                    note=_('Database name [@DEFAULT@]: '),
                    prompt=True,
                    default=osetupcons.Defaults.DEFAULT_DB_DATABASE,
                )

            if user is None:
                user = self.dialog.queryString(
                    name='OVESETUP_DB_USER',
                    note=_('Database user [@DEFAULT@]: '),
                    prompt=True,
                    default=osetupcons.Defaults.DEFAULT_DB_USER,
                )

            if password is None:
                password = self.dialog.queryString(
                    name='OVESETUP_DB_PASSWORD',
                    note=_('Database password: '),
                    prompt=True,
                    hidden=True,
                )

                self.environment[otopicons.CoreEnv.LOG_FILTER].append(password)

            dbenv = {
                osetupcons.DBEnv.HOST: host,
                osetupcons.DBEnv.PORT: port,
                osetupcons.DBEnv.SECURED: secured,
                osetupcons.DBEnv.SECURED_HOST_VALIDATION: (
                    securedHostValidation
                ),
                osetupcons.DBEnv.USER: user,
                osetupcons.DBEnv.PASSWORD: password,
                osetupcons.DBEnv.DATABASE: db,
            }

            if interactive:
                try:
                    dbovirtutils.tryDatabaseConnect(dbenv)
                    self._checkDbEncoding(dbenv)
                    self.environment.update(dbenv)
                    connectionValid = True
                except RuntimeError as e:
                    self.logger.error(
                        _('Cannot connect to database: {error}').format(
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
                osetupcons.DBEnv.NEW_DATABASE
            ] = dbovirtutils.isNewDatabase()
        except:
            self.logger.debug('database connection failed', exc_info=True)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.DB_CONNECTION_AVAILABLE,
        after=(
            osetupcons.Stages.DB_SCHEMA,
        ),
    )
    def _connection(self):
        # must be here as we do not have database at validation
        self.environment[
            osetupcons.DBEnv.CONNECTION
        ] = psycopg2.connect(
            host=self.environment[osetupcons.DBEnv.HOST],
            port=self.environment[osetupcons.DBEnv.PORT],
            user=self.environment[osetupcons.DBEnv.USER],
            password=self.environment[osetupcons.DBEnv.PASSWORD],
            database=self.environment[osetupcons.DBEnv.DATABASE],
        )
        self.environment[
            osetupcons.DBEnv.STATEMENT
        ] = database.Statement(
            environment=self.environment,
        )


# vim: expandtab tabstop=4 shiftwidth=4
