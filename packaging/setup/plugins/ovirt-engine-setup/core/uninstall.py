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


"""Uninstall plugin."""


import os
import datetime
import configparser
import hashlib
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Uninstall plugin."""

    def _digestFile(self, filename):
        md5 = hashlib.new('md5')
        with open(filename, 'rb') as f:
            md5.update(f.read(1000))
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

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
    )
    def _cleanup(self):
        config = configparser.ConfigParser()
        config.optionxform = str

        def _addFiles(section, files):
            config.add_section(section)
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

        _addFiles(
            'files',
            self.environment[
                otopicons.CoreEnv.MODIFIED_FILES
            ],
        )
        _addFiles(
            'unremovable',
            self.environment[
                osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
            ],
        )

        output = os.path.join(
            osetupcons.FileLocations.OVIRT_ENGINE_UNINSTALL_DIR,
            '%s-uninstall.conf' % (
                datetime.datetime.now().strftime('%Y%m%d%H%M%S'),
            ),
        )
        if not os.path.exists(os.path.dirname(output)):
            os.makedirs(os.path.dirname(output))
        with open(output, 'w') as f:
            config.write(f)


# vim: expandtab tabstop=4 shiftwidth=4
