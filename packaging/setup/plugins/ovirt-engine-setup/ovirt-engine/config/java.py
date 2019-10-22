#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Java plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.ConfigEnv.ENGINE_HEAP_MIN,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ENGINE_HEAP_MAX,
            None
        )
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            oenginecons.Stages.CORE_ENABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _customization(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG,
        ])

        if not (
            config.get('ENGINE_HEAP_MIN') and
            config.get('ENGINE_HEAP_MAX')
        ):
            self._enabled = True

        calculated_heap_size = '{sizemb}M'.format(
            sizemb=max(
                1024,
                self.environment[osetupcons.ConfigEnv.TOTAL_MEMORY_MB] // 4
            )
        )

        if self.environment[
            oenginecons.ConfigEnv.ENGINE_HEAP_MIN
        ] is None:
            self.environment[
                oenginecons.ConfigEnv.ENGINE_HEAP_MIN
            ] = config.get('ENGINE_HEAP_MIN') or calculated_heap_size

        if self.environment[
            oenginecons.ConfigEnv.ENGINE_HEAP_MAX
        ] is None:
            self.environment[
                oenginecons.ConfigEnv.ENGINE_HEAP_MAX
            ] = config.get('ENGINE_HEAP_MAX') or calculated_heap_size

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_JAVA
                ),
                content=[
                    'ENGINE_HEAP_MIN="{heap_min}"'.format(
                        heap_min=self.environment[
                            oenginecons.ConfigEnv.ENGINE_HEAP_MIN
                        ],
                    ),
                    'ENGINE_HEAP_MAX="{heap_max}"'.format(
                        heap_max=self.environment[
                            oenginecons.ConfigEnv.ENGINE_HEAP_MAX
                        ],
                    ),
                ],
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
