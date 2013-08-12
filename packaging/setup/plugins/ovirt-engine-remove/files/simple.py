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


"""Simple plugin."""


import os
import glob
import hashlib
import configparser
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


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

    def _cleanLines(self, filename, remove_lines):
        new_content = []
        with open(filename, 'r') as f:
            old_content = f.read().splitlines()
        for line in old_content:
            if line not in remove_lines:
                new_content.append(line)
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
        self._infos = sorted(
            glob.glob(
                os.path.join(
                    osetupcons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
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
                    add_group = (
                        self.environment[
                            osetupcons.RemoveEnv.REMOVE_ALL
                        ] or
                        not config.getboolean(section, 'optional')
                    )
                    if (
                        not add_group and
                        interactive and
                        not group in already_asked
                    ):
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
                for name, value in config.items(section):
                    comps = name.split('.')
                    if comps[0] == 'line':
                        associated_lines.setdefault(
                            comps[1], {}
                        )[comps[2]] = value
                for f in associated_lines.values():
                    aggregated_lines.setdefault(
                        f['name'], []
                    ).append(f['content'])
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
                    #section could be missing in a conf file, for example if
                    #PKI config was not done because already existing
                    self._files.update(
                        getFiles(uninstall_section)
                    )
                    self._lines.update(
                        getLines(uninstall_section)
                    )
            if config.has_section('unremovable'):
                unremovable.update(getFiles('unremovable'))

        self._toremove = set(self._files.keys()) - set(unremovable.keys())
        self._tomodifylines = self._lines.keys()
        self.logger.debug('tomodifylines=%s', self._tomodifylines)
        self.logger.debug('files=%s', self._files)
        self.logger.debug('unremovable=%s', unremovable)
        self.logger.debug('toremove=%s', self._toremove)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):
        self.logger.info(_('Removing added lines'))
        for f in self._tomodifylines:
            if os.path.exists(f):
                self._cleanLines(f, self._lines[f])
        self.logger.info(_('Removing files'))
        for f in self._toremove:
            if os.path.exists(f):
                if self._digestFile(f) != self._files[f]:
                    self.logger.warning(
                        _(
                            "Preserving '{file}' as changed since installation"
                        ).format(
                            file=f,
                        )
                    )
                else:
                    self._safeDelete(f)

            elif os.path.islink(f):
                # dead link
                self._safeDelete(f)

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
