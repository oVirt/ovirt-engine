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


"""Database plugin."""


import re
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Databsae plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.CONFIG_DB_CREDENTIALS,
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_DATABASE
                ),
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                content=(
                    'ENGINE_DB_HOST={host}\n'
                    'ENGINE_DB_PORT={port}\n'
                    'ENGINE_DB_USER={user}\n'
                    'ENGINE_DB_PASSWORD="{password}"\n'
                    'ENGINE_DB_DATABASE={db}\n'
                    'ENGINE_DB_SECURED={secured}\n'
                    'ENGINE_DB_SECURED_VALIDATION={securedValidation}\n'
                    'ENGINE_DB_DRIVER=org.postgresql.Driver\n'
                    'ENGINE_DB_URL=' + (
                        'jdbc:postgresql://'
                        '${{ENGINE_DB_HOST}}:${{ENGINE_DB_PORT}}'
                        '/${{ENGINE_DB_DATABASE}}'
                        '?{jdbcTlsOptions}\n'
                    ) +
                    ''
                ).format(
                    host=self.environment[osetupcons.DBEnv.HOST],
                    port=self.environment[osetupcons.DBEnv.PORT],
                    user=self.environment[osetupcons.DBEnv.USER],
                    password=re.escape(
                        self.environment[osetupcons.DBEnv.PASSWORD]
                    ),
                    db=self.environment[osetupcons.DBEnv.DATABASE],
                    secured=self.environment[osetupcons.DBEnv.SECURED],
                    securedValidation=self.environment[
                        osetupcons.DBEnv.SECURED_HOST_VALIDATION
                    ],
                    jdbcTlsOptions='%s%s' % (

                        'ssl=true'
                        if self.environment[
                            osetupcons.DBEnv.SECURED
                        ] else '',

                        '&sslfactory=org.postgresql.ssl.NonValidatingFactory'
                        if not self.environment[
                            osetupcons.DBEnv.SECURED_HOST_VALIDATION
                        ] else ''

                    )
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
