#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Schema plugin."""


import gettext
import os

import libxml2

from otopi import constants as otopicons
from otopi import plugin
from otopi import transaction
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Schema plugin."""

    class SchemaTransaction(transaction.TransactionElement):
        """DB Schema transaction element."""

        def __init__(self, parent, backup=None):
            self._parent = parent
            self._backup = backup

        def __str__(self):
            return _("Engine schema Transaction")

        def prepare(self):
            pass

        def abort(self):
            self._parent.logger.info(_('Rolling back database schema'))
            try:
                dbovirtutils = database.OvirtUtils(
                    plugin=self._parent,
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                )
                self._parent.logger.info(
                    _('Clearing Engine database {database}').format(
                        database=self._parent.environment[
                            oenginecons.EngineDBEnv.DATABASE
                        ],
                    )
                )
                dbovirtutils.clearDatabase()
                if self._backup is not None and os.path.exists(self._backup):
                    self._parent.logger.info(
                        _('Restoring Engine database {database}').format(
                            database=self._parent.environment[
                                oenginecons.EngineDBEnv.DATABASE
                            ],
                        )
                    )
                    dbovirtutils.restore(backupFile=self._backup)
            except Exception as e:
                self._parent.logger.debug(
                    'Error during Engine database restore',
                    exc_info=True,
                )
                self._parent.logger.error(
                    _('Engine database rollback failed: {error}').format(
                        error=e,
                    )
                )

        def commit(self):
            pass

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _checkCompatibilityVersion(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        supported = set([
            x.strip()
            for x in self.environment[
                osetupcons.CoreEnv.UPGRADE_SUPPORTED_VERSIONS
            ].split(',')
            if x.strip()
        ])
        vms = statement.execute(
            statement="""
                select
                    vm_name,
                    custom_compatibility_version
                from
                    vms
                where
                    custom_compatibility_version is not null
                    and
                    custom_compatibility_version <> '';
            """,
            ownConnection=True,
            transaction=False,
        )
        if vms:
            names = [
                vm['vm_name']
                for vm in vms if
                vm['custom_compatibility_version']
                not in supported
            ]
            if names:
                raise RuntimeError(
                    _(
                        'Cannot upgrade the Engine due to low '
                        'custom_compatibility_version for virtual machines: '
                        '{r}. Please edit this virtual machines, in edit VM '
                        'dialog go to System->Advanced Parameters -> Custom '
                        'Compatibility Version and either reset to empty '
                        '(cluster default) or set a value supported by the '
                        'new installation: {s}.'
                    ).format(
                        r=names,
                        s=', '.join(sorted(supported)),
                    )
                )

    def _checkSnapshotCompatibilityVersion(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        supported = set([
            x.strip()
            for x in self.environment[
                osetupcons.CoreEnv.UPGRADE_SUPPORTED_VERSIONS
            ].split(',')
            if x.strip()
        ])
        snapshots = statement.execute(
            statement="""
                select
                    vms.vm_name,
                    snapshots.description,
                    snapshots.vm_configuration
                from
                    vms,
                    snapshots
                where
                    snapshots.vm_id=vms.vm_guid
                    and
                    snapshot_type='REGULAR'
            """,
            ownConnection=True,
            transaction=False,
            logResult=False,
        )
        old_snapshots = []
        if snapshots:
            for snapshot in snapshots:
                vm_configuration = snapshot['vm_configuration']
                creation_date = 'UnknownDate'
                snapshot_cl = 'UnknownLevel'
                try:
                    doc = libxml2.parseDoc(vm_configuration)
                    ctx = doc.xpathNewContext()
                    ctx.xpathRegisterNs(
                        'ovf', 'http://schemas.dmtf.org/ovf/envelope/1/'
                    )
                    compat_level_nodes = ctx.xpathEval(
                        "/ovf:Envelope/Content/ClusterCompatibilityVersion"
                    )
                    if not compat_level_nodes:
                        # Didn't find them, probably because in <= 4.2 we
                        # had a wrong namespace. Try also that one.
                        ctx.xpathRegisterNs(
                            'ovf', 'http://schemas.dmtf.org/ovf/envelope/1'
                        )
                        compat_level_nodes = ctx.xpathEval(
                            "/ovf:Envelope/Content/ClusterCompatibilityVersion"
                        )
                    creation_date_nodes = ctx.xpathEval(
                        "/ovf:Envelope/Content/CreationDate"
                    )
                    if creation_date_nodes:
                        creation_date = creation_date_nodes[0].content
                    if compat_level_nodes:
                        snapshot_cl = compat_level_nodes[0].content
                except Exception:
                    creation_date = 'UnknownDate'
                    snapshot_cl = 'UnknownLevel'
                self.logger.debug(
                    'Found snapshot: %(vm)s:%(snap)s '
                    'created %(date)s version %(v)s',
                    {
                        'vm': snapshot['vm_name'],
                        'snap': snapshot['description'],
                        'date': creation_date,
                        'v': snapshot_cl,
                    },
                )
                if snapshot_cl not in supported:
                    old_snapshots.append(
                        '{vm}:{snap} level {v} (created {date})'.format(
                            vm=snapshot['vm_name'],
                            snap=snapshot['description'],
                            date=creation_date,
                            v=snapshot_cl,
                        )
                    )
            if old_snapshots:
                if not dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_IGNORE_SNAPSHOTS_WITH_OLD_COMPAT_LEVEL',
                    note=_(
                        '\nThe following virtual machines have snapshots with '
                        'older compatibility levels, which are not supported '
                        'by the version you upgrade to, so you will not be '
                        'able to use them:\n\n'
                        '{old_snapshots}\n\n'
                        'Proceed? (@VALUES@) [@DEFAULT@]: '
                    ).format(
                        old_snapshots='\n'.join(old_snapshots),
                    ),
                    default=False,
                    prompt=True,
                ):
                    raise RuntimeError(_('Aborted by user'))

    def _checkInvalidImages(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )

        invalidImagesForVms = statement.execute(
            statement="""
                SELECT
                    disk_alias,
                    image_guid,
                    vm_name
                FROM
                    images
                INNER JOIN vm_device ON
                    images.image_group_id = vm_device.device_id
                INNER JOIN vm_static ON vm_device.vm_id = vm_static.vm_guid
                INNER JOIN base_disks ON
                    images.image_group_id = base_disks.disk_id
                AND vm_static.entity_type = 'VM'
                AND vm_device.type = 'disk'
                AND vm_device.device = 'disk'
                AND images.vm_snapshot_id =
                    '00000000-0000-0000-0000-000000000000';
            """,
            ownConnection=True,
            transaction=False,
        )

        if invalidImagesForVms:
            self.logger.warn(
                _(
                    'Engine DB is inconsistent due to the existence of invalid'
                    ' {num} image(s) for virtual machine(s) as follows:\n'
                    '{imagesList}.\n'
                    '\nPlease consult support to resolve this issue. '
                    'Note that the upgrade will be blocked in the subsequent '
                    'release (4.3) if the issue isn\'t resolved.\n'
                    'If you choose to ignore this problem then snapshot '
                    'operations on the above virtual machine(s) may fail or '
                    'may corrupt the disk(s).\nTo fix this issue, you can '
                    'clone the virtual machine(s) by starting the engine, '
                    'searching for the affected\nvirtual machine(s) by name '
                    '(as listed above) and clicking on \'Clone VM\' for each '
                    'virtual machine in the list.\n'
                    'Warning: If there are snapshots for the cloned virtual '
                    'machine(s), they will be collapsed.\n\n'
                ).format(
                    num=len(invalidImagesForVms),
                    imagesList=invalidImagesForVms
                )
            )

    def _checkDatabaseOwnership(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        result = statement.execute(
            statement="""
                select
                    nsp.nspname as object_schema,
                    cls.relname as object_name,
                    rol.rolname as owner,
                    case cls.relkind
                        when 'r' then 'TABLE'
                        when 'i' then 'INDEX'
                        when 'S' then 'SEQUENCE'
                        when 'v' then 'VIEW'
                        when 'c' then 'TYPE'
                    else
                        cls.relkind::text
                    end as object_type
                from
                    pg_class cls join
                    pg_roles rol on rol.oid = cls.relowner join
                    pg_namespace nsp on nsp.oid = cls.relnamespace
                where
                    nsp.nspname not in ('information_schema', 'pg_catalog') and
                    nsp.nspname not like 'pg_%%' and
                    cls.relname not like 'pg_%%' and
                    rol.rolname != %(user)s
                order by
                    nsp.nspname,
                    cls.relname
            """,
            args=dict(
                user=self.environment[oenginecons.EngineDBEnv.USER],
            ),
            ownConnection=True,
            transaction=False,
        )
        if len(result) > 0:
            raise RuntimeError(
                _(
                    'Cannot upgrade the Engine database schema due to wrong '
                    'ownership of some database entities.\n'
                )
            )

    def _checkSupportedVersionsPresent(self):
        # TODO: figure out a better way to do this for the future
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        dcVersions = statement.execute(
            statement="""
                SELECT name, compatibility_version FROM storage_pool;
            """,
            ownConnection=True,
            transaction=False,
        )
        clusterTable = statement.execute(
            statement="""
                SELECT table_name FROM information_schema.tables
                WHERE table_name IN ('vds_groups', 'cluster');
            """,
            ownConnection=True,
            transaction=False,
        )
        sql = _(
            'SELECT name, compatibility_version FROM {table};'
        ).format(
            table=clusterTable[0]['table_name']
        )
        clusterVersions = statement.execute(
            statement=sql,
            ownConnection=True,
            transaction=False,
        )

        versions = set([
            x['compatibility_version']
            for x in dcVersions + clusterVersions
        ])
        supported = set([
            x.strip()
            for x in self.environment[
                osetupcons.CoreEnv.UPGRADE_SUPPORTED_VERSIONS
            ].split(',')
            if x.strip()
        ])

        if versions - supported:
            for (queryres, errmsg) in (
                (
                    dcVersions,
                    _(
                        'The following Data Centers have a too old '
                        'compatibility level, please upgrade them:'
                    )
                ),
                (
                    clusterVersions,
                    _(
                        'The following Clusters have a too old '
                        'compatibility level, please upgrade them:'
                    )
                ),
            ):
                objs = [
                    x['name']
                    for x in queryres
                    if x['compatibility_version'] not in supported
                ]
                if objs:
                    self.logger.error(errmsg)
                    self.dialog.note('\n'.join(objs))

            raise RuntimeError(
                _(
                    'Trying to upgrade from unsupported versions: {versions}'
                ).format(
                    versions=' '.join(versions - supported)
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _validation(self):
        self._checkDatabaseOwnership()
        self._checkSupportedVersionsPresent()
        self._checkCompatibilityVersion()
        self._checkSnapshotCompatibilityVersion()
        self._checkInvalidImages()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_SCHEMA,
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        backupFile = None

        # If we are upgrading to a newer postgresql, do not backup or rollback.
        # If we upgrade by copying, we can rollback by using the old
        # version. If we upgrade in-place, we do not support rollback,
        # and user should take care of backups elsewhere.
        if not self.environment[
            oenginecons.EngineDBEnv.NEED_DBMSUPGRADE
        ]:
            if not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]:
                dbovirtutils = database.OvirtUtils(
                    plugin=self,
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                )
                backupFile = dbovirtutils.backup(
                    dir=self.environment[
                        oenginecons.ConfigEnv.OVIRT_ENGINE_DB_BACKUP_DIR
                    ],
                    prefix=oenginecons.Const.ENGINE_DB_BACKUP_PREFIX,
                )
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                self.SchemaTransaction(
                    parent=self,
                    backup=backupFile,
                )
            )

        self.logger.info(_('Creating/refreshing Engine database schema'))
        args = [
            oenginecons.FileLocations.OVIRT_ENGINE_DB_SCHMA_TOOL,
            '-s', self.environment[oenginecons.EngineDBEnv.HOST],
            '-p', str(self.environment[oenginecons.EngineDBEnv.PORT]),
            '-u', self.environment[oenginecons.EngineDBEnv.USER],
            '-d', self.environment[oenginecons.EngineDBEnv.DATABASE],
            '-l', self.environment[otopicons.CoreEnv.LOG_FILE_NAME],
            '-c', 'apply',
        ]
        if self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]:
            if not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
            ):
                os.makedirs(
                    oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
                )
            args.extend(
                [
                    '-m',
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR,
                        '%s-%s.scripts.md5' % (
                            self.environment[
                                oenginecons.EngineDBEnv.HOST
                            ],
                            self.environment[
                                oenginecons.EngineDBEnv.DATABASE
                            ],
                        ),
                    ),
                ]
            )
        rc, stdout, stderr = self.execute(
            args=args,
            envAppend={
                'DBFUNC_DB_PGPASSFILE': self.environment[
                    oenginecons.EngineDBEnv.PGPASS_FILE
                ]
            },
            raiseOnError=False,
        )
        if rc:
            self.logger.error(
                '%s: %s',
                os.path.basename(
                    oenginecons.FileLocations.OVIRT_ENGINE_DB_SCHMA_TOOL
                ),
                stderr[-1] if stderr else "exited with non-zero return code."
            )
            raise RuntimeError(_('Engine schema refresh failed'))


# vim: expandtab tabstop=4 shiftwidth=4
