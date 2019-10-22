#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Jboss plugin."""


import gettext
import os

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """JBoss plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_HOME,
            oengcommcons.FileLocations.JBOSS_HOME
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_NEEDED,
            False
        )
        self.environment.setdefault(
            oengcommcons.RPMDistroEnv.OVIRT_JBOSS_PACKAGES,
            '',
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DISTRO_RPM_PACKAGE_UPDATE_CHECK,
        ),
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.JBOSS_NEEDED
        ],
    )
    def _version_lock_customization(self):
        def tolist(s):
            if not s:
                return []
            return [e.strip() for e in s.split(',')]

        pkglist = tolist(
            self.environment[
                oengcommcons.RPMDistroEnv.OVIRT_JBOSS_PACKAGES
            ]
        )
        if pkglist:
            self.environment[
                osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER
            ].extend(pkglist)
            self.environment[
                osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY
            ].extend(pkglist)
            self.environment[
                osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
            ].append(
                {
                    'packages': pkglist,
                },
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_FIRST,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.JBOSS_NEEDED
        ],
    )
    def _jboss(self):
        """
        Check JBOSS_HOME after ovirt-engine upgrade since jboss may be
        upgraded as well and JBOSS_HOME may become invalid.
        This can't be done at package stage since yum transaction is committed
        as last action in that stage.
        """
        if not os.path.exists(
            self.environment[
                oengcommcons.ConfigEnv.JBOSS_HOME
            ]
        ):
            raise RuntimeError(
                _('Cannot find Jboss at {jbossHome}').format(
                    jbossHome=self.environment[
                        oengcommcons.ConfigEnv.JBOSS_HOME
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
