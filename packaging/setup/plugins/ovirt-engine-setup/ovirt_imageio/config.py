#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-imageio plugin."""

import gettext
import json
import os
import textwrap

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.ovirt_imageio import constants as oipcons
from ovirt_engine_setup.transactions import RemoveFileTransaction

OLD_FIREWALLD_SERVICE_PATH = os.path.join(
    osetupcons.FileLocations.FIREWALLD_SERVICES_DIR,
    "ovirt-imageio.xml"
)


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ovirt-imageio plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = None

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect("ovirt-imageio")

    def _write_config(self, file_name, content):
        # Write content to file_name, rolling back if there was a problem.
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=file_name,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ),
        name=oipcons.ImageIO.CONFIG_STAGE,
    )
    def _customization_enable(self):
        self._enabled = self.environment[oenginecons.CoreEnv.ENABLE]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            oipcons.ImageIO.CONFIG_STAGE,
        ),
    )
    def _customization_firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-imageio-proxy',
                'directory': 'ovirt-imageio'
            },
        ])

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and self._enabled
        ),
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            oipcons.ImageIO.CONFIG_STAGE,
        ),
    )
    def _customization_resereve_ports(self):
        # Reserve imageio ports to avoid failures during starup when port was
        # assinged to some other process.
        # See https://bugzilla.redhat.com/1517764
        self.environment[osetupcons.SystemEnv.RESERVED_PORTS].update([
            oipcons.ImageIO.PROXY_PORT,
            oipcons.ImageIO.CONTROL_PORT,
        ])

    @plugin.event(
        # otopi firewalld plugin disables services in STAGE_EARLY_MISC; we must
        # run before that.
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and self._enabled and
            os.path.exists(OLD_FIREWALLD_SERVICE_PATH)
        ),
    )
    def _remove_old_firewalld_service(self):
        """
        In older versions we installed ovirt-imageio.xml exposing proxy port
        (54323) in /etc/firewalld/services. This service overrode the builtin
        ovirt-imageio.xml in /usr/lib/firewalld/services, exposing the daemon
        port (54322).

        Remove the old service file to restore the daemon port, needed when
        using legacy and unsupported all-in-one setup.

        If the daemon port is unused, also disable ovirt-imageio firewalld
        service.
        """
        if not self._can_remove(OLD_FIREWALLD_SERVICE_PATH):
            return

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            RemoveFileTransaction(OLD_FIREWALLD_SERVICE_PATH)
        )

        if self._daemon_port_is_used():
            return

        self.environment[
            otopicons.NetEnv.FIREWALLD_DISABLE_SERVICES
        ].append("ovirt-imageio")

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
            self.logger.warn(
                _(
                    "Cannot remove {}, file was changed"
                ).format(installed_file)
            )
            return False

        return True

    def _daemon_port_is_used(self):
        """
        Return True if daemon port (54322) is used. May be true when upgrading
        an all-in-one setup.
        """
        rc, stdout, stderr = self.execute(
            (self.command.get("ovirt-imageio"), "--show-config"),
            raiseOnError=False,
        )
        if rc != 0:
            self.logger.warn(
                _(
                    "Cannot read ovirt-imageio configuration: {}"
                ).format("\n".join(stderr))
            )
            # We don't have a good way to check, so assume that the port is
            # used.
            return True

        config = json.loads("\n".join(stdout))

        return config["remote"]["port"] == oipcons.ImageIO.DAEMON_PORT

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc_config(self):
        dev_mode = self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]

        content = textwrap.dedent(oipcons.ImageIO.CONFIG_TEMPLATE).format(
            # Engine PKI, users can change this PKI.
            key_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
            cert_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
            ca_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT,
            # Host PKI setup is based on this CA, users must not change it.
            engine_ca_file=(
                oenginecons.FileLocations.
                OVIRT_ENGINE_PKI_ENGINE_CA_CERT
            ),
            remote_port=oipcons.ImageIO.PROXY_PORT,
            control_port=oipcons.ImageIO.CONTROL_PORT,
            logger_handler="stderr" if dev_mode else "logfile",
            logger_level="DEBUG" if dev_mode else "INFO",
        )

        self._write_config(oipcons.ImageIO.ENGINE_CONFIG, content)

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and self._enabled
        ),
    )
    def _closeup_restart_service(self):
        for state in (False, True):
            self.services.state(
                name=oipcons.ImageIO.SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=oipcons.ImageIO.SERVICE_NAME,
            state=True,
        )
