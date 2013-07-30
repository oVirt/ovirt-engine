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


"""Upgrade configuration from legacy plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import constants as otopicons
from otopi import filetransaction


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database
from ovirt_engine_setup import domains


@util.export
class Plugin(plugin.PluginBase):
    """Upgrade configuration from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._aio_already_configured = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
        after=(
            osetupcons.Stages.UPGRADE_FROM_LEGACY_CONFIG,
        ),
        before=(
            osetupcons.Stages.AIO_CONFIG_AVAILABLE,
        ),
    )
    def _customization(self):
        dbovirtutils = database.OvirtUtils(plugin=self)
        dbovirtutils.tryDatabaseConnect()
        dbstatement = database.Statement(environment=self.environment)
        rows = dbstatement.execute(
            statement="""
                select
                    id,
                    storage,
                    storage_name
                from storage_domain_static
                where
                    storage_type=%(storage_type)s and
                    storage_domain_type=%(storage_domain_type)s
            """,
            args=dict(
                storage_type=domains.StorageType.LOCALFS,
                storage_domain_type=domains.StorageDomainType.MASTER,
            ),
            ownConnection=True,
        )
        for domain in rows:
            conn_row = dbstatement.execute(
                statement="""
                    select
                        connection
                    from storage_server_connections
                    where
                        id=%(storage)s
                """,
                args=dict(
                    storage=domain['storage']
                ),
                ownConnection=True,
            )[0]
            #returns only one line, can't exists duplicate and must exist one
            path = conn_row['connection']
            self.environment[osetupcons.AIOEnv.CONFIGURE] = False
            self.environment[osetupcons.AIOEnv.ENABLE] = False
            self.environment[
                osetupcons.AIOEnv.STORAGE_DOMAIN_DIR
            ] = path
            self.environment[
                osetupcons.AIOEnv.STORAGE_DOMAIN_NAME
            ] = domain['storage_name']
            self._aio_already_configured = True
            break

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._aio_already_configured,
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.AIO_POST_INSTALL_CONFIG,
                content=(
                    '[environment:default]',
                    'OVESETUP_AIO/enable=bool:False',
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
