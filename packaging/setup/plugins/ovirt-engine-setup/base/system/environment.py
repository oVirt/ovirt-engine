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


"""Environment plugin."""


import gettext
import grp
import os
import pwd

from otopi import plugin, util

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
