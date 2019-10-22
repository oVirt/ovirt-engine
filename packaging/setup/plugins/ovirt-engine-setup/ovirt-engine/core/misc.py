#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Misc plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._engine_fqdn = None

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(oenginecons.CoreEnv.ENABLE, None)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG,
        ])
        if config.get('ENGINE_FQDN'):
            self._engine_fqdn = config.get('ENGINE_FQDN')

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oenginecons.Stages.CORE_ENABLE,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def _customization(self):
        if self.environment[oenginecons.CoreEnv.ENABLE] is None:
            self.environment[oenginecons.CoreEnv.ENABLE] = (
                dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_ENGINE_ENABLE',
                    note=_(
                        'Configure Engine on this host '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                ) if self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]
                else (
                    self._engine_fqdn is not None and
                    self.environment[
                        osetupcons.ConfigEnv.FQDN
                    ] == self._engine_fqdn
                )
            )
        if self.environment[oenginecons.CoreEnv.ENABLE]:
            self.environment[oengcommcons.ApacheEnv.ENABLE] = True
            self.environment[
                oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
            ] = True


# vim: expandtab tabstop=4 shiftwidth=4
