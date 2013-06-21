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


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database
from ovirt_engine_setup import domains


@util.export
class Plugin(plugin.PluginBase):
    """Upgrade configuration from legacy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
        after=[
            osetupcons.Stages.UPGRADE_FROM_LEGACY_CONFIG
        ],
        before=[
            osetupcons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
            osetupcons.Stages.CONFIG_ISO_DOMAIN_AVAILABLE,
        ],
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
                storage_type=domains.StorageType.NFS,
                storage_domain_type=domains.StorageDomainType.ISO,
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
            host, path = conn_row['connection'].split(':')
            if host == self.environment[osetupcons.ConfigEnv.FQDN]:
                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_EXISTS
                ] = True
                self.environment[
                    osetupcons.SystemEnv.NFS_CONFIG_ENABLED
                ] = True
                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                ] = path
                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_NAME
                ] = domain['storage_name']
                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
                ] = domain['id']
                self.environment[
                    osetupcons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR
                ] = os.path.join(
                    self.environment[
                        osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
                    ],
                    self.environment[
                        osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
                    ],
                    'images',
                    osetupcons.Const.ISO_DOMAIN_IMAGE_UID,
                )
                break

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[osetupcons.CoreEnv.UPGRADE_FROM_LEGACY] and
            self.environment[osetupcons.ConfigEnv.ISO_DOMAIN_EXISTS]
        ),
    )
    def _misc(self):
        uninstall_files = [
            os.path.join(
                self.environment[osetupcons.ConfigEnv.ISO_DOMAIN_STORAGE_DIR],
                '.keep'
            ),
        ]
        dom_md = os.path.join(
            self.environment[
                osetupcons.ConfigEnv.ISO_DOMAIN_NFS_MOUNT_POINT
            ],
            self.environment[
                osetupcons.ConfigEnv.ISO_DOMAIN_SD_UUID
            ],
            'dom_md'
        )
        for filename in (
            'ids',
            'inbox',
            'leases',
            'metadata',
            'outbox',
        ):
            uninstall_files.append(
                os.path.join(
                    dom_md,
                    filename
                )
            )
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='iso_domain',
            description='ISO domain layout',
            optional=True
        ).addFiles(
            group='iso_domain',
            fileList=uninstall_files,
        )

# vim: expandtab tabstop=4 shiftwidth=4
