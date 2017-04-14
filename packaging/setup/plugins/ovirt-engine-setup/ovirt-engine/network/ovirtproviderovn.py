#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2017 Red Hat, Inc.
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


"""ovirt-provider-ovn plugin."""

import base64
import gettext
import uuid

from M2Crypto import RSA

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine.constants import FileLocations
from ovirt_engine_setup.engine.constants import OvnEnv
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ovirt-provider-ovn plugin."""

    OVN_PACKAGES = (
        'openvswitch',
        'openvswitch-ovn-common',
        'openvswitch-ovn-host',
        'python-openvswitch',
        'ovirt-provider-ovn',
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _add_provider_to_db(self):
        auth_required = self._user is not None

        password = (
            self._encrypt_password(self._password)
            if self._password
            else None
        )

        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                select InsertProvider(
                    v_id:=%(provider_id)s,
                    v_name:=%(provider_name)s,
                    v_description:=%(provider_description)s,
                    v_url:=%(provider_url)s,
                    v_provider_type:=%(provider_type)s,
                    v_auth_required:=%(auth_required)s,
                    v_auth_username:=%(auth_username)s,
                    v_auth_password:=%(auth_password)s,
                    v_custom_properties:=%(custom_properties)s
                )
            """,
            args=dict(
                provider_id=str(uuid.uuid4()),
                provider_name='ovirt-provider-ovn',
                provider_description='oVirt network provider for OVN',
                provider_url='http://localhost:9696',
                provider_type='EXTERNAL_NETWORK',
                auth_required=auth_required,
                auth_username=self._user,
                auth_password=password,
                custom_properties=None
            ),
        )

        self.logger.info(_('Default OVN provider added to database'))

    def _setup_packages(self):
        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
        ].append(
            {
                'packages': self.OVN_PACKAGES
            },
        )

    def _prompt_for_credentials(self):
        user = self._query_ovn_user()
        password = self._query_ovn_password()
        return user, password

    def _encrypt_password(self, password):
        def _getRSA():
            rc, stdout, stderr = self.execute(
                args=(
                    self.command.get('openssl'),
                    'pkcs12',
                    '-in', (
                        FileLocations.OVIRT_ENGINE_PKI_ENGINE_STORE
                    ),
                    '-passin', 'pass:%s' % self.environment[
                        oenginecons.PKIEnv.STORE_PASS
                    ],
                    '-nocerts',
                    '-nodes',
                ),
                logStreams=False,
            )
            return RSA.load_key_string(
                str('\n'.join(stdout))
            )

        encrypted_password = _getRSA().private_encrypt(
            data=password,
            padding=RSA.pkcs1_padding,
        )
        return base64.b64encode(encrypted_password)

    def _query_install_ovn(self):
        return dialog.queryBoolean(
            dialog=self.dialog,
            name='ovirt-provider-ovn',
            note=_(
                'Install ovirt-provider-ovn(@VALUES@) [@DEFAULT@]?:'
            ),
            prompt=True,
            default=True
        )

    def _query_default_credentials(self, user):
        return dialog.queryBoolean(
            dialog=self.dialog,
            name='ovirt-provider-ovn-default-credentials',
            note=_(
                'Use default credentials (%s) for '
                'ovirt-provider-ovn(@VALUES@) [@DEFAULT@]?: ' % user
            ),
            prompt=True,
            default=True
        )

    def _query_ovn_user(self):
        return self.dialog.queryString(
            name='ovirt-provider-ovn-user',
            note=_(
                'oVirt OVN provider user'
                '[@DEFAULT@]: '
            ),
            prompt=True,
            default='admin',
        )

    def _query_ovn_password(self):
        return self.dialog.queryString(
            name='ovirt-provider-ovn-password',
            note=_(
                'oVirt OVN provider password[empty]: '
            ),
            prompt=True,
            hidden=True,
            default='',
        )

    def _get_provider_credentials(self):

        user = self.environment.get(
            OvnEnv.OVIRT_PROVIDER_OVN_USER
        )
        password = self.environment.get(
            OvnEnv.OVIRT_PROVIDER_OVN_PASSWORD
        )
        if user:
            return user, password

        use_default_credentials = False
        user = self.environment[
            oenginecons.ConfigEnv.ADMIN_USER
        ]
        password = self.environment[
            oenginecons.ConfigEnv.ADMIN_PASSWORD
        ]

        if user is not None and password is not None:
            use_default_credentials = self._query_default_credentials(user)

        if not use_default_credentials:
            user, password = self._prompt_for_credentials()

        self.environment[
            OvnEnv.OVIRT_PROVIDER_OVN_USER
        ] = user
        self.environment[
            OvnEnv.OVIRT_PROVIDER_OVN_PASSWORD
        ] = password

        return user, password

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DISTRO_RPM_PACKAGE_UPDATE_CHECK,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PACKAGES,
        ),
    )
    def _customization(self):
        if self.environment.get(
            OvnEnv.OVIRT_PROVIDER_OVN
        ) is not None:
            self._enabled = False
            return
        self._enabled = self._query_install_ovn()

        self.environment[
            OvnEnv.OVIRT_PROVIDER_OVN
        ] = self._enabled

        if not self._enabled:
            return
        self._setup_packages()

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_MISC,
        ),
        after=(
            oengcommcons.Stages.ADMIN_PASSWORD_SET,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization_credentials(self):
        self._user, self._password = self._get_provider_credentials()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc(self):
        self._add_provider_to_db()

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: (
            self._enabled
        )
    )
    def _restart_services(self):
        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            for service in OvnEnv.ENGINE_MACHINE_OVN_SERVICES:
                self.services.startup(
                    name=service,
                    state=True,
                )
                for state in (False, True):
                    self.services.state(
                        name=service,
                        state=state,
                    )
        else:
            self.dialog.note(
                text=_(
                    'Some services were not restarted automatically \n'
                    'in developer mode and must be restarted manually.\n'
                    'Please execute the following commands to start them:\n'
                    '    {commands}'
                ).format(
                    commands=(
                        '\n    '.join(
                            'systemctl restart ' + name
                            for name in OvnEnv.ENGINE_MACHINE_OVN_SERVICES
                        )
                    )
                ),
            )


# vim: expandtab tabstop=4 shiftwidth=4
