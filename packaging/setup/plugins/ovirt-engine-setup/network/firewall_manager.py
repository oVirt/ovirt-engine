#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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
Firewall manager selection plugin.
"""

import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


import libxml2


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall manager selection plugin.
    """

    def _isPermanentSupported(self):
        """
        check if firewall-cmd support --permanent option
        """
        rc, stdout, stderr = self.execute(
            (
                self.command.get('firewall-cmd'),
                '--help',
            ),
            raiseOnError=False,
        )
        return ''.join(stdout).find('--permanent') != -1

    def _parseFirewalld(self, format):
        ret = ''
        for content in [
            content
            for key, content in self.environment.items()
            if key.startswith(
                osetupcons.NetEnv.FIREWALLD_SERVICE_PREFIX
            )
        ]:
            doc = None
            ctx = None
            try:
                doc = libxml2.parseDoc(content)
                ctx = doc.xpathNewContext()
                nodes = ctx.xpathEval("/service/port")
                for node in nodes:
                    ret += format.format(
                        protocol=node.prop('protocol'),
                        port=node.prop('port'),
                    )
            finally:
                if doc is not None:
                    doc.freeDoc()
                if ctx is not None:
                    ctx.xpathFreeContext()

        return ret

    def _createIptablesConfig(self):
        return osetuputil.processTemplate(
            osetupcons.FileLocations.OVIRT_IPTABLES_DEFAULT,
            subst={
                '@CUSTOM_RULES@': self._parseFirewalld(
                    format=(
                        '-A INPUT -p {protocol} -m state --state NEW '
                        '-m {protocol} --dport {port} -j ACCEPT\n'
                    )
                )
            }
        )

    def _createHumanConfig(self):
        return '\n'.join(
            sorted(
                self._parseFirewalld(
                    format='{protocol}:{port}\n',
                ).splitlines()
            )
        ) + '\n'

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.FIREWALL_MANAGER,
            None
        )
        self.environment.setdefault(
            osetupcons.NetEnv.FIREWALLD_SERVICES,
            []
        )
        self.environment.setdefault(
            osetupcons.NetEnv.FIREWALLD_SUBST,
            {}
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]
        self.command.detect('firewall-cmd')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
        condition=lambda self: self._enabled,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ],
    )
    def _customization(self):
        if self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGER] is None:
            managers = []
            if self.services.exists('firewalld'):
                if self._isPermanentSupported():
                    managers.append('firewalld')
            if self.services.exists('iptables'):
                managers.append('iptables')

            for manager in managers:
                response = self.dialog.queryString(
                    name='OVESETUP_CONFIG_FIREWALL_MANAGER',
                    note=_(
                        '{manager} was detected on your computer, '
                        'do you wish setup to configure it? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        manager=manager,
                    ),
                    prompt=True,
                    validValues=(_('yes'), _('no')),
                    caseSensitive=False,
                    default=_('yes'),
                )
                if response == _('yes'):
                    self.environment[
                        osetupcons.ConfigEnv.FIREWALL_MANAGER
                    ] = manager
                    break

        self.environment[otopicons.NetEnv.IPTABLES_ENABLE] = (
            self.environment[
                osetupcons.ConfigEnv.FIREWALL_MANAGER
            ] == 'iptables'
        )
        self.environment[osetupcons.NetEnv.FIREWALLD_ENABLE] = (
            self.environment[
                osetupcons.ConfigEnv.FIREWALL_MANAGER
            ] == 'firewalld'
        )

    @plugin.event(
        # must be at customization as otopi modules
        # need a chance to validate content
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.NET_FIREWALL_MANAGER_PROCESS_TEMPLATES,
        priority=plugin.Stages.PRIORITY_LOW,
        after=[
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
        ],
        # must be always enabled to create examples
    )
    def _process_templates(self):
        for service in self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES]:
            content = osetuputil.processTemplate(
                template=os.path.join(
                    osetupcons.FileLocations.OVIRT_FIREWALLD_CONFIG,
                    service['directory'],
                    '%s.xml.in' % service['name'],
                ),
                subst=self.environment[osetupcons.NetEnv.FIREWALLD_SUBST],
            )

            self.environment[
                osetupcons.NetEnv.FIREWALLD_SERVICE_PREFIX +
                service['name']
            ] = content

            target = os.path.join(
                osetupcons.FileLocations.OVIRT_FIREWALLD_EXAMPLE_DIR,
                '%s.xml' % service['name']
            )

            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=target,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

        self.environment[
            otopicons.NetEnv.IPTABLES_RULES
        ] = self._createIptablesConfig()

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.OVIRT_IPTABLES_EXAMPLE,
                content=self.environment[otopicons.NetEnv.IPTABLES_RULES],
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ],
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGER
        ] is None
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'The following network ports should be opened:\n'
                '{ports}'
            ).format(
                ports='\n'.join([
                    '    ' + l
                    for l in self._createHumanConfig().splitlines()
                ]),
            ),
        )

        self.dialog.note(
            text=_(
                'An example of the required configuration for iptables '
                'can be found at:\n'
                '    {example}'
            ).format(
                example=osetupcons.FileLocations.OVIRT_IPTABLES_EXAMPLE
            )
        )

        commands = []
        for service in [
            key[len(osetupcons.NetEnv.FIREWALLD_SERVICE_PREFIX):]
            for key in self.environment
            if key.startswith(
                osetupcons.NetEnv.FIREWALLD_SERVICE_PREFIX
            )
        ]:
            commands.append('firewall-cmd --add-service %s' % service)
        self.dialog.note(
            text=_(
                'In order to configure firewalld, copy the '
                'files from\n'
                '{examples} to {configdir}\n'
                'and execute the following commands:\n'
                '{commands}'
            ).format(
                examples=(
                    osetupcons.FileLocations.OVIRT_FIREWALLD_EXAMPLE_DIR
                ),
                configdir=osetupcons.FileLocations.FIREWALLD_SERVICE_DIR,
                commands='\n'.join([
                    '    ' + l
                    for l in commands
                ]),
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
