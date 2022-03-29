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

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons


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
        project_playbooks_dir = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT,
            'project'
        )
        project_roles_link = os.path.join(
            project_playbooks_dir,
            'roles'
        )
        ansible_cfg_link = os.path.join(
            project_playbooks_dir,
            'ansible.cfg'
        )
        project_ssh_key = os.path.join(
            project_env_dir,
            'ssh_key'
        )
        rpm_roles_dir = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_PROJECT,
            'project/roles'
        )
        rpm_ansible_cfg = os.path.join(
            oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_PROJECT,
            'project/ansible.cfg'
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
        if not os.path.exists(project_playbooks_dir):
            os.makedirs(
                project_playbooks_dir,
                dir_permissions,
            )

        # change ownership to ovirt:ovirt for RPM installations
        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            usr_engine = osetuputil.getUid(
                self.environment[osetupcons.SystemEnv.USER_ENGINE],
            )
            grp_engine = osetuputil.getGid(
                self.environment[osetupcons.SystemEnv.GROUP_ENGINE],
            )
            for dirpath, dirnames, filenames in os.walk(
                oenginecons.FileLocations.ANSIBLE_RUNNER_PROJECT
            ):
                os.chown(dirpath, usr_engine, grp_engine)
                for filename in filenames:
                    os.chown(
                        os.path.join(dirpath, filename),
                        usr_engine,
                        grp_engine,
                    )

        # create a link to ansible.cfg included in RPM
        if not os.path.exists(ansible_cfg_link):
            os.symlink(
                rpm_ansible_cfg,
                ansible_cfg_link
            )

        # create a link to roles directory included in RPM
        if not os.path.exists(project_roles_link):
            os.symlink(
                rpm_roles_dir,
                project_roles_link
            )

        # create link to engine SSH private key
        if not os.path.exists(project_ssh_key):
            os.symlink(
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY,
                project_ssh_key
            )


# vim: expandtab tabstop=4 shiftwidth=4
