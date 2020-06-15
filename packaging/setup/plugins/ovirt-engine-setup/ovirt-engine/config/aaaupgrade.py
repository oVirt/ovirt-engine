#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""aaa upgrade plugin."""


import gettext
import os
import re

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Fixes naming of AAA classes and modules when upgrading from oVirt 4.3"""

    OLD_CONTENT = re.compile('org\\.ovirt\\.engine[-]?extensions\\.')
    NEW_CONTENT = 'org.ovirt.engine.extension.'

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.CONFIG_EXTENSIONS_UPGRADE,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ]
        ),
    )
    def _upgrade_aaa_configuration(self):
        with os.scandir(
            oenginecons.FileLocations.OVIRT_ENGINE_EXTENSIONS_DIR
        ) as entries:
            for entry in entries:
                if entry.is_file() and entry.name.endswith('.properties'):
                    content = ''
                    with open(entry.path, 'r') as f:
                        content = f.read()
                    if self.OLD_CONTENT.search(content):
                        self.logger.info(
                            _(
                                'Upgrading engine extension configuration: {}'
                            ).format(entry.path))
                        self.environment[
                            otopicons.CoreEnv.MAIN_TRANSACTION
                        ].append(
                            filetransaction.FileTransaction(
                                name=entry.path,
                                content=self.OLD_CONTENT.sub(
                                    self.NEW_CONTENT,
                                    content,
                                ),
                                modifiedList=self.environment[
                                    otopicons.CoreEnv.MODIFIED_FILES
                                ],
                            )
                        )


# vim: expandtab tabstop=4 shiftwidth=4
