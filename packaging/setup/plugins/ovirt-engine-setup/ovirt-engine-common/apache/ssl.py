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
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog

_SSL_REQUESTS_LOG_FORMAT = (
    'CustomLog logs/ovirt-requests-log '
    ' "%t %h \\"Correlation-Id: %{Correlation-Id}o\\" '
    '\\"Duration: %Dus\\" \\"%r\\" %b"'
)


def _apply_logging(lines):
    result = []
    found = False
    for line in lines:
        if not found and line.find('ovirt-requests-log') != -1:
            found = True
        if not found and line.find('</VirtualHost>') != -1:
            found = True
            result.append(_SSL_REQUESTS_LOG_FORMAT)
        result.append(line)
    return result


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache ssl plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True
        self._file_exists = False
        self._current_content = None
        self._new_content = None
        self._missing_params = None
        self._changed_lines = []
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
            'SSLProtocol': (
                oengcommcons.Const.HTTPD_SSL_PROTOCOLS
            ),
        }

    def _read_and_process_file(self):
        with open(
            self.environment[
                oengcommcons.ApacheEnv.HTTPD_CONF_SSL
            ],
            'r'
        ) as f:
            self._current_content = f.read()

        self._missing_params = []
        self._new_content = osetuputil.editConfigContent(
            content=_apply_logging(self._current_content.splitlines()),
            params=self._params,
            changed_lines=self._changed_lines,
            separator_re='\s+',
            new_line_tpl='{spaces}{param} {value}',
            added_params=self._missing_params,
        )

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

        if os.path.exists(
            self.environment[
                oengcommcons.ApacheEnv.HTTPD_CONF_SSL
            ]
        ):
            self._file_exists = True
            self._read_and_process_file()

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
            if not self._file_exists:
                self.logger.warning(
                    _(
                        "Automatic Apache SSL configuration was requested. "
                        "However, SSL configuration file '{file}' was not "
                        "found. Disabling automatic Apache SSL configuration."
                    )
                )
                self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self.environment[oengcommcons.ApacheEnv.CONFIGURED] and
            self._current_content != self._new_content
        ),
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_APACHE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_APACHE,
        ),
    )
    def _customization_already_configured(self):
        self._enabled = dialog.queryBoolean(
            dialog=self.dialog,
            name='OVESETUP_APACHE_RECONFIG_SSL',
            note=_(
                'Apache httpd SSL was already configured in the past, '
                'but some needed changes are missing there.\n'
                'Configure again? (@VALUES@) [@DEFAULT@]: '
            ),
            prompt=True,
            true=_('Automatic'),
            false=_('Manual'),
            default=True,
        )

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
        condition=lambda self: self._enabled and self._missing_params,
    )
    def _validate_ssl(self):
        self.logger.warning(
            _(
                'Expected parameter(s) {missingParams} were not '
                'found in {file}. Automatic '
                'configuration of this file will not be '
                'performed.'
            ).format(
                missingParams=self._missing_params,
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
        self._read_and_process_file()  # Read again, in case it was updated
        self.environment[oengcommcons.ApacheEnv.NEED_RESTART] = True
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    oengcommcons.ApacheEnv.HTTPD_CONF_SSL
                ],
                content=self._new_content,
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
            self._changed_lines,
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(
            self.environment[
                oengcommcons.ApacheEnv.HTTPD_CONF_SSL
            ]
        )


# vim: expandtab tabstop=4 shiftwidth=4
