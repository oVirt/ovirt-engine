#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Firewall manager selection plugin.
"""

import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall manager selection plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._detected_managers = []
        self._available_managers = []
        self._selected_manager = None

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.FIREWALL_MANAGER,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.UPDATE_FIREWALL,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.FIREWALL_CHANGES_REVIEW,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.VALID_FIREWALL_MANAGERS,
            ''
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=osetupcons.Stages.KEEP_ONLY_VALID_FIREWALL_MANAGERS,
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.VALID_FIREWALL_MANAGERS
        ],
    )
    def _keep_only_valid_firewall_managers(self):
        valid_managers = [
            x.strip()
            for x in self.environment[
                osetupcons.ConfigEnv.VALID_FIREWALL_MANAGERS
            ].split(',')
        ]
        # Note: valid_managers is just the names (parsed out of
        # env[VALID_FIREWALL_MANAGERS], which is a string), whereas
        # env[FIREWALL_MANAGERS], as well as later lists in this file
        # with 'managers' in their name, are lists of firewall manager
        # objects.
        self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGERS] = [
            m
            for m in self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGERS]
            if m.name in valid_managers or not m.selectable()
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ),
    )
    def _customization_is_requested(self):
        self._detected_managers = [
            m
            for m in self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGERS]
            if m.selectable() and m.detect()
        ]

        if self.environment[
            osetupcons.ConfigEnv.UPDATE_FIREWALL
        ] is None:
            if not self._detected_managers:
                self.environment[osetupcons.ConfigEnv.UPDATE_FIREWALL] = False
            else:
                self.dialog.note(
                    text=_(
                        '\nSetup can automatically configure the firewall '
                        'on this system.\n'
                        'Note: automatic configuration of the firewall may '
                        'overwrite current settings.\n'
                    ),
                )
                self.environment[
                    osetupcons.ConfigEnv.UPDATE_FIREWALL
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_UPDATE_FIREWALL',
                    note=_(
                        'Do you want Setup to configure the firewall? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    true=_('Yes'),
                    false=_('No'),
                    default=True,
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.UPDATE_FIREWALL
        ],
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ),
    )
    def _customization(self):
        active_managers = [m for m in self._detected_managers if m.active()]

        self._available_managers = (
            active_managers if active_managers
            else self._detected_managers
        )

        if (
            self.environment[
                osetupcons.ConfigEnv.FIREWALL_MANAGER
            ] is not None and
            self.environment[
                osetupcons.ConfigEnv.FIREWALL_MANAGER
            ] not in [
                m.name
                for m in self._available_managers
            ]
        ):
            self.logger.warning(
                _(
                    "Firewall manager was previously set to '{m}', which is "
                    "currently not available."
                ).format(
                    m=self.environment[
                        osetupcons.ConfigEnv.FIREWALL_MANAGER
                    ],
                )
            )
            self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGER] = None

        if self.environment[osetupcons.ConfigEnv.FIREWALL_MANAGER] is None:
            if active_managers and len(self._available_managers) == 1:
                self.environment[
                    osetupcons.ConfigEnv.FIREWALL_MANAGER
                ] = self._available_managers[0].name
            else:
                self.dialog.note(
                    text=_(
                        'The following firewall managers were detected on '
                        'this system: {managers}\n'
                    ).format(
                        managers=', '.join(
                            m.name
                            for m in self._available_managers
                        ),
                    ),
                )
                self.environment[
                    osetupcons.ConfigEnv.FIREWALL_MANAGER
                ] = self.dialog.queryString(
                    name='OVESETUP_CONFIG_FIREWALL_MANAGER',
                    note=_(
                        'Firewall manager to configure '
                        '(@VALUES@): '
                    ),
                    prompt=True,
                    validValues=self._available_managers,
                    caseSensitive=False,
                )
        self.logger.info(
            _('{manager} will be configured as firewall manager.').format(
                manager=self.environment[
                    osetupcons.ConfigEnv.FIREWALL_MANAGER
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.UPDATE_FIREWALL
        ],
        before=(
            otopicons.Stages.FIREWALLD_VALIDATION,
            otopicons.Stages.IPTABLES_VALIDATION,
        ),
    )
    def _validation(self):
        if self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGER
        ] not in [m.name for m in self._available_managers]:
            raise RuntimeError(
                _(
                    'Firewall manager {manager} is not available'
                ).format(
                    manager=self.environment[
                        osetupcons.ConfigEnv.FIREWALL_MANAGER
                    ],
                ),
            )
        self._selected_manager = [
            m for m in self._available_managers
            if m.name == self.environment[
                osetupcons.ConfigEnv.FIREWALL_MANAGER
            ]
        ][0]
        self._selected_manager.enable()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.UPDATE_FIREWALL
        ],
        after=(
            otopicons.Stages.FIREWALLD_VALIDATION,
            otopicons.Stages.IPTABLES_VALIDATION,
        ),
    )
    def _review_config(self):
        self._selected_manager.review_config()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _prepare_examples(self):
        for manager in self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGERS
        ]:
            manager.prepare_examples()

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGER
        ] is None
    )
    def _closeup(self):
        for manager in self.environment[
            osetupcons.ConfigEnv.FIREWALL_MANAGERS
        ]:
            manager.print_manual_configuration_instructions()


# vim: expandtab tabstop=4 shiftwidth=4
