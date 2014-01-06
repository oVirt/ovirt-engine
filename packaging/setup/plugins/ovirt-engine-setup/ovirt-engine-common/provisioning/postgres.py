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


"""Local Postgres plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


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
            osetupcons.ProvisioningEnv.POSTGRES_CONF,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_CONF
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_PG_HBA,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_HBA
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_PG_VERSION,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_PG_VERSION
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_SERVICE,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_SERVICE
        )
        self.environment.setdefault(
            osetupcons.ProvisioningEnv.POSTGRES_MAX_CONN,
            osetupcons.Defaults.DEFAULT_POSTGRES_PROVISIONING_MAX_CONN
        )


# vim: expandtab tabstop=4 shiftwidth=4
