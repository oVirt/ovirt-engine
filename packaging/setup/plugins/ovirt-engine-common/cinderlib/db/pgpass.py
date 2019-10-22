#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""cinderlib DB pgpass plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup.cinderlib import constants as oclcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """cinderlib DB pgpass plugin."""
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[oclcons.CinderlibDBEnv.PGPASS_FILE] = None

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        name=oclcons.Stages.DB_CL_CREDENTIALS_AVAILABLE_EARLY,
        condition=lambda self: self.environment[
            oclcons.CinderlibDBEnv.PASSWORD
        ] is not None
    )
    def _validation(self):
        # this required for dbvalidations
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=oclcons.Const.CINDERLIB_DB_ENV_KEYS,
        ).createPgPass()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oclcons.Stages.DB_CL_CREDENTIALS_AVAILABLE_LATE,
        condition=lambda self: self.environment[
            oclcons.CinderlibDBEnv.PASSWORD
        ] is not None,
    )
    def _misc(self):
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=oclcons.Const.CINDERLIB_DB_ENV_KEYS,
        ).createPgPass()


# vim: expandtab tabstop=4 shiftwidth=4
