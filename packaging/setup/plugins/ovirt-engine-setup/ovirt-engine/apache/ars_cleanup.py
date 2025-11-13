#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Apache ansible-runner-service cleanup plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.transactions import RemoveFileTransaction


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache ansible-runner-service cleanup plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            oenginecons.CoreEnv.ENABLE
        ] and not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and os.path.exists(
            oenginecons.FileLocations.HTTPD_CONF_ANSIBLE_RUNNER_SERVICE
        ),
    )
    def _misc(self):
        if not self._can_remove(
            oenginecons.FileLocations.HTTPD_CONF_ANSIBLE_RUNNER_SERVICE
        ):
            return

        self.environment[oengcommcons.ApacheEnv.NEED_RESTART] = True

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            RemoveFileTransaction(
                oenginecons.FileLocations.HTTPD_CONF_ANSIBLE_RUNNER_SERVICE
            )
        )

    def _can_remove(self, installed_file):
        """
        Return True if installed_file can be removed safely. Return False if
        the file was not installed by previous setup, or was changed.
        """
        uninstall_info = self.environment[
            osetupcons.CoreEnv.UNINSTALL_FILES_INFO
        ].get(installed_file)

        # Did we install this file?
        if not uninstall_info:
            return False

        # Did it change since we installed it?
        if uninstall_info.get("changed"):
            self.logger.warning(
                _(
                    "Cannot remove {}, file was changed"
                ).format(installed_file)
            )
            return False

        return True


# vim: expandtab tabstop=4 shiftwidth=4
