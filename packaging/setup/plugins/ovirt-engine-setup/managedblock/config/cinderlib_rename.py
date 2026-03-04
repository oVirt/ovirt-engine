#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""Managed Block Cinderlib rename plugin."""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.managedblock import constants as ombcons


def _(m):
    return gettext.dgettext(message=m, domain="ovirt-engine-setup")


@util.export
class Plugin(plugin.PluginBase):
    """Managed Block Cinderlib rename plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=ombcons.Stages.CONFIG_CINDERLIB_RENAME,
        after=(
            ombcons.Stages.DB_MB_CONNECTION_SETUP,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE]
            and not self.environment[ombcons.CoreEnv.ENABLE]
            and self.environment[ombcons.CoreEnv.CINDERLIB_ENABLE]
            and self.environment[osetupcons.CoreEnv.ACTION] not in {
                osetupcons.Const.ACTION_REMOVE,
                osetupcons.Const.ACTION_PROVISIONDB,
            }
            and self.environment[ombcons.ManagedBlockDBEnv.HOST] is None
        ),
    )
    def _setup(self):
        self.logger.info(_("Checking for legacy Cinderlib configuration"))
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=ombcons.Const.MANAGEDBLOCK_DB_ENV_KEYS,
        )
        dbovirtutils.detectCommands()

        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        if config.get('CINDERLIB_DB_PASSWORD'):
            self.logger.info(
                _("Loading legacy Cinderlib configuration as Managed Block")
            )
            dbenv = {}
            try:
                for e, k in (
                    (ombcons.ManagedBlockDBEnv.HOST, 'CINDERLIB_DB_HOST'),
                    (ombcons.ManagedBlockDBEnv.PORT, 'CINDERLIB_DB_PORT'),
                    (ombcons.ManagedBlockDBEnv.USER, 'CINDERLIB_DB_USER'),
                    (ombcons.ManagedBlockDBEnv.PASSWORD,
                     'CINDERLIB_DB_PASSWORD'),
                    (ombcons.ManagedBlockDBEnv.DATABASE,
                     'CINDERLIB_DB_DATABASE'),
                ):
                    dbenv[e] = config.get(k)
                for e, k in (
                    (ombcons.ManagedBlockDBEnv.SECURED,
                     'CINDERLIB_DB_SECURED'),
                    (
                        ombcons.ManagedBlockDBEnv.SECURED_HOST_VALIDATION,
                        'CINDERLIB_DB_SECURED_VALIDATION'
                    )
                ):
                    dbenv[e] = config.getboolean(k)

                dbovirtutils.tryDatabaseConnect(dbenv)
                self.environment.update(dbenv)
                self.environment[
                    ombcons.ManagedBlockDBEnv.NEW_DATABASE
                ] = False

                self.environment[
                    ombcons.ManagedBlockDBEnv.NEED_DBMSUPGRADE
                ] = dbovirtutils.checkDBMSUpgrade()
                self.environment[ombcons.CoreEnv.ENABLE] = True
            except RuntimeError:
                self.logger.debug(
                    'Legacy Cinderlib credential use failed',
                    exc_info=True,
                )
                msg = _(
                    'Cannot connect to ovirt managed block '
                    'database using legacy Cinderlib '
                    'credentials: {user}@{host}:{port}'
                ).format(
                    host=dbenv[ombcons.ManagedBlockDBEnv.HOST],
                    port=dbenv[ombcons.ManagedBlockDBEnv.PORT],
                    database=dbenv[ombcons.ManagedBlockDBEnv.DATABASE],
                    user=dbenv[ombcons.ManagedBlockDBEnv.USER],
                )
                raise RuntimeError(msg) from RuntimeError
