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


"""ssh plugin."""


import base64
import gettext
import re
import struct

from M2Crypto import EVP, X509
from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def _getSSHPublicKeyRaw(self, key):
        ALGO = 'ssh-rsa'
        return {
            'algo': ALGO,
            'blob': (
                struct.pack('!l', len(ALGO)) + ALGO.encode('ascii') +
                key.pub()[0] +
                key.pub()[1]
            ),
        }

    def _getSSHPublicKey(self, key):
        sshkey = self._getSSHPublicKeyRaw(key)
        return '%s %s' % (sshkey['algo'], base64.b64encode(sshkey['blob']))

    def _getSSHPublicKeyFingerprint(self, key):
        sshkey = self._getSSHPublicKeyRaw(key)
        md5 = EVP.MessageDigest('md5')
        md5.update(sshkey['blob'])
        return re.sub(r'(..)', r':\1', base64.b16encode(md5.digest()))[1:]

    def _getEnginePublicKey(self):
        rc, cert, stderr = self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=engine',
                '--passin=%s' % self.environment[
                    oenginecons.PKIEnv.STORE_PASS
                ],
                '--cert=-',
            ),
        )

        x509 = X509.load_cert_string(
            string='\n'.join(cert).encode('ascii'),
            format=X509.FORMAT_PEM,
        )
        return x509.get_pubkey().get_rsa()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('ssh-keygen')

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.SSH_KEY_AVAILABLE,
        after=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        rc, privkey, stderr = self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_PKCS12_EXTRACT,
                '--name=engine',
                '--passin=%s' % self.environment[
                    oenginecons.PKIEnv.STORE_PASS
                ],
                '--key=-',
            ),
            logStreams=False,
        )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY,
                content=privkey,
                mode=0o600,
                owner=self.environment[oengcommcons.SystemEnv.USER_ROOT],
                enforcePermissions=True,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[
            oenginecons.PKIEnv.ENGINE_SSH_PUBLIC_KEY
        ] = self._getSSHPublicKey(self._getEnginePublicKey())

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _closeup(self):
        self.dialog.note(
            text=_('SSH fingerprint: {fingerprint}').format(
                fingerprint=self._getSSHPublicKeyFingerprint(
                    self._getEnginePublicKey()
                ),
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
