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


"""CA plugin."""


import re
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.CONFIG_DB_ENCRYPTION_AVAILABLE,
        after=[
            osetupcons.Stages.DB_CONNECTION_AVAILABLE,
            osetupcons.Stages.CA_AVAILABLE,
        ],
    )
    def _misc(self):
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_config',
            description='PKI configuration',
            optional=True,
        ).addFiles(
            'ca_config',
            uninstall_files,
        )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_PKI
                ),
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                content=(
                    'ENGINE_PKI="{pki_dir}"\n'
                    'ENGINE_PKI_CA="{ca}"\n'
                    'ENGINE_PKI_ENGINE_CERT="{engine_cert}"\n'
                    'ENGINE_PKI_TRUST_STORE="{trust_store}"\n'
                    'ENGINE_PKI_TRUST_STORE_PASSWORD=' + (
                        '"{trust_store_password}"\n'
                    ) +
                    'ENGINE_PKI_ENGINE_STORE="{engine_store}"\n'
                    'ENGINE_PKI_ENGINE_STORE_PASSWORD=' + (
                        '"{engine_store_password}"\n'
                    ) +
                    'ENGINE_PKI_ENGINE_STORE_ALIAS="{engine_store_alias}"\n'
                ).format(
                    pki_dir=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKIDIR
                    ),
                    ca=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                    engine_cert=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CERT
                    ),
                    trust_store=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE
                    ),
                    trust_store_password=re.escape(
                        osetupcons.Const.PKI_PASSWORD
                    ),
                    engine_store=(
                        osetupcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_STORE
                    ),
                    engine_store_password=re.escape(
                        osetupcons.Const.PKI_PASSWORD
                    ),
                    engine_store_alias='1',
                ),
                modifiedList=uninstall_files,
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
