#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Environment plugin."""


import gettext
import grp
import os
import pwd

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Environment plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            engineUser = pwd.getpwuid(os.geteuid())[0]
            engineGroup = grp.getgrgid(os.getegid())[0]
        else:
            engineUser = osetupcons.Defaults.DEFAULT_SYSTEM_USER_ENGINE
            engineGroup = osetupcons.Defaults.DEFAULT_SYSTEM_GROUP_ENGINE

        self.environment.setdefault(
            osetupcons.SystemEnv.USER_ENGINE,
            engineUser
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.GROUP_ENGINE,
            engineGroup
        )


# vim: expandtab tabstop=4 shiftwidth=4
