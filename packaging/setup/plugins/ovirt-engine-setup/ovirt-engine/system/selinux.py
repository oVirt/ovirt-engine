#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
SELinux configuration plugin.
"""

import gettext
import re


from os import listdir
from os.path import isfile
from os.path import join

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    SELinux configuration plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS] = []
        self.environment[osetupcons.SystemEnv.SELINUX_RESTORE_PATHS] = []
        self.environment[osetupcons.SystemEnv.SELINUX_BOOLEANS] = []
        self.environment[osetupcons.SystemEnv.SELINUX_PORTS] = []

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _setup(self):
        self.command.detect('selinuxenabled')
        self.command.detect('semanage')
        self.command.detect('semodule')
        self.command.detect('restorecon')

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        if (
            self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] or
            self.command.get('selinuxenabled', optional=True) is None
        ):
            self._enabled = False
        else:
            rc, stdout, stderr = self.execute(
                (
                    self.command.get('selinuxenabled'),
                ),
                raiseOnError=False,
            )
            self._enabled = rc == 0

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
        name=osetupcons.Stages.SETUP_SELINUX,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):
        selinux_dir = oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_SELINUX
        for f in listdir(selinux_dir):
            file_path = join(selinux_dir, f)
            if isfile(file_path):
                self.logger.info(
                    _(
                        'Install selinux module {}'.format(file_path)
                    )
                )
                rc, stdout, stderr = self.execute(
                    (
                        self.command.get('semodule'),
                        '-i', file_path
                    )
                )
                if rc != 0:
                    self.logger.info(
                        _('Failed to apply SELINUX file {f}'.format(f=f))
                    )
        for entry in self.environment[osetupcons.SystemEnv.SELINUX_PORTS]:
            rc, stdout, stderr = self.execute(
                (self.command.get('semanage'), 'port', '-l')
            )
            if not any(
                re.match(
                    '{t}.*{p}'.format(t=entry['type'], p=entry['port']),
                    line
                ) for line in stdout
            ):
                rc, stdout, stderr = self.execute(
                    (
                        self.command.get('semanage'),
                        'port',
                        '-a',
                        '-t', entry['type'],
                        '-p', entry['protocol'],
                        entry['port']
                    ),
                )
        for entry in self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS]:
            rc, stdout, stderr = self.execute(
                (self.command.get('semanage'), 'fcontext', '-C', '-l')
            )
            if not any(
                re.match(
                    '{p}.*{t}'.format(p=entry['pattern'], t=entry['type']),
                    line
                ) for line in stdout
            ):
                rc, stdout, stderr = self.execute(
                    (
                        self.command.get('semanage'),
                        'fcontext',
                        '-a',
                        '-t', entry['type'],
                        entry['pattern']
                    )
                )
        for path in self.environment[
            osetupcons.SystemEnv.SELINUX_RESTORE_PATHS
        ]:
            rc, stdout, stderr = self.execute(
                (
                    self.command.get('restorecon'),
                    '-r',
                    path
                )
            )
            if rc != 0:
                self.logger.error(
                    _('Failed to refresh SELINUX context for {path}').format(
                        path=path
                    )
                )
        for entry in self.environment[osetupcons.SystemEnv.SELINUX_BOOLEANS]:
            rc, stdout, stderr = self.execute(
                (
                    self.command.get('semanage'),
                    'boolean',
                    '--modify',
                    '--{state}'.format(state=entry['state']),
                    entry['boolean']
                )
            )
            if rc != 0:
                self.logger.error(
                    _(
                        'Failed to modify selinux boolean {boolean}, please '
                        'make sure it is set to {state}.'
                    ).format(
                        boolean=entry['boolean'],
                        state=entry['state'],
                    )
                )


# vim: expandtab tabstop=4 shiftwidth=4
