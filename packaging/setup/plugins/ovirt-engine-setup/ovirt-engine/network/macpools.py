#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""macpool plugin."""

import gettext
import random

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """macpool plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _get_default_mac_pool_id(self):
        rows = self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                SELECT id
                FROM mac_pools
                WHERE default_pool IS true;
            """
        )
        if len(rows) > 0:
            return rows[0].get('id')

    def _get_count_of_mac_pool_ranges(self, mac_pool_id):
        rows = self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                SELECT count(mac_pool_id) AS mac_pool_ranges_count
                FROM mac_pool_ranges
                WHERE mac_pool_id = %(mac_pool_id)s
            """,
            args=dict(
                mac_pool_id=mac_pool_id,
            ),
        )
        return rows[0].get('mac_pool_ranges_count')

    def _create_new_mac_pool_range(self, range_prefix, mac_pool_id):
        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                SELECT InsertMacPoolRange(
                %(mac_pool_id)s,
                %(from_mac)s,
                %(to_mac)s
                )
            """,
            args=dict(
                mac_pool_id=mac_pool_id,
                from_mac=range_prefix + ':00:00',
                to_mac=range_prefix + ':ff:ff',
            )
        )

    @staticmethod
    def _generate_random_mac_pool_range_prefix():
        return '56:6f:{first:02x}:{second:02x}'.format(
            first=random.randrange(255),
            second=random.randrange(255)
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.MAC_POOL_DB,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE]
        ),
    )
    def _misc_db_entries(self):
        mac_pool_id = self._get_default_mac_pool_id()
        if (
            mac_pool_id and
            self._get_count_of_mac_pool_ranges(mac_pool_id) == 0
        ):
            self.logger.info(_('Creating default mac pool range'))
            range_prefix = self._generate_random_mac_pool_range_prefix()
            self._create_new_mac_pool_range(range_prefix, mac_pool_id)

# vim: expandtab tabstop=4 shiftwidth=4
