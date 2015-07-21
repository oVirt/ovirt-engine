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


"""Apache root plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache root plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ApacheEnv.HTTPD_CONF_OVIRT_ROOT,
            oengcommcons.FileLocations.HTTPD_CONF_OVIRT_ROOT
        )
        self.environment.setdefault(
            oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION,
            None
        )
        self.environment.setdefault(
            oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTIOND_DEFAULT,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _setup(self):
        if (
            self.environment[
                oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
            ] is None and
            (
                self.environment[
                    osetupcons.CoreEnv.DEVELOPER_MODE
                ] or
                self.environment[
                    oengcommcons.ApacheEnv.CONFIGURED
                ]
            )
        ):
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_APACHE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_APACHE,
        ),
        condition=lambda self: (
            self.environment[oengcommcons.ApacheEnv.ENABLE] and
            self._enabled
        ),
    )
    def _customization(self):
        if self.environment[
            oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
        ] is None:
            self.dialog.note(
                _(
                    'Setup can configure the default page of the '
                    'web server to present the application home page. '
                    'This may conflict with existing applications.'
                )
            )
            self.environment[
                oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_APACHE_CONFIG_ROOT_REDIRECTION',
                note=_(
                    'Do you wish to set the application as the default page '
                    'of the web server? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=self.environment[
                    oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTIOND_DEFAULT
                ],
            )

        self._enabled = self.environment[
            oengcommcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[oengcommcons.ApacheEnv.ENABLE] and
            self._enabled
        ),
    )
    def _misc(self):
        with open(
            oengcommcons.FileLocations.HTTPD_CONF_OVIRT_ROOT_TEMPLATE,
            'r'
        ) as f:
            content = f.read()

        self.environment[oengcommcons.ApacheEnv.NEED_RESTART] = True
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    oengcommcons.ApacheEnv.HTTPD_CONF_OVIRT_ROOT
                ],
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
