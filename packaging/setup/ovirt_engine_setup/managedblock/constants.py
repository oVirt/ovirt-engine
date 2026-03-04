#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Managed Block Constants."""


import gettext
import os

from otopi import util

from ovirt_engine_setup.constants import classproperty
from ovirt_engine_setup.constants import osetupattrs
from ovirt_engine_setup.constants import osetupattrsclass
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class FileLocations(object):
    # This keeps managed block database credentials, so that the engine
    # can access it.
    OVIRT_ENGINE_SERVICE_CONFIG_MANAGEDBLOCK_DATABASE = os.path.join(
        oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-managedblock-database.conf',
    )
    # Legacy name of the managed block database configuration file.
    OVIRT_ENGINE_SERVICE_CONFIG_LEGACY_CINDERLIB_DATABASE = os.path.join(
        oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-cinderlib-database.conf',
    )

    # This keeps a variable for indicating whether managed block database is
    # configured, so that the engine can access it.
    OVIRT_ENGINE_SERVICE_CONFIG_MANAGEDBLOCK_MISC = os.path.join(
        oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-managedblock.conf',
    )
    # Legacy name of the managed block misc configuration file.
    OVIRT_ENGINE_SERVICE_CONFIG_LEGACY_CINDERLIB_MISC = os.path.join(
        oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-cinderlib.conf',
    )


@util.export
class Defaults(object):
    DEFAULT_MANAGEDBLOCK_DB_HOST = 'localhost'
    DEFAULT_MANAGEDBLOCK_DB_PORT = 5432
    DEFAULT_MANAGEDBLOCK_DB_DATABASE = 'ovirt_managedblock'
    DEFAULT_MANAGEDBLOCK_DB_USER = 'ovirt_managedblock'
    DEFAULT_MANAGEDBLOCK_DB_PASSWORD = ''
    DEFAULT_MANAGEDBLOCK_DB_SECURED = False
    DEFAULT_MANAGEDBLOCK_DB_SECURED_HOST_VALIDATION = False
    DEFAULT_MANAGEDBLOCK_DB_DUMPER = 'pg_custom'
    DEFAULT_MANAGEDBLOCK_DB_RESTORE_JOBS = 2
    DEFAULT_MANAGEDBLOCK_DB_FILTER = None


@util.export
class Stages(object):
    POSTGRES_MB_PROVISIONING_ALLOWED = \
        'osetup.managedblock.provisioning.pgsql.allow'
    DB_MB_CONNECTION_SETUP = 'osetup.managedblock.db.connection.setup'
    CONFIG_CINDERLIB_RENAME = 'osetup.managedblock.config.cinderlib_rename'
    DB_MB_CONNECTION_CUSTOMIZATION = \
        'osetup.managedblock.db.connection.customization'
    DB_MB_CREDENTIALS_AVAILABLE_EARLY = \
        'osetup.managedblock.db.connection.credentials.early'
    DB_MB_CREDENTIALS_AVAILABLE_LATE = \
        'osetup.managedblock.db.connection.credentials.late'
    DB_MB_SCHEMA = 'osetup.managedblock.db.schema'
    MB_CONNECTION_ALLOW = 'osetup.managedblock.db.connection.allow'
    DB_MB_CONNECTION_AVAILABLE = 'osetup.managedblock.db.connection.available'


