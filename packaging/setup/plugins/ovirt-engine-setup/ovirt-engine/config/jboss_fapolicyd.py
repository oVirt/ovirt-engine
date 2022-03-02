#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Jboss fapolicyd plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import configfile
from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """JBoss fapolicyd plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.FapolicydEnv.FAPOLICYD_ALLOW_OVIRT_RULE,
            oengcommcons.FileLocations.FAPOLICYD_ALLOW_OVIRT_JBOSS_RULE
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
                self.environment[oenginecons.CoreEnv.ENABLE] and
                not os.path.exists(oengcommcons.FileLocations.FAPOLICYD_ALLOW_OVIRT_JBOSS_RULE) and
                not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        )
    )
    def _misc(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        engine_tmp_dir = os.path.join(
            config.get('JBOSS_RUNTIME'),
            'tmp'
        )

        self.environment[oengcommcons.FapolicydEnv.NEED_RESTART] = True
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self.environment[
                    oengcommcons.FapolicydEnv.FAPOLICYD_ALLOW_OVIRT_RULE
                ],
                content=outil.processTemplate(
                    template=(
                        oengcommcons.FileLocations.FAPOLICYD_ALLOW_OVIRT_JBOSS_RULE_TEMPLATE
                    ),
                    subst={
                        '@JBOSS_RUNTIME_TMP_DIR@': engine_tmp_dir,
                    },
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
