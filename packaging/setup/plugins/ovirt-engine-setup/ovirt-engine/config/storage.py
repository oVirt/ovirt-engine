#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""storage plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


SAN_WIPE_AFTER_DELETE = osetupcons.ConfigEnv.SAN_WIPE_AFTER_DELETE


@util.export
class Plugin(plugin.PluginBase):
    """storage plugin."""

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(SAN_WIPE_AFTER_DELETE, None)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_STORAGE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_STORAGE,
        ),
        condition=lambda self: self._enableCondition(),
    )
    def _configureSANWipeAfterDelete(self):
        if self.environment[SAN_WIPE_AFTER_DELETE] is None:
            # Value for SAN_WIPE_AFTER_DELETE is not forced.
            sanWipeAfterDelete = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONFIG_SAN_WIPE_AFTER_DELETE',
                note=_(
                    'Default SAN wipe after delete '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=False,
            )
            self.environment[SAN_WIPE_AFTER_DELETE] = sanWipeAfterDelete

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._enableCondition(),
    )
    def _updateSANWipeAfterDelete(self):
        option = vdcoption.VdcOption(
            statement=self.environment[oenginecons.EngineDBEnv.STATEMENT]
        )
        options = (
            {
                'name': 'SANWipeAfterDelete',
                'value': self.environment[SAN_WIPE_AFTER_DELETE],
            },
        )
        option.updateVdcOptions(options=options,)

    def _enableCondition(self):
        # Returns a condition that validates
        # we are installing the engine and we are running the setup
        # on a new database.
        return (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]
        )


# vim: expandtab tabstop=4 shiftwidth=4
