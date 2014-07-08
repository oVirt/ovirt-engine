#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014 Red Hat, Inc.
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
import os
import urllib2


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from M2Crypto import X509
from M2Crypto import EVP
from M2Crypto import RSA


from otopi import constants as otopicons
from otopi import filetransaction
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons


@util.export
class Plugin(plugin.PluginBase):
    """websocket proxy plugin."""

    def _genReq(self):

        rsa = RSA.gen_key(
            self.environment[owspcons.ConfigEnv.KEY_SIZE],
            65537,
        )
        rsapem = rsa.as_pem(cipher=None)
        evp = EVP.PKey()
        evp.assign_rsa(rsa)
        rsa = None  # should not be freed here
        req = X509.Request()
        req.set_pubkey(evp)
        req.sign(evp, 'sha1')
        return rsapem, req.as_pem()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._need_key = False
        self._need_cert = False
        self._on_separate_h = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.ConfigEnv.WSP_CERTIFICATE_CHAIN,
            None
        )
        self.environment.setdefault(
            owspcons.ConfigEnv.REMOTE_ENGINE_CER,
            None
        )
        self.environment.setdefault(
            owspcons.ConfigEnv.KEY_SIZE,
            owspcons.Defaults.DEFAULT_KEY_SIZE
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validate(self):
        self._enabled = self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ]

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

        self._need_cert = not os.path.exists(
            owspcons.FileLocations.
            OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT
        )

        self._need_key = not os.path.exists(
            owspcons.FileLocations.
            OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY
        )

        self._on_separate_h = not os.path.exists(
            owspcons.FileLocations.
            OVIRT_ENGINE_PKI_ENGINE_CERT
        )

        if self._need_key:
            wspkey, req = self._genReq()

            self.dialog.displayMultiString(
                name=owspcons.Displays.CERTIFICATE_REQUEST,
                value=req.splitlines(),
                note=_(
                    '\n\nPlease issue WebSocket Proxy certificate based '
                    'on this certificate request\n\n'
                ),
            )
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=owspcons.FileLocations.
                    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY,
                    mode=0o600,
                    owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                    enforcePermissions=True,
                    content=wspkey,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

        if self._need_cert:
            self.dialog.note(
                text=_(
                    "Enroll SSL certificate for the websocket proxy service.\n"
                    "It can be done using engine internal CA, if no 3rd "
                    "party CA is available, with this sequence:\n"

                    "1. Copy and save certificate request at\n"
                    "    /etc/pki/ovirt-engine/requests/{name}-{fqdn}.req\n"
                    "on the engine server\n\n"
                    "2. execute, on the engine host, this command "
                    "to enroll the cert:\n"
                    " /usr/share/ovirt-engine/bin/pki-enroll-request.sh \\\n"
                    "     --name={name}-{fqdn} \\\n"
                    "     --subject=\"/C=<country>/O=<organization>/"
                    "CN={fqdn}\"\n"
                    "Substitute <country>, <organization> to suite your "
                    "environment\n"
                    "(i.e. the values must match values in the "
                    "certificate authority of your engine)\n\n"

                    "3. Certificate will be available at\n"
                    "    /etc/pki/ovirt-engine/certs/{name}-{fqdn}.cer\n"
                    "on the engine host, please copy that content here "
                    "when required\n"
                ).format(
                    fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                    name=owspcons.Const.WEBSOCKET_PROXY_CERT_NAME,
                ),
            )

            if self.environment[
                owspcons.ConfigEnv.WSP_CERTIFICATE_CHAIN
            ] is None:
                self.environment[
                    owspcons.ConfigEnv.WSP_CERTIFICATE_CHAIN
                ] = self.dialog.queryMultiString(
                    name=owspcons.ConfigEnv.WSP_CERTIFICATE_CHAIN,
                    note=_(
                        '\n\nPlease input WSP certificate chain that '
                        'matches certificate request, (issuer is not '
                        'mandatory, from intermediate and upper)\n\n'
                    ),
                )
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=owspcons.FileLocations.
                    OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT,
                    mode=0o600,
                    owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                    enforcePermissions=True,
                    content=self.environment[
                        owspcons.ConfigEnv.WSP_CERTIFICATE_CHAIN
                    ],
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

        if self._on_separate_h:
            self.logger.debug('Acquiring engine.crt from the engine')
            while not self.environment[
                owspcons.ConfigEnv.REMOTE_ENGINE_CER
            ]:
                remote_engine_host = self.dialog.queryString(
                    name='REMOTE_ENGINE_HOST',
                    note=_(
                        'Please provide the FQDN or IP '
                        'of the remote engine host: '
                    ),
                    prompt=True,
                )

                with contextlib.closing(
                    urllib2.urlopen(
                        'http://{engine_fqdn}/ovirt-engine/services/'
                        'pki-resource?resource=engine-certificate&'
                        'format=X509-PEM'.format(
                            engine_fqdn=remote_engine_host
                        )
                    )
                ) as urlObj:
                    engine_cer = urlObj.read()
                    if engine_cer:
                        self.environment[
                            owspcons.ConfigEnv.REMOTE_ENGINE_CER
                        ] = engine_cer

            self.environment[
                otopicons.CoreEnv.MAIN_TRANSACTION
            ].append(
                filetransaction.FileTransaction(
                    name=owspcons.FileLocations.
                    OVIRT_ENGINE_PKI_ENGINE_CERT,
                    mode=0o600,
                    owner=self.environment[
                        osetupcons.SystemEnv.USER_ENGINE
                    ],
                    enforcePermissions=True,
                    content=self.environment[
                        owspcons.ConfigEnv.REMOTE_ENGINE_CER
                    ],
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
