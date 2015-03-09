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
import os
import pwd

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


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
            rootUser = apacheUser = pwd.getpwuid(os.geteuid())[0]
        else:
            rootUser = oengcommcons.Defaults.DEFAULT_SYSTEM_USER_ROOT
            apacheUser = oengcommcons.Defaults.DEFAULT_SYSTEM_USER_APACHE

        self.environment.setdefault(
            oengcommcons.SystemEnv.USER_ROOT,
            rootUser
        )
        self.environment.setdefault(
            oengcommcons.SystemEnv.USER_APACHE,
            apacheUser
        )
        self.environment.setdefault(
            oengcommcons.SystemEnv.USER_VDSM,
            oengcommcons.Defaults.DEFAULT_SYSTEM_USER_VDSM
        )
        self.environment.setdefault(
            oengcommcons.SystemEnv.GROUP_KVM,
            oengcommcons.Defaults.DEFAULT_SYSTEM_GROUP_KVM
        )
        self.environment.setdefault(
            oengcommcons.SystemEnv.USER_POSTGRES,
            oengcommcons.Defaults.DEFAULT_SYSTEM_USER_POSTGRES
        )


# vim: expandtab tabstop=4 shiftwidth=4
