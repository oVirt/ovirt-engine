#!/usr/bin/python

#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#

import gettext
import sys

from otopi import base

from ovirt_engine import configfile

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


class DBPlugin(base.Base):
    # Just-Enough-Plugin for our needs

    def __init__(self):
        super(DBPlugin, self).__init__()
        self.environment = {}

    def connect_to_engine_db(self):
        statement = None
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        )
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
                statement = database.Statement(
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                    environment=self.environment,
                )
            except RuntimeError:
                self.logger.debug(
                    'Existing credential use failed',
                    exc_info=True,
                )
        return statement


def get_network_rows(statement, network_name, cluster_name):
    return statement.execute(
        statement='SELECT vdsm_name FROM network, cluster ' +
                  'WHERE network.storage_pool_id=cluster.storage_pool_id ' +
                  'AND network.name=%(network_name)s ' +
                  'AND cluster.name=%(cluster_name)s',
        args={
            'network_name': network_name,
            'cluster_name': cluster_name
        },
        ownConnection=True,
    )


def get_vdsm_network_name(rows):
    if not rows:
        raise RuntimeError('No network found')
    return rows[0]['vdsm_name']


def main():
    statement = DBPlugin().connect_to_engine_db()
    if statement:
        network_name = sys.argv[1]
        cluster_name = sys.argv[2]
        rows = get_network_rows(statement, network_name, cluster_name)
        name = get_vdsm_network_name(rows)
        print(str(name))


if __name__ == '__main__':
    main()
