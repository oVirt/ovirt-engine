#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall manager iptables plugin.
"""

import difflib
import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import firewall_manager_base

from ovirt_setup_lib import dialog

from . import process_firewalld_services


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall manager iptables plugin.
    """

    class _IpTablesManager(firewall_manager_base.FirewallManagerBase):

        _SERVICE = 'iptables'

        def _get_rules(self):
            if self._rules is None:
                self._rules = outil.processTemplate(
                    osetupcons.FileLocations.OVIRT_IPTABLES_DEFAULT,
                    subst={
                        '@CUSTOM_RULES@': (
                            process_firewalld_services.Process.getInstance(
                                environment=self.environment,
                            ).parseFirewalld(
                                format=(
                                    '-A INPUT -p {protocol} -m state '
                                    '--state NEW -m {protocol} '
                                    '--dport {port} -j ACCEPT\n'
                                ),
                                portSeparator=':',
                            )
                        ),
                    }
                )
            return self._rules

        def __init__(self, plugin):
            super(Plugin._IpTablesManager, self).__init__(plugin)
            self._rules = None

        @property
        def name(self):
            return osetupcons.Const.FIREWALL_MANAGER_IPTABLES

        def detect(self):
            return self.plugin.services.exists(self._SERVICE)

        def active(self):
            return self.plugin.services.status(self._SERVICE)

        def prepare_examples(self):
            content = self._get_rules()
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=osetupcons.FileLocations.OVIRT_IPTABLES_EXAMPLE,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

        def enable(self):
            self.environment[otopicons.NetEnv.IPTABLES_ENABLE] = True
            self.environment[
                otopicons.NetEnv.IPTABLES_RULES
            ] = self._get_rules()
            # This file is updated by otopi. Here we just prevent it from
            # being deleted on cleanup.
            # TODO: copy/move some uninstall code from the engine to otopi
            # to allow just adding lines to iptables instead of replacing
            # the file and also remove these lines on cleanup.
            self.environment[
                osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
            ].append(
                osetupcons.FileLocations.SYSCONFIG_IPTABLES,
            )

        def print_manual_configuration_instructions(self):
            self.plugin.dialog.note(
                text=_(
                    'An example of the required configuration for iptables '
                    'can be found at:\n'
                    '    {example}'
                ).format(
                    example=osetupcons.FileLocations.OVIRT_IPTABLES_EXAMPLE
                )
            )

        def review_config(self):
            diff_lines = ''
            current_rules = ''
            current_modified_since_prev_setup = False
            interactive = True
            if os.path.isfile(osetupcons.FileLocations.SYSCONFIG_IPTABLES):
                with open(
                        osetupcons.FileLocations.SYSCONFIG_IPTABLES,
                        'r'
                ) as current:
                    current_rules = current.read().splitlines()
            if os.path.isfile(osetupcons.FileLocations.OVIRT_IPTABLES_EXAMPLE):
                with open(
                        osetupcons.FileLocations.OVIRT_IPTABLES_EXAMPLE,
                        'r'
                ) as prev_setup_example:
                    prev_setup_rules = prev_setup_example.read().splitlines()
                    diff_prev_cur = difflib.unified_diff(
                        current_rules,
                        prev_setup_rules,
                        lineterm='',
                    )
                    diff_lines = '\n'.join(diff_prev_cur)
                    if len(diff_lines) > 0:
                        current_modified_since_prev_setup = True
            diff = difflib.unified_diff(
                current_rules,
                self._get_rules().splitlines(),
                lineterm='',
                fromfile=_('current'),
                tofile=_('proposed'),
            )
            diff_lines = '\n'.join(diff)
            if len(diff_lines) > 0:
                if current_modified_since_prev_setup:
                    self.logger.warning(
                        _(
                            "It seams that previously generated iptables "
                            "configuration was manually edited,\n"
                            "please carefully review the proposed "
                            "configuration"
                        )
                    )
                if self.environment[
                    osetupcons.ConfigEnv.FIREWALL_CHANGES_REVIEW
                ] is None:
                    self.environment[
                        osetupcons.ConfigEnv.FIREWALL_CHANGES_REVIEW
                    ] = dialog.queryBoolean(
                        dialog=self.plugin.dialog,
                        name='OVESETUP_REVIEW_IPTABLES_CHANGES',
                        note=_(
                            'Generated iptables rules are different '
                            'from current ones.\n'
                            'Do you want to review them? '
                            '(@VALUES@) [@DEFAULT@]: '
                        ),
                        prompt=True,
                        true=_('Yes'),
                        false=_('No'),
                        default=False,
                    )
                else:
                    interactive = False

                if self.environment[
                    osetupcons.ConfigEnv.FIREWALL_CHANGES_REVIEW
                ]:
                    if not interactive:
                        self.plugin.dialog.note(
                            text=_(
                                'These are the changes that will be applied to'
                                ' iptables configuration:\n'
                                '{diff}\n\n'
                            ).format(
                                diff=diff_lines,
                            )
                        )
                    else:
                        confirmed = dialog.queryBoolean(
                            dialog=self.plugin.dialog,
                            name='OVESETUP_CONFIRM_IPTABLES_CHANGES',
                            note=_(
                                'Please review the changes:\n\n'
                                '{diff}\n\n'
                                'Do you want to proceed with firewall '
                                'configuration? '
                                '(@VALUES@) [@DEFAULT@]: '
                            ).format(
                                diff=diff_lines
                            ),
                            prompt=True,
                            true=_('Yes'),
                            false=_('No'),
                            default=True,
                        )
                        if not confirmed:
                            raise RuntimeError(
                                _(
                                    'iptables proposed configuration '
                                    'was rejected by user'
                                )
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
        ].append(Plugin._IpTablesManager(self))


# vim: expandtab tabstop=4 shiftwidth=4
