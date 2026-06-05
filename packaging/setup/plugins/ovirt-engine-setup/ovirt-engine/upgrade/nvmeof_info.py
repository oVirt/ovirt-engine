#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Informational plugin about new NVMe-oF feature availability."""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain="ovirt-engine-setup")


@util.export
class Plugin(plugin.PluginBase):
    """Inform the user that NVMe-oF/TCP storage domain support is now available."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        after=(osetupcons.Stages.CORE_CONFIG_INIT,),
    )
    def _init(self):
        self.environment.setdefault(oenginecons.ConfigEnv.NVMEOF_SUPPORTED, True)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,),
        after=(osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,),
        condition=lambda self: (
            self.environment[osetupcons.CoreEnv.ORIGINAL_GENERATED_BY_VERSION]
            is not None
            and self.environment[oenginecons.ConfigEnv.NVMEOF_SUPPORTED]
        ),
    )
    def _customization(self):
        self.dialog.note(
            _(
                "\n"
                "NVMe-oF/TCP Storage Domain Support\n"
                "----------------------------------\n"
                "This version of oVirt Engine introduces native support for\n"
                "NVMe over Fabrics (NVMe-oF) storage domains using TCP transport.\n"
                "This feature requires:\n"
                "  - nvme-cli >= 2.2 installed on each host\n"
                "  - Kernel >= 5.14 with nvme-tcp module loaded\n"
                "  - Host NQN configured on each host (/etc/nvme/hostnqn)\n"
                "\n"
                "Storage domains of type NVMe-oF can be created via the REST API.\n"
                "WebAdmin UI support is limited to read-only display in this release.\n"
                "\n"
                "This feature can be disabled via engine-config:\n"
                "  engine-config -s NVMeOfSupported=false\n"
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[osetupcons.CoreEnv.ORIGINAL_GENERATED_BY_VERSION]
            is not None
        ),
    )
    def _misc(self):
        self.environment[oenginecons.ConfigEnv.NVMEOF_SUPPORTED] = True


# vim: expandtab tabstop=4 shiftwidth=4
