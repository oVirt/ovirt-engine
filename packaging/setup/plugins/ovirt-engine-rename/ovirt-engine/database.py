#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""database plugin."""


import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import domains
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """database plugin."""

    class DBTransaction(transaction.TransactionElement):
        """DB transaction element."""

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

    def _local_domains_in_use(self):
        res = False
        dbovirtutils = database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        )
        dbovirtutils.tryDatabaseConnect()
        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        self._my_domains = []
        rows = dbstatement.execute(
            statement="""
                select
                    s.id,
                    s.status,
                    s.storage,
                    storage_name,
                    connection,
                    storage_domain_type
                from
                    storage_domains s,
                    storage_server_connections c
                where
                    s.storage = c.id and
                    s.storage_type=%(storage_type)s
            """,
            args=dict(
                storage_type=domains.StorageType.NFS,
            ),
            ownConnection=True,
        )
        for row in rows:
            host, path = row['connection'].split(':', 1)
            if host == self._current_fqdn:
                self._my_domains.append(row)

        if self._my_domains:
            self.logger.info(_('Engine machine hosting Storage Domains'))

        vms_with_iso = []
        for dom in self._my_domains:
            rows = dbstatement.execute(
                statement="""
                    select
                        distinct vm_name
                    from
                        storage_domains s,
                        vms,
                        cluster c,
                        storage_pool sp,
                        storage_domains sd
                    where
                        sd.storage_pool_id = sp.id and
                        sp.id = c.storage_pool_id and
                        c.cluster_id = vms.cluster_id and
                        sd.id = %(sd_id)s and
                        vms.current_cd != '' and
                        vms.status > 0
                """,
                args=dict(
                    sd_id=dom['id'],
                ),
                ownConnection=True,
            )
            vms_with_iso.extend([r['vm_name'] for r in rows])
        if vms_with_iso:
            res = True
            self.dialog.note(
                text=_(
                    'The following Virtual Machines have attached ISO images '
                    'from one or more of the below Storage Domains:\n'
                    '\n'
                    '{vms_with_iso}\n'
                    '\n'
                    'Needed action: They should be shut down, and/or '
                    'have the ISO images disconnected (e.g. by ejecting them).'
                    '\n\n'
                ).format(
                    vms_with_iso='\n'.join(sorted(set(vms_with_iso))),
                ),
            )

        vms_with_disks = []
        for dom in self._my_domains:
            rows = dbstatement.execute(
                statement="""
                    select
                        distinct vm_name
                    from
                        all_disks_for_vms adfv,
                        vms
                    where
                        storage_id = %(sd_id)s and
                        adfv.vm_id = vms.vm_guid and
                        vms.status > 0
                """,
                args=dict(
                    sd_id=dom['id'],
                ),
                ownConnection=True,
            )
            vms_with_disks.extend([r['vm_name'] for r in rows])
        if vms_with_disks:
            res = True
            self.dialog.note(
                text=_(
                    'The following Virtual Machines have attached disk '
                    'images from one or more of the below Storage Domains:\n'
                    '\n'
                    '{vms_with_disks}\n'
                    '\n'
                    'Needed action: They should be shut down.'
                    '\n\n'
                ).format(
                    vms_with_disks='\n'.join(sorted(set(vms_with_disks))),
                ),
            )

        active_domains = [
            r for r in self._my_domains
            if r['status'] == domains.StorageDomainStatus.ACTIVE
        ]
        if active_domains:
            res = True
            self.dialog.note(
                text=_(
                    'The following Storage Domains use the engine '
                    'machine as an NFS server, and are active:\n'
                    '\n'
                    '{domains}\n'
                    '\n'
                    'Needed action: They should be moved to Maintenance.'
                    '\n\n'
                ).format(
                    domains='\n'.join(
                        sorted(
                            [d['storage_name'] for d in active_domains]
                        )
                    )
                ),
            )

        if not res and self._my_domains:
            # Lastly inform the user if we are going to rename
            # local storage domains (which are not in use)
            self.dialog.note(
                text=_(
                    'The following Storage Domains use the engine '
                    'machine as an NFS server:\n'
                    '\n'
                    '{domains}\n'
                    '\n'
                    'They will be modified to use the new name.\n'
                    '\n'
                ).format(
                    domains='\n'.join(
                        sorted(
                            [d['storage_name'] for d in self._my_domains]
                        )
                    )
                ),
            )

        return res

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            self.DBTransaction(self)
        )
        self._current_fqdn = self.environment[osetupcons.ConfigEnv.FQDN]
        self._my_domains = []

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        while self._local_domains_in_use():
            self.dialog.queryString(
                name='OVESETUP_RENAME_LOCAL_STORAGE_USED',
                note=_(
                    'Some storage domains hosted on the engine machine '
                    'are in use. Please complete the above detailed '
                    'actions.\n'
                    'To abort, simply kill this utility with ^C.\n'
                    'Press Enter to continue: '
                ),
                prompt=True,
                default='y',  # Allow just pressing Enter
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
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._my_domains,
    )
    def _rename_storage(self):
        for row in self._my_domains:
            self.logger.debug(
                'Modifying connection of Storage Domain id %(id)s '
                'name %(storage_name)s',
                row
            )
            self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ].execute(
                statement="""
                    update storage_server_connections
                    set connection = regexp_replace(
                        connection,
                        '^{old_fqdn}:',
                        '{new_fqdn}:'
                    )
                    where id = %(storage_id)s
                """.format(
                    old_fqdn=self._current_fqdn,
                    new_fqdn=self.environment[
                        osetupcons.RenameEnv.FQDN
                    ],
                ),
                args=dict(
                    storage_id=row['storage'],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
