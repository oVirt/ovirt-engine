#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Titles plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Titles plugin."""

    def _title(self, text):
        self.dialog.note(
            text='\n--== %s ==--\n\n' % text,
        )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup_common_titles(self):
        self.environment.setdefault(
            oengcommcons.ConfigEnv.NEED_COMMON_TITLES,
            True
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.NEED_COMMON_TITLES
        ],
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        )
    )
    def _title_s_product_options(self):
        self._title(
            text=_('PRODUCT OPTIONS'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.NEED_COMMON_TITLES
        ],
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def _title_e_product_options(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.NEED_COMMON_TITLES
        ],
    )
    def _title_s_summary(self):
        self._title(
            text=_('SUMMARY'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        condition=lambda self: self.environment[
            oengcommcons.ConfigEnv.NEED_COMMON_TITLES
        ],
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _title_e_summary(self):
        self._title(
            text=_('END OF SUMMARY'),
        )


# vim: expandtab tabstop=4 shiftwidth=4
