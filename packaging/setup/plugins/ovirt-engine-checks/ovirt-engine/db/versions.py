#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Versions plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Versions plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    # TODO: refactor avoid code duplication with ovirt-engine-setup
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

    # TODO: refactor avoid code duplication with ovirt-engine-setup
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

    def _checkHostType(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        hosts = statement.execute(
            statement="""
                            select
                                vds_name, vds_type
                            from
                                vds_static
                            where
                                vds_type = 2;
                """,
            ownConnection=True,
            transaction=False,
        )
        if hosts:
            names = [
                host['vds_name']
                for host in hosts
            ]
            raise RuntimeError(
                _(
                    'Cannot upgrade engine because legacy node hosts are not '
                    'supported anymore, but some legacy nodes are still '
                    'present in your setup: {}. '
                    'Please remove those hosts from your setup and try to '
                    'upgrade again.'
                ).format(
                    names
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
        self._checkSupportedVersionsPresent()
        self._checkCompatibilityVersion()
        self._checkHostType()


# vim: expandtab tabstop=4 shiftwidth=4
