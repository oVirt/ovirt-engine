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
Package upgrade plugin.
"""

import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """
    Package upgrade plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER
        ].append(osetupcons.Const.ENGINE_PACKAGE_NAME)
        self.environment[
            osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY
        ].extend(osetupcons.Const.RPM_LOCK_LIST)
        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
        ].append(
            {
                'group': osetupcons.Const.UPGRADE_YUM_GROUP,
                'packages': (osetupcons.Const.ENGINE_PACKAGE_NAME,),
            },
        )
        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_SETUP
        ].append(
            osetupcons.Const.ENGINE_PACKAGE_SETUP_NAME,
        )


# vim: expandtab tabstop=4 shiftwidth=4
