#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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


"""options plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """options plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.ADMIN_PASSWORD,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
        after=(
            osetupcons.Stages.DB_CONNECTION_STATUS,
            osetupcons.Stages.DIALOG_TITLES_S_ENGINE,
        ),
    )
    def _customization(self):
        self._enabled = self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ]

        if not self._enabled:
            self.dialog.note(
                text=_(
                    'Skipping storing options as database already '
                    'prepared'
                ),
            )
        else:
            if self.environment[osetupcons.ConfigEnv.ADMIN_PASSWORD] is None:
                valid = False
                password = None
                while not valid:
                    password = self.dialog.queryString(
                        name='OVESETUP_CONFIG_ADMIN_SETUP',
                        note=_('Engine admin password: '),
                        prompt=True,
                        hidden=True,
                    )
                    password2 = self.dialog.queryString(
                        name='OVESETUP_CONFIG_ADMIN_SETUP',
                        note=_('Confirm engine admin password: '),
                        prompt=True,
                        hidden=True,
                    )

                    if password != password2:
                        self.logger.warning(_('Passwords do not match'))
                    else:
                        try:
                            import cracklib
                            cracklib.FascistCheck(password)
                            valid = True
                        except ImportError:
                            # do not force this optional feature
                            self.logger.debug(
                                'cannot import cracklib',
                                exc_info=True,
                            )
                            valid = True
                        except ValueError as e:
                            self.logger.warning(
                                _('Password is weak: {error}').format(
                                    error=e,
                                )
                            )
                            valid = dialog.queryBoolean(
                                dialog=self.dialog,
                                name='OVESETUP_CONFIG_WEAK_ENGINE_PASSWORD',
                                note=_(
                                    'Use weak password? '
                                    '(@VALUES@) [@DEFAULT@]: '
                                ),
                                prompt=True,
                                default=False,
                            )

                self.environment[
                    osetupcons.ConfigEnv.ADMIN_PASSWORD
                ] = password

            self.environment[otopicons.CoreEnv.LOG_FILTER].append(
                self.environment[osetupcons.ConfigEnv.ADMIN_PASSWORD]
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            osetupcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
    )
    def _miscAlways(self):
        self.environment[osetupcons.DBEnv.STATEMENT].updateVdcOptions(
            options=(
                {
                    'name': 'ProductRPMVersion',
                    'value': osetupcons.Const.DISPLAY_VERSION,
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            osetupcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ]
    )
    def _miscNewDatabase(self):
        self.environment[osetupcons.DBEnv.STATEMENT].updateVdcOptions(
            options=(
                {
                    'name': 'SSLEnabled',
                    'value': 'true',
                },
                {
                    'name': 'UseSecureConnectionWithServers',
                    'value': 'true',
                },
                {
                    'name': 'UseSecureConnectionWithServers',
                    'value': 'true',
                },
                {
                    'name': 'ConfigDir',
                    'value': osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                },
                {
                    'name': 'DataDir',
                    'value': osetupcons.FileLocations.DATADIR,
                },
                {
                    'name': 'SysPrepXPPath',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.xp',
                    ),
                },
                {
                    'name': 'SysPrep2K3Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.2k3',
                    ),
                },
                {
                    'name': 'SysPrep2K8Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.2k8x86',
                    ),
                },
                {
                    'name': 'SysPrep2K8x64Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.2k8',
                    ),
                },
                {
                    'name': 'SysPrep2K8R2Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gs4ysprep/sysprep.2k8',
                    ),
                },
                {
                    'name': 'SysPrepWindows7Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.w7',
                    ),
                },
                {
                    'name': 'SysPrepWindows7x64Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.w7x64',
                    ),
                },
                {
                    'name': 'SysPrepWindows8Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.w8',
                    ),
                },
                {
                    'name': 'SysPrepWindows8x64Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.w8x64',
                    ),
                },
                {
                    'name': 'SysPrepWindows2012x64Path',
                    'value': os.path.join(
                        osetupcons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                        'gsysprep/sysprep.2k12x64',
                    ),
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            osetupcons.Stages.CONFIG_DB_ENCRYPTION_AVAILABLE,
        ),
        condition=lambda self: self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ]
    )
    def _miscEncrypted(self):
        self.environment[osetupcons.DBEnv.STATEMENT].updateVdcOptions(
            options=(
                {
                    'name': 'LocalAdminPassword',
                    'value': self.environment[
                        osetupcons.ConfigEnv.ADMIN_PASSWORD
                    ],
                    'encrypt': True,
                },
                {
                    'name': 'AdminPassword',
                    'value': self.environment[
                        osetupcons.ConfigEnv.ADMIN_PASSWORD
                    ],
                    'encrypt': True,
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ]
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Please use the user "admin" and password specified in '
                'order to login into oVirt Engine'
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
