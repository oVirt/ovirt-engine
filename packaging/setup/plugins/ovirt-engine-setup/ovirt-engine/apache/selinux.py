#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Apache ansible-runner plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Apache ansible-runner plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            oenginecons.CoreEnv.ENABLE
        ] and not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _misc(self):
        project_dir = oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_PROJECT
        port = oenginecons.Defaults.DEFAULT_ANSIBLE_RUNNER_SERVICE_PORT

        self.environment[osetupcons.SystemEnv.SELINUX_CONTEXTS].append({
            'type': 'httpd_user_rw_content_t',
            'pattern': '{}{}'.format(project_dir.rstrip('/'), '(/.*)?'),
        })
        self.environment[osetupcons.SystemEnv.SELINUX_PORTS].append({
            'type': 'http_port_t',
            'protocol': 'tcp',
            'port': port,
        })
        self.environment[osetupcons.SystemEnv.SELINUX_RESTORE_PATHS].append(
            project_dir
        )

# vim: expandtab tabstop=4 shiftwidth=4
