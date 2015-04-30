#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015 Red Hat, Inc.
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


"""Misc plugin."""


import gettext
import logging
import os

from otopi import constants as otopicons
from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
        priority=plugin.Stages.PRIORITY_HIGH - 10,
    )
    def _preinit(self):
        self.environment.setdefault(
            otopicons.CoreEnv.CONFIG_FILE_NAME,
            self.resolveFile(
                os.environ.get(
                    otopicons.SystemEnvironment.CONFIG,
                    self.resolveFile(
                        osetupcons.FileLocations.OVIRT_OVIRT_SETUP_CONFIG_FILE
                    )
                )
            )
        )
        logging.getLogger('ovirt').setLevel(logging.DEBUG)
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_DIR,
            osetupcons.FileLocations.OVIRT_SETUP_LOGDIR
        )
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_FILE_NAME_PREFIX,
            osetupcons.FileLocations.OVIRT_OVIRT_PROVISIONDB_LOG_PREFIX
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[
            osetupcons.CoreEnv.ACTION
        ] = osetupcons.Const.ACTION_PROVISIONDB
        self.environment.setdefault(
            osetupcons.CoreEnv.DEVELOPER_MODE,
            None
        )
        self.logger.debug(
            'Package: %s-%s (%s)',
            osetupcons.Const.PACKAGE_NAME,
            osetupcons.Const.PACKAGE_VERSION,
            osetupcons.Const.DISPLAY_VERSION,
        )
        self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ] = [osetupcons]
        self.environment.setdefault(
            oengcommcons.SystemEnv.USER_POSTGRES,
            oengcommcons.Defaults.DEFAULT_SYSTEM_USER_POSTGRES
        )
        self.environment.setdefault(
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES,
            []
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
    )
    def _cleanup(self):
        self.dialog.note(
            text=_('Log file is located at {path}').format(
                path=self.environment[
                    otopicons.CoreEnv.LOG_FILE_NAME
                ],
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_TERMINATE,
    )
    def _terminate(self):
        if self.environment[otopicons.BaseEnv.ERROR]:
            self.logger.error(
                _('Execution of {action} failed').format(
                    action=self.environment[
                        osetupcons.CoreEnv.ACTION
                    ],
                ),
            )
        else:
            self.logger.info(
                _('Execution of {action} completed successfully').format(
                    action=self.environment[
                        osetupcons.CoreEnv.ACTION
                    ],
                ),
            )


# vim: expandtab tabstop=4 shiftwidth=4
