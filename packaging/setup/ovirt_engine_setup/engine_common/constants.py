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


"""Constants."""


import os
import platform
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util

from . import config
from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.constants import classproperty
from ovirt_engine_setup.constants import osetupattrsclass
from ovirt_engine_setup.constants import osetupattrs


@util.export
class FileLocations(object):
    OVIRT_ENGINE_COMMON_DATADIR = config.ENGINE_COMMON_DATADIR

    JBOSS_HOME = os.path.join(
        osetupcons.FileLocations.DATADIR,
        'jboss-as',
    )


@util.export
class Defaults(object):
    DEFAULT_SYSTEM_USER_ROOT = 'root'
    DEFAULT_SYSTEM_USER_VDSM = 'vdsm'
    DEFAULT_SYSTEM_GROUP_KVM = 'kvm'
    DEFAULT_SYSTEM_USER_APACHE = 'apache'
    DEFAULT_SYSTEM_USER_POSTGRES = 'postgres'

    @classproperty
    def DEFAULT_SYSTEM_SHMMAX(self):
        SHMMAX = {
            'x86_64': 68719476736,
            'i686': 4294967295,
            'ppc64':  137438953472,
            'default': 4294967295,
        }
        return SHMMAX.get(platform.machine(), SHMMAX['default'])

    DEFAULT_DB_HOST = 'localhost'
    DEFAULT_DB_PORT = 5432
    DEFAULT_DB_DATABASE = 'engine'
    DEFAULT_DB_USER = 'engine'
    DEFAULT_DB_PASSWORD = ''
    DEFAULT_DB_SECURED = False
    DEFAULT_DB_SECURED_HOST_VALIDATION = False

    DEFAULT_PKI_COUNTRY = 'US'
    DEFAULT_PKI_STORE_PASS = 'mypass'

    DEFAULT_NETWORK_HTTP_PORT = 80
    DEFAULT_NETWORK_HTTPS_PORT = 443
    DEFAULT_NETWORK_JBOSS_HTTP_PORT = 8080
    DEFAULT_NETWORK_JBOSS_HTTPS_PORT = 8443
    DEFAULT_NETWORK_JBOSS_AJP_PORT = 8702
    DEFAULT_NETWORK_JBOSS_DEBUG_ADDRESS = '127.0.0.1:8787'

    DEFAULT_HTTPD_SERVICE = 'httpd'

    DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR = os.path.join(
        osetupcons.FileLocations.LOCALSTATEDIR,
        'lib',
        'pgsql',
        'data',
    )

    DEFAULT_POSTGRES_PROVISIONING_PG_CONF = os.path.join(
        DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR,
        'postgresql.conf',
    )

    DEFAULT_POSTGRES_PROVISIONING_PG_HBA = os.path.join(
        DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR,
        'pg_hba.conf',
    )

    DEFAULT_POSTGRES_PROVISIONING_PG_VERSION = os.path.join(
        DEFAULT_POSTGRES_PROVISIONING_PGDATA_DIR,
        'PG_VERSION',
    )

    DEFAULT_POSTGRES_PROVISIONING_SERVICE = 'postgresql'
    DEFAULT_POSTGRES_PROVISIONING_MAX_CONN = 150
    DEFAULT_POSTGRES_PROVISIONING_LISTEN_ADDRESS = "'*'"


@util.export
class Stages(object):
    CORE_ENGINE_START = 'osetup.core.engine.start'

    DB_CONNECTION_SETUP = 'osetup.db.connection.setup'
    DB_CONNECTION_CUSTOMIZATION = 'osetup.db.connection.customization'
    DB_CONNECTION_STATUS = 'osetup.db.connection.status'
    DB_CREDENTIALS_AVAILABLE_EARLY = 'osetup.db.connection.credentials.early'
    DB_CREDENTIALS_AVAILABLE_LATE = 'osetup.db.connection.credentials.late'
    DB_CONNECTION_AVAILABLE = 'osetup.db.connection.available'
    DB_SCHEMA = 'osetup.db.schema'

    CONFIG_DB_ENCRYPTION_AVAILABLE = 'osetup.config.encryption.available'

    DIALOG_TITLES_S_ALLINONE = 'osetup.dialog.titles.allinone.start'
    DIALOG_TITLES_S_APACHE = 'osetup.dialog.titles.apache.start'
    DIALOG_TITLES_S_DATABASE = 'osetup.dialog.titles.database.start'
    DIALOG_TITLES_S_PKI = 'osetup.dialog.titles.pki.start'
    DIALOG_TITLES_E_ALLINONE = 'osetup.dialog.titles.allinone.end'
    DIALOG_TITLES_E_APACHE = 'osetup.dialog.titles.apache.end'
    DIALOG_TITLES_E_DATABASE = 'osetup.dialog.titles.database.end'
    DIALOG_TITLES_E_PKI = 'osetup.dialog.titles.pki.end'

    RENAME_PKI_CONF_MISC = 'osetup.rename.pki.conf.misc'


