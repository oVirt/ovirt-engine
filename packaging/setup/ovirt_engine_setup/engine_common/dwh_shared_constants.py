#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2016 Red Hat, Inc.
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
# TODO: check if we can do better, now sync again with dwh

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
@util.codegen
@osetupattrsclass
class CoreEnv(object):
    """Sync with ovirt-dwh"""
    ENABLE = 'OVESETUP_DWH_CORE/enable'


@util.export
@util.codegen
class Const(object):
    @classproperty
    def DWH_DB_ENV_KEYS(self):
        return {
            DEK.HOST: DWHDBEnv.HOST,
            DEK.PORT: DWHDBEnv.PORT,
            DEK.SECURED: DWHDBEnv.SECURED,
            DEK.HOST_VALIDATION: DWHDBEnv.SECURED_HOST_VALIDATION,
            DEK.USER: DWHDBEnv.USER,
            DEK.PASSWORD: DWHDBEnv.PASSWORD,
            DEK.DATABASE: DWHDBEnv.DATABASE,
            DEK.CONNECTION: DWHDBEnv.CONNECTION,
            DEK.PGPASSFILE: DWHDBEnv.PGPASS_FILE,
            DEK.NEW_DATABASE: DWHDBEnv.NEW_DATABASE,
            DEK.DUMPER: DWHDBEnv.DUMPER,
            DEK.FILTER: DWHDBEnv.FILTER,
            DEK.RESTORE_JOBS: DWHDBEnv.RESTORE_JOBS,
        }

    SERVICE_NAME = 'ovirt-engine-dwhd'


@util.export
@util.codegen
@osetupattrsclass
class DWHDBEnv(object):
    """Sync with ovirt-dwh"""

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('DWH database host'),
    )
    def HOST(self):
        return 'OVESETUP_DWH_DB/host'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('DWH database port'),
    )
    def PORT(self):
        return 'OVESETUP_DWH_DB/port'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('DWH database secured connection'),
    )
    def SECURED(self):
        return 'OVESETUP_DWH_DB/secured'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('DWH database host name validation'),
    )
    def SECURED_HOST_VALIDATION(self):
        return 'OVESETUP_DWH_DB/securedHostValidation'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('DWH database name'),
    )
    def DATABASE(self):
        return 'OVESETUP_DWH_DB/database'

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('DWH database user name'),
    )
    def USER(self):
        return 'OVESETUP_DWH_DB/user'

    @osetupattrs(
        answerfile=True,
        answerfile_condition=lambda env: not env.get(
            DWHProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        )
    )
    def PASSWORD(self):
        return 'OVESETUP_DWH_DB/password'

    @osetupattrs(
        answerfile=True,
    )
    def DUMPER(self):
        return 'OVESETUP_DWH_DB/dumper'

    @osetupattrs(
        answerfile=True,
    )
    def FILTER(self):
        return 'OVESETUP_DWH_DB/filter'

    @osetupattrs(
        answerfile=True,
    )
    def RESTORE_JOBS(self):
        return 'OVESETUP_DWH_DB/restoreJobs'

    CONNECTION = 'OVESETUP_DWH_DB/connection'
    STATEMENT = 'OVESETUP_DWH_DB/statement'
    PGPASS_FILE = 'OVESETUP_DWH_DB/pgPassFile'
    NEW_DATABASE = 'OVESETUP_DWH_DB/newDatabase'


@util.export
@util.codegen
@osetupattrsclass
class DWHProvisioningEnv(object):
    """Sync with ovirt-dwh"""

    @osetupattrs(
        answerfile=True,
        summary=True,
        description=_('Configure local DWH database'),
    )
    def POSTGRES_PROVISIONING_ENABLED(self):
        return 'OVESETUP_DWH_PROVISIONING/postgresProvisioningEnabled'


# vim: expandtab tabstop=4 shiftwidth=4
