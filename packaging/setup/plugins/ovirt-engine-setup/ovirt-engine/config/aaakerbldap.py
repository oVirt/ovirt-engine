#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""aaa kerbldap plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import database


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """aaa kerbldap plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

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
        domain = None
        try:
            domain = vdcoption.VdcOption(
                statement=database.Statement(
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                    environment=self.environment,
                ),
            ).getVdcOption(
                'DomainName',
                ownConnection=True,
            )
        except RuntimeError:
            pass

        if domain:
            raise RuntimeError(
                _(
                    'Setup found legacy kerberos/ldap directory '
                    'integration in use (added by engine-manage-domains). '
                    'This provider is no longer supported, please '
                    'migrate to ovirt-engine-extension-aaa-ldap '
                    'provider or contact support for assistance.'
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
