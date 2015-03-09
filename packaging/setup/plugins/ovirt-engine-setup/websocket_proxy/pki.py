#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014-2015 Red Hat, Inc.
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


import contextlib
import gettext
import os
import time
import urllib2

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import remote_engine
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """websocket proxy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._enrolldata = None
        self._need_eng_cert = False
        self._engine_cert = None

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.ConfigEnv.PKI_WSP_CSR_FILENAME,
            None
        )
        self.environment.setdefault(
            owspcons.ConfigEnv.KEY_SIZE,
            owspcons.Defaults.DEFAULT_KEY_SIZE
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_PKI,
        ),
        after=(
            owspcons.Stages.CONFIG_WEBSOCKET_PROXY_CUSTOMIZATION,
            owspcons.Stages.ENGINE_CORE_ENABLE,
            oengcommcons.Stages.DIALOG_TITLES_S_PKI,
        ),
        condition=lambda self: (
            self.environment[
                owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
            ] and
            # If on same host as engine, engine setup code creates pki for us
            not self.environment[
                owspcons.EngineCoreEnv.ENABLE
            ]
        ),
    )
    def _customization(self):
        self._enabled = True

        engine_wsp_pki_found = (
            os.path.exists(
                owspcons.FileLocations.OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY
            ) and os.path.exists(
                owspcons.FileLocations.OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT
            ) and os.path.exists(
                owspcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CERT
            )
        )

        if not engine_wsp_pki_found:
            self._enrolldata = remote_engine.EnrollCert(
                remote_engine=self.environment[
                    osetupcons.CoreEnv.REMOTE_ENGINE
                ],
                engine_fqdn=self.environment[
                    owspcons.EngineConfigEnv.ENGINE_FQDN
                ],
                base_name=owspcons.Const.WEBSOCKET_PROXY_CERT_NAME,
                base_touser=_('WebSocket Proxy'),
                key_file=owspcons.FileLocations.
                OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY,
                cert_file=owspcons.FileLocations.
                OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT,
                csr_fname_envkey=owspcons.ConfigEnv.
                PKI_WSP_CSR_FILENAME,
                engine_ca_cert_file=os.path.join(
                    owspcons.FileLocations.OVIRT_ENGINE_PKIDIR,
                    'ca.pem'
                ),
                engine_pki_requests_dir=owspcons.FileLocations.
                OVIRT_ENGINE_PKIREQUESTSDIR,
                engine_pki_certs_dir=owspcons.FileLocations.
                OVIRT_ENGINE_PKICERTSDIR,
                key_size=self.environment[owspcons.ConfigEnv.KEY_SIZE],
                url="http://http://www.ovirt.org/Features/"
                    "WebSocketProxy_on_a_separate_host",
            )
            self._enrolldata.enroll_cert()

            self._need_eng_cert = not os.path.exists(
                owspcons.FileLocations.
                OVIRT_ENGINE_PKI_ENGINE_CERT
            )
        else:
            self._enabled = False

        tries_left = 30
        while (
            self._need_eng_cert and
            self._engine_cert is None and
            tries_left > 0
        ):
            remote_engine_host = self.environment[
                owspcons.EngineConfigEnv.ENGINE_FQDN
            ]

            with contextlib.closing(
                urllib2.urlopen(
                    'http://{engine_fqdn}/ovirt-engine/services/'
                    'pki-resource?resource=engine-certificate&'
                    'format=X509-PEM'.format(
                        engine_fqdn=remote_engine_host
                    )
                )
            ) as urlObj:
                engine_ca_cert = urlObj.read()
                if engine_ca_cert:
                    self._engine_cert = engine_ca_cert
                else:
                    self.logger.error(
                        _(
                            'Failed to get the engine certificate '
                            'from the engine host. '
                            'Please check access to the engine and its '
                            'status.'
                        )
                    )
                    time.sleep(10)
                    tries_left -= 1
        if self._need_eng_cert and self._engine_cert is None:
            raise RuntimeError(_('Failed to get the engine certificate from '
                                 'the engine host'))

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self._enabled
        ),
        after=(
            owspcons.Stages.CA_AVAILABLE,
        ),
    )
    def _misc_pki(self):
        self._enrolldata.add_to_transaction(
            uninstall_group_name='ca_pki_wsp',
            uninstall_group_desc='WSP PKI keys',
        )
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_pki_wsp',
            description='WSP PKI keys',
            optional=True,
        ).addFiles(
            group='ca_pki_wsp',
            fileList=uninstall_files,
        )

        if self._need_eng_cert:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=owspcons.FileLocations.
                    OVIRT_ENGINE_PKI_ENGINE_CERT,
                    mode=0o600,
                    owner=self.environment[
                        osetupcons.SystemEnv.USER_ENGINE
                    ],
                    enforcePermissions=True,
                    content=self._engine_cert,
                    modifiedList=uninstall_files,
                )
            )
            uninstall_files.append(
                owspcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CERT
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
        condition=lambda self: (
            self._enabled
        ),
    )
    def _cleanup(self):
        self._enrolldata.cleanup()


# vim: expandtab tabstop=4 shiftwidth=4
