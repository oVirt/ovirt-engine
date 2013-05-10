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


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """Simple plugin."""

    def _digestFile(self, filename):
        md5 = hashlib.new('md5')
        with open(filename, 'rb') as f:
            md5.update(f.read(1000))
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

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._infos = None
        self._files = {}
        self._toremove = None
        self._descriptions = {}

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.UNINSTALL_ENABLED_FILE_GROUPS,
            ''
        )
        self.environment.setdefault(
            osetupcons.CoreEnv.CONFIRM_UNINSTALL_GROUPS,
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
    )
    def _customization(self):
        interactive = self.environment[
            osetupcons.CoreEnv.CONFIRM_UNINSTALL_GROUPS
        ]
        unremovable = {}
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
                    add_group = not config.getboolean(section, 'optional')
                    if not add_group and interactive:
                        add_group = dialog.queryBoolean(
                            dialog=self.dialog,
                            name='OVESETUP_REMOVE_GROUP/' + group,
                            note=_(
                                'Do you want to clean {description}? '
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
                            osetupcons.CoreEnv.
                            UNINSTALL_ENABLED_FILE_GROUPS
                        ] += group + ','

            def getFiles(section):
                files = {}
                for name, value in config.items(section):
                    comps = name.split('.')
                    if comps[0] == 'file':
                        files.setdefault(comps[1], {})[comps[2]] = value
                return {f['name']: f['md5'] for f in files.values()}

            for uninstall_group in [
                x.strip()
                for x in self.environment[
                    osetupcons.CoreEnv.UNINSTALL_ENABLED_FILE_GROUPS
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
            unremovable.update(getFiles('unremovable'))

        self._toremove = set(self._files.keys()) - set(unremovable.keys())
        self.logger.debug('files=%s', self._files)
        self.logger.debug('unremovable=%s', unremovable)
        self.logger.debug('toremove=%s', self._toremove)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):
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
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ],
    )
    def _closeup(self):
        all_groups = set(self._descriptions.keys())
        uninstalled_groups = set([
            x.strip()
            for x in self.environment[
                osetupcons.CoreEnv.UNINSTALL_ENABLED_FILE_GROUPS
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
