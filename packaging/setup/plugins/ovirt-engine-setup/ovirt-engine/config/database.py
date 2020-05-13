#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Database plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
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
    """Database plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_CREDENTIALS_WRITTEN,
        after=(
            oengcommcons.Stages.DB_SCHEMA,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_DATABASE
                ),
                mode=0o640,
                owner=self.environment[oengcommcons.SystemEnv.USER_ROOT],
                group=self.environment[osetupcons.SystemEnv.GROUP_ENGINE],
                enforcePermissions=True,
                content=database.OvirtUtils(
                    plugin=self,
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS
                ).getDBConfig(
                    prefix="ENGINE"
                ),
                visibleButUnsafe=True,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
