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


"""Local Postgres plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Local Postgres plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_CONF,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_CONF
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_PG_HBA,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_HBA
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_PG_VERSION,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_VERSION
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_SERVICE,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_SERVICE
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_MAX_CONN,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_MAX_CONN
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_LISTEN_ADDRESS,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_LISTEN_ADDRESS
        )
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_LC_MESSAGES,
            oengcommcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_LC_MESSAGES
        )


# vim: expandtab tabstop=4 shiftwidth=4
