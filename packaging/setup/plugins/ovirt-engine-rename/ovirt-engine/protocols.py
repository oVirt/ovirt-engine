#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

# This file was copied from:
# packaging/setup/plugins/ovirt-engine-setup/config/protocols.py
# with the follwing changes:
# - Using the fqdn from RenameEnv
# - Updating FILES_TO_BE_MODIFIED
# TODO: Some day merge these back

"""Protocols plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Protocols plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
        ].append(
            oenginecons.FileLocations.
            OVIRT_ENGINE_SERVICE_CONFIG_PROTOCOLS
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        config = (
            oenginecons.FileLocations.
            OVIRT_ENGINE_SERVICE_CONFIG_PROTOCOLS
        )

        with open(config, 'r') as f:
            content = []
            for line in f:
                line = line.rstrip('\n')
                if line.startswith('ENGINE_FQDN='):
                    line = (
                        'ENGINE_FQDN=%s'
                    ) % (
                        self.environment[
                            osetupcons.RenameEnv.FQDN
                        ],
                    )
                content.append(line)

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=config,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
