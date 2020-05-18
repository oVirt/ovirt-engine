#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Constants."""


import gettext

from otopi import util

from ovirt_engine_setup.constants import classproperty
from ovirt_engine_setup.constants import osetupattrs
from ovirt_engine_setup.constants import osetupattrsclass
from ovirt_engine_setup.engine_common import constants as oengcommcons

DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Defaults(object):
    DEFAULT_DB_HOST = 'localhost'
    DEFAULT_DB_PORT = 5432
    DEFAULT_DB_DATABASE = ''
    DEFAULT_DB_USER = ''
    DEFAULT_DB_PASSWORD = ''
    DEFAULT_DB_SECURED = False
    DEFAULT_DB_SECURED_HOST_VALIDATION = False
    DEFAULT_DB_DUMPER = 'pg_custom'
    DEFAULT_DB_RESTORE_JOBS = 2
    DEFAULT_DB_FILTER = None
    DEFAULT_DB_INVALID_CONFIG_ITEMS = None


@util.export
@util.codegen
class Const(object):

    @classproperty
    def PROVISION_DB_ENV_KEYS(self):
        return {
            DEK.HOST: ProvDBEnv.HOST,
            DEK.PORT: ProvDBEnv.PORT,
            DEK.SECURED: ProvDBEnv.SECURED,
            DEK.HOST_VALIDATION: ProvDBEnv.SECURED_HOST_VALIDATION,
            DEK.USER: ProvDBEnv.USER,
            DEK.PASSWORD: ProvDBEnv.PASSWORD,
            DEK.DATABASE: ProvDBEnv.DATABASE,
            DEK.CONNECTION: ProvDBEnv.CONNECTION,
            DEK.PGPASSFILE: ProvDBEnv.PGPASS_FILE,
            DEK.NEW_DATABASE: ProvDBEnv.NEW_DATABASE,
            DEK.NEED_DBMSUPGRADE: ProvDBEnv.NEED_DBMSUPGRADE,
            DEK.DUMPER: ProvDBEnv.DUMPER,
            DEK.FILTER: ProvDBEnv.FILTER,
            DEK.RESTORE_JOBS: ProvDBEnv.RESTORE_JOBS,
        }

    @classproperty
    def DEFAULT_PROVISION_DB_ENV_KEYS(self):
        return {
            DEK.HOST: Defaults.DEFAULT_DB_HOST,
            DEK.PORT: Defaults.DEFAULT_DB_PORT,
            DEK.SECURED: Defaults.DEFAULT_DB_SECURED,
            DEK.HOST_VALIDATION: Defaults.DEFAULT_DB_SECURED_HOST_VALIDATION,
            DEK.USER: Defaults.DEFAULT_DB_USER,
            DEK.PASSWORD: Defaults.DEFAULT_DB_PASSWORD,
            DEK.DATABASE: Defaults.DEFAULT_DB_DATABASE,
            DEK.DUMPER: Defaults.DEFAULT_DB_DUMPER,
            DEK.FILTER: Defaults.DEFAULT_DB_FILTER,
            DEK.RESTORE_JOBS: Defaults.DEFAULT_DB_RESTORE_JOBS,
            DEK.INVALID_CONFIG_ITEMS: Defaults.DEFAULT_DB_INVALID_CONFIG_ITEMS,
        }


@util.export
@util.codegen
@osetupattrsclass
class ProvDBEnv(object):

    HOST = 'OVESETUP_PROVISION_DB/host'
    PORT = 'OVESETUP_PROVISION_DB/port'
    SECURED = 'OVESETUP_PROVISION_DB/secured'
    SECURED_HOST_VALIDATION = 'OVESETUP_PROVISION_DB/securedHostValidation'
    DATABASE = 'OVESETUP_PROVISION_DB/database'
    USER = 'OVESETUP_PROVISION_DB/user'

    @osetupattrs(
        is_secret=True,
    )
    def PASSWORD(self):
        return 'OVESETUP_PROVISION_DB/password'

    CONNECTION = 'OVESETUP_PROVISION_DB/connection'
    STATEMENT = 'OVESETUP_PROVISION_DB/statement'
    PGPASS_FILE = 'OVESETUP_PROVISION_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_PROVISION_DB/newDatabase'
    NEED_DBMSUPGRADE = 'OVESETUP_PROVISION_DB/needDBMSUpgrade'
    DUMPER = 'OVESETUP_PROVISION_DB/dumper'
    FILTER = 'OVESETUP_PROVISION_DB/filter'
    RESTORE_JOBS = 'OVESETUP_PROVISION_DB/restoreJobs'


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):
    PROVISION_DB = 'OVESETUP_PROVISIONDB_CONFIG/provisionDb'
    PROVISION_USER = 'OVESETUP_PROVISIONDB_CONFIG/provisionUser'
    ADD_TO_PG_HBA = 'OVESETUP_PROVISIONDB_CONFIG/addToPGHBA'
    GRANT_READONLY = 'OVESETUP_PROVISIONDB_CONFIG/grantReadOnly'

# vim: expandtab tabstop=4 shiftwidth=4
