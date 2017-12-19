#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2017 Red Hat, Inc.
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


import gettext

from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.engine_common import postgres
from ovirt_engine_setup.engine_common.constants import ProvisioningEnv

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Database configuration update plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._provisioning = postgres.Provisioning(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            defaults=oenginecons.Const.DEFAULT_ENGINE_DB_ENV_KEYS,
        )

    def _setupOwnsDB(self):
        # FIXME localhost is inappropriate in case of docker e.g
        # we need a deterministic notion of local/remote pg_host in sense of
        # 'we own postgres' or not.
        return self.environment[oenginecons.EngineDBEnv.HOST] == 'localhost'

    def _suggestAutoFixing(self):
        autofix_approved = dialog.queryBoolean(
            dialog=self.dialog,
            name='UPGRADE_PG_CONF_ENGINE',
            note=_(
                'The database requires these configurations values to be '
                'changed. Setup can fix them for you or abort.'
                ' Fix automatically? ('
                '@VALUES@) [@DEFAULT@]: '
            ),
            prompt=True,
            default=True,
        )
        if autofix_approved:
            self.environment[
                oenginecons.EngineDBEnv.FIX_DB_CONFIGURATION
            ] = True
        else:
            raise RuntimeError(_('Aborted by user'))

    def _pg_conf_items(self):
        return (
            {
                'key': 'autovacuum_vacuum_scale_factor',
                'expected': self.environment[
                    ProvisioningEnv.PG_AUTOVACUUM_VACUUM_SCALE_FACTOR
                ],
                'ok': lambda key, current, expected: (
                    float(current) <= float(expected)
                ),
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': '{specific}'.format(
                    specific='%s' % database.AT_MOST_EXPECTED,
                )
            },
            {
                'key': 'autovacuum_analyze_scale_factor',
                'expected': self.environment[
                    ProvisioningEnv.PG_AUTOVACUUM_ANALYZE_SCALE_FACTOR
                ],
                'ok': lambda key, current, expected: (
                    float(current) <= float(expected)
                ),
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': '{specific}'.format(
                    specific=database.AT_MOST_EXPECTED,
                )
            },
            {
                'key': 'autovacuum_max_workers',
                'expected': self.environment[
                    ProvisioningEnv.PG_AUTOVACUUM_MAX_WORKERS
                ],
                'ok': lambda key, current, expected: (
                    int(current) >= int(expected)
                ),
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': '{specific}'.format(
                    specific=database.AT_LEAST_EXPECTED,
                )
            },
            {
                'key': 'maintenance_work_mem',
                'expected': self.environment[
                    ProvisioningEnv.PG_AUTOVACUUM_MAINTENANCE_WORK_MEM
                ],
                'useQueryForValue': True,
                'ok': lambda key, current, expected: (
                    int(current) >= int(expected)
                ),
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': '{specific}'.format(
                    specific='%s' % database.AT_LEAST_EXPECTED,
                )
            },
            {
                'key': 'work_mem',
                'expected': self.environment[
                    ProvisioningEnv.PG_WORK_MEM_KB
                ],
                'useQueryForValue': True,
                'ok': lambda key, current, expected: (
                    int(current) >= int(expected)
                ),
                'check_on_use': True,
                'needed_on_create': True,
                'error_msg': '{specific}'.format(
                    specific='%s' % database.AT_LEAST_EXPECTED,
                )
            },
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.EngineDBEnv.FIX_DB_CONFIGURATION,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.INVALID_CONFIG_ITEMS,
            None
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_EXTRA_CONFIG_ITEMS,
            ()
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            oengcommcons.ProvisioningEnv.POSTGRES_EXTRA_CONFIG_ITEMS
        ] += self._pg_conf_items()

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
        ),
        after=(
            oengcommcons.Stages.DB_CONNECTION_CUSTOMIZATION,
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
    )
    def _customization(self):
        invalid_config_items = self.environment[
            oenginecons.EngineDBEnv.INVALID_CONFIG_ITEMS
        ]
        if invalid_config_items:
            self.dialog.note(
                _(
                    '\nFound the following problems in PostgreSQL '
                    'configuration for the Engine database:\n'
                    '{items}\n'
                ).format(
                    items='\n'.join(
                        [
                            ' %s' % e['format_str'].format(**e)
                            for e in invalid_config_items
                        ]
                    ),
                )
            )
            self.dialog.note(
                database.getInvalidConfigItemsMessage(invalid_config_items)
            )
            fix = self.environment[
                oenginecons.EngineDBEnv.FIX_DB_CONFIGURATION
            ]
            if self._setupOwnsDB() and fix is not False:
                if fix is None:
                    self._suggestAutoFixing()
            else:
                raise RuntimeError(
                    _(
                        'Please fix PostgreSQL configuration and retry.'
                    )
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_EARLY_MISC,
        after=(
            oengcommcons.Stages.DB_UPGRADEDBMS_ENGINE,
        ),
        condition=lambda self:
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.EngineDBEnv.FIX_DB_CONFIGURATION
            ] and self.environment[
                oenginecons.EngineDBEnv.INVALID_CONFIG_ITEMS
            ]
    )
    def _updatePGConf(self):
        self.logger.info(_('Updating PostgreSQL configuration'))
        with transaction.Transaction() as localtransaction:
            self._provisioning._updatePostgresConf(
                transaction=localtransaction,
            )
        self._provisioning.restartPG()
        dbutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment
        )
        invalid_config_items = dbutils.validateDbConf(
            'Engine',
            self.environment
        )
        if invalid_config_items:
            raise RuntimeError(
                database.getInvalidConfigItemsMessage(invalid_config_items)
            )


# vim: expandtab tabstop=4 shiftwidth=4
