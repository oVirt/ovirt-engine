#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


"""Local Postgres plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.provisiondb import constants as oprovisioncons
from ovirt_engine_setup.engine_common import postgres
from ovirt_engine_setup import util as osetuputil


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Local Postgres plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._renamedDBResources = False
        self._provisioning = postgres.Provisioning(
            plugin=self,
            dbenvkeys=oprovisioncons.Const.PROVISION_DB_ENV_KEYS,
            defaults=oprovisioncons.Const.DEFAULT_PROVISION_DB_ENV_KEYS,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._provisioning.detectCommands()
        if not self._provisioning.supported():
            osetuputil.addExitCode(
                environment=self.environment,
                code=(
                    osetupcons.Const.
                    EXIT_CODE_PROVISIONING_NOT_SUPPORTED
                )
            )
            raise RuntimeError('Provisioning not supported')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
    )
    def _customization(self):
        self.environment[oprovisioncons.ProvDBEnv.HOST] = 'localhost'
        self.environment[
            oprovisioncons.ProvDBEnv.PORT
        ] = oprovisioncons.Defaults.DEFAULT_DB_PORT
        self._provisioning.applyEnvironment()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        self._provisioning.validate()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        self._provisioning.provision()
        if self._provisioning.databaseRenamed:
            osetuputil.addExitCode(
                environment=self.environment,
                code=(
                    osetupcons.Const.
                    EXIT_CODE_PROVISIONING_EXISTING_RESOURCES_FOUND
                )
            )
            raise RuntimeError(
                _(
                    'Existing resources found, new ones created: \n'
                    'database {database} user {user}'
                ).format(
                    database=self.environment[
                        oprovisioncons.ProvDBEnv.DATABASE
                    ],
                    user=self.environment[
                        oprovisioncons.ProvDBEnv.USER
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
