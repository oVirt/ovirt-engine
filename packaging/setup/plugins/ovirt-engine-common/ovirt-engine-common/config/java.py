#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Java plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import java

from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Java plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JAVA_HOME,
            None
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JAVA_NEEDED,
            False
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: (
            self.environment[
                oengcommcons.ConfigEnv.JAVA_NEEDED
            ] and self.environment[
                oengcommcons.ConfigEnv.JAVA_HOME
            ] is None
        ),
    )
    def _validation(self):
        self.environment[
            oengcommcons.ConfigEnv.JAVA_HOME
        ] = java.Java().getJavaHome()


# vim: expandtab tabstop=4 shiftwidth=4
