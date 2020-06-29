#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""OVN plugin."""


import gettext
import os

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
    """OVN plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._config = (
            oenginecons.OvnFileLocations.
            OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE
        )
        self._service = oenginecons.OvnEnv.OVIRT_PROVIDER_OVN_SERVICE
        self._service_was_up = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        condition=lambda self: os.path.exists(self._config),
    )
    def _init(self):
        self.environment[
            osetupcons.RenameEnv.PKI_ENTITIES
        ].append(
            {
                'name': 'ovirt-provider-ovn',
                'display_name': 'ovirt-provider-ovn',
                'ca_cert': None,
                'extract_key': True,
                'extra_action': None,
            }
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: os.path.exists(self._config),
    )
    def _setup(self):
        self.environment[
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
        ].append(self._config)

    @plugin.event(
        stage=plugin.Stages.STAGE_TRANSACTION_BEGIN,
    )
    def _transaction_begin_ovn(self):
        if self.services.status(self._service):
            self.logger.info("Stopping {}".format(self._service))
            self._service_was_up = True
            self.services.state(
                name=self._service,
                state=False,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: os.path.exists(self._config),
    )
    def _misc(self):
        uninstall_files = []

        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ovirt-provider-ovn',
            description='ovirt-provider-ovn configuration files',
            optional=True,
        ).addFiles(
            group='ovirt-provider-ovn',
            fileList=uninstall_files,
        )
        with open(self._config, 'r') as f:
            content = []
            for line in f:
                line = line.rstrip('\n')
                if line.startswith('ovirt-host='):
                    line = (
                        'ovirt-host=https://{fqdn}:{port}'
                    ).format(
                        fqdn=self.environment[osetupcons.RenameEnv.FQDN],
                        port=self.environment[
                            oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
                        ],
                    )
                elif line.startswith('provider-host='):
                    line = (
                        'provider-host={fqdn}'
                    ).format(
                        fqdn=self.environment[osetupcons.RenameEnv.FQDN],
                    )
                content.append(line)

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self._config,
                content=content,
                modifiedList=uninstall_files,
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self.environment.get(
            oenginecons.OvnEnv.OVIRT_PROVIDER_ID
        ),
    )
    def _rename_ovn_provider(self):
        self.logger.info(_('Update OVN provider'))
        fqdn = self.environment[osetupcons.RenameEnv.FQDN]
        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                UPDATE providers
                SET
                    url=%(url)s,
                    auth_url=%(auth_url)s
                WHERE
                    id=%(id)s
            """,
            args=dict(
                url='https://{}:9696'.format(fqdn),
                auth_url='https://{}:35357/v2.0/'.format(fqdn),
                id=self.environment[oenginecons.OvnEnv.OVIRT_PROVIDER_ID],
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: self._service_was_up,
    )
    def _closeup_ovn(self):
        self.logger.info("Starting {}".format(self._service))
        self.services.state(
            name=self._service,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
