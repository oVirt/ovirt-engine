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


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup.engine_common \
    import enginecommonconstants as oengcommcons
from ovirt_engine_setup.engine_common import database


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            oengcommcons.EngineDBEnv.PASSWORD
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.HOST,
            None
        )
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.PORT,
            None
        )
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.SECURED,
            None
        )
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.SECURED_HOST_VALIDATION,
            None
        )
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.USER,
            None
        )
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.PASSWORD,
            None
        )
        self.environment.setdefault(
            oengcommcons.EngineDBEnv.DATABASE,
            None
        )

        self.environment[oengcommcons.EngineDBEnv.CONNECTION] = None
        self.environment[oengcommcons.EngineDBEnv.STATEMENT] = None
        self.environment[oengcommcons.EngineDBEnv.NEW_DATABASE] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _commands(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oengcommcons.Const.ENGINE_DB_ENV_KEYS,
        )
        dbovirtutils.detectCommands()


# vim: expandtab tabstop=4 shiftwidth=4
