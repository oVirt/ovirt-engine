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
import os
import random
import string
import uuid


from collections import namedtuple

from M2Crypto import RSA

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine.constants import Const
from ovirt_engine_setup.engine.constants import FileLocations
from ovirt_engine_setup.engine.constants import OvnEnv
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


OvnDbConfig = namedtuple(
    'OvnDbConfig',
    [
        'name',
        'port',
        'protocol',
        'command',
        'key_file',
        'cert_file'
    ]
)


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

    CONNECTION_TCP = 'tcp'
    CONNECTION_SSL = 'ssl'

    # TODO: OVN north db will be temporarily configured to
    # connect over TCP, not SSL.
    # This is because of OVN bug: 1446538
    # Once the bug is fixed, the connection can be moved to SSL
    OVN_NORTH_DB_CONFIG = OvnDbConfig(
        'OVN NORTH DB',
        '6641',
        CONNECTION_TCP,
        'ovn-nbctl',
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_NDB_KEY,
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_NDB_CERT,
    )

    OVN_SOUTH_DB_CONFIG = OvnDbConfig(
        'OVN SOUTH DB',
        '6642',
        CONNECTION_SSL,
        'ovn-sbctl',
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_SDB_KEY,
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_SDB_CERT,
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._manual_commands = []
        self._failed_commands = []

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
    )
    def _boot(self):
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].append(
            OvnEnv.OVIRT_PROVIDER_OVN_SECRET
        )

    def _add_provider_to_db(self):
        auth_required = self._user is not None
        fqdn = self.environment[osetupcons.ConfigEnv.FQDN]

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
                    v_custom_properties:=%(custom_properties)s,
                    v_auth_url:=%(auth_url)s
                )
            """,
            args=dict(
                provider_id=str(uuid.uuid4()),
                provider_name='ovirt-provider-ovn',
                provider_description='oVirt network provider for OVN',
                provider_url='https://%s:9696' % fqdn,
                provider_type='EXTERNAL_NETWORK',
                auth_required=auth_required,
                auth_username=self._user,
                auth_password=self._password,
                custom_properties=None,
                auth_url='https://%s:35357/v2.0/' % fqdn
            ),
        )

        self.logger.info(_('Default OVN provider added to database'))

    def _generate_client_secret(self):

        def generatePassword():
            rand = random.SystemRandom()
            return ''.join([
                rand.choice(string.ascii_letters + string.digits)
                for i in range(32)
            ])
        self.environment.setdefault(
            OvnEnv.OVIRT_PROVIDER_OVN_SECRET,
            generatePassword()
        )

    def _add_client_secret_to_db(self):
        rc, stdout, stderr = self.execute(
            (
                oenginecons.FileLocations.OVIRT_ENGINE_CRYPTO_TOOL,
                'pbe-encode',
                '--password=env:pass',
            ),
            envAppend={
                'OVIRT_ENGINE_JAVA_HOME_FORCE': '1',
                'OVIRT_ENGINE_JAVA_HOME': self.environment[
                    oengcommcons.ConfigEnv.JAVA_HOME
                ],
                'OVIRT_JBOSS_HOME': self.environment[
                    oengcommcons.ConfigEnv.JBOSS_HOME
                ],
                'pass': self.environment[
                    OvnEnv.OVIRT_PROVIDER_OVN_SECRET
                ]
            },
            logStreams=False,
        )

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select sso_oauth_register_client(
                    %(client_id)s,
                    %(client_secret)s,
                    %(scope)s,
                    %(certificate)s,
                    %(callback_prefix)s,
                    %(description)s,
                    %(email)s,
                    %(trusted)s,
                    %(notification_callback)s,
                    %(notification_callback_host_protocol)s,
                    %(notification_callback_host_verification)s,
                    %(notification_callback_chain_validation)s
                )
            """,
            args=dict(
                client_id=Const.OVIRT_PROVIDER_OVN_CLIENT_ID_VALUE,
                client_secret=stdout[0],
                scope=' '.join(
                    (
                        'ovirt-app-api',
                        'ovirt-ext=token-info:validate',
                        'ovirt-ext=token-info:public-authz-search',
                    )
                ),
                certificate=(
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_PKI_ENGINE_CERT
                ),
                callback_prefix='',
                description='ovirt-provider-ovn',
                email='',
                trusted=True,
                notification_callback='',
                notification_callback_host_protocol='TLS',
                notification_callback_host_verification=False,
                notification_callback_chain_validation=True,
            ),
        )

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

        encrypted_password = _getRSA().public_encrypt(
            data=password,
            padding=RSA.pkcs1_padding,
        )
        return base64.b64encode(encrypted_password)

    def _query_install_ovn(self):
        return dialog.queryBoolean(
            dialog=self.dialog,
            name='ovirt-provider-ovn',
            note=_(
                'Install ovirt-provider-ovn(@VALUES@) [@DEFAULT@]?: '
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

    def _generate_pki(self):
        self.environment[oenginecons.PKIEnv.ENTITIES].extend(
            (
                {
                    'name':
                        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_NDB,
                    'extract': True,
                    'user': oengcommcons.SystemEnv.USER_ROOT,
                    'keepKey': False,
                },
                {
                    'name':
                        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_SDB,
                    'extract': True,
                    'user': oengcommcons.SystemEnv.USER_ROOT,
                    'keepKey': False,
                },
                {
                    'name':
                        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_HTTPS,
                    'extract': True,
                    'user': oengcommcons.SystemEnv.USER_ROOT,
                    'keepKey': False,
                }
            )
        )

    def _execute_command(self, command, error_message):
        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            rc, stdout, stderr = self.execute(
                command,
                raiseOnError=False
            )
            if rc != 0:
                self.logger.error(error_message)
                self._failed_commands.append(command)
        else:
            self._manual_commands.append(command)

    def _configure_ovndb_connection(self, ovn_db_config):
        if (ovn_db_config.protocol == self.CONNECTION_SSL):
            self._execute_command(
                (
                    ovn_db_config.command,
                    'set-ssl',
                    ovn_db_config.key_file,
                    ovn_db_config.cert_file,
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                ),
                _(
                    'Failed to configure {name} with SSL'
                ).format(
                    name=ovn_db_config.name
                )
            )

        self._execute_command(
            (
                ovn_db_config.command,
                'set-connection',
                'p%s:%s' % (ovn_db_config.protocol, ovn_db_config.port),
            ),
            _(
                'Failed to open {name} SSL connection'
            ).format(
                name=ovn_db_config.name
            )
        )

    def _configure_ovndb_north_connection(self):
        self._configure_ovndb_connection(self.OVN_NORTH_DB_CONFIG)

    def _configure_ovndb_south_connection(self):
        self._configure_ovndb_connection(self.OVN_SOUTH_DB_CONFIG)

    def _display_config_file_commands(self, modified_parameters):
        if modified_parameters:
            self._manual_commands.append(
                (
                    'sed',
                    '-i',
                    '"\\'
                )
            )

            for key, content in modified_parameters.items():

                self._manual_commands.append(
                    (
                        '    ',
                        's/^{key}=.*/{key}={value}/; \\'.format(
                            key=key,
                            value=content.replace('/', '\/')
                        ),
                    )
                )
            self._manual_commands.append(
                (
                    '" ',
                    oenginecons.OvnFileLocations.OVIRT_PROVIDER_CONFIG_FILE,
                )
            )

    def _update_provider_config_with_pki(self, modified_parameters):
        content = []

        if os.path.exists(
            oenginecons.OvnFileLocations.OVIRT_PROVIDER_CONFIG_FILE
        ):
            with open(
                oenginecons.OvnFileLocations.OVIRT_PROVIDER_CONFIG_FILE,
                'r'
            ) as f:
                content = f.read().splitlines()
        modified_content = osetuputil.editConfigContent(
            content=content,
            params=modified_parameters,
            param_re='[\w-]+',
        )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=oenginecons.OvnFileLocations.OVIRT_PROVIDER_CONFIG_FILE,
                content=modified_content,
                visibleButUnsafe=True,
            )
        )

    def _configure_ovirt_provider_ovn(self):
        engine_port = self.environment[
            oengcommcons.ConfigEnv.HTTPS_PORT
        ] if self.environment[
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT
        ] else self.environment[
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
        ]

        modified_parameters = {
            'ssl-cert-file':
                oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_HTTPS_CERT,
            'ssl-key-file':
                oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_HTTPS_KEY,
            'ssl-cacert-file':
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            'https-enabled':
                'true',
            'ovn-remote':
                '%s:127.0.0.1:%s' % (
                    self.OVN_NORTH_DB_CONFIG.protocol,
                    self.OVN_NORTH_DB_CONFIG.port,
                ),
            'sso-client-id':
                Const.OVIRT_PROVIDER_OVN_CLIENT_ID_VALUE,
            'sso-client-secret':
                self.environment[
                    OvnEnv.OVIRT_PROVIDER_OVN_SECRET
                ],
            'host':
                'https://%s:%s' % (
                    self.environment[osetupcons.ConfigEnv.FQDN],
                    engine_port
                ),
            'ca-file':
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
        }
        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self._update_provider_config_with_pki(modified_parameters)
        else:
            self._display_config_file_commands(modified_parameters)

    def _upate_external_providers_keystore(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_SSO
        ])
        truststore = config.get(
            'ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE'
        )
        truststore_password = config.get(
            'ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE_PASSWORD'
        )
        self._execute_command(
            (
                'keytool',
                '-import',
                '-alias',
                OvnEnv.PROVIDER_NAME,
                '-keystore',
                truststore,
                '-file',
                oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                '-noprompt',
                '-storepass',
                truststore_password,
            ),
            _(
                'Failed to import provider certificate into '
                'the external provider keystore.)'
            )
        )

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

    def _print_commands(self, message, commands):
        self.dialog.note(
            text='{message}\n   {commands}'.format(
                message=message,
                commands=(
                    '\n    '.join(
                        command
                        for command in commands
                    )
                )
            ),
        )

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
        before=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc_pki(self):
        self._generate_pki()

    def _restart_service(self, service):
        self.services.startup(
            name=service,
            state=True,
        )
        for state in (False, True):
            self.services.state(
                name=service,
                state=state,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.OVN_SERVICES_RESTART,
        condition=lambda self: (
            self._enabled and
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        )
    )
    def _restart_ovn_services(self):
        for service in OvnEnv.ENGINE_MACHINE_OVN_SERVICES:
            self._restart_service(service)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oenginecons.Stages.OVN_PROVIDER_SERVICE_RESTART
        ),
        after=(
            oenginecons.Stages.CA_AVAILABLE,
            oenginecons.Stages.OVN_SERVICES_RESTART,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc_configure_ovn_pki(self):
        self._configure_ovndb_north_connection()
        self._configure_ovndb_south_connection()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oenginecons.Stages.OVN_PROVIDER_SERVICE_RESTART
        ),
        after=(
            oenginecons.Stages.CA_AVAILABLE,
            oenginecons.Stages.OVN_SERVICES_RESTART,
        ),
        condition=lambda self:
            self._enabled
    )
    def _misc_configure_provider(self):
        self._generate_client_secret()
        self._configure_ovirt_provider_ovn()
        self._upate_external_providers_keystore()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.OVN_PROVIDER_SERVICE_RESTART,
        condition=lambda self: (
            self._enabled and
            not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        )
    )
    def _restart_provider_service(self):
        self._restart_service(OvnEnv.OVIRT_PROVIDER_OVN_SERVICE)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
            oenginecons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc_db_entries(self):
        self._add_provider_to_db()
        self._add_client_secret_to_db()

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: (
            self._enabled and
            self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        )
    )
    def _print_restart_services_commands(self):
        self._print_commands(
            _(
                'Some services were not restarted automatically \n'
                'in developer mode and must be restarted manually.\n'
                'Please execute the following commands to start them:'
            ),
            [
                'systemctl restart ' + name
                for name in OvnEnv.ENGINE_MACHINE_OVN_SERVICES
            ]
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: (
            self._enabled and (
                self._manual_commands or
                self._failed_commands
            )
        ),
    )
    def _print_manual_commands(self):
        if self._manual_commands:
            self._print_commands(
                _(
                    'The following commands can not be executed in\n'
                    'developer mode. Please execute them as root:'
                ),
                [
                    ' '.join(command)
                    for command
                    in self._manual_commands
                ]
            )
        if self._failed_commands:
            self._print_commands(
                _(
                    'The following commands failed to execute.\n'
                    'Please execute them manually as root:'
                ),
                [
                    ' '.join(command)
                    for command
                    in self._failed_commands
                    ]
            )


# vim: expandtab tabstop=4 shiftwidth=4
