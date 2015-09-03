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


import gettext
import tempfile

from M2Crypto import X509

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

    def _getSSHPublicKey(self, key):
        rc, stdout, stderr = self.execute(
            (
                self.command.get('ssh-keygen'),
                '-i',
                '-m', 'PKCS8',
                '-f', '/proc/self/fd/0',
            ),
            stdin=key.split('\n'),
        )
        return stdout[0]

    def _getSSHPublicKeyFingerprint(self, key):
        # until openssh-7.1 -l does not support pipe
        with tempfile.NamedTemporaryFile() as f:
            f.write(key)
            f.flush()
            rc, stdout, stderr = self.execute(
                (
                    self.command.get('ssh-keygen'),
                    '-l',
                    '-f', f.name,
                ),
            )
            return stdout[0].split()[1]

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

        return X509.load_cert_string(
            string='\n'.join(cert).encode('ascii'),
            format=X509.FORMAT_PEM,
        ).get_pubkey().get_rsa().as_pem()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_PROGRAMS,
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
                    self.environment[
                        oenginecons.PKIEnv.ENGINE_SSH_PUBLIC_KEY
                    ]
                ),
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
