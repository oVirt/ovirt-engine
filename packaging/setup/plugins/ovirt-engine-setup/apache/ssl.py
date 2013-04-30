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


"""Apache ssl plugin."""


import os
import re
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
    """Apache ssl plugin."""

    _RE_PARAM = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            (?P<spaces>\s*)
            [#]*
            (?P<param>\w+)
            \s*
            .*
            $
        """
    )

    def _editParams(self, params, content):
        newcontent = []
        for line in content.splitlines():
            f = self._RE_PARAM.match(line)
            if f is not None and f.group('param') in params:
                line = '{spaces}{param} {value}'.format(
                    spaces=f.group('spaces'),
                    param=f.group('param'),
                    value=params[f.group('param')],
                )
            newcontent.append(line)

        return '\n'.join(newcontent) + '\n'

    def _findMissingParams(self, params, content):
        missingParams = params.keys()
        for line in content.splitlines():
            f = self._RE_PARAM.match(line)
            if (
                f is not None and
                f.group('param') in missingParams
            ):
                missingParams.remove(f.group('param'))

        return missingParams

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True
        self._params = {
            'SSLCertificateFile': (
                osetupcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT
            ),
            'SSLCertificateKeyFile': (
                osetupcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY
            ),
            'SSLCACertificateFile': (
                osetupcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
            ),
        }

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ApacheEnv.HTTPD_CONF_SSL,
            osetupcons.FileLocations.HTTPD_CONF_SSL
        )
        self.environment.setdefault(
            osetupcons.ApacheEnv.CONFIGURE_SSL,
            None
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
        condition=lambda self: self._enabled,
        before=[
            osetupcons.Stages.DIALOG_TITLES_E_APACHE,
        ],
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_APACHE,
        ],
    )
    def _customization(self):
        if self.environment[
            osetupcons.ApacheEnv.CONFIGURE_SSL
        ] is None:
            self.dialog.note(
                _(
                    'Setup can configure apache to use SSL using a '
                    'certificate issued from the internal CA'
                )
            )
            self._enabled = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_APACHE_CONFIG_SSL',
                note=_(
                    'Do you wish to setup to configure or prefer to perform '
                    'that manually? (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Automatic'),
                false=_('Manual'),
                default=True,
            )
        else:
            self._enabled = self.environment[
                osetupcons.ApacheEnv.CONFIGURE_SSL
            ]

        if self._enabled:
            if not os.path.exists(
                self.environment[
                    osetupcons.ApacheEnv.HTTPD_CONF_SSL
                ]
            ):
                self.logger.warning(
                    _(
                        "Apache SSL automatic configuration was requested, "
                        "however, SSL configuration file '{file}' was not "
                        "found. Disabling configuration."
                    )
                )
                self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validate_ssl(self):
        with open(
            self.environment[
                osetupcons.ApacheEnv.HTTPD_CONF_SSL
            ],
            'r'
        ) as f:
            self._sslData = f.read()

        missingParams = self._findMissingParams(
            self._params,
            self._sslData
        )
        if missingParams:
            self.logger.warning(
                _(
                    'The expected parameters {missingParams} were not '
                    'found in the {file}. The automatic '
                    'configuration of this file will not be '
                    'performed.'
                ).format(
                    missingParams=missingParams,
                    file=self.environment[
                        osetupcons.ApacheEnv.HTTPD_CONF_SSL
                    ]
                )
            )
            self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    osetupcons.ApacheEnv.HTTPD_CONF_SSL
                ],
                content=self._editParams(
                    self._params,
                    self._sslData
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES
        ].append(
            self.environment[
                osetupcons.ApacheEnv.HTTPD_CONF_SSL
            ]
        )


# vim: expandtab tabstop=4 shiftwidth=4
