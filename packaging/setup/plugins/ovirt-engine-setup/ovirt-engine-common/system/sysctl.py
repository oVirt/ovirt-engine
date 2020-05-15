#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""sysctl plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """sysctl plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._settings = []

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.SystemEnv.SHMMAX,
            oengcommcons.Defaults.DEFAULT_SYSTEM_SHMMAX
        )
        # Plugins that want to reserve a port should add the port to this set.
        self.environment.setdefault(
            osetupcons.SystemEnv.RESERVED_PORTS,
            set()
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('sysctl')

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validate_shmmax(self):
        content = "kernel.shmmax = {}\n".format(
            self.environment[oengcommcons.SystemEnv.SHMMAX]
        )

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
                    self._settings.append(content)
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
                            content=content,
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
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validate_reserved_ports(self):
        reserved_ports = self.environment[
            osetupcons.SystemEnv.RESERVED_PORTS
        ]
        if reserved_ports:
            self._settings.append(
                "net.ipv4.ip_local_reserved_ports = {}\n".format(
                    ",".join(str(port) for port in sorted(reserved_ports))
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.SYSTEM_SYSCTL_CONFIG_AVAILABLE,
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _misc(self):
        if self._settings:
            content = "".join(
                ["# ovirt-engine configuration.\n"] + self._settings
            )

            sysctl = filetransaction.FileTransaction(
                name=oengcommcons.FileLocations.OVIRT_ENGINE_SYSCTL,
                content=content,
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
