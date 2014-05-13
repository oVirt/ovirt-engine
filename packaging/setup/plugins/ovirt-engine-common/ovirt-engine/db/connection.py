#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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


"""Connection plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine import configfile


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import engineconstants as oenginecons
from ovirt_engine_setup.engine_common \
    import enginecommonconstants as oengcommcons
from ovirt_engine_setup.engine_common import database


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=oengcommcons.Stages.DB_CONNECTION_SETUP,
    )
    def _setup(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        if config.get('ENGINE_DB_PASSWORD'):
            try:
                dbenv = {}
                for e, k in (
                    (oengcommcons.EngineDBEnv.HOST, 'ENGINE_DB_HOST'),
                    (oengcommcons.EngineDBEnv.PORT, 'ENGINE_DB_PORT'),
                    (oengcommcons.EngineDBEnv.USER, 'ENGINE_DB_USER'),
                    (oengcommcons.EngineDBEnv.PASSWORD, 'ENGINE_DB_PASSWORD'),
                    (oengcommcons.EngineDBEnv.DATABASE, 'ENGINE_DB_DATABASE'),
                ):
                    dbenv[e] = config.get(k)
                for e, k in (
                    (oengcommcons.EngineDBEnv.SECURED, 'ENGINE_DB_SECURED'),
                    (
                        oengcommcons.EngineDBEnv.SECURED_HOST_VALIDATION,
                        'ENGINE_DB_SECURED_VALIDATION'
                    )
                ):
                    dbenv[e] = config.getboolean(k)

                dbovirtutils = database.OvirtUtils(
                    plugin=self,
                    dbenvkeys=oengcommcons.Const.ENGINE_DB_ENV_KEYS,
                )
                dbovirtutils.tryDatabaseConnect(dbenv)
                self.environment.update(dbenv)
                self.environment[
                    oengcommcons.EngineDBEnv.NEW_DATABASE
                ] = dbovirtutils.isNewDatabase()
            except RuntimeError as e:
                self.logger.debug(
                    'Existing credential use failed',
                    exc_info=True,
                )
                msg = _(
                    'Cannot connect to Engine database using existing '
                    'credentials: {user}@{host}:{port}'
                ).format(
                    host=dbenv[oengcommcons.EngineDBEnv.HOST],
                    port=dbenv[oengcommcons.EngineDBEnv.PORT],
                    database=dbenv[oengcommcons.EngineDBEnv.DATABASE],
                    user=dbenv[oengcommcons.EngineDBEnv.USER],
                )
                if self.environment[
                    osetupcons.CoreEnv.ACTION
                ] == osetupcons.Const.ACTION_REMOVE:
                    self.logger.warning(msg)
                else:
                    raise RuntimeError(msg)


# vim: expandtab tabstop=4 shiftwidth=4
