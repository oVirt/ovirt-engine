#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Apache ansible-runner plugin."""


import gettext
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
        pki_path = oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_SSH_KEY
        runner_wsgi_file = oenginecons.FileLocations.HTTPD_RUNNER_WSGI_SCRIPT
        port = oenginecons.Defaults.DEFAULT_ANSIBLE_RUNNER_SERVICE_PORT

        self.environment[oengcommcons.ApacheEnv.NEED_RESTART] = True

        # ansible-runner-service configuration
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=oenginecons.FileLocations.ANSIBLE_RUNNER_SERVICE_CONF,
                content=textwrap.dedent('''
                    version: 1
                    playbooks_root_dir: '{playbooks_root_dir}'
                    ssh_private_key: '{pki_path}'
                    port: {port}
                    target_user: root
                    log_path: '/var/log/ovirt-engine'
                    ssh_checks: False
                ''').format(
                    playbooks_root_dir=project_dir,
                    pki_path=pki_path,
                    port=port,
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

        # Apache ansible-runner-service configuration
        user = self.environment[osetupcons.SystemEnv.USER_ENGINE]
        group = self.environment[osetupcons.SystemEnv.GROUP_ENGINE]
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    oenginecons.FileLocations.HTTPD_CONF_ANSIBLE_RUNNER_SERVICE
                ),
                content=textwrap.dedent('''
                Listen {port}
                <VirtualHost _default_:{port}>
                  WSGIDaemonProcess runner user={user} group={group} threads=4
                  WSGIProcessGroup runner
                  WSGIScriptAlias / {runner_wsgi_file}
                </VirtualHost>
                ''').format(
                    runner_wsgi_file=runner_wsgi_file,
                    port=port,
                    user=user,
                    group=group,
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
