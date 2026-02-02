#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Managed Block Misc plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.managedblock import constants as ombcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Managed Block Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            ombcons.CoreEnv.ENABLE,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def __customization_managedblock_db_enable(self):
        if self.environment[ombcons.CoreEnv.ENABLE] is None:
            self.environment[ombcons.CoreEnv.ENABLE] = \
                dialog.queryBoolean(
                    dialog=self.dialog,
                    name='ovirt-managedblock-enable',
                    note=_(
                        'Configure managed block integration '
                        '(note that the legacy CinderLib '
                        'adapter is not supported on RHEL 10)'
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=False
                )

# vim: expandtab tabstop=4 shiftwidth=4
