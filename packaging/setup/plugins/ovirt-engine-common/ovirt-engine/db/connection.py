#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Connection plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.EngineDBEnv.HOST,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.PORT,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.SECURED,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.SECURED_HOST_VALIDATION,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.USER,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.PASSWORD,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.DATABASE,
            None
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.DUMPER,
            oenginecons.Defaults.DEFAULT_DB_DUMPER
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.FILTER,
            oenginecons.Defaults.DEFAULT_DB_FILTER
        )
        self.environment.setdefault(
            oenginecons.EngineDBEnv.RESTORE_JOBS,
            oenginecons.Defaults.DEFAULT_DB_RESTORE_JOBS
        )

        self.environment[oenginecons.EngineDBEnv.CONNECTION] = None
        self.environment[oenginecons.EngineDBEnv.STATEMENT] = None
        self.environment[oenginecons.EngineDBEnv.NEW_DATABASE] = True
        self.environment[oenginecons.EngineDBEnv.NEED_DBMSUPGRADE] = False
        self.environment[oenginecons.EngineDBEnv.JUST_RESTORED] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=oengcommcons.Stages.DB_CONNECTION_SETUP,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.ACTION
        ] != osetupcons.Const.ACTION_PROVISIONDB,
    )
    def _setup(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        )
        dbovirtutils.detectCommands()

        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        if config.get('ENGINE_DB_PASSWORD'):
            try:
                dbenv = {}
                for e, k in (
                    (oenginecons.EngineDBEnv.HOST, 'ENGINE_DB_HOST'),
                    (oenginecons.EngineDBEnv.PORT, 'ENGINE_DB_PORT'),
                    (oenginecons.EngineDBEnv.USER, 'ENGINE_DB_USER'),
                    (oenginecons.EngineDBEnv.PASSWORD, 'ENGINE_DB_PASSWORD'),
                    (oenginecons.EngineDBEnv.DATABASE, 'ENGINE_DB_DATABASE'),
                ):
                    dbenv[e] = config.get(k)
                for e, k in (
                    (oenginecons.EngineDBEnv.SECURED, 'ENGINE_DB_SECURED'),
                    (
                        oenginecons.EngineDBEnv.SECURED_HOST_VALIDATION,
                        'ENGINE_DB_SECURED_VALIDATION'
                    )
                ):
                    dbenv[e] = config.getboolean(k)

                dbovirtutils.tryDatabaseConnect(dbenv)
                self.environment.update(dbenv)
                self.environment[
                    oenginecons.EngineDBEnv.NEW_DATABASE
                ] = dbovirtutils.isNewDatabase()

                self.environment[
                    oenginecons.EngineDBEnv.NEED_DBMSUPGRADE
                ] = dbovirtutils.checkDBMSUpgrade()

            except RuntimeError:
                self.logger.debug(
                    'Existing credential use failed',
                    exc_info=True,
                )
                msg = _(
                    'Cannot connect to Engine database using existing '
                    'credentials: {user}@{host}:{port}'
                ).format(
                    host=dbenv[oenginecons.EngineDBEnv.HOST],
                    port=dbenv[oenginecons.EngineDBEnv.PORT],
                    database=dbenv[oenginecons.EngineDBEnv.DATABASE],
                    user=dbenv[oenginecons.EngineDBEnv.USER],
                )
                if self.environment[
                    osetupcons.CoreEnv.ACTION
                ] == osetupcons.Const.ACTION_REMOVE:
                    self.logger.warning(msg)
                else:
                    raise RuntimeError(msg)
            if not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]:
                statement = database.Statement(
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                    environment=self.environment,
                )
                try:
                    justRestored = vdcoption.VdcOption(
                        statement=statement,
                    ).getVdcOption(
                        'DbJustRestored',
                        ownConnection=True,
                    )
                    self.environment[
                        oenginecons.EngineDBEnv.JUST_RESTORED
                    ] = (justRestored == '1')
                except RuntimeError:
                    pass
                if self.environment[
                    oenginecons.EngineDBEnv.JUST_RESTORED
                ]:
                    self.logger.info(_(
                        'The engine DB has been restored from a backup'
                    ))


# vim: expandtab tabstop=4 shiftwidth=4
