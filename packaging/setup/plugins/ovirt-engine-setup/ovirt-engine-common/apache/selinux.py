#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2014 Red Hat, Inc.
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


"""Apache selinux plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


@util.export
class Plugin(plugin.PluginBase):
    """Apache selinux plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('selinuxenabled')
        self.command.detect('semanage')
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
        priority=plugin.Stages.PRIORITY_HIGH
    )
    def _validation_enable(self):
        if not self.environment[oengcommcons.ApacheEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        if self.command.get('selinuxenabled', optional=True) is None:
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
    )
    def _misc(self):
        command = (
            self.command.get('semanage'),
            'boolean',
            '--modify',
            '--on',
            'httpd_can_network_connect',
        )
        rc, stdout, stderr = self.execute(
            command,
            raiseOnError=False,
        )
        if rc != 0:
            self.logger.warning(
                _(
                    'Failed to modify httpd selinux context, please make '
                    'sure httpd_can_network_connect is set.'
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
