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


"""Jboss plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """JBoss plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_HOME,
            osetupcons.FileLocations.JBOSS_HOME
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        if not os.path.exists(
            self.environment[
                osetupcons.ConfigEnv.JBOSS_HOME
            ]
        ):
            raise RuntimeError(
                _('Cannot find Jboss at {jbossHome}').format(
                    jbossHome=self.environment[
                        osetupcons.ConfigEnv.JBOSS_HOME
                    ],
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        for f in (
            (
                osetupcons.FileLocations.
                OVIRT_ENGINE_SERVICE_CONFIG_JBOSS
            ),
            (
                osetupcons.FileLocations.
                OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JBOSS
            ),
        ):
            content = [
                'JBOSS_HOME="{jbossHome}"'.format(
                    jbossHome=self.environment[
                        osetupcons.ConfigEnv.JBOSS_HOME
                    ],
                ),
            ]
            if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
                content.append(
                    'ENGINE_LOG_TO_CONSOLE=true'
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
