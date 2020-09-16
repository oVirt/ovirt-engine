#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Cinderlib connection plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.cinderlib import constants as oclcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Cinderlib connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.HOST,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.PORT,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.SECURED,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.SECURED_HOST_VALIDATION,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.USER,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.PASSWORD,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.DATABASE,
            None
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.DUMPER,
            oenginecons.Defaults.DEFAULT_DB_DUMPER
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.FILTER,
            oenginecons.Defaults.DEFAULT_DB_FILTER
        )
        self.environment.setdefault(
            oclcons.CinderlibDBEnv.RESTORE_JOBS,
            oenginecons.Defaults.DEFAULT_DB_RESTORE_JOBS
        )

        self.environment[oclcons.CinderlibDBEnv.CONNECTION] = None
        self.environment[oclcons.CinderlibDBEnv.STATEMENT] = None
        self.environment[oclcons.CinderlibDBEnv.NEW_DATABASE] = True
        self.environment[oclcons.CinderlibDBEnv.NEED_DBMSUPGRADE] = False

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=oclcons.Stages.DB_CL_CONNECTION_SETUP,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.ACTION
        ] != osetupcons.Const.ACTION_PROVISIONDB,
    )
    def _setup(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oclcons.Const.CINDERLIB_DB_ENV_KEYS,
        )
        dbovirtutils.detectCommands()

        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        if config.get('CINDERLIB_DB_PASSWORD'):
            try:
                dbenv = {}
                for e, k in (
                    (oclcons.CinderlibDBEnv.HOST, 'CINDERLIB_DB_HOST'),
                    (oclcons.CinderlibDBEnv.PORT, 'CINDERLIB_DB_PORT'),
                    (oclcons.CinderlibDBEnv.USER, 'CINDERLIB_DB_USER'),
                    (oclcons.CinderlibDBEnv.PASSWORD,
                     'CINDERLIB_DB_PASSWORD'),
                    (oclcons.CinderlibDBEnv.DATABASE,
                     'CINDERLIB_DB_DATABASE'),
                ):
                    dbenv[e] = config.get(k)
                for e, k in (
                    (oclcons.CinderlibDBEnv.SECURED,
                     'CINDERLIB_DB_SECURED'),
                    (
                        oclcons.CinderlibDBEnv.SECURED_HOST_VALIDATION,
                        'CINDERLIB_DB_SECURED_VALIDATION'
                    )
                ):
                    dbenv[e] = config.getboolean(k)

                dbovirtutils.tryDatabaseConnect(dbenv)
                self.environment.update(dbenv)
                # current cinderlib engine-setup code leaves the database
                # empty after creation, so we can't rely on
                # dbovirtutils.isNewDatabase for checking this (because it
                # checks if there are tables in the public schema).
                # Always set to False if we managed to connect. TODO think
                # of something more robust. Perhaps create our own dummy
                # table to mark that it's 'populated', or save in postinstall
                # something saying that it's created.
                self.environment[
                    oclcons.CinderlibDBEnv.NEW_DATABASE
                ] = False

                self.environment[
                    oclcons.CinderlibDBEnv.NEED_DBMSUPGRADE
                ] = dbovirtutils.checkDBMSUpgrade()

            except RuntimeError:
                self.logger.debug(
                    'Existing credential use failed',
                    exc_info=True,
                )
                msg = _(
                    'Cannot connect to ovirt cinderlib '
                    'database using existing '
                    'credentials: {user}@{host}:{port}'
                ).format(
                    host=dbenv[oclcons.CinderlibDBEnv.HOST],
                    port=dbenv[oclcons.CinderlibDBEnv.PORT],
                    database=dbenv[oclcons.CinderlibDBEnv.DATABASE],
                    user=dbenv[oclcons.CinderlibDBEnv.USER],
                )
                if self.environment[
                    osetupcons.CoreEnv.ACTION
                ] == osetupcons.Const.ACTION_REMOVE:
                    self.logger.warning(msg)
                else:
                    raise RuntimeError(msg)


# vim: expandtab tabstop=4 shiftwidth=4
