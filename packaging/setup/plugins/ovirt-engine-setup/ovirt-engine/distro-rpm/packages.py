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
Package upgrade plugin.
"""

import gettext

from otopi import plugin, util

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
            oenginecons.RPMDistroEnv.UPGRADE_YUM_GROUP,
            oenginecons.Const.UPGRADE_YUM_GROUP_NAME
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
                    'group': self.environment[
                        oenginecons.RPMDistroEnv.UPGRADE_YUM_GROUP
                    ],
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
