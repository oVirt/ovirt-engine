#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall manager firewalld plugin.
"""

import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import firewall_manager_base

from . import process_firewalld_services


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall manager firewalld plugin.
    """

    class _FirewalldManager(firewall_manager_base.FirewallManagerBase):

        _SERVICE = 'firewalld'

        def __init__(self, plugin):
            super(Plugin._FirewalldManager, self).__init__(plugin)

        @property
        def name(self):
            return osetupcons.Const.FIREWALL_MANAGER_FIREWALLD

        def detect(self):
            return self.plugin.environment[
                otopicons.NetEnv.FIREWALLD_AVAILABLE
            ]

        def active(self):
            return self.plugin.services.status(self._SERVICE)

        def enable(self):
            process_firewalld_services.Process.getInstance(
                environment=self.environment,
            ).process_firewalld_services()
            self.environment[otopicons.NetEnv.FIREWALLD_ENABLE] = True

        def remove(self):
            enable_firewalld = False
            for file in self.environment[osetupcons.RemoveEnv.FILES_TO_REMOVE]:
                if file.startswith(
                    osetupcons.FileLocations.FIREWALLD_SERVICES_DIR
                ):
                    enable_firewalld = True
                    self.environment[
                        otopicons.NetEnv.FIREWALLD_DISABLE_SERVICES
                    ].append(
                        os.path.splitext(
                            os.path.basename(file)
                        )[0]
                    )
            self.environment[
                otopicons.NetEnv.FIREWALLD_ENABLE
            ] = enable_firewalld

        def prepare_examples(self):
            process_firewalld_services.Process.getInstance(
                environment=self.environment,
            ).process_firewalld_services()
            for service in self.environment[
                osetupcons.NetEnv.FIREWALLD_SERVICES
            ]:
                content = self.environment[
                    otopicons.NetEnv.FIREWALLD_SERVICE_PREFIX +
                    service['name']
                ]

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

        def print_manual_configuration_instructions(self):
            commands = []
            for service in [
                key[len(otopicons.NetEnv.FIREWALLD_SERVICE_PREFIX):]
                for key in self.environment
                if key.startswith(
                    otopicons.NetEnv.FIREWALLD_SERVICE_PREFIX
                )
            ]:
                commands.append(
                    'firewall-cmd --permanent --add-service %s' % service
                )
            commands.append('firewall-cmd --reload')
            self.plugin.dialog.note(
                text=_(
                    'In order to configure firewalld, copy the '
                    'files from\n'
                    '    {examples} to {configdir}\n'
                    '    and execute the following commands:\n'
                    '{commands}'
                ).format(
                    examples=(
                        osetupcons.FileLocations.OVIRT_FIREWALLD_EXAMPLE_DIR
                    ),
                    configdir=osetupcons.FileLocations.FIREWALLD_SERVICES_DIR,
                    commands='\n'.join([
                        '    ' + command
                        for command in commands
                    ]),
                )
            )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
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
        before=(
            osetupcons.Stages.KEEP_ONLY_VALID_FIREWALL_MANAGERS,
        ),
    )
    def _setup(self):
        self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGERS
        ].append(Plugin._FirewalldManager(self))


# vim: expandtab tabstop=4 shiftwidth=4
