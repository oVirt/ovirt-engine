#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2018 Red Hat, Inc.
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
ProxyCommand in system SSH configuration checking plugin.
"""


import gettext
import re

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    ProxyCommand in system SSH configuration checking plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _getSshConfig(self):
        _, stdout, _ = self.execute(
            (
                self.command.get('ssh'),
                '-G',
                'localhost',
            ),
        )
        return stdout

    def _check_ssh_config(self):
        proxy_command_found = False
        pattern = re.compile("^\s*proxycommand\s+")

        # Find ProxyCommand in ssh configuration:
        for line in self._getSshConfig():
            if re.match(pattern, line):
                proxy_command_found = True
                break

        # Log error if any occurance of ProxyCommand was found:
        if proxy_command_found:
            self.logger.warn(
                _(
                    "There is 'ProxyCommand' configuration option in"
                    " system SSH configuration."
                    "\n If you have configured IPA client on your machine,"
                    " please re-configure it with --no-ssh option."
                    "\n If you don't have IPA client configured"
                    " please remove or comment 'ProxyCommand' configuration."
                    "\n It is very important to remove the configuration"
                    " option or the engine won't work properly."
                )
            )

        return proxy_command_found

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _checkSshConfiguration(self):
        """
        Check if SSH on system has configured ProxyCommand.
        """
        if self._check_ssh_config():
            if not dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_VERIFY_SSH_CLIENT_CONFIG',
                note=_(
                    'Do you want Setup to continue, with the incorrect ssh'
                    ' configuration? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            ):
                raise RuntimeError(_('Aborted by user'))

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
        self._check_ssh_config()


# vim: expandtab tabstop=4 shiftwidth=4
