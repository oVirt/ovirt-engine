#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""AAA-JDBC extension admin user setup plugin."""

import datetime
import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """AAA-JDBC extension admin user setup plugin."""

    PACKAGE_NAME = 'ovirt-engine-extension-aaa-jdbc'

    AAA_JDBC_SETUP_ADMIN_USER = 'osetup.aaa_jdbc.config.setup.admin.user'

    AAA_JDBC_AUTHZ_TYPE = 'ovirt-engine-extension-aaa-jdbc'

    _AAA_JDBC_SCHEMA = 'aaa_jdbc'

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _userExists(self, toolArgs, toolEnv, name):
        rc, stdout, stderr = self.execute(
            args=toolArgs + (
                'query',
                '--what=user',
                '--pattern=name=%s' % name,
            ),
            envAppend=toolEnv,
        )
        return (
            rc == 0 and
            name in ' '.join(stdout)
        )

    def _createUser(self, toolArgs, toolEnv, name, email, id):
        self.execute(
            args=toolArgs + (
                'user',
                'add',
                name,
                '--attribute=firstName=%s' % name,
                '--attribute=email=%s' % email,
            ) + (
                (
                    '--id=%s' % id,
                ) if id is not None else ()
            ),
            envAppend=toolEnv,
        )

    def _getUserId(self, toolArgs, toolEnv, name):
        rc, stdout, stderr = self.execute(
            args=toolArgs + (
                'user',
                'show',
                name,
                '--attribute=id',
            ),
            envAppend=toolEnv,
        )
        return stdout[0]

    def _getUserEmail(self, toolArgs, toolEnv, name):
        rc, stdout, stderr = self.execute(
            args=toolArgs + (
                'user',
                'show',
                name,
                '--attribute=email',
            ),
            envAppend=toolEnv,
        )
        return stdout[0]

    def _setUserEmail(self, toolArgs, toolEnv, name, email):
        rc, stdout, stderr = self.execute(
            args=toolArgs + (
                'user',
                'edit',
                name,
                '--attribute=email=%s' % email,
            ),
            envAppend=toolEnv,
        )

    def _setupSchema(self):
        self.logger.info(
            _("Creating/refreshing Engine 'internal' domain database schema")
        )
        args = [
            oenginecons.FileLocations.AAA_JDBC_DB_SCHMA_TOOL,
            '-s', self.environment[oenginecons.EngineDBEnv.HOST],
            '-p', str(self.environment[oenginecons.EngineDBEnv.PORT]),
            '-u', self.environment[oenginecons.EngineDBEnv.USER],
            '-d', self.environment[oenginecons.EngineDBEnv.DATABASE],
            '-e', self._AAA_JDBC_SCHEMA,
            '-l', self.environment[otopicons.CoreEnv.LOG_FILE_NAME],
            '-c', 'apply',
        ]
        if self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ]:
            if not os.path.exists(
                oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
            ):
                os.makedirs(
                    oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR
                )
            args.extend(
                [
                    '-m',
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_DB_MD5_DIR,
                        '%s-%s-aaa-jdbc.scripts.md5' % (
                            self.environment[
                                oenginecons.EngineDBEnv.HOST
                            ],
                            self.environment[
                                oenginecons.EngineDBEnv.DATABASE
                            ],
                        ),
                        ),
                ]
            )
        self.execute(
            args=args,
            envAppend={
                'DBFUNC_DB_PGPASSFILE': self.environment[
                    oenginecons.EngineDBEnv.PGPASS_FILE
                ]
            },
        )

    def _setupAuth(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=oenginecons.FileLocations.AAA_JDBC_CONFIG_DB,
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                content=(
                    'config.datasource.jdbcurl={jdbcUrl}\n'
                    'config.datasource.dbuser={user}\n'
                    'config.datasource.dbpassword={password}\n'
                    'config.datasource.jdbcdriver=org.postgresql.Driver\n'
                    'config.datasource.schemaname={schemaName}\n'
                ).format(
                    jdbcUrl=database.OvirtUtils(
                        plugin=self,
                        dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                    ).getJdbcUrl(),
                    user=self.environment[oenginecons.EngineDBEnv.USER],
                    password=outil.escape(
                        self.environment[oenginecons.EngineDBEnv.PASSWORD],
                        '"\\$',
                    ),
                    schemaName=self._AAA_JDBC_SCHEMA
                ),
                visibleButUnsafe=True,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

        profile = self.environment[
            oenginecons.ConfigEnv.ADMIN_USER
        ].rsplit('@', 1)[1]

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_EXTENSIONS_DIR,
                        '%s-authn.properties' % profile
                    )
                ),
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                content=(
                    'ovirt.engine.extension.name = internal-authn\n'
                    'ovirt.engine.extension.bindings.method = jbossmodule\n'

                    'ovirt.engine.extension.binding.jbossmodule.module = '
                    'org.ovirt.engine.extension.aaa.jdbc\n'

                    'ovirt.engine.extension.binding.jbossmodule.class = '
                    'org.ovirt.engine.extension.aaa.jdbc.binding.api.'
                    'AuthnExtension\n'

                    'ovirt.engine.extension.provides = '
                    'org.ovirt.engine.api.extensions.aaa.Authn\n'

                    'ovirt.engine.aaa.authn.profile.name = {profile}\n'
                    'ovirt.engine.aaa.authn.authz.plugin = {authzName}\n'
                    'config.datasource.file = {dbConfigFile}\n'
                ).format(
                    profile=profile,
                    authzName=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME
                    ],
                    dbConfigFile=oenginecons.FileLocations.AAA_JDBC_CONFIG_DB,
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    os.path.join(
                        oenginecons.FileLocations.OVIRT_ENGINE_EXTENSIONS_DIR,
                        '%s-authz.properties' % profile
                    )
                ),
                mode=0o600,
                owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                enforcePermissions=True,
                content=(
                    'ovirt.engine.extension.name = {authzName}\n'
                    'ovirt.engine.extension.bindings.method = jbossmodule\n'

                    'ovirt.engine.extension.binding.jbossmodule.module = '
                    'org.ovirt.engine.extension.aaa.jdbc\n'

                    'ovirt.engine.extension.binding.jbossmodule.class = '
                    'org.ovirt.engine.extension.aaa.jdbc.binding.api.'
                    'AuthzExtension\n'

                    'ovirt.engine.extension.provides = '
                    'org.ovirt.engine.api.extensions.aaa.Authz\n'

                    'config.datasource.file = {dbConfigFile}\n'
                ).format(
                    profile=profile,
                    authzName=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME
                    ],
                    dbConfigFile=oenginecons.FileLocations.AAA_JDBC_CONFIG_DB,
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    def _setupAdminUser(self):
        toolArgs = (
            oenginecons.FileLocations.AAA_JDBC_TOOL,
            '--db-config=%s' % oenginecons.FileLocations.AAA_JDBC_CONFIG_DB,
        )

        toolEnv = {
            'OVIRT_ENGINE_JAVA_HOME_FORCE': '1',
            'OVIRT_ENGINE_JAVA_HOME': self.environment[
                oengcommcons.ConfigEnv.JAVA_HOME
            ],
            'OVIRT_JBOSS_HOME': self.environment[
                oengcommcons.ConfigEnv.JBOSS_HOME
            ],
        }

        adminUser = self.environment[
            oenginecons.ConfigEnv.ADMIN_USER
        ].rsplit('@', 1)[0]

        # Should this be configurable? User can change it later.
        adminEmail = 'root@localhost'

        if not self._userExists(
            toolArgs=toolArgs,
            toolEnv=toolEnv,
            name=adminUser,
        ):
            self._createUser(
                toolArgs=toolArgs,
                toolEnv=toolEnv,
                name=adminUser,
                email=adminEmail,
                id=self.environment[oenginecons.ConfigEnv.ADMIN_USER_ID],
            )

            if self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_ID
            ] is None:
                self.environment[
                    oenginecons.ConfigEnv.ADMIN_USER_ID
                ] = self._getUserId(
                    toolArgs=toolArgs,
                    toolEnv=toolEnv,
                    name=adminUser,
                )
        elif '@' not in self._getUserEmail(
            toolArgs=toolArgs,
            toolEnv=toolEnv,
            name=adminUser,
        ):
            self._setUserEmail(
                toolArgs=toolArgs,
                toolEnv=toolEnv,
                name=adminUser,
                email=adminEmail,
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.RPMDistroEnv.ENGINE_AAA_JDBC_PACKAGE,
            self.PACKAGE_NAME
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            oenginecons.Stages.CORE_ENABLE,
        ),
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            os.path.exists(oenginecons.FileLocations.AAA_JDBC_DB_SCHMA_TOOL)
        ),
    )
    def _customization(self):
        self.environment[
            oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE
        ] = self.AAA_JDBC_AUTHZ_TYPE

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DISTRO_RPM_PACKAGE_UPDATE_CHECK,
        ),
    )
    def _version_lock_customization(self):
        self.environment[
            osetupcons.RPMDistroEnv.VERSION_LOCK_FILTER
        ].append(
            self.environment[
                oenginecons.RPMDistroEnv.ENGINE_AAA_JDBC_PACKAGE
            ]
        )
        self.environment[
            osetupcons.RPMDistroEnv.VERSION_LOCK_APPLY
        ].append(
            self.environment[
                oenginecons.RPMDistroEnv.ENGINE_AAA_JDBC_PACKAGE
            ]
        )
        self.environment[
            osetupcons.RPMDistroEnv.PACKAGES_UPGRADE_LIST
        ].append(
            {
                'packages': [
                    self.environment[
                        oenginecons.RPMDistroEnv.ENGINE_AAA_JDBC_PACKAGE
                    ]
                ]
            },
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=AAA_JDBC_SETUP_ADMIN_USER,
        after=(
            oengcommcons.Stages.DB_SCHEMA,
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
            oenginecons.Stages.CONFIG_EXTENSIONS_UPGRADE,
        ),
        before=(
            oenginecons.Stages.CONFIG_AAA_ADMIN_USER_SETUP,
        ),
        condition=lambda self: self.environment[
            oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE
        ] == self.AAA_JDBC_AUTHZ_TYPE,
    )
    def _misc(self):
        # TODO: if we knew that aaa-jdbc package was upgraded by engine-setup
        # TODO: we could display summary note that custom profiles have to be
        # TODO: upgraded manually
        self._setupSchema()
        self._setupAuth()
        self._setupAdminUser()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            AAA_JDBC_SETUP_ADMIN_USER,
        ),
        condition=lambda self: (
            self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE
            ] == self.AAA_JDBC_AUTHZ_TYPE and
            self.environment[
                oenginecons.ConfigEnv.ADMIN_PASSWORD
            ] is not None
        ),
    )
    def _setupAdminPassword(self):
        adminUser = self.environment[
            oenginecons.ConfigEnv.ADMIN_USER
        ].rsplit('@', 1)[0]

        self.logger.info(
            _(
                'Setting a password for internal user {admin}'
            ).format(
                admin=adminUser,
            )
        )
        self.execute(
            args=(
                oenginecons.FileLocations.AAA_JDBC_TOOL,
                '--db-config=%s' % (
                    oenginecons.FileLocations.AAA_JDBC_CONFIG_DB
                ),

                'user',
                'password-reset',
                adminUser,
                '--password=env:pass',

                # we need to skip password validity checks when upgrading
                # from legacy internal provider
                '--force',

                # we need to specify password validity, otherwise password
                # will be expired at the same moment when we set it
                '--password-valid-to=%sZ' % (
                    (
                        datetime.datetime.utcnow() +
                        datetime.timedelta(days=73000)
                    ).replace(
                        microsecond=0,
                    ).isoformat(' ')
                ),
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
                    oenginecons.ConfigEnv.ADMIN_PASSWORD
                ],
            },
        )


# vim: expandtab tabstop=4 shiftwidth=4
