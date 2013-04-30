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

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _misc(self):

        infos = sorted(
            glob.glob(
                os.path.join(
                    osetupcons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
                    '*.conf',
                )
            )
        )

        files = {}
        unremovable = {}
        for info in infos:
            config = configparser.ConfigParser()
            config.optionxform = str
            config.read([info])

            def getFiles(section):
                files = {}
                for name, value in config.items(section):
                    comps = name.split('.')
                    if comps[0] == 'file':
                        files.setdefault(comps[1], {})[comps[2]] = value
                return {f['name']: f['md5'] for f in files.values()}

            files.update(getFiles('files'))
            unremovable.update(getFiles('unremovable'))

        toremove = set(files.keys()) - set(unremovable.keys())
        self.logger.debug('files=%s', files)
        self.logger.debug('unremovable=%s', unremovable)
        self.logger.debug('toremove=%s', toremove)

        self.logger.info(_('Removing files'))
        for f in toremove:
            if os.path.exists(f):
                if self._digestFile(f) != files[f]:
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

        for info in infos:
            self._safeDelete(info)


# vim: expandtab tabstop=4 shiftwidth=4
