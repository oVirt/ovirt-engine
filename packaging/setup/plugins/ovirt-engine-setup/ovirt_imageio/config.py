#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-imageio plugin."""

import textwrap

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.ovirt_imageio import constants as oipcons


@util.export
class Plugin(plugin.PluginBase):
    """ovirt-imageio plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = None

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
                'name': 'ovirt-imageio',
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
            oipcons.ImageIO.DATA_PORT,
            oipcons.ImageIO.CONTROL_PORT,
        ])

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
    )
    def _misc_config(self):
        dev_mode = self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]

        content = textwrap.dedent(oipcons.ImageIO.CONFIG_TEMPLATE).format(
            key_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_KEY,
            cert_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CERT,
            ca_file=oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT,
            data_port=oipcons.ImageIO.DATA_PORT,
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
