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


"""Apache ovirt-engine plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil


@util.export
class Plugin(plugin.PluginBase):
    """Apache ovirt-engine plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ApacheEnv.HTTPD_CONF_OVIRT_ENGINE,
            osetupcons.FileLocations.HTTPD_CONF_OVIRT_ENGINE
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _misc(self):
        self.environment[osetupcons.ApacheEnv.NEED_RESTART] = True
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    osetupcons.ApacheEnv.HTTPD_CONF_OVIRT_ENGINE
                ],
                content=osetuputil.processTemplate(
                    template=(
                        osetupcons.FileLocations.
                        HTTPD_CONF_OVIRT_ENGINE_TEMPLATE
                    ),
                    subst={
                        '@JBOSS_AJP_PORT@': self.environment[
                            osetupcons.ConfigEnv.JBOSS_AJP_PORT
                        ],
                    },
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
