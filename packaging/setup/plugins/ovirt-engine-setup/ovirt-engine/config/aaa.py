#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""aaa plugin."""


import gettext
import random
import string

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database

from ovirt_setup_lib import dialog

try:
    import pwquality
    _use_pwquality = True
except ImportError:
    # do not force this optional feature
    _use_pwquality = False


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
        name=oengcommcons.Stages.ADMIN_PASSWORD_SET,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
        after=(
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
                    if(_use_pwquality):
                        pwq = pwquality.PWQSettings()
                        pwq.read_config()
                        pwq.check(password, None, None)
                    valid = True
                except pwquality.PWQError as e:
                    self.logger.warning(
                        _('Password is weak: {error}').format(
                            error=e.args[1],
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

        # we have legacy user. Shouldn't happen anymore, after
        # 3.6 https://gerrit.ovirt.org/q/Ica85b6a
        if adminPassword is not None:
            self.dialog.note(
                text=_(
                    'Old AdminPassword found in vdc_options. This should not '
                    'happen, and is likely a result of a bad past upgrade.\n'
                    'Please contact support.\n'
                    'If you are certain that it is not in use anymore, you '
                    'can remove it with this command:\n'
                    '# /usr/share/ovirt-engine/dbscripts/engine-psql.sh -c '
                    '\\\n'
                    '   "select fn_db_delete_config_value(\'AdminPassword\','
                    '\'general\');"\n'
                    '\nand then try again.\n'
                )
            )
            raise RuntimeError(_('Old AdminPassword found in vdc_options'))

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
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: (
            self.environment[
                oenginecons.CoreEnv.ENABLE
            ]
        ),
    )
    def _attach_group_to_role(self):
        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select attach_group_to_role(
                    'ovirt-administrator',
                    'SuperUser'
                )
            """
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
