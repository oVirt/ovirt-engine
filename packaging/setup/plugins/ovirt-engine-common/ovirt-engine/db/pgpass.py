#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""DB pgpass plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """DB pgpass plugin."""
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[oenginecons.EngineDBEnv.PGPASS_FILE] = None

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        name=oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        condition=lambda self: self.environment[
            oenginecons.EngineDBEnv.PASSWORD
        ] is not None
    )
    def _validation(self):
        # this required for dbvalidations
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        ).createPgPass()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
        condition=lambda self: self.environment[
            oenginecons.EngineDBEnv.PASSWORD
        ] is not None,
    )
    def _misc(self):
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
        ).createPgPass()


# vim: expandtab tabstop=4 shiftwidth=4
