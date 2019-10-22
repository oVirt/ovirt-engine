#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Preview plugin."""


import gettext

from otopi import plugin
from otopi import util

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
                                text=_(u'{key:40}: {value}').format(
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
