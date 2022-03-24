#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ansible-runner runtime configuration plugin."""


import gettext
import os
import stat
import textwrap

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ansible-runner runtime configuration plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            oenginecons.CoreEnv.ENABLE
        ],
    )
    def _misc(self):
        project_env_dir = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT,
            'env'
        )
        project_inventory_dir = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT,
            'inventory'
        )
        project_link_dir = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT,
            'project'
        )
        project_ssh_key = os.path.join(
            project_env_dir,
            'ssh_key'
        )

        rpm_project_dir = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_PROJECT,
            'project'
        )

        dir_permissions = (
            stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR
            | stat.S_IRGRP | stat.S_IWGRP | stat.S_IXGRP
        )


        # create runtime directory structure for ansible-runner
        if not os.path.exists(
            oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT
        ):
            os.makedirs(
                oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT,
                dir_permissions,
            )
        if not os.path.exists(project_env_dir):
            os.makedirs(
                project_env_dir,
                dir_permissions,
            )
        if not os.path.exists(project_inventory_dir):
            os.makedirs(
                project_inventory_dir,
                dir_permissions,
            )

        # create link to roles and playbooks included within RPM
        if not os.path.exists(project_link_dir):
            os.symlink(
                oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_PROJECT,
                project_link_dir
            )

        # create link to engine SSH private key
        if not os.path.exists(project_ssh_key):
            os.symlink(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY,
                project_ssh_key
            )