@util.export
@util.codegen
class Const(object):

    @classproperty
    def ENGINE_DB_ENV_KEYS(self):
        return {
            'host': EngineDBEnv.HOST,
            'port': EngineDBEnv.PORT,
            'secured': EngineDBEnv.SECURED,
            'hostValidation': EngineDBEnv.SECURED_HOST_VALIDATION,
            'user': EngineDBEnv.USER,
            'password': EngineDBEnv.PASSWORD,
            'database': EngineDBEnv.DATABASE,
            'connection': EngineDBEnv.CONNECTION,
            'pgpassfile': EngineDBEnv.PGPASS_FILE,
        }


@util.export
@util.codegen
@osetupattrsclass
class EngineDBEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database host'),
    )
    def HOST(self):
        return 'OVESETUP_DB/host'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database port'),
    )
    def PORT(self):
        return 'OVESETUP_DB/port'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database secured connection'),
    )
    def SECURED(self):
        return 'OVESETUP_DB/secured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database host name validation'),
    )
    def SECURED_HOST_VALIDATION(self):
        return 'OVESETUP_DB/securedHostValidation'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database name'),
    )
    def DATABASE(self):
        return 'OVESETUP_DB/database'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Engine database user name'),
    )
    def USER(self):
        return 'OVESETUP_DB/user'

    @osetupattrs(
        answerfile=True,
    )
    def PASSWORD(self):
        return 'OVESETUP_DB/password'

    CONNECTION = 'OVESETUP_DB/connection'
    STATEMENT = 'OVESETUP_DB/statement'
    PGPASS_FILE = 'OVESETUP_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_DB/newDatabase'

    @osetupattrs(
        answerfile=True,
    )
    def FIX_DB_VIOLATIONS(self):
        return 'OVESETUP_DB/fixDbViolations'


@util.export
@util.codegen
@osetupattrsclass
class SystemEnv(object):

    USER_APACHE = 'OVESETUP_SYSTEM/userApache'
    USER_POSTGRES = 'OVESETUP_SYSTEM/userPostgres'
    USER_ROOT = 'OVESETUP_SYSTEM/userRoot'
    USER_VDSM = 'OVESETUP_SYSTEM/userVdsm'
    GROUP_KVM = 'OVESETUP_SYSTEM/groupKvm'

    SHMMAX = 'OVESETUP_SYSTEM/shmmax'


@util.export
@util.codegen
@osetupattrsclass
class ConfigEnv(object):

    JAVA_HOME = 'OVESETUP_CONFIG/javaHome'
    JBOSS_HOME = 'OVESETUP_CONFIG/jbossHome'

    PUBLIC_HTTP_PORT = 'OVESETUP_CONFIG/publicHttpPort'  # internal use
    PUBLIC_HTTPS_PORT = 'OVESETUP_CONFIG/publicHttpsPort'  # internal use
    HTTP_PORT = 'OVESETUP_CONFIG/httpPort'
    HTTPS_PORT = 'OVESETUP_CONFIG/httpsPort'
    JBOSS_HTTP_PORT = 'OVESETUP_CONFIG/jbossHttpPort'
    JBOSS_HTTPS_PORT = 'OVESETUP_CONFIG/jbossHttpsPort'
    JBOSS_AJP_PORT = 'OVESETUP_CONFIG/jbossAjpPort'
    JBOSS_DIRECT_HTTP_PORT = 'OVESETUP_CONFIG/jbossDirectHttpPort'
    JBOSS_DIRECT_HTTPS_PORT = 'OVESETUP_CONFIG/jbossDirectHttpsPort'
    JBOSS_DEBUG_ADDRESS = 'OVESETUP_CONFIG/jbossDebugAddress'


@util.export
@util.codegen
@osetupattrsclass
class ProvisioningEnv(object):

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure local Engine database'),
    )
    def POSTGRES_PROVISIONING_ENABLED(self):
        return 'OVESETUP_PROVISIONING/postgresProvisioningEnabled'

    POSTGRES_CONF = 'OVESETUP_PROVISIONING/postgresConf'
    POSTGRES_PG_HBA = 'OVESETUP_PROVISIONING/postgresPgHba'
    POSTGRES_PG_VERSION = 'OVESETUP_PROVISIONING/postgresPgVersion'
    POSTGRES_SERVICE = 'OVESETUP_PROVISIONING/postgresService'
    POSTGRES_MAX_CONN = 'OVESETUP_PROVISIONING/postgresMaxConn'
    POSTGRES_LISTEN_ADDRESS = 'OVESETUP_PROVISIONING/postgresListenAddress'


@util.export
@util.codegen
@osetupattrsclass
class ApacheEnv(object):
    HTTPD_SERVICE = 'OVESETUP_APACHE/httpdService'

    NEED_RESTART = 'OVESETUP_APACHE/needRestart'


# vim: expandtab tabstop=4 shiftwidth=4
