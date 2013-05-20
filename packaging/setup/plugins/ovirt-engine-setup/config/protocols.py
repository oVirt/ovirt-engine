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


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
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

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[
                osetupcons.ConfigEnv.HTTP_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTP_PORT
            ]
            self.environment[
                osetupcons.ConfigEnv.HTTPS_PORT
            ] = self.environment[
                osetupcons.ConfigEnv.JBOSS_HTTPS_PORT
            ]

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        #
        # TODO
        # Defaults of engine should be HTTP[s]_ENABLED=false
        #
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            content = (
                'ENGINE_FQDN={fqdn}\n'
                'ENGINE_PROXY_ENABLED=false\n'
                'ENGINE_HTTP_ENABLED=true\n'
                'ENGINE_HTTPS_ENABLED=true\n'
                'ENGINE_HTTP_PORT={httpPort}\n'
                'ENGINE_HTTPS_PORT={httpsPort}\n'
                'ENGINE_AJP_ENABLED=false\n'
                'ENGINE_DEBUG_ADDRESS={debugAddress}\n'
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                httpPort=self.environment[
                    osetupcons.ConfigEnv.HTTP_PORT
                ],
                httpsPort=self.environment[
                    osetupcons.ConfigEnv.HTTPS_PORT
                ],
                debugAddress=self.environment[
                    osetupcons.ConfigEnv.JBOSS_DEBUG_ADDRESS
                ],
            )
        else:
            content = (
                'ENGINE_FQDN={fqdn}\n'
                'ENGINE_PROXY_ENABLED=true\n'
                'ENGINE_PROXY_HTTP_PORT={httpPort}\n'
                'ENGINE_PROXY_HTTPS_PORT={httpsPort}\n'
                'ENGINE_HTTP_ENABLED=false\n'
                'ENGINE_HTTPS_ENABLED=false\n'
                'ENGINE_AJP_ENABLED=true\n'
                'ENGINE_AJP_PORT={ajpPort}\n'
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                httpPort=self.environment[
                    osetupcons.ConfigEnv.HTTP_PORT
                ],
                httpsPort=self.environment[
                    osetupcons.ConfigEnv.HTTPS_PORT
                ],
                ajpPort=self.environment[
                    osetupcons.ConfigEnv.JBOSS_AJP_PORT
                ],
            )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_PROTOCOLS
                ),
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ],
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Web access is enabled at:\n'
                '    http://{fqdn}:{httpPort}{engineURI}\n'
                '    https://{fqdn}:{httpsPort}{engineURI}\n'
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                httpPort=self.environment[
                    osetupcons.ConfigEnv.HTTP_PORT
                ],
                httpsPort=self.environment[
                    osetupcons.ConfigEnv.HTTPS_PORT
                ],
                engineURI=osetupcons.Const.ENGINE_URI,
            )
        )

        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.dialog.note(
                text=_(
                    'JBoss is listending for debug connection at: {address}'
                ).format(
                    address=self.environment[
                        osetupcons.ConfigEnv.JBOSS_DEBUG_ADDRESS
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
