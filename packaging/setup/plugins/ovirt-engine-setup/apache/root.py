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


"""Apache root plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


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
            osetupcons.ApacheEnv.HTTPD_CONF_OVIRT_ROOT,
            osetupcons.FileLocations.HTTPD_CONF_OVIRT_ROOT
        )
        self.environment.setdefault(
            osetupcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION,
            None
        )
        self.environment.setdefault(
            osetupcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTIOND_DEFAULT,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_APACHE,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_APACHE,
        ],
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        if self.environment[
            osetupcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
        ] is None:
            self.dialog.note(
                _(
                    'Setup can configure the default welcome page of the '
                    'web server to present ovirt-engine application. '
                    'This may conflict with existing applications.'
                )
            )
            self.environment[
                osetupcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_APACHE_CONFIG_ROOT_REDIRECTION',
                note=_(
                    'Do you wish to set ovirt-engine as default web server '
                    'page? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=self.environment[
                    osetupcons.ApacheEnv.
                    CONFIGURE_ROOT_REDIRECTIOND_DEFAULT
                ],
            )

            self._enabled = self.environment[
                osetupcons.ApacheEnv.CONFIGURE_ROOT_REDIRECTION
            ]

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        with open(
            osetupcons.FileLocations.HTTPD_CONF_OVIRT_ROOT_TEMPLATE,
            'r'
        ) as f:
            content = f.read()

        self.environment[osetupcons.ApacheEnv.NEED_RESTART] = True
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    osetupcons.ApacheEnv.HTTPD_CONF_OVIRT_ROOT
                ],
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
