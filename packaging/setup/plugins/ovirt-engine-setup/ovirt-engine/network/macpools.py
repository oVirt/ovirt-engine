#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2018 Red Hat, Inc.
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

    MAC_POOL_ID = '58ca604b-017d-0374-0220-00000000014e'

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _get_count_of_mac_pool_ranges(self):
        rows = self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                SELECT count(mac_pool_id) AS mac_pool_ranges_count
                FROM mac_pool_ranges
                WHERE mac_pool_id = %(mac_pool_id)s
            """,
            args=dict(
                mac_pool_id=self.MAC_POOL_ID,
            ),
        )
        return rows[0].get('mac_pool_ranges_count')

    def _create_new_mac_pool_range(self, range_prefix):
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
                mac_pool_id=self.MAC_POOL_ID,
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
        if self._get_count_of_mac_pool_ranges() == 0:
            self.logger.info(_('Creating default mac pool'))
            range_prefix = self._generate_random_mac_pool_range_prefix()
            self._create_new_mac_pool_range(range_prefix)

# vim: expandtab tabstop=4 shiftwidth=4
