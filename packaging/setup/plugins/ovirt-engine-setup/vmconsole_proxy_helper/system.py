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


"""System plugin."""


import gettext
import grp
import os
import pwd

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """system plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            ovmpcons.SystemEnv.USER_VMCONSOLE,
            pwd.getpwuid(os.geteuid())[0] if self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] else ovmpcons.Defaults.DEFAULT_SYSTEM_USER_VMCONSOLE
        )
        self.environment.setdefault(
            ovmpcons.SystemEnv.GROUP_VMCONSOLE,
            grp.getgrgid(os.getegid())[0] if self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] else ovmpcons.Defaults.DEFAULT_SYSTEM_GROUP_VMCONSOLE
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_PACKAGES,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ]
        ),
    )
    def _packages(self):
        self.packager.installUpdate(
            packages=(ovmpcons.Const.OVIRT_VMCONSOLE_PROXY_PACKAGE,)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ]
        ),
    )
    def _closeup(self):
        self.logger.info(_('Restarting ovirt-vmconsole proxy service'))
        for state in (False, True):
            self.services.state(
                name=ovmpcons.Const.OVIRT_VMCONSOLE_PROXY_SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=ovmpcons.Const.OVIRT_VMCONSOLE_PROXY_SERVICE_NAME,
            state=True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: (
            self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ]
        ),
    )
    def _devenv_closeup(self):
        self.dialog.note(
            _(
                'DEVENV NOTE:\n'
                'ovirt-vmconsole configuration was installed at devenv '
                'prefix.\n'
                'if you want to test vmconsole you should copy the '
                'following directories into filesystem root and chown to '
                'ovirt-vmconsole:\n'
                ' - {sysconf}\n'
                ' - {pki}\n'
            ).format(
                sysconf=ovmpcons.FileLocations.VMCONSOLE_SYSCONF_DIR,
                pki=ovmpcons.FileLocations.VMCONSOLE_PKI_DIR,
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
