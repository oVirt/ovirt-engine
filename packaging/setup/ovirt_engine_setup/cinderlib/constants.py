#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Cinderlib Constants."""


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
    # This keeps cinderlib database credentials, so that the engine
    # can access it.
    OVIRT_ENGINE_SERVICE_CONFIG_CINDERLIB_DATABASE = os.path.join(
        oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-cinderlib-database.conf',
    )

    # This keeps a variable for indicating whether cinderlib database is
    # configured, so that the engine can access it.
    OVIRT_ENGINE_SERVICE_CONFIG_CINDERLIB_MISC = os.path.join(
        oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIGD,
        '10-setup-cinderlib.conf',
    )


@util.export
class Defaults(object):
    DEFAULT_CINDERLIB_DB_HOST = 'localhost'
    DEFAULT_CINDERLIB_DB_PORT = 5432
    DEFAULT_CINDERLIB_DB_DATABASE = 'ovirt_cinderlib'
    DEFAULT_CINDERLIB_DB_USER = 'ovirt_cinderlib'
    DEFAULT_CINDERLIB_DB_PASSWORD = ''
    DEFAULT_CINDERLIB_DB_SECURED = False
    DEFAULT_CINDERLIB_DB_SECURED_HOST_VALIDATION = False
    DEFAULT_CINDERLIB_DB_DUMPER = 'pg_custom'
    DEFAULT_CINDERLIB_DB_RESTORE_JOBS = 2
    DEFAULT_CINDERLIB_DB_FILTER = None


@util.export
class Stages(object):
    POSTGRES_CL_PROVISIONING_ALLOWED = \
        'osetup.cinderlib.provisioning.pgsql.allow'
    DB_CL_CONNECTION_SETUP = 'osetup.cinderlib.db.connection.setup'
    DB_CL_CONNECTION_CUSTOMIZATION = \
        'osetup.cinderlib.db.connection.customization'
    DB_CL_CREDENTIALS_AVAILABLE_EARLY = \
        'osetup.cinderlib.db.connection.credentials.early'
    DB_CL_CREDENTIALS_AVAILABLE_LATE = \
        'osetup.cinderlib.db.connection.credentials.late'
    DB_CL_SCHEMA = 'osetup.cinderlib.db.schema'
    CL_CONNECTION_ALLOW = 'osetup.cinderlib.db.connection.allow'
    DB_CL_CONNECTION_AVAILABLE = 'osetup.cinderlib.db.connection.available'


@util.export
class Const(object):
    @classproperty
    def CINDERLIB_DB_ENV_KEYS(self):
        return {
            DEK.HOST: CinderlibDBEnv.HOST,
            DEK.PORT: CinderlibDBEnv.PORT,
            DEK.SECURED: CinderlibDBEnv.SECURED,
            DEK.HOST_VALIDATION: CinderlibDBEnv.SECURED_HOST_VALIDATION,
            DEK.USER: CinderlibDBEnv.USER,
            DEK.PASSWORD: CinderlibDBEnv.PASSWORD,
            DEK.DATABASE: CinderlibDBEnv.DATABASE,
            DEK.CONNECTION: CinderlibDBEnv.CONNECTION,
            DEK.PGPASSFILE: CinderlibDBEnv.PGPASS_FILE,
            DEK.NEW_DATABASE: CinderlibDBEnv.NEW_DATABASE,
            DEK.NEED_DBMSUPGRADE: CinderlibDBEnv.NEED_DBMSUPGRADE,
            DEK.DUMPER: CinderlibDBEnv.DUMPER,
            DEK.FILTER: CinderlibDBEnv.FILTER,
            DEK.RESTORE_JOBS: CinderlibDBEnv.RESTORE_JOBS,
        }

    @classproperty
    def DEFAULT_CINDERLIB_DB_ENV_KEYS(self):
        return {
            DEK.HOST: Defaults.DEFAULT_CINDERLIB_DB_HOST,
            DEK.PORT: Defaults.DEFAULT_CINDERLIB_DB_PORT,
            DEK.SECURED: Defaults.DEFAULT_CINDERLIB_DB_SECURED,
            DEK.HOST_VALIDATION:
                Defaults.DEFAULT_CINDERLIB_DB_SECURED_HOST_VALIDATION,
            DEK.USER: Defaults.DEFAULT_CINDERLIB_DB_USER,
            DEK.PASSWORD: Defaults.DEFAULT_CINDERLIB_DB_PASSWORD,
            DEK.DATABASE: Defaults.DEFAULT_CINDERLIB_DB_DATABASE,
            DEK.DUMPER: Defaults.DEFAULT_CINDERLIB_DB_DUMPER,
            DEK.FILTER: Defaults.DEFAULT_CINDERLIB_DB_FILTER,
            DEK.RESTORE_JOBS: Defaults.DEFAULT_CINDERLIB_DB_RESTORE_JOBS,
        }


@util.export
@util.codegen
@osetupattrsclass
class CinderlibDBEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('CinderLib database host'),
    )
    def HOST(self):
        return 'OVESETUP_CL_DB/host'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('CinderLib database port'),
    )
    def PORT(self):
        return 'OVESETUP_CL_DB/port'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('CinderLib database secured connection'),
    )
    def SECURED(self):
        return 'OVESETUP_CL_DB/secured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('CinderLib database host name validation'),
    )
    def SECURED_HOST_VALIDATION(self):
        return 'OVESETUP_CL_DB/securedHostValidation'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('CinderLib database name'),
    )
    def DATABASE(self):
        return 'OVESETUP_CL_DB/database'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('CinderLib database user name'),
    )
    def USER(self):
        return 'OVESETUP_CL_DB/user'

    @osetupattrs(
        answerfile=True,
        answerfile_condition=lambda env: not env.get(
            ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ),
        is_secret=True,
    )
    def PASSWORD(self):
        return 'OVESETUP_CL_DB/password'

    CONNECTION = 'OVESETUP_CL_DB/connection'
    STATEMENT = 'OVESETUP_CL_DB/statement'
    PGPASS_FILE = 'OVESETUP_CL_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_CL_DB/newDatabase'
    NEED_DBMSUPGRADE = 'OVESETUP_CL_DB/needDBMSUpgrade'

    @osetupattrs(
        answerfile=True,
    )
    def DUMPER(self):
        return 'OVESETUP_CL_DB/dumper'

    @osetupattrs(
        answerfile=True,
    )
    def FILTER(self):
        return 'OVESETUP_CL_DB/filter'

    @osetupattrs(
        answerfile=True,
    )
    def RESTORE_JOBS(self):
        return 'OVESETUP_CL_DB/restoreJobs'

    @osetupattrs(
        answerfile=True,
    )
    def CL_VACUUM_FULL(self):
        return 'OVESETUP_CL_DB/engineVacuumFull'


@util.export
@util.codegen
@osetupattrsclass
class CoreEnv(object):

    @osetupattrs(
        answerfile=True,
        description=_('Set up Cinderlib integration'),
        postinstallfile=True,
        reconfigurable=True,
        summary=True,
    )
    def ENABLE(self):
        return 'OVESETUP_CL_DB/enable'


@util.export
@util.codegen
@osetupattrsclass
class ProvisioningEnv(object):
    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure local CinderLib database'),
    )
    def POSTGRES_PROVISIONING_ENABLED(self):
        return 'OVESETUP_CL_PROVISIONING/postgresProvisioningEnabled'


# vim: expandtab tabstop=4 shiftwidth=4
