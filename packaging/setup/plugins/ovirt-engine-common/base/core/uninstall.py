#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Uninstall plugin."""


import configparser
import datetime
import gettext
import glob
import hashlib
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


_HASH_ALGORITHMS = ('sha256', 'md5')


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

    def _digestFile(self, filename, algo):
        hashobj = hashlib.new(algo)
        # Read file in chunks of 10KB
        with open(filename, 'rb') as f:
            while True:
                data = f.read(10240)
                if not data:
                    break
                hashobj.update(data)
        return hashobj.hexdigest()

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
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNINSTALL_FILES
        ] = sorted(
            glob.glob(
                os.path.join(
                    osetupcons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
                    '*.conf',
                )
            )
        )
        self.environment[osetupcons.CoreEnv.UNINSTALL_GROUPS_DESCRIPTIONS] = {}

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        uninstall_files = {}
        uninstall_lines = {}
        for info in self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNINSTALL_FILES
        ]:
            config = configparser.ConfigParser()
            config.optionxform = str
            config.read([info])
            for section in config.sections():
                if section.startswith(
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX
                ):
                    group = section[
                        len(osetupcons.Const.FILE_GROUP_SECTION_PREFIX):
                    ]
                    description = config.get(section, 'description')
                    template = "%s.description" % group
                    msg = gettext.dgettext(
                        message=template,
                        domain='ovirt-engine-setup'
                    )
                    if msg == template:
                        msg = description
                    self.environment[
                        osetupcons.CoreEnv.UNINSTALL_GROUPS_DESCRIPTIONS
                    ][group] = msg

            def updateFiles(section):
                files = {}
                group = section[
                    len(osetupcons.Const.FILE_GROUP_SECTION_PREFIX):
                ] if section.startswith(
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX
                ) else section
                for name, value in config.items(section):
                    comps = name.split('.')
                    if comps[0] == 'file':
                        files.setdefault(comps[1], {})[comps[2]] = value

                for f in files.values():
                    uninstall_files.setdefault(f['name'], {})
                    uninstall_files[f['name']].setdefault('groups', [])
                    if group not in uninstall_files[f['name']]['groups']:
                        uninstall_files[f['name']]['groups'].append(group)
                    for algo in _HASH_ALGORITHMS:
                        if f.get(algo):
                            uninstall_files[f['name']][algo] = f[algo]
                        elif uninstall_files[f['name']].get(algo):
                            # If we updated the hash, remove older hashes
                            del uninstall_files[f['name']][algo]

            def getLines(section):
                associated_lines = {}
                aggregated_lines = {}
                # line.{file_index:03}{line_index:03}.name
                # line.{file_index:03}{line_index:03}.content.added
                # line.{file_index:03}{line_index:03}.content.removed
                for name, value in config.items(section):
                    comps = name.split('.')
                    if comps[0] == 'line':

                        index = comps[1]        # '00001', '00002', etc
                        line_type = comps[2]    # 'name' or 'content'

                        if len(comps) == 3 and line_type == 'content':
                            comps.append('added')

                        if line_type == 'content':
                            action = comps[3]   # 'added' or 'removed'

                        associated_lines.setdefault(index, {})
                        if line_type == 'name':
                            associated_lines[index][line_type] = value
                        elif line_type == 'content':
                            associated_lines[index].setdefault(line_type, {})[
                                action
                            ] = value

                for f in associated_lines.values():
                    aggregated_lines.setdefault(
                        f['name'], []
                    ).append(f['content'])
                self.logger.debug(
                    'getLines %s %s: aggregated_lines = %s',
                    info,
                    section,
                    aggregated_lines,
                )
                return aggregated_lines

            for uninstall_section in config.sections():
                updateFiles(uninstall_section)
                uninstall_lines.update(
                    getLines(uninstall_section)
                )

        for name, f in uninstall_files.items():
            if os.path.exists(name):
                for algo in _HASH_ALGORITHMS:
                    if f.get(algo):
                        f['changed'] = self._digestFile(name, algo) != f[algo]
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_FILES_INFO
        ] = uninstall_files
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_LINES_INFO
        ] = uninstall_lines

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation_changed_files(self):
        # Log list of files changed outside of engine-setup.
        # One day might also warn and/or prompt, thus putting in
        # validation stage, but currently I think it might be too
        # annoying. Also useful as an example for other plugins
        # that want to check if specific files changed.
        changed_files = [
            f
            for f, info in self.environment[
                osetupcons.CoreEnv.UNINSTALL_FILES_INFO
            ].items()
            if info.get('changed')
        ]
        if changed_files:
            self.logger.debug(
                'The following files were changed outside of '
                'engine-setup:\n%s',
                '\n'.join(changed_files)
            )

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
                config.set(section, 'description', str(description))
                config.set(section, 'optional', str(optional))

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
                        prefix + '.sha256',
                        self._digestFile(name, 'sha256'),
                    )

        def _addChanges(section, changes):
            for file_index, filename in enumerate(
                sorted(set(changes))
            ):
                for line_index, content in enumerate(
                    list(changes[filename])
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
            osetupcons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
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
