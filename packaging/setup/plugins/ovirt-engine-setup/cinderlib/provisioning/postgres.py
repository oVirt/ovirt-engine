#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Local cinderlib Postgres plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.cinderlib import constants as oclcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import postgres

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Local cinderlib Postgres plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False
        self._renamedDBResources = False
        self._provisioning = postgres.Provisioning(
            plugin=self,
            dbenvkeys=oclcons.Const.CINDERLIB_DB_ENV_KEYS,
            defaults=oclcons.Const.DEFAULT_CINDERLIB_DB_ENV_KEYS,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oclcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        after=(
            oclcons.Stages.DB_CL_CONNECTION_SETUP,
        ),
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and
            self.environment[
                oclcons.CinderlibDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _setup(self):
        self._provisioning.detectCommands()

        self._enabled = self._provisioning.supported()

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
            oclcons.Stages.DB_CL_CONNECTION_CUSTOMIZATION,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_DATABASE,
        ),
        condition=lambda self: not self.environment[
            oenginecons.CoreEnv.ENABLE
        ],
        name=oclcons.Stages.POSTGRES_CL_PROVISIONING_ALLOWED,
    )
    def _customization_enable(self):
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_DATABASE,
            oclcons.Stages.DB_CL_CONNECTION_CUSTOMIZATION,
        ),
        after=(
            oclcons.Stages.POSTGRES_CL_PROVISIONING_ALLOWED,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        enabled = self.environment[
            oclcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ]

        if not self.environment[oclcons.CoreEnv.ENABLE]:
            enabled = False

        if enabled is None:
            local = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_PROVISIONING_POSTGRES_LOCATION',
                note=_(
                    'Where is the ovirt cinderlib database located? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('Local'),
                false=_('Remote'),
                default=True,
            )
            if local:
                self.environment[oclcons.CinderlibDBEnv.HOST] = 'localhost'
                self.environment[
                    oclcons.CinderlibDBEnv.PORT
                ] = oclcons.Defaults.DEFAULT_CINDERLIB_DB_PORT

                # TODO:
                # consider creating database and role
                # at engine_@RANDOM@
                self.environment[
                    oclcons.ProvisioningEnv.
                    POSTGRES_PROVISIONING_ENABLED
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_PROVISIONING__CL_POSTGRES_ENABLED',
                    note=_(
                        'Setup can configure the local postgresql server '
                        'automatically for the CinderLib to run. This may '
                        'conflict with existing applications.\n'
                        'Would you like Setup to automatically configure '
                        'postgresql and create CinderLib database, '
                        'or prefer to perform that '
                        'manually? (@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    true=_('Automatic'),
                    false=_('Manual'),
                    default=True,
                )

            else:
                self.environment[
                    oclcons.ProvisioningEnv.
                    POSTGRES_PROVISIONING_ENABLED
                ] = False

        self._enabled = self.environment[
            oclcons.ProvisioningEnv.POSTGRES_PROVISIONING_ENABLED
        ]
        if self._enabled:
            self._provisioning.applyEnvironment()

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        priority=plugin.Stages.PRIORITY_LAST,
        condition=lambda self: (
            self.environment[
                oclcons.CinderlibDBEnv.HOST
            ] == 'localhost'
        ),
    )
    def _customization_firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-postgres',
                'directory': 'ovirt-common'
            },
        ])

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
    )
    def _validation(self):
        self._provisioning.validate()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oclcons.Stages.DB_CL_CREDENTIALS_AVAILABLE_LATE,
            oclcons.Stages.DB_CL_SCHEMA,
        ),
        after=(
            osetupcons.Stages.SYSTEM_SYSCTL_CONFIG_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self._provisioning.provision()

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._provisioning.databaseRenamed,
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'ovirt cinderlib database resources:\n'
                '    Database name:      {database}\n'
                '    Database user name: {user}\n'
            ).format(
                database=self.environment[
                    oclcons.CinderlibDBEnv.DATABASE
                ],
                user=self.environment[
                    oclcons.CinderlibDBEnv.USER
                ],
            )
        )

# vim: expandtab tabstop=4 shiftwidth=4
