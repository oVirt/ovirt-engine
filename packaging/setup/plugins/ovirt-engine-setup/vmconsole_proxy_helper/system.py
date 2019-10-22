#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""System plugin."""


import gettext
import grp
import os
import pwd

from otopi import plugin
from otopi import util

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
