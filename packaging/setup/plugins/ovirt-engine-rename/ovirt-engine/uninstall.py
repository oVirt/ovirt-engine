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

from otopi import plugin, util

import configparser
from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.engine import constants as oenginecons


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

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._infos = None
        self._existing_conf_files = {}
        self._descriptions = {}

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.RenameEnv.FORCE_OVERWRITE,
            False
        )
        self.environment.setdefault(
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED,
            []
        )
        self._infos = sorted(
            glob.glob(
                os.path.join(
                    oenginecons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
                    '*.conf',
                )
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        for info in self._infos:
            config = configparser.ConfigParser()
            config.optionxform = str
            config.read([info])

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

            for section in config.sections():
                self._existing_conf_files.update(
                    getFiles(section)
                )

        all_modified_files = self.environment[
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
        ]

        externally_modified_files = []

        for f in all_modified_files:
            if (
                f in self._existing_conf_files and
                self._digestFile(f) != self._existing_conf_files[f]
            ):
                externally_modified_files.append(f)

        self.logger.info(_('The following files will be updated:'))
        self.dialog.note(
            text=_(
                '\n'
                '{files}\n'
                '\n'
            ).format(
                files='\n'.join(sorted(all_modified_files))
            )
        )

        if (
            externally_modified_files and
            not self.environment[osetupcons.RenameEnv.FORCE_OVERWRITE]
        ):
            self.logger.warn(_("Files modified externally"))
            self.dialog.note(
                text=_(
                    'The following files were externally modified - outside\n'
                    'of package management and/or engine-setup - perhaps by\n'
                    'the system administrator:\n'
                    '\n'
                    '{files}\n'
                    '\n'
                ).format(
                    files='\n'.join(sorted(externally_modified_files))
                )
            )

            if not dialog.queryBoolean(
                dialog=self.dialog,
                name='FORCE_OVERWRITE_FILES',
                note=_(
                    'Do you want to overwrite all of the above files '
                    'with newly-generated content using the new host name? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Yes'),
                false=_('No'),
                default=False,
            ):
                raise RuntimeError(_('Aborted by user'))


# vim: expandtab tabstop=4 shiftwidth=4
