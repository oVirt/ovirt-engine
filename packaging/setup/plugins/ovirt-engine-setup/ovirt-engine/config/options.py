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


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common \
    import constants as oengcommcons
from ovirt_engine_setup import dialog
from ovirt_engine_setup.engine import vdcoption


@util.export
class Plugin(plugin.PluginBase):
    """options plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            osetupcons.ConfigEnv.ADMIN_PASSWORD
        )

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
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
        after=(
            oengcommcons.Stages.DB_CONNECTION_STATUS,
            oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _customization(self):
        if not self.environment[
            oenginecons.EngineDBEnv.NEW_DATABASE
        ]:
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

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: not (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]
        ),
    )
    def _validation_enable(self):
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _miscAlways(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
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
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _miscNewDatabase(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
            options=(
                {
                    'name': 'SSLEnabled',
                    'value': 'true',
                },
                {
                    'name': 'EncryptHostCommunication',
                    'value': 'true',
                },
                {
                    'name': 'EncryptHostCommunication',
                    'value': 'true',
                },
                {
                    'name': 'ConfigDir',
                    'value': oenginecons.FileLocations.OVIRT_ENGINE_SYSCONFDIR,
                },
                {
                    'name': 'DataDir',
                    'value': osetupcons.FileLocations.DATADIR,
                },
                {
                    'name': 'WebSocketProxy',
                    'value': '%s:%s' % (
                        self.environment[osetupcons.ConfigEnv.FQDN],
                        osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT,
                    ),
                },
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.CONFIG_DB_ENCRYPTION_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _miscEncrypted(self):
        vdcoption.VdcOption(
            statement=self.environment[
                oenginecons.EngineDBEnv.STATEMENT
            ]
        ).updateVdcOptions(
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
        condition=lambda self: self._enabled,
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                'Please use the user "admin" and password specified in '
                'order to login'
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
