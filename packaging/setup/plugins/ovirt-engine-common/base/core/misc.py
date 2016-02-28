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


"""Misc plugin."""


import gettext
import logging
import os

from otopi import constants as otopicons
from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        priority=plugin.Stages.PRIORITY_HIGH - 20,
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

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _init(self):

        self.environment.setdefault(
            osetupcons.CoreEnv.GENERATED_BY_VERSION,
            None
        )

        self.environment[
            osetupcons.CoreEnv.ORIGINAL_GENERATED_BY_VERSION
        ] = self.environment[
            osetupcons.CoreEnv.GENERATED_BY_VERSION
        ]

        self.environment[
            osetupcons.CoreEnv.GENERATED_BY_VERSION
        ] = osetupcons.Const.PACKAGE_VERSION

        self.environment.setdefault(
            osetupcons.CoreEnv.DEVELOPER_MODE,
            None
        )
        self.environment.setdefault(
            osetupcons.CoreEnv.UPGRADE_SUPPORTED_VERSIONS,
            '3.6,4.0'
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

        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] is None:
            self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] = False
            if os.geteuid() != 0:
                if not dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_SYSTEM_UNPRIVILEGED',
                    note=_(
                        'Setup was run under unprivileged user '
                        'this will produce development installation '
                        'do you wish to proceed? (@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=False,
                ):
                    raise RuntimeError(_('Aborted by user'))
                self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] = True

        if (
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] and
            os.geteuid() != 0
        ):
            raise RuntimeError(
                _('Running as non root and not in development mode')
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
