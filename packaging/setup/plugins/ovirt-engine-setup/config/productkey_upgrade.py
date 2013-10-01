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


"""Product key plugin."""

import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Product key plugin."""

    DB_TO_OSINFO = {
        'ProductKey2003': 'windows_2003',
        'ProductKey2003x64': 'windows_2003x64',
        'ProductKey2008': 'windows_2008',
        'ProductKey2008R2': 'windows_2008R2x64',
        'ProductKey2008x64': 'windows_2008x64',
        'ProductKey': 'windows_xp',
        'ProductKeyWindow7': 'windows_7',
        'ProductKeyWindow7x64': 'windows_7x64',
        'ProductKeyWindows8': 'windows_8',
        'ProductKeyWindows8x64': 'windows_8x64',
        'ProductKeyWindows2012x64': 'windows_2012x64',
    }

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            osetupcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: not os.path.exists(
            osetupcons.FileLocations.EXTRACTED_PRODUCTKEYS
        ),
    )
    def _misc(self):
        content = []
        for key in self.DB_TO_OSINFO.keys():
            val = self.environment[
                osetupcons.DBEnv.STATEMENT
            ].getVdcOption(key)
            if val:
                content.append(
                    'os.%s.productKey.value=%s' % (
                        self.DB_TO_OSINFO[key],
                        val,
                    )
                )

        if content:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=osetupcons.FileLocations.EXTRACTED_PRODUCTKEYS,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )

# vim: expandtab tabstop=4 shiftwidth=4
