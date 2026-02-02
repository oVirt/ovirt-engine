#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Managed Block Misc plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.managedblock import constants as ombcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Managed Block Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            ombcons.Stages.DB_MB_SCHEMA,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE]
        ),
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    ombcons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_MANAGEDBLOCK_MISC
                ),
                mode=0o640,
                owner=self.environment[oengcommcons.SystemEnv.USER_ROOT],
                group=self.environment[osetupcons.SystemEnv.GROUP_ENGINE],
                enforcePermissions=True,
                content=self.get_managedblock_db_enable_content(),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    def get_managedblock_db_enable_content(self):
        return (
            '{prefix}_DB_ENABLE="{enable}"\n'.format(
                prefix="MANAGEDBLOCK",
                enable=self.environment[ombcons.CoreEnv.ENABLE]
            )
        )
