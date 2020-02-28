#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Tools configuration plugin."""


import gettext
import os

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
    """Tools configuration plugin."""

    TOOLS_CONFIG = [
        {
            "dir": "{engine_sysconf}/logcollector.conf.d",
            "section": "LogCollector",
        },
    ]

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        for entry in self.TOOLS_CONFIG:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=(
                        os.path.join(
                            entry['dir'],
                            "10-engine-setup.conf"
                        ).format(
                            engine_sysconf=(
                                oenginecons.FileLocations.
                                OVIRT_ENGINE_SYSCONFDIR
                            ),
                        )
                    ),
                    content=(
                        (
                            "[{section}]\n"
                            "engine={fqdn}:{port}\n"
                            "user={user}\n"
                        ).format(
                            section=entry['section'],
                            fqdn=self.environment[
                                osetupcons.ConfigEnv.FQDN
                            ],
                            port=self.environment[
                                oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
                            ],
                            user=self.environment[
                                oenginecons.ConfigEnv.ADMIN_USER
                            ],
                        )
                    ),
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