@util.export
class Const(object):
    @classproperty
    def MANAGEDBLOCK_DB_ENV_KEYS(self):
        return {
            DEK.HOST: ManagedBlockDBEnv.HOST,
            DEK.PORT: ManagedBlockDBEnv.PORT,
            DEK.SECURED: ManagedBlockDBEnv.SECURED,
            DEK.HOST_VALIDATION: ManagedBlockDBEnv.SECURED_HOST_VALIDATION,
            DEK.USER: ManagedBlockDBEnv.USER,
            DEK.PASSWORD: ManagedBlockDBEnv.PASSWORD,
            DEK.DATABASE: ManagedBlockDBEnv.DATABASE,
            DEK.CONNECTION: ManagedBlockDBEnv.CONNECTION,
            DEK.PGPASSFILE: ManagedBlockDBEnv.PGPASS_FILE,
            DEK.NEW_DATABASE: ManagedBlockDBEnv.NEW_DATABASE,
            DEK.NEED_DBMSUPGRADE: ManagedBlockDBEnv.NEED_DBMSUPGRADE,
            DEK.DUMPER: ManagedBlockDBEnv.DUMPER,
            DEK.FILTER: ManagedBlockDBEnv.FILTER,
            DEK.RESTORE_JOBS: ManagedBlockDBEnv.RESTORE_JOBS,
            DEK.CREDS_Q_NAME_FUNC: managedblock_question_name,
        }

    @classproperty
    def DEFAULT_MANAGEDBLOCK_DB_ENV_KEYS(self):
        return {
            DEK.HOST: Defaults.DEFAULT_MANAGEDBLOCK_DB_HOST,
            DEK.PORT: Defaults.DEFAULT_MANAGEDBLOCK_DB_PORT,
            DEK.SECURED: Defaults.DEFAULT_MANAGEDBLOCK_DB_SECURED,
            DEK.HOST_VALIDATION:
                Defaults.DEFAULT_MANAGEDBLOCK_DB_SECURED_HOST_VALIDATION,
            DEK.USER: Defaults.DEFAULT_MANAGEDBLOCK_DB_USER,
            DEK.PASSWORD: Defaults.DEFAULT_MANAGEDBLOCK_DB_PASSWORD,
            DEK.DATABASE: Defaults.DEFAULT_MANAGEDBLOCK_DB_DATABASE,
            DEK.DUMPER: Defaults.DEFAULT_MANAGEDBLOCK_DB_DUMPER,
            DEK.FILTER: Defaults.DEFAULT_MANAGEDBLOCK_DB_FILTER,
            DEK.RESTORE_JOBS: Defaults.DEFAULT_MANAGEDBLOCK_DB_RESTORE_JOBS,
        }


def managedblock_question_name(what):
    return f'OVESETUP_MANAGEDBLOCK_DB_{what.upper()}'


@util.export
@util.codegen
@osetupattrsclass
class ManagedBlockDBEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Managed block database host'),
    )
    def HOST(self):
        return 'OVESETUP_MB_DB/host'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Managed block database port'),
    )
    def PORT(self):
        return 'OVESETUP_MB_DB/port'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Managed block database secured connection'),
    )
    def SECURED(self):
        return 'OVESETUP_MB_DB/secured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Managed block database host name validation'),
    )
    def SECURED_HOST_VALIDATION(self):
        return 'OVESETUP_MB_DB/securedHostValidation'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Managed block database name'),
    )
    def DATABASE(self):
        return 'OVESETUP_MB_DB/database'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Managed block database user name'),
    )
    def USER(self):
        return 'OVESETUP_MB_DB/user'

    @osetupattrs(
        answerfile=True,
        answerfile_condition=lambda env: not env.get(
            ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ),
        is_secret=True,
        asked_on=(managedblock_question_name(DEK.PASSWORD),),
    )
    def PASSWORD(self):
        return 'OVESETUP_MB_DB/password'

    CONNECTION = 'OVESETUP_MB_DB/connection'
    STATEMENT = 'OVESETUP_MB_DB/statement'
    PGPASS_FILE = 'OVESETUP_MB_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_MB_DB/newDatabase'
    NEED_DBMSUPGRADE = 'OVESETUP_MB_DB/needDBMSUpgrade'

    @osetupattrs(
        answerfile=True,
    )
    def DUMPER(self):
        return 'OVESETUP_MB_DB/dumper'

    @osetupattrs(
        answerfile=True,
    )
    def FILTER(self):
        return 'OVESETUP_MB_DB/filter'

    @osetupattrs(
        answerfile=True,
    )
    def RESTORE_JOBS(self):
        return 'OVESETUP_MB_DB/restoreJobs'

    @osetupattrs(
        answerfile=True,
    )
    def MB_VACUUM_FULL(self):
        return 'OVESETUP_MB_DB/engineVacuumFull'


@util.export
@util.codegen
@osetupattrsclass
class CoreEnv(object):

    @osetupattrs(
        answerfile=True,
        description=_('Set up managed block integration'),
        postinstallfile=True,
        reconfigurable=True,
        summary=True,
    )
    def ENABLE(self):
        return 'OVESETUP_MB_DB/enable'

    @osetupattrs(
        description=_(
            'Legacy name for managed block integration, '
            'used to trigger migration'
        ),
        postinstallfile=True,
    )
    def CINDERLIB_ENABLE(self):
        return 'OVESETUP_CL_DB/enable'


@util.export
@util.codegen
@osetupattrsclass
class ProvisioningEnv(object):
    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure local managed block database'),
    )
    def POSTGRES_PROVISIONING_ENABLED(self):
        return 'OVESETUP_MB_PROVISIONING/postgresProvisioningEnabled'


# vim: expandtab tabstop=4 shiftwidth=4
