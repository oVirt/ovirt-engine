#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Misc plugin."""


import gettext

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import hostname as osetuphostname


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
    )
    def _preinit(self):
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_DIR,
            osetupcons.FileLocations.OVIRT_SETUP_LOGDIR
        )
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_FILE_NAME_PREFIX,
            osetupcons.FileLocations.OVIRT_OVIRT_RENAME_LOG_PREFIX
        )
        self.environment[
            osetupcons.CoreEnv.ACTION
        ] = osetupcons.Const.ACTION_RENAME
        self.environment[
            oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
        ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.RenameEnv.FQDN,
            None
        )
        self.dialog.note(
            text=_(
                '\n'
                'Welcome to the ovirt-engine-rename utility\n'
                '\n'
                'More details about the operation and possible implications\n'
                'of running this utility can be found here:\n'
                'http://www.ovirt.org/documentation/how-to/networking/'
                'changing-engine-hostname/\n'
                '\n'
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
    )
    def _customization(self):
        if self.environment[
            osetupcons.RenameEnv.FQDN
        ] is None:
            osetuphostname.Hostname(
                plugin=self,
            ).getHostname(
                envkey=osetupcons.RenameEnv.FQDN,
                whichhost=_('New'),
                supply_default=False,
                prompttext=_('New fully qualified server name: '),
                validate_syntax=True,
                system=True,
                dns=True,
                local_non_loopback=self.environment[
                    osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION
                ],
                reverse_dns=self.environment[
                    osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION
                ],
            )
        self.environment[
            osetupcons.ConfigEnv.FQDN
        ] = self.environment[
            osetupcons.RenameEnv.FQDN
        ]
        self.environment[
            oenginecons.ConfigEnv.ENGINE_FQDN
        ] = self.environment[
            osetupcons.RenameEnv.FQDN
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Rename completed successfully'
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
