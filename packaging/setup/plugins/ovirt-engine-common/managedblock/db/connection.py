#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Managed block connection plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup.managedblock import constants as ombcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Managed block connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.HOST,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.PORT,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.SECURED,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.SECURED_HOST_VALIDATION,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.USER,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.PASSWORD,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.DATABASE,
            None
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.DUMPER,
            oenginecons.Defaults.DEFAULT_DB_DUMPER
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.FILTER,
            oenginecons.Defaults.DEFAULT_DB_FILTER
        )
        self.environment.setdefault(
            ombcons.ManagedBlockDBEnv.RESTORE_JOBS,
            oenginecons.Defaults.DEFAULT_DB_RESTORE_JOBS
        )

        self.environment[ombcons.ManagedBlockDBEnv.CONNECTION] = None
        self.environment[ombcons.ManagedBlockDBEnv.STATEMENT] = None
        self.environment[ombcons.ManagedBlockDBEnv.NEW_DATABASE] = True
        self.environment[ombcons.ManagedBlockDBEnv.NEED_DBMSUPGRADE] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=ombcons.Stages.DB_MB_CONNECTION_SETUP,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.ACTION
        ] != osetupcons.Const.ACTION_PROVISIONDB,
    )
    def _setup(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=ombcons.Const.MANAGEDBLOCK_DB_ENV_KEYS,
        )
        dbovirtutils.detectCommands()

        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        if config.get('MANAGEDBLOCK_DB_PASSWORD'):
            try:
                dbenv = {}
                for e, k in (
                    (ombcons.ManagedBlockDBEnv.HOST, 'MANAGEDBLOCK_DB_HOST'),
                    (ombcons.ManagedBlockDBEnv.PORT, 'MANAGEDBLOCK_DB_PORT'),
                    (ombcons.ManagedBlockDBEnv.USER, 'MANAGEDBLOCK_DB_USER'),
                    (ombcons.ManagedBlockDBEnv.PASSWORD,
                     'MANAGEDBLOCK_DB_PASSWORD'),
                    (ombcons.ManagedBlockDBEnv.DATABASE,
                     'MANAGEDBLOCK_DB_DATABASE'),
                ):
                    dbenv[e] = config.get(k)
                for e, k in (
                    (ombcons.ManagedBlockDBEnv.SECURED,
                     'MANAGEDBLOCK_DB_SECURED'),
                    (
                        ombcons.ManagedBlockDBEnv.SECURED_HOST_VALIDATION,
                        'MANAGEDBLOCK_DB_SECURED_VALIDATION'
                    )
                ):
                    dbenv[e] = config.getboolean(k)

                dbovirtutils.tryDatabaseConnect(dbenv)
                self.environment.update(dbenv)
                # current managed block engine-setup code leaves the database
                # empty after creation, so we can't rely on
                # dbovirtutils.isNewDatabase for checking this (because it
                # checks if there are tables in the public schema).
                # Always set to False if we managed to connect. TODO think
                # of something more robust. Perhaps create our own dummy
                # table to mark that it's 'populated', or save in postinstall
                # something saying that it's created.
                self.environment[
                    ombcons.ManagedBlockDBEnv.NEW_DATABASE
                ] = False

                self.environment[
                    ombcons.ManagedBlockDBEnv.NEED_DBMSUPGRADE
                ] = dbovirtutils.checkDBMSUpgrade()

            except RuntimeError:
                self.logger.debug(
                    'Existing credential use failed',
                    exc_info=True,
                )
                msg = _(
                    'Cannot connect to ovirt managed block '
                    'database using existing '
                    'credentials: {user}@{host}:{port}'
                ).format(
                    host=dbenv[ombcons.ManagedBlockDBEnv.HOST],
                    port=dbenv[ombcons.ManagedBlockDBEnv.PORT],
                    database=dbenv[ombcons.ManagedBlockDBEnv.DATABASE],
                    user=dbenv[ombcons.ManagedBlockDBEnv.USER],
                )
                if self.environment[
                    osetupcons.CoreEnv.ACTION
                ] == osetupcons.Const.ACTION_REMOVE:
                    self.logger.warning(msg)
                else:
                    raise RuntimeError(msg) from RuntimeError


# vim: expandtab tabstop=4 shiftwidth=4
