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


"""sysctl plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """sysctl plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.SystemEnv.SHMMAX,
            oengcommcons.Defaults.DEFAULT_SYSTEM_SHMMAX
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('sysctl')

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        self._content = (
            "# ovirt-engine configuration.\n"
            "kernel.shmmax = %s\n"
        ) % self.environment[oengcommcons.SystemEnv.SHMMAX]

        interactive = self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]

        while True:
            shmmax = self._get_shmmax()

            if shmmax >= self.environment[oengcommcons.SystemEnv.SHMMAX]:
                break
            else:
                self.logger.debug(
                    'sysctl shared memory is %s lower than %s' % (
                        shmmax,
                        self.environment[oengcommcons.SystemEnv.SHMMAX],
                    )
                )

                if not interactive:
                    self._enabled = True
                    break
                else:
                    self.logger.warning(_('Manual intervention is required'))
                    self.dialog.note(
                        text=_(
                            "Current shared memory setting is too low.\n"
                            "Unable to set while running unprivileged.\n"
                            "Please write the following file: '{file}', "
                            "with the following content:\n"
                            "---\n"
                            "{content}"
                            "---\n"
                            "Then execute the following command as root:\n"
                            "{sysctl} -p {file}"
                        ).format(
                            file=(
                                oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL
                            ),
                            content=self._content,
                            sysctl=self.command.get('sysctl'),
                        )
                    )

                    if not dialog.queryBoolean(
                        dialog=self.dialog,
                        name='OVESETUP_SYSTEM_SYSCTL_SHMEM',
                        note=_(
                            'Proceed? (@VALUES@) [@DEFAULT@]: '
                        ),
                        prompt=True,
                        true=_('OK'),
                        false=_('Cancel'),
                        default=True,
                    ):
                        raise RuntimeError(
                            _('Aborted by user')
                        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.SYSTEM_SYSCTL_CONFIG_AVAILABLE,
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _misc(self):
        if self._enabled:
            sysctl = filetransaction.FileTransaction(
                name=oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL,
                content=self._content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
            self.environment[
                osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
            ].append(oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL)

            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(sysctl)

            # we must do this here as postgres requires it
            self.execute(
                (
                    self.command.get('sysctl'),
                    '-p', sysctl.tmpname,
                ),
            )

        # Verify shmmax is set correctly
        shmmax = self._get_shmmax()

        if shmmax < self.environment[oengcommcons.SystemEnv.SHMMAX]:
            self.logger.debug(
                'sysctl kernel.shmmax is %s lower than %s' % (
                    shmmax,
                    self.environment[oengcommcons.SystemEnv.SHMMAX],
                )
            )

            raise RuntimeError(
                _('Unable to set sysctl kernel.shmmax to minimum requirement')
            )

    def _get_shmmax(self):
        rc, shmmax, stderr = self.execute(
            (
                self.command.get('sysctl'),
                '-n',
                'kernel.shmmax',
            ),
        )
        return int(shmmax[0])


# vim: expandtab tabstop=4 shiftwidth=4
