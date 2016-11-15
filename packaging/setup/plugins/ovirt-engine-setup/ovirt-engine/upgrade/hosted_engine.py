#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2016 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


""" Hosted-Engine handling plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ Hosted-Engine handling plugin."""

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.EngineDBEnv.UPGRADE_WITH_HE_EL6_HOSTS,
            None
        )

    def _have_el6_hosted_engine_host(self):
        el6hosts = []
        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        try:
            HostedEngineVmName = vdcoption.VdcOption(
                statement=dbstatement,
            ).getVdcOption(
                'HostedEngineVmName',
                ownConnection=True,
            )
        except RuntimeError:
            HostedEngineVmName = 'HostedEngine'
        vm_rows = dbstatement.execute(
            statement="""
                SELECT vds_group_id
                FROM vm_static
                WHERE vm_name='{vmname}'
            """.format(vmname=HostedEngineVmName),
            ownConnection=True,
            transaction=False,
        )
        if not vm_rows:
            # We are not a self-hosted-engine, or engine vm
            # was renamed - probably already upgraded to 3.6.
            return el6hosts

        hosts = dbstatement.execute(
            statement="""
                SELECT vds_name,host_os
                FROM vds_static s,vds_dynamic d
                WHERE
                    s.vds_id=d.vds_id
                    AND vds_group_id=%(vds_group_id)s
            """,
            args=dict(
                vds_group_id=vm_rows[0]['vds_group_id'],
            ),
            ownConnection=True,
            transaction=False,
        )
        if not hosts:
            # Could not find hosts. Something is probably wrong, but do
            # not fail on this.
            return el6hosts

        # Check el6. Logic is partially copied from:
        # backend/manager/modules/vdsbroker/src/main/java/org/ovirt/engine/
        # core/vdsbroker/monitoring/VirtMonitoringStrategy.java
        for host in hosts:
            host_os_info = host['host_os'].split('-')
            if len(host_os_info) != 3:
                break
            os_name = host_os_info[0].strip()
            os_release = host_os_info[2].strip()
            # both CentOS and RHEL have os_name 'RHEL'
            if os_name in ('RHEL', 'oVirt Node', 'RHEV Hypervisor'):
                if 'el6' in os_release:
                    self.logger.debug(
                        'Found el6 host: host name %s os name %s release %s',
                        host['vds_name'],
                        os_name,
                        os_release,
                    )
                    el6hosts.append(host['vds_name'])
        return el6hosts

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            oenginecons.CoreEnv.ENABLE
        ] and not self.environment[
            oenginecons.EngineDBEnv.NEW_DATABASE
        ] and not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
    )
    def _validate_hosted_engine(self):
        # Prevent upgrade to 3.6 if we are running as a self-hosted-engine
        # and we have el6 hosts.
        el6_hosts = self._have_el6_hosted_engine_host()

        if self.environment[
            oenginecons.EngineDBEnv.UPGRADE_WITH_HE_EL6_HOSTS
        ] is None and el6_hosts:
            self.dialog.note(
                text=_(
                    'It seems like this engine is self-hosted, '
                    'and the operating system of some of its hosts is '
                    'el6 or a variant. Please upgrade {el6_hosts} to el7 '
                    'before upgrading the engine to 3.6.\n'
                    'Please check the log file for more details.\n'
                ).format(
                    el6_hosts=el6_hosts,
                ),
            )

        if not self.environment[
            oenginecons.EngineDBEnv.UPGRADE_WITH_HE_EL6_HOSTS
        ] and el6_hosts:
            raise RuntimeError(_('self hosted engine uses el6 hosts'))


# vim: expandtab tabstop=4 shiftwidth=4
