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


"""Firewalld plugin."""


import os


from otopi import util
from otopi import plugin
from otopi import constants as otopicons


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Firewalld plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        name=osetupcons.Stages.REMOVE_FIREWALLD_SERVICES,
        # TODO: Add:
        # before=(
        #    otopicons.Stages.FIREWALLD_VALIDATION,
        #),
        # and remove:
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _validation(self):
        enable_firewalld = False
        for file in self.environment[osetupcons.RemoveEnv.FILES_TO_REMOVE]:
            if file.startswith(
                osetupcons.FileLocations.FIREWALLD_SERVICES_DIR
            ):
                enable_firewalld = True
                self.environment[
                    otopicons.NetEnv.FIREWALLD_DISABLE_SERVICES
                ].append(
                    os.path.splitext(
                        os.path.basename(file)
                    )[0]
                )
        self.environment[otopicons.NetEnv.FIREWALLD_ENABLE] = enable_firewalld


# vim: expandtab tabstop=4 shiftwidth=4
