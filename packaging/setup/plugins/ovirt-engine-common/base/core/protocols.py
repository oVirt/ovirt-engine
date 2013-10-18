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


"""Protocols plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Protocols plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.HTTP_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_HTTP_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.HTTPS_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_HTTPS_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_HTTP_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_HTTP_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_HTTPS_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_HTTPS_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_AJP_PORT,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_AJP_PORT
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_DEBUG_ADDRESS,
            osetupcons.Defaults.DEFAULT_NETWORK_JBOSS_DEBUG_ADDRESS
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[
                osetupcons.ConfigEnv.JBOSS_AJP_PORT
            ] = None
            self.environment[
                osetupcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTP_PORT
            ]
            self.environment[
                osetupcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTPS_PORT
            ]
        if self.environment[
            osetupcons.ConfigEnv.JBOSS_AJP_PORT
        ] is None:
            self.environment[
                osetupcons.ConfigEnv.PUBLIC_HTTP_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTP_PORT
            ]
            self.environment[
                osetupcons.ConfigEnv.PUBLIC_HTTPS_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTPS_PORT
            ]
        else:
            self.environment[
                osetupcons.ConfigEnv.PUBLIC_HTTP_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.HTTP_PORT
            ]
            self.environment[
                osetupcons.ConfigEnv.PUBLIC_HTTPS_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.HTTPS_PORT
            ]


# vim: expandtab tabstop=4 shiftwidth=4
