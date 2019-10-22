#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""
Package upgrade plugin.
"""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Package upgrade plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.RPMDistroEnv.ENGINE_PACKAGES,
            oenginecons.Const.ENGINE_PACKAGE_NAME
        )
        self.environment.setdefault(
            oenginecons.RPMDistroEnv.ENGINE_SETUP_PACKAGES,
            oenginecons.Const.ENGINE_PACKAGE_SETUP_NAME
        )
        self.environment.setdefault(
            oenginecons.RPMDistroEnv.ADDITIONAL_PACKAGES,
            oenginecons.Defaults.DEFAULT_ADDITIONAL_PACKAGES
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            oenginecons.Stages.CORE_ENABLE,
        ),
        before=(
            osetupcons.Stages.DISTRO_RPM_PACKAGE_UPDATE_CHECK,
        ),
    )
    def _customization(self):
        def tolist(s):
            if not s:
                return []
            return [e.strip() for e in s.split(',')]

        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_SETUP
        ].extend(
            tolist(
                self.environment[
                    oenginecons.RPMDistroEnv.ENGINE_SETUP_PACKAGES
                ]
            )
        )

        if self.environment[oenginecons.CoreEnv.ENABLE]:
            self.environment[
                osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER
            ].extend(
                tolist(
                    self.environment[oenginecons.RPMDistroEnv.ENGINE_PACKAGES]
                )
            )
            self.environment[
                osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY
            ].extend(
                [
                    '%s%s' % (prefix, suffix)
                    for prefix in tolist(
                        self.environment[
                            oenginecons.RPMDistroEnv.ENGINE_PACKAGES
                        ]
                    )
                    for suffix in osetupcons.Const.RPM_LOCK_LIST_SUFFIXES
                ]
            )
            self.environment[
                osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
            ].append(
                {
                    'packages': (
                        tolist(
                            self.environment[
                                oenginecons.RPMDistroEnv.ENGINE_PACKAGES
                            ]
                        ) + tolist(
                            self.environment[
                                oenginecons.RPMDistroEnv.ADDITIONAL_PACKAGES
                            ]
                        )
                    ),
                },
            )


# vim: expandtab tabstop=4 shiftwidth=4
