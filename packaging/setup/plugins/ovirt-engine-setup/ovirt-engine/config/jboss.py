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


"""Jboss plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """JBoss plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[oengcommcons.ConfigEnv.JAVA_NEEDED] = True
        self.environment[oengcommcons.ConfigEnv.JBOSS_NEEDED] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        for f in (
            (
                oenginecons.FileLocations.
                OVIRT_ENGINE_SERVICE_CONFIG_JBOSS
            ),
            (
                oenginecons.FileLocations.
                OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JBOSS
            ),
        ):
            content = [
                'JBOSS_HOME="{jbossHome}"'.format(
                    jbossHome=self.environment[
                        oengcommcons.ConfigEnv.JBOSS_HOME
                    ],
                ),
            ]
            if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
                content.extend(
                    (
                        'ENGINE_LOG_TO_CONSOLE=true',
                        'ENGINE_DEPLOYMENT_SCANNER=true',
                    )
                )
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=f,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
