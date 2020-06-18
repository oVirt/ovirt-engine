#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Cinderlib Misc plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.cinderlib import constants as oclcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Cinderlib Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oclcons.CoreEnv.ENABLE,
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
    def __customization_cinderlib_db_enable(self):
        if self.environment[oclcons.CoreEnv.ENABLE] is None:
            self.environment[oclcons.CoreEnv.ENABLE] = \
                dialog.queryBoolean(
                    dialog=self.dialog,
                    name='ovirt-cinderlib-enable',
                    note=_(
                        'Configure Cinderlib integration '
                        '(Currently in tech preview) '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=False
                )

# vim: expandtab tabstop=4 shiftwidth=4
