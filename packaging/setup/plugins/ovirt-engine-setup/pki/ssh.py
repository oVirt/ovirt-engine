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


"""ssh plugin."""


import os
import tempfile
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('openssl')
        self.command.detect('ssh-keygen')

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            osetupcons.Stages.CA_AVAILABLE,
        ),
    )
    def _misc(self):
        rc, stdout, stderr = self.execute(
            (
                self.command.get('openssl'),
                'pkcs12',
                '-in', (
                    osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE
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
                name=osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY,
                content=stdout,
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ROOT],
                enforcePermissions=True,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _closeup(self):
        temp = None
        try:
            rc, pubkey, stderr = self.execute(
                (
                    self.command.get('ssh-keygen'),
                    '-y',
                    '-f', (
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_SSH_KEY
                    ),
                ),
            )
            fd, temp = tempfile.mkstemp(suffix='.pub')
            os.close(fd)
            with open(temp, "w") as f:
                f.write(pubkey[0])
                f.write('\n')

            rc, fingerprint, stderr = self.execute(
                (
                    self.command.get('ssh-keygen'),
                    '-l',
                    '-f', temp,
                ),
            )

            self.dialog.note(
                text=_('SSH fingerprint: {fingerprint}').format(
                    fingerprint=fingerprint[0].split()[1],
                )
            )
        finally:
            if temp is not None and os.path.exists(temp):
                os.unlink(temp)


# vim: expandtab tabstop=4 shiftwidth=4
