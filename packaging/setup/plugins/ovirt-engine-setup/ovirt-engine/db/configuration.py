#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext

from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.engine_common import postgres


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
        self._pg_hba_refresh_flag = 'OVESETUP_ENGINE_DB/needsPgHbaRefresh'

    def _setupOwnsDB(self):
        # FIXME localhost is inappropriate in case of docker e.g
        # we need a deterministic notion of local/remote pg_host in sense of
        # 'we own postgres' or not.
        return self.environment[oenginecons.EngineDBEnv.HOST] == 'localhost'

    def _suggestAutoFixing(self):
        validValues = (_('Yes'), _('No'))
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            validValues = validValues + (_('Ignore'),)
        autofix_approved = self.dialog.queryString(
            name='UPGRADE_PG_CONF_ENGINE',
            validValues=validValues,
            caseSensitive=False,
            note=_(
                'The database requires these configurations values to be '
                'changed. Setup can fix them for you or abort.'
                ' Fix automatically? ('
                '@VALUES@) [@DEFAULT@]: '
            ),
            prompt=True,
            default=_("Yes"),
        )
        if autofix_approved.upper() == _("Yes").upper():
            self.environment[
                oenginecons.EngineDBEnv.FIX_DB_CONFIGURATION
            ] = True
        elif autofix_approved.upper() == _("No").upper():
            raise RuntimeError(_('Aborted by user'))

    # Checks if uuid-ossp extension was installed in DB
    def _uuidOsspInstalled(self):
        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        statement = """
            select count(*) as count
            from pg_available_extensions
            where name = 'uuid-ossp'
            and installed_version IS NOT NULL
        """
        return dbstatement.execute(
            statement=statement,
            args=None,
            ownConnection=True,
            transaction=False,
        )[0]['count'] != 0

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
        self.environment.setdefault(
            self._pg_hba_refresh_flag,
            False,
        )

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
        invalid_config_items = (
            self.environment[oenginecons.EngineDBEnv.INVALID_CONFIG_ITEMS]
            or []
        )
        needs_auth_update = any(
            item.get('key') == 'password_encryption'
            for item in invalid_config_items
        ) and self._setupOwnsDB()
        self.logger.info(_('Updating PostgreSQL configuration'))
        with transaction.Transaction() as localtransaction:
            self._provisioning._updatePostgresConf(
                transaction=localtransaction,
            )
        self.environment[self._pg_hba_refresh_flag] = needs_auth_update
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

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
        after=(
            oengcommcons.Stages.SCRAM_ROTATION,
        ),
        condition=lambda self: (self.environment.get(
            self._pg_hba_refresh_flag, False)),
    )
    def _updatePgHbaAfterScram(self):
        self.logger.info(
            _('Updating PostgreSQL client access configuration (pg_hba)'))
        with transaction.Transaction() as localtransaction:
            self._provisioning.addPgHbaDatabaseAccess(
                transaction=localtransaction,
            )
        self._provisioning.reloadPgConf()
        self.environment[self._pg_hba_refresh_flag] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
            (
                self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] or
                not self._setupOwnsDB()
            ) and
            not self._uuidOsspInstalled()
        ),
    )
    def checkUuidOsspExtensionForRemoteDbOrDevEnv(self):
        self.dialog.note(
            '\nPostgreSQL uuid-ossp extension is not installed in'
            '  the engine :database\n'
            'Please run the following commands from psql prompt'
            '  with database administrator privileges and run'
            '  engine-setup again:\n'
            'DROP FUNCTION IF EXISTS uuid_generate_v1();\n'
            'CREATE EXTENSION "uuid-ossp";\n'
            'For ''DROP'' you should connect to the engine database,\n'
            '  even though you use admin user.'
        )
        raise RuntimeError(
            "uuid-ossp extension is not installed on database"
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oengcommcons.Stages.DB_SCHEMA,
        ),
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
        ),
    )
    def installUuidOsspExtensionForLocalDb(self):
        if (
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] and
            self._setupOwnsDB() and
                not self._uuidOsspInstalled()
        ):
            self.logger.info(
                _(
                    'Installing PostgreSQL uuid-ossp extension into database'
                )
            )
            self._provisioning.installUuidOsspExtension()


# vim: expandtab tabstop=4 shiftwidth=4
