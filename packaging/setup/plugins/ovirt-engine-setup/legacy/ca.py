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


"""Allow CA pki cleanup on upgrade from legacy plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Allow CA pki cleanup on upgrade from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[osetupcons.CoreEnv.UPGRADE_FROM_LEGACY]
        ),
    )
    def _misc(self):
        uninstall_files = []
        for name in (
            osetupcons.FileLocations.
                OVIRT_ENGINE_PKI_CA_TEMPLATE[:-len('.in')],
            osetupcons.FileLocations.
                OVIRT_ENGINE_PKI_CERT_TEMPLATE[:-len('.in')],
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_STORE,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_KEY,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CERT,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE,
            osetupcons.FileLocations.OVIRT_ENGINE_PKI_JBOSS_STORE,
        ):
            if os.path.exists(name):
                uninstall_files.append(name)

        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_pki',
            description='PKI keys',
            optional=True,
        ).addFiles(
            group='ca_pki',
            fileList=uninstall_files,
        )

# vim: expandtab tabstop=4 shiftwidth=4
