#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
#


"""firewalld handler plugin."""

import os
import platform
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """iptables updater.

    Environment:
        NetEnv.FIREWALLD_ENABLE -- enable firewalld update
        NetEnv.FIREWALLD_SERVICE_PREFIX -- services key=service value=content

    """

    def _get_active_zones(self):
        rc, stdout, stderr = self.execute(
            (
                self.command.get('firewall-cmd'),
                '--get-active-zones',
            ),
        )
        zones = {}
        for line in stdout:
            zone_name, devices = line.split(':')
            zones[zone_name] = devices.split()
        return zones

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._distribution = platform.linux_distribution(
            full_distribution_name=0
        )[0]
        self._enabled = True
        self._services = []

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.NetEnv.FIREWALLD_ENABLE,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _setup(self):
        self.command.detect('firewall-cmd')
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        self._enabled = self.environment[
            osetupcons.NetEnv.FIREWALLD_ENABLE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        for service, content in [
            (
                key[len(osetupcons.NetEnv.FIREWALLD_SERVICE_PREFIX):],
                content,
            )
            for key, content in self.environment.items()
            if key.startswith(
                osetupcons.NetEnv.FIREWALLD_SERVICE_PREFIX
            )
        ]:
            self._services.append(service)
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=os.path.join(
                        osetupcons.FileLocations.FIREWALLD_SERVICE_DIR,
                        '%s.xml' % service,
                    ),
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        # avoid conflicts, diable iptables
        if self.services.exists(name='iptables'):
            self.services.startup(name='iptables', state=False)
            self.services.state(name='iptables', state=False)

        self.services.state(
            name='firewalld',
            state=True,
        )
        self.services.startup(name='firewalld', state=True)
        #Ensure to load the newly written services if firewalld was already
        #running.
        self.execute(
            (
                self.command.get('firewall-cmd'),
                '--reload'
            )
        )
        for zone in self._get_active_zones():
            for service in self._services:
                self.execute(
                    (
                        self.command.get('firewall-cmd'),
                        '--zone', zone,
                        '--permanent',
                        '--add-service', service,
                    ),
                )
        self.execute(
            (
                self.command.get('firewall-cmd'),
                '--reload'
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
