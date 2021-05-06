#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import datetime
import gettext
import os
import random
import re
import shutil
import time

from otopi import base
from otopi import constants as otopicons
from otopi import filetransaction
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.provisiondb import constants as oprovisioncons

DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


def _ind_env(inst, keykey):
    return inst.environment[inst._dbenvkeys[keykey]]


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
                database=_ind_env(self, DEK.DATABASE),
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
                user=_ind_env(self, DEK.USER),
            ),
            ownConnection=True,
            transaction=False,
        )[0]['count'] != 0

        generate = hasDatabase or hasUser
        existing = False

        if hasDatabase and hasUser:
            dbovirtutils = database.OvirtUtils(
                plugin=self._plugin,
                dbenvkeys=self._dbenvkeys,
                environment=environment,
            )
            if dbovirtutils.isNewDatabase(
                database=_ind_env(self, DEK.DATABASE),
            ):
                self.logger.debug('Found empty database')
                generate = False
                existing = True
            else:
                generate = True

        if generate:
            self.logger.debug('Existing resources found, generating names')
            suffix = '_%s' % datetime.datetime.now().strftime('%Y%m%d%H%M%S')
            self.environment[self._dbenvkeys[DEK.DATABASE]] += suffix
            self.environment[self._dbenvkeys[DEK.USER]] += suffix
            self._renamedDBResources = True

        return existing

    def _userExists(self, environment):
        dbstatement = database.Statement(
            dbenvkeys=self._dbenvkeys,
            environment=environment,
        )
        hasUser = dbstatement.execute(
            statement="""
                select count(*) as count
                from pg_user
                where usename = %(user)s
            """,
            args=dict(
                user=_ind_env(self, DEK.USER),
            ),
            ownConnection=True,
            transaction=False,
        )[0]['count'] != 0

        return hasUser

    def _performDatabase(
        self,
        environment,
        op,
    ):
        statements = [
            (
                True,
                (
                    """
                        {op} role {user}
                        with
                            login
                            encrypted password %(password)s
                    """
                ).format(
                    op=op,
                    user=_ind_env(self, DEK.USER),
                ),
            ),
            (
                _ind_env(self, DEK.DATABASE),
                (
                    """
                        {op} database {database}
                        owner {to} {user}
                        {encoding}
                    """
                ).format(
                    op=op,
                    to='to' if op == 'alter' else '',
                    database=_ind_env(self, DEK.DATABASE),
                    user=_ind_env(self, DEK.USER),
                    encoding="""
                        template template0
                        encoding 'UTF8'
                        lc_collate 'en_US.UTF-8'
                        lc_ctype 'en_US.UTF-8'
                    """ if op != 'alter' else '',
                ),
            ),
        ]

        dbstatement = database.Statement(
            dbenvkeys=self._dbenvkeys,
            environment=environment,
        )
        for condition, statement in statements:
            if condition:
                dbstatement.execute(
                    statement=statement,
                    args=dict(
                        password=_ind_env(self, DEK.PASSWORD),
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
                        '--initdb',
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
    ):
        with open(
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_CONF
            ]
        ) as f:
            content = f.read().splitlines()

        dbovirtutils = database.OvirtUtils(
            plugin=self._plugin,
            dbenvkeys=self._dbenvkeys,
        )
        needUpdate, content = dbovirtutils.getUpdatedPGConf(content)

        if needUpdate:
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

    def addPgHbaDatabaseAccess(
        self,
        transaction,
    ):
        lines = [
            # we cannot use all for address <psql-9
            (
                '{host:7} '
                '{database:15} '
                '{user:15} '
                '{address:23} '
                '{auth}'
            ).format(
                host='host',
                user=_ind_env(self, DEK.USER),
                database=_ind_env(self, DEK.DATABASE),
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

    def restartPG(self):
        for state in (False, True):
            self.services.state(
                name=self.environment[
                    oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
                ],
                state=state,
            )
        # If using systemd, reset its service-restart counter, so that
        # we do not fail if restarting PG too much - which can happen
        # e.g. during engine-backup --mode=restore with lots of db entities
        # provisioned.
        systemctl = self.command.get(
            command='systemctl',
            optional=True
        )
        if systemctl is not None:
            self._plugin.execute(
                args=(
                    systemctl,
                    'reset-failed',
                    self.environment[
                        oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
                    ],
                ),
                raiseOnError=False,
            )

    def _waitForDatabase(self, environment=None):
        dbovirtutils = database.OvirtUtils(
            plugin=self._plugin,
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
        self.command.detect('psql')

    def supported(self):
        return osetuputil.is_ovirt_packaging_supported_distro()

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
        for k in DEK.DEFAULTS_KEYS:
            if self.environment.get(self._dbenvkeys[k]) is None:
                self.environment[self._dbenvkeys[k]] = self._defaults[k]
        if self.environment.get(self._dbenvkeys[DEK.PASSWORD]) is None:
            self.environment[
                self._dbenvkeys[DEK.PASSWORD]
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
                database=_ind_env(self, DEK.DATABASE),
            )
        )
        localtransaction = transaction.Transaction()
        try:
            localtransaction.prepare()

            self._setPgHbaLocalPeer(
                transaction=localtransaction,
            )

            self.restartPG()

            with AlternateUser(
                user=self.environment[
                    oengcommcons.SystemEnv.USER_POSTGRES
                ],
            ):
                usockenv = {
                    self._dbenvkeys[DEK.HOST]: '',  # usock
                    self._dbenvkeys[DEK.PORT]: '',
                    self._dbenvkeys[DEK.SECURED]: False,
                    self._dbenvkeys[DEK.HOST_VALIDATION]: False,
                    self._dbenvkeys[DEK.USER]: 'postgres',
                    self._dbenvkeys[DEK.PASSWORD]: '',
                    self._dbenvkeys[DEK.DATABASE]: 'template1',
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
            )
            self.addPgHbaDatabaseAccess(
                transaction=localtransaction,
            )

        self.services.startup(
            name=self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
            ],
            state=True,
        )

        self.restartPG()
        self._waitForDatabase()
        # We should install the UUID extension when a new db is created
        self.installUuidOsspExtension()

    def createUser(self):
        if not self.supported():
            raise RuntimeError(
                _(
                    'Unsupported distribution for '
                    'postgresql proisioning'
                )
            )

        self._initDbIfRequired()

        localtransaction = transaction.Transaction()
        try:
            localtransaction.prepare()

            self._setPgHbaLocalPeer(
                transaction=localtransaction,
            )

            self.restartPG()

            with AlternateUser(
                user=self.environment[
                    oengcommcons.SystemEnv.USER_POSTGRES
                ],
            ):
                usockenv = {
                    self._dbenvkeys[DEK.HOST]: '',  # usock
                    self._dbenvkeys[DEK.PORT]: '',
                    self._dbenvkeys[DEK.SECURED]: False,
                    self._dbenvkeys[DEK.HOST_VALIDATION]: False,
                    self._dbenvkeys[DEK.USER]: 'postgres',
                    self._dbenvkeys[DEK.PASSWORD]: '',
                    self._dbenvkeys[DEK.DATABASE]: 'template1',
                }
                self._waitForDatabase(
                    environment=usockenv,
                )
                perform_role_sql = (
                    """
                        {op} role {user}
                        with
                            login
                            encrypted password %(password)s
                    """
                ).format(
                    op=(
                        'alter'
                        if self._userExists(environment=usockenv)
                        else 'create'
                    ),
                    user=_ind_env(self, DEK.USER),
                )
                database.Statement(
                    dbenvkeys=self._dbenvkeys,
                    environment=usockenv,
                ).execute(
                    statement=perform_role_sql,
                    args=dict(
                        password=_ind_env(self, DEK.PASSWORD),
                    ),
                    ownConnection=True,
                    transaction=False,
                )
        finally:
            # restore everything
            localtransaction.abort()

        self.restartPG()

    def getConfigFiles(
        self,
    ):
        if not self.supported():
            raise RuntimeError(
                _(
                    'Unsupported distribution for automatic '
                    'upgrading postgresql'
                )
            )

        conf_f = {}

        with AlternateUser(
            user=self.environment[
                oengcommcons.SystemEnv.USER_POSTGRES
            ],
        ):
            usockenv = {
                self._dbenvkeys[DEK.HOST]: '',  # usock
                self._dbenvkeys[DEK.PORT]: '',
                self._dbenvkeys[DEK.SECURED]: False,
                self._dbenvkeys[DEK.HOST_VALIDATION]: False,
                self._dbenvkeys[DEK.USER]: 'postgres',
                self._dbenvkeys[DEK.PASSWORD]: '',
                self._dbenvkeys[DEK.DATABASE]: 'template1',
            }
            self._waitForDatabase(
                environment=usockenv,
            )

            dbstatement = database.Statement(
                dbenvkeys=self._dbenvkeys,
                environment=usockenv,
            )
            for f in [
                'config_file',
                'hba_file',
                'ident_file',
                'data_directory'
            ]:
                ret = dbstatement.execute(
                    statement="SHOW {f}".format(f=f),
                    ownConnection=True,
                    transaction=False,
                )
                conf_f[f] = ret[0][f]

        return conf_f

    # Install uuid-ossp extension on DB using DB admin role
    def installUuidOsspExtension(self):
        with AlternateUser(
            user=self.environment[
                oengcommcons.SystemEnv.USER_POSTGRES
            ],
        ):
            # uuid-ossp extension needs to be installed into engine db, but
            # only administrator user is allowed to perform this, so we need
            # to use 'postgres' user
            usockenv = {
                self._dbenvkeys[DEK.HOST]: '',  # usock
                self._dbenvkeys[DEK.PORT]: '',
                self._dbenvkeys[DEK.SECURED]: False,
                self._dbenvkeys[DEK.HOST_VALIDATION]: False,
                self._dbenvkeys[DEK.USER]: 'postgres',
                self._dbenvkeys[DEK.PASSWORD]: '',
                self._dbenvkeys[DEK.DATABASE]: _ind_env(self, DEK.DATABASE)
            }
            self._waitForDatabase(
                environment=usockenv,
            )
            dbstatement = database.Statement(
                dbenvkeys=self._dbenvkeys,
                environment=usockenv,
            )
            extensionInstalled = dbstatement.execute(
                statement="""
                    select count(*) as count
                    from pg_available_extensions
                    where name = 'uuid-ossp'
                    and installed_version IS NOT NULL
                """,
                args=None,
                ownConnection=True,
                transaction=False,
            )[0]['count'] != 0

            if (not extensionInstalled):
                dbstatement.execute(
                    statement="""
                        drop function if exists uuid_generate_v1()
                    """,
                    args=None,
                    ownConnection=True,
                    transaction=False,
                )
                dbstatement.execute(
                    statement="""
                        create extension "uuid-ossp"
                    """,
                    args=None,
                    ownConnection=True,
                    transaction=False,
                )

    def grantReadOnlyAccessToUser(self):
        with AlternateUser(
            user=self.environment[
                oengcommcons.SystemEnv.USER_POSTGRES
            ],
        ):
            usockenv = {
                self._dbenvkeys[DEK.HOST]: '',  # usock
                self._dbenvkeys[DEK.PORT]: '',
                self._dbenvkeys[DEK.SECURED]: False,
                self._dbenvkeys[DEK.HOST_VALIDATION]: False,
                self._dbenvkeys[DEK.USER]: 'postgres',
                self._dbenvkeys[DEK.PASSWORD]: '',
                self._dbenvkeys[DEK.DATABASE]: _ind_env(self, DEK.DATABASE)
            }
            self._waitForDatabase(
                environment=usockenv,
            )
            dbstatement = database.Statement(
                dbenvkeys=self._dbenvkeys,
                environment=usockenv,
            )
            dbstatement.execute(
                statement="""
                    GRANT CONNECT ON DATABASE {database} TO {user}
                """.format(
                    user=_ind_env(self, DEK.USER),
                    database=_ind_env(self, DEK.DATABASE),
                ),
                args=None,
                ownConnection=True,
                transaction=False,
            )
            dbstatement.execute(
                statement="""
                    GRANT USAGE ON SCHEMA public TO {user}
                """.format(
                    user=_ind_env(self, DEK.USER),
                ),
                args=None,
                ownConnection=True,
                transaction=False,
            )
            dbstatement.execute(
                statement="""
                    GRANT SELECT ON ALL TABLES IN SCHEMA public TO {user}
                """.format(
                    user=_ind_env(self, DEK.USER),
                ),
                args=None,
                ownConnection=True,
                transaction=False,
            )
            dbstatement.execute(
                statement="""
                    ALTER DEFAULT PRIVILEGES IN SCHEMA public
                    GRANT SELECT ON TABLES TO {user}
                """.format(
                    user=_ind_env(self, DEK.USER),
                ),
                args=None,
                ownConnection=True,
                transaction=False,
            )

    def getPostgresLocaleAndEncodingInitEnv(
        self,
    ):
        clivals = []
        if not self.supported():
            raise RuntimeError(
                _(
                    'Unsupported distribution for automatic '
                    'upgrading postgresql'
                )
            )

        with AlternateUser(
            user=self.environment[
                oengcommcons.SystemEnv.USER_POSTGRES
            ],
        ):
            usockenv = {
                self._dbenvkeys[DEK.HOST]: '',  # usock
                self._dbenvkeys[DEK.PORT]: '',
                self._dbenvkeys[DEK.SECURED]: False,
                self._dbenvkeys[DEK.HOST_VALIDATION]: False,
                self._dbenvkeys[DEK.USER]: 'postgres',
                self._dbenvkeys[DEK.PASSWORD]: '',
                self._dbenvkeys[DEK.DATABASE]: 'postgres',
            }
            self._waitForDatabase(
                environment=usockenv,
            )
            dbstatement = database.Statement(
                dbenvkeys=self._dbenvkeys,
                environment=usockenv,
            )
            entities_and_cli_options = {
                'server_encoding': '--encoding={val}',
                'lc_collate': '--lc-collate={val}',
                'lc_ctype': '--lc-ctype={val}',
            }
            for entity, cli_option in entities_and_cli_options.items():
                val = dbstatement.execute(
                    statement="SHOW {entity}".format(entity=entity),
                    ownConnection=True,
                    transaction=False,
                )[0][entity]
                clivals.append(cli_option.format(val=val))

        return {
            'PGSETUP_INITDB_OPTIONS': ' '.join(clivals),
        }


class DBMSUpgradeTransaction(transaction.TransactionElement):
    """dbms upgrade transaction element."""

    def __init__(
        self,
        parent,
        inplace=False,
        cleanupold=False,
        upgrade_from='postgresql',
    ):
        self._parent = parent
        self._inplace = inplace
        self._cleanupold = cleanupold
        self._upgrade_from = upgrade_from
        self._upgrade_to = self._parent.environment[
            oengcommcons.ProvisioningEnv.POSTGRES_SERVICE
        ]
        self._old_data_directory = None

    def __str__(self):
        return _("DBMS Upgrade Transaction")

    @property
    def command(self):
        return self._parent.command

    @property
    def environment(self):
        return self._parent.environment

    @property
    def services(self):
        return self._parent.services

    @property
    def logger(self):
        return self._parent.logger

    def prepare(self):

        provisioning = Provisioning(
            plugin=self._parent,
            dbenvkeys=oprovisioncons.Const.PROVISION_DB_ENV_KEYS,
            defaults=oprovisioncons.Const.DEFAULT_PROVISION_DB_ENV_KEYS,
        )

        conf_f = provisioning.getConfigFiles()
        envAppend = provisioning.getPostgresLocaleAndEncodingInitEnv()
        self._old_data_directory = conf_f['data_directory']
        self.services.state(
            name=self._upgrade_from,
            state=False,
        )
        if self._inplace:
            envAppend['PGSETUP_PGUPGRADE_OPTIONS'] = '--link'
        self._parent.execute(
            (
                self.command.get('postgresql-setup'),
                '--upgrade',
                '--upgrade-from={f}'.format(f=self._upgrade_from)
            ),
            envAppend=envAppend,
            raiseOnError=True,
        )
        shutil.copy2(
            conf_f['config_file'],
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_CONF
            ]
        )
        shutil.copy2(
            conf_f['hba_file'],
            self.environment[
                oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
            ]
        )
        self.logger.info(
            _(
                'PostgreSQL has been successfully upgraded, '
                'starting the new instance ({service}).'
            ).format(
                service=self._upgrade_to,
            )
        )
        self.services.state(
            name=self._upgrade_to,
            state=True,
        )

    def abort(self):
        if not self._inplace:
            self.logger.info(
                _(
                    'Rolling back to the previous PostgreSQL '
                    'instance ({service}).'
                ).format(
                    service=self._upgrade_from,
                )
            )
            self.services.state(
                name=self._upgrade_to,
                state=False,
            )
            self.services.state(
                name=self._upgrade_from,
                state=True,
            )
            shutil.rmtree(
                path=os.path.dirname(
                    self.environment[
                        oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA
                    ]
                ),
                ignore_errors=True,
            )
        else:
            self.logger.error(_(
                'FATAL: engine-setup failed and, '
                'since PostgreSQL has been upgraded in place, '
                'an automatic rollback of it is not possible.\n'
                'Please manually roll it back restoring a backup or '
                'a snapshot.'
            ))

    def commit(self):
        # TODO: mask the service if available in otopi
        self.services.startup(
            name=self._upgrade_from,
            state=False,
        )
        self.services.startup(
            name=self._upgrade_to,
            state=True,
        )
        if self._old_data_directory:
            if self._cleanupold:
                self.logger.info(
                    'Cleaning the previous PostgreSQL data directory'
                )
                shutil.rmtree(self._old_data_directory)
            else:
                if not self._inplace:
                    self.logger.info(
                        (
                            'The previous PostgreSQL configuration '
                            'and data are stored in folder {d}. '
                            'You can safely delete it.'
                        ).format(d=self._old_data_directory)
                    )

# vim: expandtab tabstop=4 shiftwidth=4
