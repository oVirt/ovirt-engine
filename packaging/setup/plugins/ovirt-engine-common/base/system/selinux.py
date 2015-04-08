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
SELinux configuration plugin.
"""

import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons


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

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _setup(self):
        self.command.detect('selinuxenabled')
        self.command.detect('semanage')
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
        for entry in self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS]:
            rc, stdout, stderr = self.execute(
                (
                    self.command.get('semanage'),
                    'fcontext',
                    '-a',
                    '-t', entry['type'],
                    entry['pattern']
                )
            )
            if rc != 0:
                self.logger.error(
                    _('Failed to set SELINUX policy for {pattern}').format(
                        pattern=entry['pattern']
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
