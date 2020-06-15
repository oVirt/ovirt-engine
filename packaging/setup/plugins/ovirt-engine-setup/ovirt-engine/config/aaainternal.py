#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""aaainternal plugin."""


import gettext
import os
import uuid

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
    """aaainternal plugin."""

    MY_TYPE = 'builtin-internal'

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LAST,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE
            ] is None
        ),
    )
    def _validation(self):
        self.environment[
            oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE
        ] = self.MY_TYPE

        if self.environment[oenginecons.ConfigEnv.ADMIN_USER_ID] is None:
            self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_ID
            ] = str(uuid.uuid4())

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ] and self.environment[
                oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_TYPE
            ] == self.MY_TYPE and
            self.environment[
                oenginecons.ConfigEnv.ADMIN_PASSWORD
            ] is not None
        ),
    )
    def _misc(self):
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
                    oenginecons.ConfigEnv.ADMIN_PASSWORD
                ],
            },
        )
        pbe = stdout[0]

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
                    'org.ovirt.engine.extension.aaa.builtin\n'

                    'ovirt.engine.extension.binding.jbossmodule.class = '
                    'org.ovirt.engine.extension.aaa.builtin.internal.'
                    'InternalAuthn\n'

                    'ovirt.engine.extension.provides = '
                    'org.ovirt.engine.api.extensions.aaa.Authn\n'

                    'ovirt.engine.aaa.authn.profile.name = {profile}\n'
                    'ovirt.engine.aaa.authn.authz.plugin = {authzName}\n'
                    'config.authn.user.name = {admin_user}\n'
                    'config.authn.user.password = {pbe}\n'
                ).format(
                    profile=profile,
                    authzName=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME
                    ],
                    admin_user=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER
                    ].rsplit('@', 1)[0],
                    pbe=pbe,
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
                    'org.ovirt.engine.extension.aaa.builtin\n'

                    'ovirt.engine.extension.binding.jbossmodule.class = '
                    'org.ovirt.engine.extension.aaa.builtin.internal.'
                    'InternalAuthz\n'

                    'ovirt.engine.extension.provides = '
                    'org.ovirt.engine.api.extensions.aaa.Authz\n'

                    'config.authz.user.name = {admin_user}\n'
                    'config.authz.user.id = {admin_id}\n'
                ).format(
                    authzName=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER_AUTHZ_NAME
                    ],
                    admin_user=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER
                    ].rsplit('@', 1)[0],
                    admin_id=self.environment[
                        oenginecons.ConfigEnv.ADMIN_USER_ID
                    ],
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
