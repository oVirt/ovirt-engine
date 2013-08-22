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


"""Upgrade firewalld configuration from legacy plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Upgrade firewalld configuration from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
        # TODO: add:
        # before=(
        #    otopicons.Stages.FIREWALLD_VALIDATION,
        #),
        # and remove:
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _validation(self):
        if os.path.exists(
            osetupcons.FileLocations.LEGACY_FIREWALLD_SERVICE_FILE
        ):
            self.environment[
                otopicons.NetEnv.FIREWALLD_DISABLE_SERVICES
            ].append(
                os.path.splitext(
                    os.path.basename(
                        osetupcons.FileLocations.
                        LEGACY_FIREWALLD_SERVICE_FILE
                    )
                )[0]
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _closeup(self):
        #Priority low because we want to remove the file after firewalld
        #plugin closeup
        if os.path.exists(
            osetupcons.FileLocations.LEGACY_FIREWALLD_SERVICE_FILE
        ):
            os.remove(osetupcons.FileLocations.LEGACY_FIREWALLD_SERVICE_FILE)


# vim: expandtab tabstop=4 shiftwidth=4
