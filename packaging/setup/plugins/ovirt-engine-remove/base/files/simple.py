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


"""Simple plugin."""


import gettext
import glob
import hashlib
import os

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

import configparser
from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Simple plugin."""

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

    def _safeDelete(self, filename):
        try:
            os.unlink(filename)
        except OSError as e:
            self.logger.debug(
                "Cannot delete '%s'",
                filename,
                exc_info=True,
            )
            self.logger.error(
                _("Cannot delete '{file}': {error}").format(
                    file=filename,
                    error=e,
                )
            )

    def _revertChanges(self, filename, changes):
        new_content = []
        with open(filename, 'r') as f:
            old_content = f.read().splitlines()
        just_remove = []
        just_add = []
        replace = {}

        for c in changes:
            if 'removed' not in c:
                just_remove.append(c['added'])
            elif 'added' not in c:
                just_add.append(c['removed'])
            else:
                replace[c['added']] = c['removed']
        # For checking if remove/replace lines were found, we work on copies,
        # because there might be duplicate lines in the file.
        remove_unremoved = just_remove[:]
        replace_unremoved = replace.copy()
        for line in old_content:
            if line in just_remove:
                if line in remove_unremoved:
                    remove_unremoved.remove(line)
            else:
                # should be updated or added
                if line in replace:
                    orig_line = line
                    line = replace[line]
                    if orig_line in replace_unremoved:
                        del replace_unremoved[orig_line]
                new_content.append(line)
        new_content.extend(just_add)
        if remove_unremoved or replace_unremoved:
            self.logger.warning(
                _(
                    'Some changes to {file} could not be reverted. More '
                    'details can be found in the log.'
                ).format(
                    file=filename,
                )
            )
        if remove_unremoved:
            self.logger.debug(
                (
                    'The following lines were not found in {file} and so '
                    'were not removed:\n{lines}'
                ).format(
                    file=filename,
                    lines='\n'.join(
                        [
                            '\t{line}'.format(line=newline)
                            for newline in remove_unremoved
                        ]
                    ),
                )
            )
        if replace_unremoved:
            self.logger.debug(
                (
                    'The following lines were not found in {file} and so '
                    'were not reverted to their old content:\n{lines}'
                ).format(
                    file=filename,
                    lines='\n'.join(
                        [
                            '\tnew:\t{new}\n\told:\t{old}\n'.format(
                                new=new,
                                old=old,
                            )
                            for new, old in replace_unremoved.items()
                        ]
                    ),
                )
            )

        if new_content != old_content:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=filename,
                    content=new_content
                )
            )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._infos = None
        self._files = {}
        self._toremove = None
        self._lines = {}
        self._descriptions = {}

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_GROUPS,
            ''
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.ASK_GROUPS,
            True
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.FILES_TO_REMOVE,
            []
        )

        # TODO: check if we need to allow to override this by answer file.
        # Using a list here won't allow you to override this
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_SPEC_OPTION_GROUP_LIST,
            []
        )
        self.environment.setdefault(
            osetupcons.RemoveEnv.REMOVE_CHANGED,
            None
        )
        self._infos = sorted(
            glob.glob(
                os.path.join(
                    osetupcons.FileLocations.OVIRT_SETUP_UNINSTALL_DIR,
                    '*.conf',
                )
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.REMOVE_CUSTOMIZATION_GROUPS,
        after=(
            osetupcons.Stages.REMOVE_CUSTOMIZATION_COMMON,
        ),
    )
    def _customization(self):
        interactive = self.environment[
            osetupcons.RemoveEnv.ASK_GROUPS
        ]
        unremovable = {}
        already_asked = []
        for info in self._infos:
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
                    self._descriptions[group] = msg

                    add_group = self.environment[
                        osetupcons.RemoveEnv.REMOVE_ALL
                    ]
                    if not add_group:
                        if group in self.environment[
                            osetupcons.RemoveEnv.REMOVE_SPEC_OPTION_GROUP_LIST
                        ]:
                            add_group = True

                    if (
                        not add_group and
                        interactive and
                        group not in already_asked
                    ):
                        if group not in self.environment[
                            osetupcons.RemoveEnv.REMOVE_SPEC_OPTION_GROUP_LIST
                        ]:
                            already_asked.append(group)
                            add_group = dialog.queryBoolean(
                                dialog=self.dialog,
                                name='OVESETUP_REMOVE_GROUP/' + group,
                                note=_(
                                    'Do you want to remove {description}? '
                                    '(@VALUES@) [@DEFAULT@]: '
                                ).format(
                                    description=msg,
                                ),
                                prompt=True,
                                true=_('Yes'),
                                false=_('No'),
                                default=False,
                            )
                    if add_group:
                        self.environment[
                            osetupcons.RemoveEnv.
                            REMOVE_GROUPS
                        ] += ',' + group

            def getFiles(section):
                files = {}
                for name, value in config.items(section):
                    comps = name.split('.')
                    if comps[0] == 'file':
                        files.setdefault(comps[1], {})[comps[2]] = value

                # python 2.6 doesn't support dict comprehensions.
                # TODO: we may move to it when minimal python version
                # available is 2.7+
                return dict((f['name'], f['md5']) for f in files.values())

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
                    'getLines: aggregated_lines = %s',
                    aggregated_lines,
                )
                return aggregated_lines

            for uninstall_group in [
                x.strip()
                for x in self.environment[
                    osetupcons.RemoveEnv.REMOVE_GROUPS
                ].split(',')
                if x.strip()
            ]:
                uninstall_section = (
                    osetupcons.Const.FILE_GROUP_SECTION_PREFIX +
                    uninstall_group
                )
                if config.has_section(uninstall_section):
                    # section could be missing in a conf file, for example if
                    # PKI config was not done because already existing
                    self._files.update(
                        getFiles(uninstall_section)
                    )
                    self._lines.update(
                        getLines(uninstall_section)
                    )
            if config.has_section('unremovable'):
                unremovable.update(getFiles('unremovable'))

        self._toremove = set(self._files.keys()) - set(unremovable.keys())
        changed = []
        for f in self._toremove:
            if os.path.exists(f):
                if self._digestFile(f) != self._files[f]:
                    changed.append(f)
        self.logger.debug('changed=%s', changed)
        if changed:
            if self.environment[osetupcons.RemoveEnv.REMOVE_CHANGED] is None:
                self.environment[
                    osetupcons.RemoveEnv.REMOVE_CHANGED
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_ENGINE_REMOVE_CHANGED',
                    note=_(
                        'The following files were changed since setup:\n'
                        '{files}\n'
                        'Remove them anyway? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        files='\n'.join(changed),
                    ),
                    prompt=True,
                    true=_('Yes'),
                    false=_('No'),
                    default=True,
                )

        if not self.environment[osetupcons.RemoveEnv.REMOVE_CHANGED]:
            self._toremove -= set(changed)

        self._tomodifylines = self._lines.keys()
        self.logger.debug('tomodifylines=%s', self._tomodifylines)
        self.logger.debug('files=%s', self._files)
        self.logger.debug('unremovable=%s', unremovable)
        self.logger.debug('toremove=%s', self._toremove)
        self.environment[
            osetupcons.RemoveEnv.FILES_TO_REMOVE
        ] = self._toremove

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):
        self.logger.info(_('Removing files'))
        for f in self._toremove:
            if os.path.exists(f):
                self._safeDelete(f)

            elif os.path.islink(f):
                # dead link
                self._safeDelete(f)

        self.logger.info(_('Reverting changes to files'))
        for f in self._tomodifylines:
            if os.path.exists(f):
                self._revertChanges(f, self._lines[f])

        for info in self._infos:
            self._safeDelete(info)

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _closeup(self):
        all_groups = set(self._descriptions.keys())
        uninstalled_groups = set([
            x.strip()
            for x in self.environment[
                osetupcons.RemoveEnv.REMOVE_GROUPS
            ].split(',')
            if x.strip()
        ])
        not_uninstalled = set(all_groups - uninstalled_groups)
        for group in not_uninstalled:
            self.dialog.note(
                text=_(
                    '{description} files not removed'
                ).format(
                    description=self._descriptions[group],
                ),
            )


# vim: expandtab tabstop=4 shiftwidth=4
