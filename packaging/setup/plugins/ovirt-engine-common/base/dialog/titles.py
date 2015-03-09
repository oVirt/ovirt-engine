#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Titles plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons


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
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
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
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def _title_e_product_options(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
    )
    def _title_s_summary(self):
        self._title(
            text=_('SUMMARY'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        name=osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _title_e_summary(self):
        self._title(
            text=_('END OF SUMMARY'),
        )


# vim: expandtab tabstop=4 shiftwidth=4
