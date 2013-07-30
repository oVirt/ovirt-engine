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


"""Local Postgres plugin."""


import os
import platform
import re
import random
import datetime
import string
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import transaction
from otopi import filetransaction
from otopi import plugin
from otopi import constants as otopicons


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database
from ovirt_engine_setup import dialog
from ovirt_engine_setup import util as osetuputil


@util.export
class Plugin(plugin.PluginBase):
    """Local Postgres plugin."""

    _RE_POSTGRES_PGHBA_LOCAL = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            (?P<host>local)
            \s+
            .*
            \s+
            (?P<param>\w+)
            $
        """
    )
    _RE_POSTGRES_MAX_CONN = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            \s*
            max_connections
            \s*
            =
            .*
            $
        """
    )

    class _alternateUser(object):
        def __init__(self, user):
            self._user = osetuputil.getUid(user)

        def __enter__(self):
            os.seteuid(self._user)

        def __exit__(self, exc_type, exc_value, traceback):
            os.seteuid(os.getuid())

    def _generatePassword(self):
        return '%s%s' % (
            ''.join([random.choice(string.digits) for i in range(4)]),
            ''.join([random.choice(string.letters) for i in range(4)]),
        )

    def _initDB(self):
        self.logger.info(_('Initializing PostgreSQL'))

        setup = self.command.get(
            command='postgresql-setup',
            optional=True
        )
        if setup is not None:
            # new method (post-systemd)
            self.execute(
                (
                    setup,
                    'initdb',
                ),
            )
        else:
            # old method (pre-systemd)
            self.execute(
                (
                    os.path.join(
                        osetupcons.FileLocations.SYSCONFDIR,
                        'init.d',
                        self.environment[
                            osetupcons.ProvisioningEnv.POSTGRES_SERVICE
                        ],
                    ),
                    'initdb',
                ),
            )

    def _updateMaxConnections(
        self,
        transaction,
        filename,
        maxconn,
    ):
        content = []
        with open(filename, 'r') as f:
            for line in f.read().splitlines():
                if self._RE_POSTGRES_MAX_CONN.match(line) is not None:
                    line = 'max_connections = {maxconn}'.format(
                        maxconn=maxconn,
                    )
                content.append(line)

        transaction.append(
            filetransaction.FileTransaction(
                name=filename,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            ),
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(filename)

    def _setPgHbaLocalPeer(
        self,
        transaction,
        filename,
    ):
        content = []
        with open(filename, 'r') as f:
            for line in f.read().splitlines():
                matcher = self._RE_POSTGRES_PGHBA_LOCAL.match(line)
                if matcher is not None:
                    line = line.replace(
                        matcher.group('param'),
                        'ident',  # we cannot use peer <psql-9
                    )
                content.append(line)

        transaction.append(
            filetransaction.FileTransaction(
                name=filename,
                content=content,
                visibleButUnsafe=True,
            )
        )

    def _addPgHbaDatabaseAccess(
        self,
        transaction,
        filename,
    ):
        content = []
        with open(filename, 'r') as f:
            for line in f.read().splitlines():
                content.append(line)

                # order is important, add after local
                # so we be first
                if line.lstrip().startswith('local'):
                    # we cannot use all for address <psql-9
                    for address in ('0.0.0.0/0', '::0/0'):
                        content.append(
                            (
                                '{host:7} '
                                '{user:15} '
                                '{database:15} '
                                '{address:23} '
                                '{auth}'
                            ).format(
                                host='host',
                                user=self.environment[
                                    osetupcons.DBEnv.USER
                                ],
                                database=self.environment[
                                    osetupcons.DBEnv.DATABASE
                                ],
                                address=address,
                                auth='md5',
                            )
                        )

        transaction.append(
            filetransaction.FileTransaction(
                name=filename,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(filename)

    def _setDatabaseResources(self):
        existing = False

        dbstatement = database.Statement(self.environment)
        hasDatabase = dbstatement.execute(
            statement="""
                select count(*) as count
                from pg_database
                where datname = %(database)s
            """,
            args=dict(
                database=self.environment[
                    osetupcons.DBEnv.DATABASE
                ],
            ),
            host='',    # use uscock
            user='postgres',
            password='',
            database='template1',
            ownConnection=True,
            transaction=False,
        )[0]['count'] != 0
        hasUser = dbstatement.execute(
            statement="""
                select count(*) as count
                from pg_user
                where usename = %(user)s
            """,
            args=dict(
                user=self.environment[
                    osetupcons.DBEnv.USER
                ],
            ),
            host='',    # use uscock
            user='postgres',
            password='',
            database='template1',
            ownConnection=True,
            transaction=False,
        )[0]['count'] != 0

        if hasDatabase and hasUser:
            try:
                dbovirtutils = database.OvirtUtils(plugin=self)
                if not dbovirtutils.isNewDatabase(
                    host='',
                    user='postgres',
                ):
                    raise RuntimeError(
                        _('Not new database')
                    )
                self.logger.debug('Using existing empty database')
                existing = True
            except RuntimeError:
                self.logger.debug(
                    'Error while trying to connect to existing database'
                )

        if not existing and (hasDatabase or hasUser):
            suffix = '_%s' % datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            self.environment[osetupcons.DBEnv.DATABASE] += suffix
            self.environment[osetupcons.DBEnv.USER] += suffix
            self._renamedDBResources = True

        return existing

    def _performDatabase(
        self,
        op,
        user,
        password,
        databaseName,
    ):
        statements = [
            (
                """
                    {op} role {user}
                    with
                        login
                        encrypted password %(password)s
                """
            ).format(
                op=op,
                user=user,
            ),

            (
                """
                    {op} database {database}
                    owner {to} {user}
                """
            ).format(
                op=op,
                to='to' if op == 'alter' else '',
                database=databaseName,
                user=user,
            ),
        ]

        dbstatement = database.Statement(self.environment)
        for statement in statements:
            dbstatement.execute(
                statement=statement,
                args=dict(
                    password=password,
                ),
                host='',    # use uscock
                user='postgres',
                password='',
                database='template1',
                ownConnection=True,
                transaction=False,
            )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._renamedDBResources = False
        self._distribution = platform.linux_distribution(
            full_distribution_name=0
        )[0]

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED,
            None
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_CONF,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_CONF
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_PG_HBA,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_HBA
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_PG_VERSION,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_VERSION
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_SERVICE,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_SERVICE
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_MAX_CONN,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_MAX_CONN
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        after=(
            osetupcons.Stages.DB_CONNECTION_SETUP,
        ),
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            self.environment[
                osetupcons.DBEnv.NEW_DATABASE
            ]
        ),
    )
    def _setup(self):
        self.command.detect('postgresql-setup')

        self._enabled = self._distribution in ('redhat', 'fedora', 'centos')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_DATABASE,
            osetupcons.Stages.DB_CONNECTION_CUSTOMIZATION,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        if self.environment[
            osetupcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ] is None:
            local = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_PROVISIONING_POSTGRES_LOCATION',
                note=_(
                    'Where is the database located? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Local'),
                false=_('Remote'),
                default=True,
            )
            if local:
                self.environment[osetupcons.DBEnv.HOST] = 'localhost'
                self.environment[
                    osetupcons.DBEnv.PORT
                ] = osetupcons.Defaults.DEFAULT_DB_PORT

                # TODO:
                # consider creating database and role
                # at engine_@RANDOM@
                self.environment[
                    osetupcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_PROVISIONING_POSTGRES_ENABLED',
                    note=_(
                        'Setup can configure the local postgresql server '
                        'automatically for engine to run. This may conflict '
                        'with existing applications.\n'
                        '\n'
                        'Would you like the setup to automatically configure '
                        'postgresql or prefer to perform that '
                        'manually? (@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    true=_('Automatic'),
                    false=_('Manual'),
                    default=True,
                )

        self._enabled = self.environment[
            osetupcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ]

        if self._enabled:

            self.environment[
                osetupcons.DBEnv.USER
            ] = osetupcons.Defaults.DEFAULT_DB_USER

            self.environment[
                osetupcons.DBEnv.PASSWORD
            ] = self._generatePassword()

            self.environment[otopicons.CoreEnv.LOG_FILTER].append(
                self.environment[
                    osetupcons.DBEnv.PASSWORD
                ]
            )

            self.environment[
                osetupcons.DBEnv.DATABASE
            ] = osetupcons.Defaults.DEFAULT_DB_DATABASE

            self.environment[
                osetupcons.DBEnv.SECURED
            ] = osetupcons.Defaults.DEFAULT_DB_SECURED

            self.environment[
                osetupcons.DBEnv.SECURED_HOST_VALIDATION
            ] = osetupcons.Defaults.DEFAULT_DB_SECURED_HOST_VALIDATION

            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
                {
                    'name': 'ovirt-postgres',
                    'directory': 'base'
                },
            ])

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        if not self.services.exists(
            name=self.environment[
                osetupcons.ProvisioningEnv.POSTGRES_SERVICE
            ]
        ):
            raise RuntimeError(
                _(
                    'Database configuration was requested, '
                    'however, postgresql service was not found. '
                    'This may happen because postgresql database '
                    'is not installed on system.'
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            osetupcons.Stages.DB_SCHEMA,
        ),
        after=(
            osetupcons.Stages.SYSTEM_SYSCTL_CONFIG_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc(self):

        if not os.path.exists(
            self.environment[
                osetupcons.ProvisioningEnv.POSTGRES_PG_VERSION
            ]
        ):
            self._initDB()

        self.logger.info(_('Creating PostgreSQL database'))
        localtransaction = transaction.Transaction()
        try:
            localtransaction.prepare()

            self._setPgHbaLocalPeer(
                transaction=localtransaction,
                filename=self.environment[
                    osetupcons.ProvisioningEnv.POSTGRES_PG_HBA
                ],
            )

            # restart to take effect
            for state in (False, True):
                self.services.state(
                    name=self.environment[
                        osetupcons.ProvisioningEnv.POSTGRES_SERVICE
                    ],
                    state=state,
                )

            with self._alternateUser(
                user=self.environment[
                    osetupcons.SystemEnv.USER_POSTGRES
                ],
            ):
                existing = self._setDatabaseResources()
                self._performDatabase(
                    op=(
                        'alter' if existing
                        else 'create'
                    ),
                    user=self.environment[
                        osetupcons.DBEnv.USER
                    ],
                    password=self.environment[
                        osetupcons.DBEnv.PASSWORD
                    ],
                    databaseName=self.environment[
                        osetupcons.DBEnv.DATABASE
                    ],
                )
        finally:
            # restore everything
            localtransaction.abort()

        self.logger.info(_('Configurating PostgreSQL'))
        localtransaction = transaction.Transaction()
        with localtransaction:
            self._addPgHbaDatabaseAccess(
                transaction=localtransaction,
                filename=self.environment[
                    osetupcons.ProvisioningEnv.POSTGRES_PG_HBA
                ],
            )

            self._updateMaxConnections(
                transaction=localtransaction,
                filename=self.environment[
                    osetupcons.ProvisioningEnv.POSTGRES_CONF
                ],
                maxconn=self.environment[
                    osetupcons.ProvisioningEnv.POSTGRES_MAX_CONN
                ],
            )

        # restart to take effect
        for state in (False, True):
            self.services.state(
                name=self.environment[
                    osetupcons.ProvisioningEnv.POSTGRES_SERVICE
                ],
                state=state,
            )

        self.services.startup(
            name=self.environment[
                osetupcons.ProvisioningEnv.POSTGRES_SERVICE
            ],
            state=True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._renamedDBResources,
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Database resources:\n'
                '    Database name:      {database}\n'
                '    Database user name: {user}\n'
            ).format(
                database=self.environment[
                    osetupcons.DBEnv.DATABASE
                ],
                user=self.environment[
                    osetupcons.DBEnv.USER
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
