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
ProxyCommand in file /etc/ssh/ssh_config checking plugin.
"""


import gettext
import os.path
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
    ProxyCommand in file /etc/ssh/ssh_config checking plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _check_ssh_config(self):
        proxy_command_found = False
        ssh_config_path = oenginecons.FileLocations.SSH_CLIENT_CONFIG
        pattern = re.compile("^\s*ProxyCommand\s+")

        # Exit if ssh configuration file does not exist:
        if not os.path.isfile(ssh_config_path):
            self.logger.debug(
                _(
                    "Ssh configuration file {config_file} does not exist"
                ).format(
                    config_file=ssh_config_path,
                )
            )
            return proxy_command_found

        # Find ProxyCommand in ssh configuration file:
        try:
            for line in open(ssh_config_path):
                if re.match(pattern, line):
                    proxy_command_found = True
                    break
        except IOError as e:
            self.logger.warning(
                _(
                    'Failed to read {config_file}: {error}'
                ).format(
                    config_file=ssh_config_path,
                    error=e,
                )
            )

        # Log error if any occurance of ProxyCommand was found:
        if proxy_command_found:
            self.logger.warn(
                _(
                    "There is 'ProxyCommand' configuration option in"
                    " {config_file} file."
                    "\n If you have configured IPA client on your machine,"
                    " please re-configure it with --no-ssh option."
                    "\n If you don't have IPA client configured"
                    " please remove or comment 'ProxyCommand' configuration."
                    "\n It is very important to remove the configuration"
                    " option or the engine won't work properly."
                ).format(
                    config_file=ssh_config_path,
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
