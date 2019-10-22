#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Misc plugin."""


import gettext
import logging
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.provisiondb import constants as oprovisioncons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self.environment.setdefault(
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES,
            []
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
    )
    def _preinit(self):
        self.environment.setdefault(
            otopicons.CoreEnv.CONFIG_FILE_NAME,
            self.resolveFile(
                os.environ.get(
                    otopicons.SystemEnvironment.CONFIG,
                    self.resolveFile(
                        osetupcons.FileLocations.OVIRT_OVIRT_SETUP_CONFIG_FILE
                    )
                )
            )
        )
        logging.getLogger('ovirt').setLevel(logging.DEBUG)
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_DIR,
            osetupcons.FileLocations.OVIRT_SETUP_LOGDIR
        )
        self.environment.setdefault(
            otopicons.CoreEnv.LOG_FILE_NAME_PREFIX,
            osetupcons.FileLocations.OVIRT_OVIRT_PROVISIONDB_LOG_PREFIX
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        before=(
            osetupcons.Stages.SECRETS_FILTERED_FROM_SETUP_ATTRS_MODULES,
        )
    )
    def _boot(self):
        self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ].extend((
            osetupcons,
            oprovisioncons,
        ))

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[
            osetupcons.CoreEnv.ACTION
        ] = osetupcons.Const.ACTION_PROVISIONDB
        self.environment.setdefault(
            osetupcons.CoreEnv.DEVELOPER_MODE,
            None
        )
        self.logger.debug(
            'Package: %s-%s (%s)',
            osetupcons.Const.PACKAGE_NAME,
            osetupcons.Const.PACKAGE_VERSION,
            osetupcons.Const.DISPLAY_VERSION,
        )
        self.environment.setdefault(
            oengcommcons.SystemEnv.USER_POSTGRES,
            oengcommcons.Defaults.DEFAULT_SYSTEM_USER_POSTGRES
        )
        self.environment.setdefault(
            osetupcons.CoreEnv.UNINSTALL_UNREMOVABLE_FILES,
            []
        )
        self.environment[osetupcons.ConfigEnv.FQDN_IS_NEEDED] = False
        self.environment[osetupcons.CoreEnv.GENERATE_POSTINSTALL] = False
        self.environment[
            osetupcons.CoreEnv.GENERATE_STANDARD_ANSWERFILE
        ] = False
        self.environment[oengcommcons.ConfigEnv.NEED_COMMON_TITLES] = False
        self.environment[
            oengcommcons.ConfigEnv.ENGINE_SERVICE_STOP_NEEDED
        ] = False


# vim: expandtab tabstop=4 shiftwidth=4
