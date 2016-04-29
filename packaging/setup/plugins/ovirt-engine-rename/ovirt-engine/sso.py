#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2016 Red Hat, Inc.
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


"""sso plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """sso plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _update_client_callback_prefix(self):
        engine_port = self.environment[
            oengcommcons.ConfigEnv.HTTPS_PORT
        ] if self.environment[
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT
        ] else self.environment[
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
        ]

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select update_oauth_client_callback_prefix(
                    %(client_id)s,
                    %(callback_prefix)s
                )
            """,
            args=dict(
                client_id='ovirt-engine-core',
                callback_prefix='https://%s:%s/ovirt-engine/' % (
                    self.environment[osetupcons.RenameEnv.FQDN],
                    engine_port,
                ),
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
