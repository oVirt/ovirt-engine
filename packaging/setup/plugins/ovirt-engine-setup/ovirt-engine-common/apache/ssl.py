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


"""Apache ssl plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_setup_lib import dialog
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache ssl plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True
        self._params = {
            'SSLCertificateFile': (
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT
            ),
            'SSLCertificateKeyFile': (
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY
            ),
            'SSLCACertificateFile': (
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
            ),
        }

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ApacheEnv.HTTPD_CONF_SSL,
            oengcommcons.FileLocations.HTTPD_CONF_SSL
        )
        self.environment.setdefault(
            oengcommcons.ApacheEnv.CONFIGURE_SSL,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self._enabled,
    )
    def _setup(self):
        if (
            self.environment[
                oengcommcons.ApacheEnv.CONFIGURE_SSL
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
        condition=lambda self: (
            self.environment[oengcommcons.ApacheEnv.ENABLE] and
            self._enabled
        ),
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_APACHE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_APACHE,
        ),
    )
    def _customization(self):
        if self.environment[
            oengcommcons.ApacheEnv.CONFIGURE_SSL
        ] is None:
            self.dialog.note(
                _(
                    'Setup can configure apache to use SSL using a '
                    'certificate issued from the internal CA.'
                )
            )
            self.environment[
                oengcommcons.ApacheEnv.CONFIGURE_SSL
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_APACHE_CONFIG_SSL',
                note=_(
                    'Do you wish Setup to configure that, or prefer to '
                    'perform that manually? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Automatic'),
                false=_('Manual'),
                default=True,
            )

        self._enabled = self.environment[
            oengcommcons.ApacheEnv.CONFIGURE_SSL
        ]

        if self._enabled:
            if not os.path.exists(
                self.environment[
                    oengcommcons.ApacheEnv.HTTPD_CONF_SSL
                ]
            ):
                self.logger.warning(
                    _(
                        "Automatic Apache SSL configuration was requested. "
                        "However, SSL configuration file '{file}' was not "
                        "found. Disabling automatic Apache SSL configuration."
                    )
                )
                self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _validate_enable(self):
        if not self.environment[oengcommcons.ApacheEnv.ENABLE]:
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validate_ssl(self):
        with open(
            self.environment[
                oengcommcons.ApacheEnv.HTTPD_CONF_SSL
            ],
            'r'
        ) as f:
            self._sslData = f.read()

        missingParams = []
        osetuputil.editConfigContent(
            content=self._sslData.splitlines(),
            params=self._params,
            separator_re='\s+',
            new_line_tpl='{spaces}{param} {value}',
            added_params=missingParams,
        )
        if missingParams:
            self.logger.warning(
                _(
                    'Expected parameter(s) {missingParams} were not '
                    'found in {file}. Automatic '
                    'configuration of this file will not be '
                    'performed.'
                ).format(
                    missingParams=missingParams,
                    file=self.environment[
                        oengcommcons.ApacheEnv.HTTPD_CONF_SSL
                    ]
                )
            )
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self.environment[oengcommcons.ApacheEnv.NEED_RESTART] = True
        changed_lines = []
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    oengcommcons.ApacheEnv.HTTPD_CONF_SSL
                ],
                content=osetuputil.editConfigContent(
                    content=self._sslData.splitlines(),
                    params=self._params,
                    changed_lines=changed_lines,
                    separator_re='\s+',
                    new_line_tpl='{spaces}{param} {value}',
                ),
            )
        )
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ssl',
            description='Apache SSL configuration',
            optional=True
        ).addChanges(
            'ssl',
            self.environment[oengcommcons.ApacheEnv.HTTPD_CONF_SSL],
            changed_lines,
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(
            self.environment[
                oengcommcons.ApacheEnv.HTTPD_CONF_SSL
            ]
        )


# vim: expandtab tabstop=4 shiftwidth=4
