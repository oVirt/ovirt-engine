#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


"""aaa plugin."""


import base64
import gettext
import os
import random
import string

from M2Crypto import RSA

from otopi import constants as otopicons
from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """aaa plugin."""

    @staticmethod
    def _generatePassword():
        return ''.join([
            random.SystemRandom().choice(
                string.ascii_letters +
                string.digits
            ) for i in range(22)
        ])

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            oenginecons.ConfigEnv.ADMIN_PASSWORD
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME,
            'internal-authz'
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ADMIN_USER,
            'admin@internal'
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ADMIN_USER_NAMESPACE,
            '*'
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ADMIN_USER_ID,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ADMIN_PASSWORD,
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
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ] and self.environment[
                oenginecons.ConfigEnv.ADMIN_PASSWORD
            ] is None
        ),
    )
    def _customization(self):
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
            oenginecons.ConfigEnv.ADMIN_PASSWORD
        ] = password

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LOW,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _validation_late(self):
        adminPassword = None
        try:
            adminPassword = vdcoption.VdcOption(
                statement=database.Statement(
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                    environment=self.environment,
                ),
            ).getVdcOption(
                'AdminPassword',
                ownConnection=True,
            )
        except RuntimeError:
            pass

        # we have legacy user
        if adminPassword is not None:
            self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME
            ] = 'internal'
            self.environment[
                oenginecons.ConfigEnv.ADMIN_USER
            ] = 'admin@internal'
            self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_ID
            ] = 'fdfc627c-d875-11e0-90f0-83df133b58cc'
            if self.environment[
                oenginecons.ConfigEnv.ADMIN_PASSWORD
            ] is None:
                if not adminPassword:
                    self.environment[
                        oenginecons.ConfigEnv.ADMIN_PASSWORD
                    ] = ''
                elif os.path.exists(
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE
                ):
                    def _getRSA():
                        rc, stdout, stderr = self.execute(
                            args=(
                                self.command.get('openssl'),
                                'pkcs12',
                                '-in', (
                                    oenginecons.FileLocations.
                                    OVIRT_ENGINE_PKI_ENGINE_STORE
                                ),
                                '-passin', 'pass:%s' % self.environment[
                                    oenginecons.PKIEnv.STORE_PASS
                                ],
                                '-nocerts',
                                '-nodes',
                            ),
                        )
                        return RSA.load_key_string(
                            str('\n'.join(stdout))
                        )

                    self.environment[
                        oenginecons.ConfigEnv.ADMIN_PASSWORD
                    ] = _getRSA().private_decrypt(
                        data=base64.b64decode(adminPassword),
                        padding=RSA.pkcs1_padding,
                    )
                else:
                    self.environment[
                        oenginecons.ConfigEnv.ADMIN_PASSWORD
                    ] = self._generatePassword()
                    self.logger.warning(
                        _(
                            "Cannot decrypt admin's password during upgrade. "
                            "Admin's password was set to a random password: "
                            "{password}. Please replace password as soon as "
                            "possible."
                        ).format(
                            password=self.environment[
                                oenginecons.ConfigEnv.ADMIN_PASSWORD
                            ],
                        )
                    )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.CONFIG_AAA_ADMIN_USER_SETUP,
        priority=plugin.Stages.PRIORITY_POST,   # order hint
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and
            self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _misc(self):
        if self.environment[
            oenginecons.ConfigEnv.ADMIN_USER_ID
        ] is None:
            raise RuntimeError(_('Missing admin user id'))

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select attach_user_to_role(
                    %(admin_user)s,
                    %(authz_name)s,
                    %(namespace)s,
                    %(admin_user_id)s,
                    'SuperUser'
                )
            """,
            args=dict(
                admin_user=self.environment[
                    oenginecons.ConfigEnv.ADMIN_USER
                ].rsplit('@', 1)[0],
                authz_name=self.environment[
                    oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME
                ],
                namespace=self.environment[
                    oenginecons.ConfigEnv.ADMIN_USER_NAMESPACE
                ],
                admin_user_id=self.environment[
                    oenginecons.ConfigEnv.ADMIN_USER_ID
                ],
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
            oenginecons.ConfigEnv.ADMIN_PASSWORD
        ] is not None,
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                "Please use the user '{user}' and password specified in "
                "order to login"
            ).format(
                user=self.environment[
                    oenginecons.ConfigEnv.ADMIN_USER
                ],
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
