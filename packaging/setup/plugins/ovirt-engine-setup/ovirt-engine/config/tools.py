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


"""Tools configuration plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Tools configuration plugin."""

    TOOLS_CONFIG = [
        {
            "dir": "{engine_sysconf}/isouploader.conf.d",
            "section": "ISOUploader",
        },
        {
            "dir": "{engine_sysconf}/imageuploader.conf.d",
            "section": "ImageUploader",
        },
        {
            "dir": "{engine_sysconf}/logcollector.conf.d",
            "section": "LogCollector",
        },
    ]

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        for entry in self.TOOLS_CONFIG:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=(
                        os.path.join(
                            entry['dir'],
                            "10-engine-setup.conf"
                        ).format(
                            engine_sysconf=(
                                oenginecons.FileLocations.
                                OVIRT_ENGINE_SYSCONFDIR
                            ),
                        )
                    ),
                    content=(
                        (
                            "[{section}]\n"
                            "engine={fqdn}:{port}\n"
                            "user={user}\n"
                        ).format(
                            section=entry['section'],
                            fqdn=self.environment[
                                osetupcons.ConfigEnv.FQDN
                            ],
                            port=self.environment[
                                oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
                            ],
                            user=self.environment[
                                oenginecons.ConfigEnv.ADMIN_USER
                            ],
                        )
                    ),
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
