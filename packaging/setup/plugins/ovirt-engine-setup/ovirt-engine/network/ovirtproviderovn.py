#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-provider-ovn plugin."""

import base64
import errno
import gettext
import grp
import os
import random
import stat
import string
import uuid


from collections import namedtuple

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import configfile

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine.constants import Const
from ovirt_engine_setup.engine.constants import Defaults
from ovirt_engine_setup.engine.constants import FileLocations
from ovirt_engine_setup.engine.constants import OvnEnv
from ovirt_engine_setup.engine.constants import OvnFileLocations
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database

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
        'cert_file',
        'ssl_protocol',
        'ciphers'
    ]
)


@util.export
class Plugin(plugin.PluginBase):
    """ovirt-provider-ovn plugin."""

    CONNECTION_TCP = 'tcp'
    CONNECTION_SSL = 'ssl'
    SSL_PROTOCOLS = 'TLSv1.2'
    ALLOWED_CIPHERS = 'kRSA:-aDSS:-3DES:!DES:!RC4:!RC2:!IDEA:-SEED:!eNULL:' \
                      '!aNULL:!MD5:-SHA384:-CAMELLIA:-ARIA:-AESCCM8'

    PROVIDER_NAME = 'ovirt-provider-ovn'

    OVN_NORTH_DB_CONFIG = OvnDbConfig(
        'OVN NORTH DB',
        '6641',
        CONNECTION_SSL,
        'ovn-nbctl',
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_NDB_KEY,
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_NDB_CERT,
        None,
        None
    )

    OVN_SOUTH_DB_CONFIG = OvnDbConfig(
        'OVN SOUTH DB',
        '6642',
        CONNECTION_SSL,
        'ovn-sbctl',
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_SDB_KEY,
        oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_SDB_CERT,
        SSL_PROTOCOLS,
        ALLOWED_CIPHERS,
    )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._manual_commands = []
        self._failed_commands = []
        self._manual_tasks = []

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            OvnEnv.OVIRT_PROVIDER_OVN,
            None
        )
        self.environment.setdefault(
            OvnEnv.FIREWALLD_SERVICES_DIR,
            OvnFileLocations.DEFAULT_FIREWALLD_SERVICES_DIR
        )
        self.environment.setdefault(
            OvnEnv.OVN_FIREWALLD_SERVICES,
            Defaults.DEFAULT_OVN_FIREWALLD_SERVICES
        )

    def _check_provider_exists(self):
        statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        results = statement.execute(
            statement="""
                select id from GetProviderByName(
                    v_name:=%(provider_name)s
                )
            """,
            args=dict(
                provider_name=self.PROVIDER_NAME,
            ),
            ownConnection=True,
        )
        if not results:
            return None

        return results[0]['id']

    def _notify_provider_already_exists(self, existing_provider_id):
        short_error_message = _(
            'Cannot add {provider_name}: '
            'Provider name must be unique.'
        ).format(provider_name=self.PROVIDER_NAME)
        long_error_message = _(
            'Cannot install {provider_name}. '
            'A provider named "{provider_name}" '
            'already exists in the database and is not '
            'recognized as an engine-setup managed provider.'
            'To add a new {provider_name}, please '
            'rename or remove the existing provider.'
            'The offending provider id is: {provider_id}'
        ).format(
            provider_name=self.PROVIDER_NAME,
            provider_id=existing_provider_id
        )

        self.dialog.note(
            text=long_error_message
        )
        raise RuntimeError(short_error_message)

    def _add_provider_to_db(self):
        auth_required = self._user is not None
        fqdn = self.environment[osetupcons.ConfigEnv.FQDN]
        provider_id = str(uuid.uuid4())
        self.logger.info(_('Adding default OVN provider to database'))

        password = (
            self._encrypt_password(self._password).decode()
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
                    v_custom_properties:=%(custom_properties)s,
                    v_plugin_type:=%(plugin_type)s,
                    v_auth_url:=%(auth_url)s,
                    v_auto_sync:=%(auto_sync)s
                )
            """,
            args=dict(
                provider_id=provider_id,
                provider_name=self.PROVIDER_NAME,
                provider_description='oVirt network provider for OVN',
                provider_url='https://%s:9696' % fqdn,
                provider_type='EXTERNAL_NETWORK',
                auth_required=auth_required,
                auth_username=self._user,
                auth_password=password,
                custom_properties=None,
                plugin_type='OVIRT_PROVIDER_OVN',
                auth_url='https://%s:35357/v2.0/' % fqdn,
                auto_sync='TRUE'
            ),
        )

        return provider_id

    def _set_default_network_provider_in_db(self):
        self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                update cluster
                    set default_network_provider_id=%(provider_id)s
                    where default_network_provider_id is null and
                    name = 'Default'
                    and exists (
                      select 1 from providers where id = %(provider_id)s
                    )
            """,
            args=dict(
                provider_id=self.environment[OvnEnv.OVIRT_PROVIDER_ID],
            ),
        )

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
        self.logger.info(_('Adding OVN provider secret to database'))
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
                    %(encrypted_userinfo)s,
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
                encrypted_userinfo=True,
                trusted=True,
                notification_callback='',
                notification_callback_host_protocol='TLS',
                notification_callback_host_verification=False,
                notification_callback_chain_validation=True,
            ),
        )

    def _getSink(self, pm, pmsinkbase):

        class MyPMSink(pmsinkbase):

            def __init__(self, log):
                super(MyPMSink, self).__init__()
                self._log = log

            def verbose(self, msg):
                super(MyPMSink, self).verbose(msg)
                self._log.debug('%s %s', pm, msg)

            def info(self, msg):
                super(MyPMSink, self).info(msg)
                self._log.info('%s %s', pm, msg)

            def error(self, msg):
                super(MyPMSink, self).error(msg)
                self._log.error('%s %s', pm, msg)
        return MyPMSink(self.logger)

    def _setup_firewalld_services(self):
        services = [
            s.strip()
            for s in self.environment[
                OvnEnv.OVN_FIREWALLD_SERVICES
            ].split(',')
        ]

        # TODO: handle services that were copied over to
        # /etc/firewalld/services
        services_dir = self.environment[
            OvnEnv.FIREWALLD_SERVICES_DIR
        ]

        for service in services:
            self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].append(
                {
                    'name': service,
                    'absolute_path': os.path.join(
                        services_dir, '%s.xml' % (service,)
                    )
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
            return serialization.load_pem_private_key(
                '\n'.join(stdout).encode(),
                password=None,
                backend=default_backend(),
            )

        encrypted_password = _getRSA().public_key().encrypt(
            password.encode(),
            # TODO replace PKCS1v15 with PSS if/when we know we do not
            # need m2crypto compatibility. Would likely require changes
            # also in the engine and in the ovn provider.
            padding=padding.PKCS1v15(),
        )
        return base64.b64encode(encrypted_password)

    def _query_install_ovn(self):
        return dialog.queryBoolean(
            dialog=self.dialog,
            name='ovirt-provider-ovn',
            note=_(
                '\nConfiguring ovirt-provider-ovn also sets the Default '
                'cluster\'s default network provider to ovirt-provider-ovn.\n'
                'Non-Default clusters may be configured with an OVN after '
                'installation.\n'
                'Configure ovirt-provider-ovn (@VALUES@) [@DEFAULT@]: '
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
                'ovirt-provider-ovn (@VALUES@) [@DEFAULT@]: ' % user
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
            default='admin@internal',
        )

    def _query_ovn_password(self):
        return self.dialog.queryString(
            name='ovirt-provider-ovn-password',
            note=_(
                'oVirt OVN provider password: '
            ),
            prompt=True,
            hidden=True,
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

    def _execute_command(
        self,
        command,
        error_message,
        manual_commands=None,
        env=None,
    ):
        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            rc, stdout, stderr = self.execute(
                command,
                raiseOnError=False,
                env=env,
            )
            if rc != 0:
                self.logger.error(error_message)
                self._append_manual_commands(
                    self._failed_commands, command, manual_commands
                )
        else:
            self._append_manual_commands(
                self._manual_commands, command, manual_commands
            )

    def _append_manual_commands(self, append_to, command, manual_commands):
        if manual_commands:
            for manual_command in manual_commands:
                append_to.append(manual_command)
        else:
            append_to.append(command)

    def _configure_ovndb_connection(self, ovn_db_config):
        if (ovn_db_config.protocol == self.CONNECTION_SSL):
            if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
                self._sanitize_ovn_key_file_permissions(
                    ovn_db_config.key_file,
                    False
                )
            self._execute_command(
                (
                    ovn_db_config.command,
                    'set-ssl',
                    ovn_db_config.key_file,
                    ovn_db_config.cert_file,
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                    ovn_db_config.ssl_protocol or 'TLSv1.2',
                    ovn_db_config.ciphers or 'HIGH',
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
                'p%s:%s:[::]' % (ovn_db_config.protocol, ovn_db_config.port),
            ),
            _(
                'Failed to open {name} SSL connection'
            ).format(
                name=ovn_db_config.name
            )
        )

    def _set_file_permissions(
            self,
            file_path,
            enable_logging,
            desired_permissions
    ):
        current_permissions = stat.S_IMODE(
            os.lstat(
                file_path
            ).st_mode
        )
        if desired_permissions != current_permissions:
            if enable_logging:
                self.logger.info(_(
                    'Setting permissions on {file} '
                    'to enable OVN and engine to read it.'
                ).format(
                    file=file_path
                ))
            # This is not reverted on rollback.
            # This is ok, because older versions of OVN ran as root,
            # and thus would still have access to the file
            os.chmod(
                file_path,
                desired_permissions
            )

    def _sanitize_ovn_key_file_permissions(self, file_path, enable_logging):
        desired_permissions = stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP
        self._set_file_permissions(
            file_path,
            enable_logging,
            desired_permissions
        )
        desired_gid = grp.getgrnam('openvswitch').gr_gid
        current_gid = os.stat(file_path).st_gid
        if desired_gid != current_gid:
            if enable_logging:
                self.logger.info(_(
                    'Setting group ownership on {file} '
                    'to enable OVN to read it.'
                ).format(
                    file=file_path
                ))
            # This is not reverted on rollback.
            # This is ok, because older versions of OVN ran as root,
            # and thus would still have access to the file
            os.chown(
                file_path,
                os.stat(file_path).st_uid,
                desired_gid
            )

    def _configure_ovndb_north_connection(self):
        self._configure_ovndb_connection(self.OVN_NORTH_DB_CONFIG)

    def _configure_ovndb_south_connection(self):
        self._configure_ovndb_connection(self.OVN_SOUTH_DB_CONFIG)

    def _format_config_file_content(self, parameters):
        content = []
        content.append(
            '# This file is automatically generated by engine-setup. '
            'Please do not edit manually'
        )
        for section in parameters:
            content.append(section)
            content.extend([
                '{key}={value}'.format(key=k, value=v)
                for k, v in parameters[section].items()
            ])
        return content

    def _display_config_file_dev_task(self):
        self._manual_tasks.append(
            _(
                'ovirt-provider-ovn could not be configured because setup'
                ' is executed in developer mode.\n'
                'To configure ovirt-provider-ovn, please copy the '
                'content of:\n'
                '    {example}'
                '\ninto:\n'
                '    {target}'
            ).format(
                example=oenginecons.OvnFileLocations.
                OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_EXAMPLE,
                target=oenginecons.OvnFileLocations.
                OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE
            )
        )

    def _display_config_file_production_message(self):
        self._manual_tasks.append(
            _(
                'ovirt-provider-ovn configuration file was created in:\n'
                '{target}\n'
                'A sample configuration file for future reference was '
                'created in:\n'
                '{example}'
            ).format(
                example=oenginecons.OvnFileLocations.
                OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_EXAMPLE,
                target=oenginecons.OvnFileLocations.
                OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE
            )
        )

    def _create_provider_config(self, content, config_file, uninstall_files):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=config_file,
                content=content,
                visibleButUnsafe=True,
                modifiedList=uninstall_files,
                mode=0o600
            )
        )

    def _create_config_content(self):
        engine_port = self.environment[
            oengcommcons.ConfigEnv.HTTPS_PORT
        ] if self.environment[
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT
        ] else self.environment[
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
        ]

        parameters = {
            '[PROVIDER]': {
                'provider-host': self.environment[osetupcons.ConfigEnv.FQDN]
            },
            '[SSL]': {
                'ssl-cert-file':
                    oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_HTTPS_CERT,
                'ssl-key-file':
                    oenginecons.OvnFileLocations.OVIRT_PROVIDER_OVN_HTTPS_KEY,
                'ssl-cacert-file':
                    oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
                'https-enabled':
                    'true',
            },
            '[OVN REMOTE]': {
                'ovn-remote':
                    '%s:127.0.0.1:%s' % (
                        self.OVN_NORTH_DB_CONFIG.protocol,
                        self.OVN_NORTH_DB_CONFIG.port,
                    ),
            },
            '[OVIRT]': {
                'ovirt-sso-client-id':
                    Const.OVIRT_PROVIDER_OVN_CLIENT_ID_VALUE,
                'ovirt-sso-client-secret':
                    self.environment[
                        OvnEnv.OVIRT_PROVIDER_OVN_SECRET
                    ],
                'ovirt-host':
                    'https://%s:%s' % (
                        self.environment[osetupcons.ConfigEnv.FQDN],
                        engine_port
                    ),
                'ovirt-ca-file':
                    oengcommcons.FileLocations.
                    OVIRT_ENGINE_PKI_APACHE_CA_CERT,
            },
            '[NETWORK]': {
                'port-security-enabled-default': True
            }
        }
        return self._format_config_file_content(parameters)

    def _configure_ovirt_provider_ovn(self):
        content = self._create_config_content()
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

        self._create_provider_config(
            content,
            oenginecons.OvnFileLocations.
            OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_EXAMPLE,
            uninstall_files
        )

        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self._display_config_file_dev_task()
        else:
            self._create_provider_config(
                content,
                oenginecons.OvnFileLocations.
                OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE,
                uninstall_files
            )
            self._display_config_file_production_message()

    def _upate_external_providers_keystore(self):
        config = configfile.ConfigFile([
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_DEFAULTS,
            oenginecons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        truststore = config.get(
            'ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE'
        )
        truststore_password = config.get(
            'ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE_PASSWORD'
        )

        # We need to disable FIPS configuration of OpenJDK to be able to work
        # with file system keystores and interoperability with openssl
        command = (
            'keytool',
            '-J-Dcom.redhat.fips=false',
            '-import',
            '-alias',
            OvnEnv.PROVIDER_NAME,
            '-keystore',
            truststore,
            '-file',
            oenginecons.FileLocations.OVIRT_ENGINE_PKI_ENGINE_CA_CERT,
            '-noprompt',
            '-storepass:env',
            'pass',
        )
        self._execute_command(
            command,
            _(
                'Failed to import provider certificate into '
                'the external provider keystore'
            ),
            manual_commands=(
                (
                    '.',
                    FileLocations.OVIRT_ENGINE_DATADIR +
                    '/bin/engine-prolog.sh'
                ),
                (
                    (
                        'export pass='
                        '"${ENGINE_EXTERNAL_PROVIDERS_TRUST_STORE_PASSWORD}"'
                    ),
                ),
                command,
            ),
            env={
                'pass': truststore_password,
            },
        )
        self._set_file_permissions(
            truststore,
            True,
            stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP | stat.S_IROTH,
        )

    def _is_provider_installed(self):
        # TODO: we currently only check against installations done by
        # engine-setup
        # In the future we should also add a check against manual installation
        if self.environment.get(
            OvnEnv.OVIRT_PROVIDER_ID
        ):
            return True
        return False

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def _customization(self):
        self._provider_installed = self._is_provider_installed()
        if self._provider_installed:
            self.logger.info(_(
                'ovirt-provider-ovn already installed, skipping.'))
        if (
            self.environment[OvnEnv.OVIRT_PROVIDER_OVN] is None and
            not self._provider_installed
        ):
            self.environment[OvnEnv.OVIRT_PROVIDER_OVN] = \
                self._query_install_ovn()

        self._enabled = (
            self.environment[OvnEnv.OVIRT_PROVIDER_OVN] and
            not self._provider_installed
        )
        if self._enabled or self._provider_installed:
            self._setup_firewalld_services()

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
        before=(
                oenginecons.Stages.OVN_SERVICES_RESTART,
        ),
        condition=lambda self: (
                self._provider_installed and
                not self.environment[
                    osetupcons.CoreEnv.DEVELOPER_MODE
                ]
        ),
    )
    def _upgrade(self):
        self._sanitize_config_file_permissions()
        self._update_ovn_key_file_permissions()

    def _update_ovn_key_file_permissions(self):
        for ovn_db_config in [
            self.OVN_SOUTH_DB_CONFIG,
            self.OVN_NORTH_DB_CONFIG
        ]:
            self._sanitize_ovn_key_file_permissions(
                ovn_db_config.key_file,
                True
            )

    def _sanitize_config_file_permissions(self):
        try:
            current_premissions = stat.S_IMODE(
                os.lstat(
                    oenginecons.OvnFileLocations.
                    OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE
                ).st_mode
            )
            permissions_to_remove = stat.S_IRWXO | stat.S_IRWXG
            desired_permissions = current_premissions & ~permissions_to_remove
            if desired_permissions != current_premissions:
                self.logger.info(_(
                    'Removing unnecessary permissions on {file}.'
                ).format(
                    file=oenginecons.OvnFileLocations.
                    OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE
                ))
                # This is not reverted on rollback.
                os.chmod(
                    oenginecons.OvnFileLocations.
                    OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE,
                    desired_permissions
                )
        except OSError as error:
            if error.errno == errno.ENOENT:
                self.logger.warning(_(
                    'Unable to ensure permissions on {file}'
                ).format(
                    file=oenginecons.OvnFileLocations.
                    OVIRT_PROVIDER_ENGINE_SETUP_CONFIG_FILE
                )
                )
            else:
                raise

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.OVN_SERVICES_RESTART,
        condition=lambda self: (
            (self._enabled or self._provider_installed) and not
            self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        )
    )
    def _restart_ovn_services(self):
        for service in OvnEnv.ENGINE_MACHINE_OVN_SERVICES:
            self._restart_service(service)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oenginecons.Stages.OVN_PROVIDER_SERVICE_RESTART,
        ),
        after=(
            oenginecons.Stages.CA_AVAILABLE,
            oenginecons.Stages.OVN_SERVICES_RESTART,
        ),
        condition=lambda self: self._enabled or self._provider_installed,
    )
    def _misc_configure_ovn_pki(self):
        self.logger.info(_('Updating OVN SSL configuration'))
        self._configure_ovndb_north_connection()
        self._configure_ovndb_south_connection()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
                oenginecons.Stages.OVN_SERVICES_RESTART,
        ),
        condition=lambda self: self._enabled or self._provider_installed,
    )
    def _misc_configure_ovn_timeout(self):
        self.logger.info(_('Updating OVN timeout configuration'))
        self._execute_command(
            (
                self.OVN_SOUTH_DB_CONFIG.command,
                'set',
                'connection',
                '.',
                'inactivity_probe=60000',
            ),
            _(
                'Failed to configure timeout on {name}'
            ).format(
                name=self.OVN_SOUTH_DB_CONFIG.name
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oenginecons.Stages.OVN_PROVIDER_SERVICE_RESTART,
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
            (self._enabled or self._provider_installed) and not
            self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]
        )
    )
    def _restart_provider_service(self):
        self._restart_service(OvnEnv.OVIRT_PROVIDER_OVN_SERVICE)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        before=(
            oenginecons.Stages.OVN_PROVIDER_OVN_DB,
        ),
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
        condition=lambda self: (
            self._enabled and
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _validate_provider_uniqueness(self):
        existing_provider_id = self._check_provider_exists()
        if existing_provider_id:
            self._notify_provider_already_exists(existing_provider_id)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oenginecons.Stages.OVN_PROVIDER_OVN_DB,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
            oenginecons.Stages.CA_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc_db_entries(self):
        provider_id = self._add_provider_to_db()
        if self.environment.get(OvnEnv.OVIRT_PROVIDER_ID) is not None:
            raise Exception(_(
                'Attempting to set ovirt-provider-ovn id, but'
                ' the id has already been set. Overwriting'
                ' an already existing provider id is not allowed.'
            ))

        self.environment[
            OvnEnv.OVIRT_PROVIDER_ID
        ] = provider_id
        self._add_client_secret_to_db()
        self._set_default_network_provider_in_db()

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: (
            (self._enabled or self._provider_installed) and
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
        if self._manual_tasks:
            for task in self._manual_tasks:
                self.dialog.note(
                    text=task
                )

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
