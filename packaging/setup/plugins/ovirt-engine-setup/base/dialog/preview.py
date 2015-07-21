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


"""Preview plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Preview plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.DialogEnv.CONFIRM_SETTINGS,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LOW,
    )
    def _customization(self):
        self.dialog.note(
            text=_('\n--== CONFIGURATION PREVIEW ==--\n\n'),
        )
        shown = set()
        for c in sum(
            [
                constobj.__dict__['__osetup_attrs__']
                for constobj in self.environment[
                    osetupcons.CoreEnv.SETUP_ATTRS_MODULES
                ]
            ],
            [],
        ):
            for k in c.__dict__.values():
                if hasattr(k, '__osetup_attrs__'):
                    attrs = k.__osetup_attrs__
                    if (
                        attrs['summary'] and
                        attrs['summary_condition'](self.environment)
                    ):
                        env = k.fget(None)
                        value = self.environment.get(env)
                        if value is not None and env not in shown:
                            shown.add(env)
                            self.dialog.note(
                                text=_('{key:40}: {value}').format(
                                    key=(
                                        attrs['description']
                                        if attrs['description'] is not None
                                        else env
                                    ),
                                    value=value,
                                ),
                            )

        confirmed = self.environment[
            osetupcons.DialogEnv.CONFIRM_SETTINGS
        ]
        if confirmed is None:
            confirmed = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_DIALOG_CONFIRM_SETTINGS',
                note=_(
                    '\n'
                    'Please confirm installation settings '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('OK'),
                false=_('Cancel'),
                default=True,
            )

        if not confirmed:
            raise RuntimeError(_('Configuration was rejected by user'))

        self.environment[
            osetupcons.DialogEnv.CONFIRM_SETTINGS
        ] = True


# vim: expandtab tabstop=4 shiftwidth=4
