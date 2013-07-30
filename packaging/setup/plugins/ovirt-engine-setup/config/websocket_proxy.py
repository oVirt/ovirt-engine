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


"""websocket proxy plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import filetransaction
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """websocket proxy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._needStart = False
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.WEBSOCKET_PROXY_PORT,
            osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('openssl')

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
    )
    def _late_setup(self):
        if self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]:
            self._enabled = True
        else:
            if (
                not os.path.exists(
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_STORE
                ) and
                self.services.exists(name='ovirt-websocket-proxy')
            ):
                self._enabled = True

            self._needStart = self.services.status(
                name='ovirt-websocket-proxy',
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DB_CONNECTION_STATUS,
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
    )
    def _customization(self):

        if self.environment[
            osetupcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ] is None:
            self.environment[
                osetupcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONFIG_WEBSOCKET_PROXY',
                note=_(
                    'Configure WebSocket Proxy on this machine? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=True,
            )
        self._enabled = self.environment[
            osetupcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ]

        if self._enabled:
            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
                {
                    'name': 'ovirt-websocket-proxy',
                    'directory': 'base'
                },
            ])
            self.environment[
                osetupcons.NetEnv.FIREWALLD_SUBST
            ].update({
                '@WEBSOCKET_PROXY_PORT@': self.environment[
                    osetupcons.ConfigEnv.WEBSOCKET_PROXY_PORT
                ],
            })

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
        after=(
            osetupcons.Stages.DB_CONNECTION_AVAILABLE,
            osetupcons.Stages.CA_AVAILABLE,
        ),
    )
    def _misc(self):

        self.logger.info(_('Configurating WebSocket Proxy'))

        self.environment[osetupcons.DBEnv.STATEMENT].updateVdcOptions(
            options=(
                {
                    'name': 'WebSocketProxy',
                    'value': 'Engine:%s' % self.environment[
                        osetupcons.ConfigEnv.WEBSOCKET_PROXY_PORT
                    ],
                },
            ),
        )

        self.execute(
            args=(
                osetupcons.FileLocations.OVIRT_ENGINE_PKI_CA_ENROLL,
                '--name=%s' % 'websocket-proxy',
                '--password=%s' % (
                    self.environment[osetupcons.PKIEnv.STORE_PASS],
                ),
                '--subject=/C=%s/O=%s/CN=%s' % (
                    self.environment[osetupcons.PKIEnv.COUNTRY],
                    self.environment[osetupcons.PKIEnv.ORG],
                    self.environment[osetupcons.ConfigEnv.FQDN],
                ),
            ),
        )
        self.environment[
            otopicons.CoreEnv.MODIFIED_FILES
        ].extend(
            (
                (
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT
                ),
                (
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_STORE
                ),
            )
        )

        rc, stdout, stderr = self.execute(
            args=(
                self.command.get('openssl'),
                'pkcs12',
                '-in', (
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_STORE
                ),
                '-passin', 'pass:%s' % self.environment[
                    osetupcons.PKIEnv.STORE_PASS
                ],
                '-nodes',
                '-nocerts',
            ),
            logStreams=False,
        )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                osetupcons.FileLocations.OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY,
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                content=stdout,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG_SETUP
                ),
                content=(
                    "PROXY_PORT={port}\n"
                    "SSL_CERTIFICATE={certificate}\n"
                    "SSL_KEY={key}\n"
                    "FORCE_DATA_VERIFICATION=True\n"
                    "CERT_FOR_DATA_VERIFICATION={engine_cert}\n"
                    "SSL_ONLY=True\n"
                ).format(
                    port=self.environment[
                        osetupcons.ConfigEnv.WEBSOCKET_PROXY_PORT
                    ],
                    certificate=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT
                    ),
                    key=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY
                    ),
                    engine_cert=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CERT
                    ),
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            self._needStart or
            (
                self._enabled and
                not self.environment[
                    osetupcons.CoreEnv.DEVELOPER_MODE
                ]
            )
        ),
    )
    def _closeup(self):
        for state in (False, True):
            self.services.state(
                name=osetupcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=osetupcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
