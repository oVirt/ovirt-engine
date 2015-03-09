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
hostile services handler plugin.
"""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    hostile services handler plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.SystemEnv.HOSTILE_SERVICES,
            'ovirt-engine-dwhd,ovirt-engine-notifier'
        )
        self._enabled = False
        self._toStart = []

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _setup(self):
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        name=osetupcons.Stages.SYSTEM_HOSTILE_SERVICES_DETECTION,
        condition=lambda self: self._enabled,
    )
    def _transaction_begin(self):
        for service in self.environment[
            osetupcons.SystemEnv.HOSTILE_SERVICES
        ].split(','):
            service = service.strip()
            if self.services.exists(service):
                if self.services.status(
                    name=service,
                ):
                    self._toStart.append(service)
                    self.services.state(
                        name=service,
                        state=False,
                    )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            self._enabled and
            self.environment[
                osetupcons.CoreEnv.ACTION
            ] != osetupcons.Const.ACTION_REMOVE
        ),
    )
    def _closeup(self):
        """
        Ensure that services we stopped while upgrading are
        restarted and will start at reboot.
        """
        for service in self._toStart:
            if self.services.exists(service):
                self.services.state(
                    name=service,
                    state=True
                )
                # See https://bugzilla.redhat.com/1083551
                self.services.startup(
                    name=service,
                    state=True
                )


# vim: expandtab tabstop=4 shiftwidth=4
