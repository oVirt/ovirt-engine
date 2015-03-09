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


"""Uninstall plugin."""


import datetime
import gettext
import hashlib
import os

from otopi import constants as otopicons
from otopi import plugin, util

import configparser
from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


class RegisterGroups(object):

    def __init__(self, environment):
        super(RegisterGroups, self).__init__()
        self.environment = environment
        self.config = {}

    def createGroup(self, group, description, optional):
        self.environment.setdefault(
            osetupcons.CoreEnv.FILE_GROUP_PREFIX + group,
            []
        )
        self.environment.setdefault(
            osetupcons.CoreEnv.LINES_GROUP_PREFIX + group,
            {}
        )
        self.config[group] = {}
        self.config[group]['description'] = description
        self.config[group]['optional'] = optional
        return self

    def addFiles(self, group, fileList):
        # Note that we are using append instead of extend because we're
        # usually appending an empty list that will be filled later
        # when the filetransactions will be executed. We need to preserve
        # the object reference!
        self.environment[
            osetupcons.CoreEnv.FILE_GROUP_PREFIX + group
        ].append(fileList)

    def addChanges(self, group, filename, changes):
        self.environment[
            osetupcons.CoreEnv.LINES_GROUP_PREFIX + group
        ].setdefault(filename, []).extend(changes)


@util.export
class Plugin(plugin.PluginBase):
    """Uninstall plugin."""

    def _digestFile(self, filename):
        md5 = hashlib.new('md5')
        # Read file in chunks of 10KB
        with open(filename, 'rb') as f:
            while True:
                data = f.read(10240)
                if not data:
                    break
                md5.update(data)
        return md5.hexdigest()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES,
            []
        )
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ] = RegisterGroups(self.environment)

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _cleanup(self):
        config = configparser.ConfigParser()
        config.optionxform = str

        def _addSection(section, description, optional):
            if not config.has_section(section):
                config.add_section(section)
                config.set(section, 'description', description)
                config.set(section, 'optional', optional)

        def _addFiles(section, files):
            for index, name in enumerate(sorted(set(files))):
                if os.path.exists(name):
                    prefix = 'file.{index:03}'.format(index=index)
                    config.set(
                        section,
                        prefix + '.name',
                        name,
                    )
                    config.set(
                        section,
                        prefix + '.md5',
                        self._digestFile(name),
                    )

        def _addChanges(section, changes):
            for file_index, filename in enumerate(
                sorted(set(changes))
            ):
                for line_index, content in enumerate(
                    sorted(list(changes[filename]))
                ):
                    if os.path.exists(filename):
                        prefix = 'line.{file_index:03}{line_index:03}'.format(
                            file_index=file_index,
                            line_index=line_index,
                        )
                        config.set(
                            section,
                            prefix + '.name',
                            filename,
                        )
                        if 'added' in content:
                            config.set(
                                section,
                                prefix + '.content.added',
                                content['added'],
                            )
                        if 'removed' in content:
                            config.set(
                                section,
                                prefix + '.content.removed',
                                content['removed'],
                            )
        if self.environment[
            otopicons.CoreEnv.MODIFIED_FILES
        ]:
            _addSection(
                osetupcons.Const.FILE_GROUP_SECTION_PREFIX + 'core',
                'Core files',
                False,
            )
            _addFiles(
                osetupcons.Const.FILE_GROUP_SECTION_PREFIX + 'core',
                self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        if self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ]:
            _addSection(
                'unremovable',
                'Unremovable files',
                False,
            )
            _addFiles(
                'unremovable',
                self.environment[
                    osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
                ],
            )

        for section, content in [
            (
                key[len(osetupcons.CoreEnv.FILE_GROUP_PREFIX):],
                content,
            )
            for key, content in self.environment.items()
            if key.startswith(
                osetupcons.CoreEnv.FILE_GROUP_PREFIX
            )
        ]:
            fileList = []
            for x in content:
                fileList.extend(x)
            group_config = self.environment[
                osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
            ].config[section]
            if fileList:
                _addSection(
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX + section,
                    group_config['description'],
                    group_config['optional'],
                )
                _addFiles(
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX + section,
                    fileList,
                )

        for section, changes in [
            (
                key[len(osetupcons.CoreEnv.LINES_GROUP_PREFIX):],
                changes,
            )
            for key, changes in self.environment.items()
            if key.startswith(
                osetupcons.CoreEnv.LINES_GROUP_PREFIX
            )
        ]:
            group_config = self.environment[
                osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
            ].config[section]
            if changes:
                _addSection(
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX + section,
                    group_config['description'],
                    group_config['optional'],
                )
                _addChanges(
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX + section,
                    changes,
                )

        output = os.path.join(
            osetupcons.FileLocations.OVIRT_SETUP_UNINSTALL_DIR,
            '%s-uninstall.conf' % (
                datetime.datetime.now().strftime('%Y%m%d%H%M%S'),
            ),
        )
        if not os.path.exists(os.path.dirname(output)):
            os.makedirs(os.path.dirname(output))
        if config.sections():
            # avoid to create empty uninstall files
            with open(output, 'w') as f:
                config.write(f)


# vim: expandtab tabstop=4 shiftwidth=4
