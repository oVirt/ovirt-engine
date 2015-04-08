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


"""
sshd service handler plugin.
"""


import gettext
import os
import re

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    sshd service handler plugin.
    """

    _PERMIT_ROOT_LOGIN_RE = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            \s*
            PermitRootLogin
            \s+
            no
            [\s#]*
            $
        """
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.AIOEnv.SSHD_PORT,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]
        self.command.detect('sshd')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self._enabled and
            self.environment[oenginecons.AIOEnv.CONFIGURE]
        ),
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ALLINONE,
        ),
        after=(
            oenginecons.Stages.AIO_CONFIG_AVAILABLE,
        ),
    )
    def _customization(self):
        if not self.services.exists(name='sshd'):
            raise RuntimeError('sshd service is required')
        if not self.services.status(name='sshd'):
            self.services.state(
                name='sshd',
                state=True,
            )
        if self.environment[oenginecons.AIOEnv.SSHD_PORT] is None:
            rc, stdout, stderr = self.execute(
                args=(
                    self.command.get('sshd'),
                    '-T',
                ),
            )
            for line in stdout:
                words = line.split()
                if words[0] == 'port':
                    self.environment[
                        oenginecons.AIOEnv.SSHD_PORT
                    ] = int(words[1])
                    break
        self.environment.setdefault(
            oenginecons.AIOEnv.SSHD_PORT,
            oenginecons.AIOEnv.DEFAULT_SSH_PORT
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
            self._enabled and
            self.environment[oenginecons.AIOEnv.CONFIGURE]
        ),
    )
    def _validation(self):
        with open('/etc/ssh/sshd_config') as f:
            for l in f.read().splitlines():
                if self._PERMIT_ROOT_LOGIN_RE.match(l):
                    raise RuntimeError(
                        _(
                            'Your sshd configuration does not permit root '
                            'login, please enable PermitRootLogin to at '
                            'least without-password at /etc/ssh/sshd_config, '
                            'and restart sshd'
                        )
                    )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self._enabled and
            self.environment[oenginecons.AIOEnv.CONFIGURE]
        ),
        after=(
            osetupcons.Stages.SSH_KEY_AVAILABLE,
        ),
        before=(
            osetupcons.Stages.SETUP_SELINUX,
        ),
    )
    def _misc(self):
        authorized_keys_line = self.environment[
            oenginecons.PKIEnv.ENGINE_SSH_PUBLIC_KEY
        ] + ' ovirt-engine'

        sshdir = os.path.join(
            os.path.expanduser('~root'),
            '.ssh'
        )
        self.environment[
            osetupcons.SystemEnv.SELINUX_RESTORE_PATHS
        ].append(sshdir)
        authorized_keys_file = os.path.join(
            sshdir,
            'authorized_keys'
        )

        content = []
        if os.path.exists(authorized_keys_file):
            with open(authorized_keys_file, 'r') as f:
                content = f.read().splitlines()

        if authorized_keys_line not in content:
            self.environment[
                osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
            ].append(authorized_keys_file)

            content.append(authorized_keys_line)
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=authorized_keys_file,
                    content=content,
                    mode=0o600,
                    dmode=0o700,
                    owner='root',
                    enforcePermissions=True,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=oenginecons.Stages.AIO_CONFIG_SSH,
        condition=lambda self: (
            self._enabled and
            self.environment[oenginecons.AIOEnv.CONFIGURE]
        ),
    )
    def _closeup(self):
        self.services.startup(
            name='sshd',
            state=True
        )


# vim: expandtab tabstop=4 shiftwidth=4
