#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ssh plugin."""


import gettext
import os
import tempfile

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons


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
            stdin=key.decode().split('\n'),
        )
        return stdout[0]

    def _getSSHPublicKeyFingerprint(self, key):
        # until openssh-7.1 -l does not support pipe
        with tempfile.NamedTemporaryFile() as f:
            f.write(key.encode())
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

        return x509.load_pem_x509_certificate(
            '\n'.join(cert).encode('ascii'),
            backend=default_backend(),
        ).public_key(
        ).public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )

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
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        if os.path.exists(
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY
        ):
            # Previous versions created it as root:root 0600.
            # We now want to use it also from the engine (for ansible).
            # The filetransaction above will not change ownership
            # if content is not changed. So do this here. We do not
            # do this in a transaction, should be ok.
            os.chown(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY,
                osetuputil.getUid(
                    self.environment[osetupcons.SystemEnv.USER_ENGINE],
                ),
                osetuputil.getGid(
                    self.environment[osetupcons.SystemEnv.GROUP_ENGINE],
                ),
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
