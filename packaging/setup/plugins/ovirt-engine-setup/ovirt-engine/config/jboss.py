#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Jboss plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """JBoss plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[oengcommcons.ConfigEnv.JAVA_NEEDED] = True
        self.environment[oengcommcons.ConfigEnv.JBOSS_NEEDED] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        for f in (
            (
                oenginecons.FileLocations.
                OVIRT_ENGINE_SERVICE_CONFIG_JBOSS
            ),
            (
                oenginecons.FileLocations.
                OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JBOSS
            ),
        ):
            content = [
                'JBOSS_HOME="{jbossHome}"'.format(
                    jbossHome=self.environment[
                        oengcommcons.ConfigEnv.JBOSS_HOME
                    ],
                ),
            ]
            if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
                content.extend(
                    (
                        'ENGINE_LOG_TO_CONSOLE=true',
                        'ENGINE_DEPLOYMENT_SCANNER=true',
                    )
                )
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=f,
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
