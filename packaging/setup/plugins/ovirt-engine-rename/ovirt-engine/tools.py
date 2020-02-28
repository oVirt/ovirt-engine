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

    # 'section' is not used here, left for reference - hopefully
    # one day the code will be merged with the generation code
    TOOLS_CONFIG = (
        {
            'dir': '{engine_sysconf}/logcollector.conf.d',
            'section': 'LogCollector',
        },
    )

    def _entry_filename(self, entry):
        return os.path.join(
            entry['dir'],
            '10-engine-setup.conf'
        ).format(
            engine_sysconf=(
                oenginecons.FileLocations.
                OVIRT_ENGINE_SYSCONFDIR
            ),
        )

    def _content_with_renamed_fqdn(self, config):
        with open(config, 'r') as f:
            content = []
            for line in f:
                line = line.rstrip('\n')
                if line.startswith('engine='):
                    line = (
                        'engine=%s:%s'
                    ) % (
                        self.environment[
                            osetupcons.RenameEnv.FQDN
                        ],
                        self.environment[
                            oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
                        ],
                    )
                content.append(line)
        return content

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        for entry in self.TOOLS_CONFIG:
            self.environment[
                osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
            ].append(self._entry_filename(entry))

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        for entry in self.TOOLS_CONFIG:
            name = self._entry_filename(entry)
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=name,
                    content=self._content_with_renamed_fqdn(name),
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
