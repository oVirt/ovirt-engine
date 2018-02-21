#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2018 Red Hat, Inc.
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


"""Versionlock configuration plugin."""


import os
import platform

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil

YUM_VERSIONLOCK_CONF = (
    osetupcons.FileLocations.
    OVIRT_ENGINE_YUM_VERSIONLOCK_CONF
)


@util.export
class Plugin(plugin.PluginBase):
    """Versionlock configuration plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            os.path.exists(YUM_VERSIONLOCK_CONF) and
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] and
            not self.environment[osetupcons.CoreEnv.OFFLINE_PACKAGER] and
            platform.linux_distribution(full_distribution_name=0)[0] in (
                'redhat',
                'centos'
            )
        )
    )
    def _misc(self):
        changed_lines = []
        with open(YUM_VERSIONLOCK_CONF, 'r') as f:
            content = f.read().splitlines()
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=YUM_VERSIONLOCK_CONF,
                content=osetuputil.editConfigContent(
                    content=content,
                    params={
                        'follow_obsoletes': '1',
                    },
                    changed_lines=changed_lines,
                    new_line_tpl='{spaces}{param}={value}',
                ),
            )
        )
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='versionlock_conf',
            description='Versionlock Plugin Configuration',
            optional=True
        ).addChanges(
            'versionlock_conf',
            YUM_VERSIONLOCK_CONF,
            changed_lines,
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(
            YUM_VERSIONLOCK_CONF
        )


# vim: expandtab tabstop=4 shiftwidth=4
