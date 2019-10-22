#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Notifier plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup.engine import constants as oenginecons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Notifier plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.ConfigEnv.IGNORE_VDS_GROUP_IN_NOTIFIER,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            oenginecons.CoreEnv.ENABLE
        ] and not self.environment[
            oenginecons.ConfigEnv.IGNORE_VDS_GROUP_IN_NOTIFIER
        ],
    )
    def _validation(self):
        config = configfile.ConfigFile(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG,
            ),
        )
        filterStr = config.get('FILTER')
        self.logger.debug('filterStr: %s', filterStr)
        if filterStr is not None and 'VDS_GROUP' in filterStr:
            ans = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_WAIT_NOTIFIER_FILTER',
                note=_(
                    'Setup found filter/s in engine-notifier configuration '
                    'files in {conf}.d/*.conf containing the string '
                    '"VDS_GROUP".\n You must manually change "VDS_GROUP" to '
                    '"CLUSTER" throughout the notifier configuration in '
                    'order to get notified on cluster related events.\n Do '
                    'you want to continue?\n'
                    '(Answering "no" will stop the upgrade '
                    '(@VALUES@) [@DEFAULT@]: '
                ).format(
                    conf=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG
                    ),
                ),
                prompt=True,
                default=False,
            )
            self.environment[
                oenginecons.ConfigEnv.IGNORE_VDS_GROUP_IN_NOTIFIER
            ] = ans
            if not ans:
                raise RuntimeError(_('Aborted by user'))


# vim: expandtab tabstop=4 shiftwidth=4
