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


"""database plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin
from otopi import transaction


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common \
    import constants as oengcommcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import database
from ovirt_engine_setup import domains


@util.export
class Plugin(plugin.PluginBase):
    """database plugin."""

    class DBTransaction(transaction.TransactionElement):
        """yum transaction element."""

        def __init__(self, parent):
            self._parent = parent

        def __str__(self):
            return _("Database Transaction")

        def prepare(self):
            pass

        def abort(self):
            connection = self._parent.environment[
                oenginecons.EngineDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.rollback()
                self._parent.environment[
                    oenginecons.EngineDBEnv.CONNECTION
                ] = None

        def commit(self):
            connection = self._parent.environment[
                oenginecons.EngineDBEnv.CONNECTION
            ]
            if connection is not None:
                connection.commit()

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.DBTransaction(self)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
    )
    def _connection(self):
        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ] = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        # must be here as we do not have database at validation
        self.environment[
            oenginecons.EngineDBEnv.CONNECTION
        ] = self.environment[oenginecons.EngineDBEnv.STATEMENT].connect()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        )
        dbovirtutils.tryDatabaseConnect()
        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        my_domains = []
        rows = dbstatement.execute(
            statement="""
                select
                    storage_name,
                    connection
                from
                    storage_domain_static s,
                    storage_server_connections c
                where
                    s.storage = c.id and
                    s.storage_type=%(storage_type)s and
                    s.storage_domain_type=%(storage_domain_type)s
            """,
            args=dict(
                storage_type=domains.StorageType.NFS,
                storage_domain_type=domains.StorageDomainType.ISO,
            ),
            ownConnection=True,
        )
        for row in rows:
            host, path = row['connection'].split(':', 1)
            if host == self.environment[osetupcons.ConfigEnv.FQDN]:
                my_domains.append(row['storage_name'])
        if my_domains:
            self.logger.warning(_('Engine host hosting Storage Domains'))
            self.dialog.note(
                text=_(
                    'The following Storage Domains use the engine host\n'
                    'as an NFS server:\n'
                    '\n'
                    '{domains}\n'
                    '\n'
                    'Cannot rename the engine host. Please backup relevant\n'
                    'data if needed, remove all of these domains, and then\n'
                    'run this utility again.\n'
                ).format(
                    domains='\n'.join(sorted(my_domains))
                ),
            )
            raise RuntimeError(_('Cannot rename host hosting Storage Domains'))


# vim: expandtab tabstop=4 shiftwidth=4
