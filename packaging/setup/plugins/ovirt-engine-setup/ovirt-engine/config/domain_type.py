#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""
Storage pool type configuration plugin
"""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """
    Storage pool type configuration plugin
    """
    STORAGE_TYPES = {
        'nfs': 1,
        'fc': 2,
        'iscsi': 3,
        'posixfs': 6,
        'glusterfs': 7,
    }

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.STORAGE_TYPE,
            None
        )
        self.environment.setdefault(
            osetupcons.ConfigEnv.STORAGE_IS_LOCAL,
            oenginecons.Defaults.DEFAULT_CONFIG_STORAGE_IS_LOCAL
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
        after=(
            osetupcons.Stages.CONFIG_APPLICATION_MODE_AVAILABLE,
            oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[oenginecons.EngineDBEnv.NEW_DATABASE] and
            self.environment[
                osetupcons.ConfigEnv.APPLICATION_MODE
            ] != 'gluster'
        ),
    )
    def _customization(self):
        self._enabled = True

        if self.environment[
            osetupcons.ConfigEnv.STORAGE_TYPE
        ] is not None:
            if self.environment[
                osetupcons.ConfigEnv.STORAGE_TYPE
            ] in self.STORAGE_TYPES:
                self.environment[
                    osetupcons.ConfigEnv.STORAGE_IS_LOCAL
                ] = False
            self.environment[
                osetupcons.ConfigEnv.STORAGE_TYPE
            ] = None

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select inst_update_default_storage_pool_type (%(is_local)s)
            """,
            args={
                'is_local': self.environment[
                    osetupcons.ConfigEnv.STORAGE_IS_LOCAL
                ],
            },
        )


# vim: expandtab tabstop=4 shiftwidth=4
