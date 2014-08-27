#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2014 Red Hat, Inc.
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


import datetime
import os
import platform
import random
import re
import time
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import base
from otopi import util
from otopi import transaction
from otopi import filetransaction
from otopi import constants as otopicons


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common \
    import constants as oengcommcons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup import util as osetuputil


@util.export
class AlternateUser(object):
    def __init__(self, user):
        self._user = osetuputil.getUid(user)

    def __enter__(self):
        os.seteuid(self._user)

    def __exit__(self, exc_type, exc_value, traceback):
        os.seteuid(os.getuid())


@util.export
class Provisioning(base.Base):

    _PASSWORD_CHARS = (
        '0123456789' +
        'ABCDEFGHIJKLMNOPQRSTUVWXYZ' +
        'abcdefghijklmnopqrstuvwxyz'
    )

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
        """,
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

    @property
    def environment(self):
        return self._plugin.environment

    @property
    def command(self):
        return self._plugin.command

    @property
    def services(self):
        return self._plugin.services

    @property
    def databaseRenamed(self):
        return self._renamedDBResources

    def _setDatabaseResources(self, environment):
        dbstatement = database.Statement(
            dbenvkeys=self._dbenvkeys,
            environment=environment,
        )
        hasDatabase = dbstatement.execute(
            statement="""
                select count(*) as count
                from pg_database
                where datname = %(database)s
            """,
            args=dict(
                database=self.environment[
                    self._dbenvkeys['database']
                ],
            ),
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
                    self._dbenvkeys['user']
                ],
            ),
            ownConnection=True,
            transaction=False,
        )[0]['count'] != 0

        generate = hasDatabase or hasUser
        existing = False

        if hasDatabase and hasUser:
            dbovirtutils = database.OvirtUtils(
                plugin=self,
                dbenvkeys=self._dbenvkeys,
                environment=environment,
            )
            if dbovirtutils.isNewDatabase(
                database=self.environment[
                    self._dbenvkeys['database']
                ],
            ):
                self.logger.debug('Found empty database')
                generate = False
                existing = True
            else:
                generate = True

        if generate:
            self.logger.debug('Existing resources found, generating names')
            suffix = '_%s' % datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            self.environment[self._dbenvkeys['database']] += suffix
            self.environment[self._dbenvkeys['user']] += suffix
            self._renamedDBResources = True

        return existing

    def _performDatabase(
        self,
        environment,
        op,
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
                user=self.environment[
                    self._dbenvkeys['user']
                ],
            ),

            (
                """
                    {op} database {database}
                    owner {to} {user}
                    {encoding}
                """
            ).format(
                op=op,
                to='to' if op == 'alter' else '',
                database=self.environment[
                    self._dbenvkeys['database']
                ],
                user=self.environment[
                    self._dbenvkeys['user']
                ],
                encoding="""
                    template template0
                    encoding 'UTF8'
                    lc_collate 'en_US.UTF-8'
                    lc_ctype 'en_US.UTF-8'
                """ if op != 'alter' else '',
            ),
        ]

        dbstatement = database.Statement(
            dbenvkeys=self._dbenvkeys,
            environment=environment,
        )
        for statement in statements:
            dbstatement.execute(
                statement=statement,
                args=dict(
                    password=self.environment[
                        self._dbenvkeys['password']
                    ],
                ),
                ownConnection=True,
                transaction=False,
            )

    def _initDbIfRequired(self):
        if not os.path.exists(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_PG_VERSION
            ]
        ):
            self.logger.info(_('Initializing PostgreSQL'))

            setup = self.command.get(
                command='postgresql-setup',
                optional=True
            )
            if setup is not None:
                # new method (post-systemd)
                self._plugin.execute(
                    (
                        setup,
                        'initdb',
                    ),
                )
            else:
                # old method (pre-systemd)
                self._plugin.execute(
                    (
                        os.path.join(
                            osetupcons.FileLocations.SYSCONFDIR,
                            'init.d',
                            self.environment[
                                oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
                            ],
                        ),
                        'initdb',
                    ),
                )

    def _updatePostgresConf(
        self,
        transaction,
        maxconn,
        listenaddr,
    ):
        with open(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_CONF
            ]
        ) as f:
            content = f.read().splitlines()

        needUpdate = True
        maxConnOK = False
        listenAddrOK = False
        for l in content:
            m = self._RE_KEY_VALUE.match(l)
            if m is not None:
                if m.group('key') == 'max_connections':
                    if int(m.group('value')) >= int(maxconn):
                        maxConnOK = True
                    else:
                        break
                elif m.group('key') == 'listen_addresses':
                    if m.group('value') == listenaddr:
                        listenAddrOK = True
                    else:
                        break
            if (maxConnOK and listenAddrOK):
                needUpdate = False
                break

        if needUpdate:
            content = osetuputil.editConfigContent(
                content=content,
                params={
                    'max_connections': maxconn,
                    'listen_addresses': listenaddr
                },
            )

            transaction.append(
                filetransaction.FileTransaction(
                    name=self.environment[
                        oengcommcons.ProvisioningEnv.POSTGRES_CONF
                    ],
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                ),
            )
            self.environment[
                osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
            ].append(
                self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_CONF
                ]
            )

    def _setPgHbaLocalPeer(
        self,
        transaction,
    ):
        content = []
        with open(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
            ]
        ) as f:
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
                name=self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
                ],
                content=content,
                visibleButUnsafe=True,
            )
        )

    def _addPgHbaDatabaseAccess(
        self,
        transaction,
    ):
        lines = [
            # we cannot use all for address <psql-9
            (
                '{host:7} '
                '{user:15} '
                '{database:15} '
                '{address:23} '
                '{auth}'
            ).format(
                host='host',
                user=self.environment[
                    self._dbenvkeys['user']
                ],
                database=self.environment[
                    self._dbenvkeys['database']
                ],
                address=address,
                auth='md5',
            )
            for address in ('0.0.0.0/0', '::0/0')
        ]

        content = []
        with open(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
            ]
        ) as f:
            for line in f.read().splitlines():
                if line not in lines:
                    content.append(line)

                # order is important, add after local
                # so we be first
                if line.lstrip().startswith('local'):
                    content.extend(lines)

        transaction.append(
            filetransaction.FileTransaction(
                name=self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
                ],
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
            ]
        )

    def _restart(self):
            for state in (False, True):
                self.services.state(
                    name=self.environment[
                        oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
                    ],
                    state=state,
                )

    def _waitForDatabase(self, environment=None):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=self._dbenvkeys,
        )
        for i in range(60):
            try:
                self.logger.debug('Attempting to connect database')
                dbovirtutils.tryDatabaseConnect(environment=environment)
                break
            except RuntimeError:
                self.logger.debug(
                    'Database connection failed',
                    exc_info=True,
                )
                time.sleep(1)
            except Exception:
                self.logger.debug(
                    'Database connection failed, unknown exception',
                    exc_info=True,
                )
                raise

    def __init__(
        self,
        plugin,
        dbenvkeys,
        defaults,
    ):
        super(Provisioning, self).__init__()
        self._plugin = plugin
        self._dbenvkeys = dbenvkeys
        self._defaults = defaults
        self._renamedDBResources = False

    def detectCommands(self):
        self.command.detect('postgresql-setup')

    def supported(self):
        return platform.linux_distribution(
            full_distribution_name=0
        )[0] in ('redhat', 'fedora', 'centos')

    def validate(self):
        if not self.services.exists(
            name=self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
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

    def generatePassword(self):
        rand = random.SystemRandom()
        return ''.join([rand.choice(self._PASSWORD_CHARS) for i in range(22)])

    def applyEnvironment(self):
        for k in ('user', 'database', 'port', 'secured', 'hostValidation'):
            if self.environment[self._dbenvkeys[k]] is None:
                self.environment[self._dbenvkeys[k]] = self._defaults[k]
        if self.environment[self._dbenvkeys['password']] is None:
            self.environment[
                self._dbenvkeys['password']
            ] = self.generatePassword()

    def provision(self):
        if not self.supported():
            raise RuntimeError(
                _(
                    'Unsupported distribution for '
                    'postgresql proisioning'
                )
            )

        self._initDbIfRequired()

        self.logger.info(
            _("Creating PostgreSQL '{database}' database").format(
                database=self.environment[self._dbenvkeys['database']],
            )
        )
        localtransaction = transaction.Transaction()
        try:
            localtransaction.prepare()

            self._setPgHbaLocalPeer(
                transaction=localtransaction,
            )

            self._restart()

            with AlternateUser(
                user=self.environment[
                    oengcommcons.SystemEnv.USER_POSTGRES
                ],
            ):
                usockenv = {
                    self._dbenvkeys['host']: '',  # usock
                    self._dbenvkeys['port']: '',
                    self._dbenvkeys['secured']: False,
                    self._dbenvkeys['hostValidation']: False,
                    self._dbenvkeys['user']: 'postgres',
                    self._dbenvkeys['password']: '',
                    self._dbenvkeys['database']: 'template1',
                }
                self._waitForDatabase(
                    environment=usockenv,
                )
                existing = self._setDatabaseResources(
                    environment=usockenv,
                )
                self._performDatabase(
                    environment=usockenv,
                    op=(
                        'alter' if existing
                        else 'create'
                    ),
                )
        finally:
            # restore everything
            localtransaction.abort()

        self.logger.info(_('Configuring PostgreSQL'))
        with transaction.Transaction() as localtransaction:
            self._updatePostgresConf(
                transaction=localtransaction,
                maxconn=self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_MAX_CONN
                ],
                listenaddr=self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_LISTEN_ADDRESS
                ],
            )
            self._addPgHbaDatabaseAccess(
                transaction=localtransaction,
            )

        self.services.startup(
            name=self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
            ],
            state=True,
        )

        self._restart()
        self._waitForDatabase()


# vim: expandtab tabstop=4 shiftwidth=4
