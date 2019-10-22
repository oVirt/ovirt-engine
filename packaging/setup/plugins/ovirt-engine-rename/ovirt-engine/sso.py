#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""sso plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """sso plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _update_client_callback_prefix(self):
        engine_port = self.environment[
            oengcommcons.ConfigEnv.HTTPS_PORT
        ] if self.environment[
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT
        ] else self.environment[
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
        ]

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select update_oauth_client_callback_prefix(
                    %(client_id)s,
                    %(callback_prefix)s
                )
            """,
            args=dict(
                client_id='ovirt-engine-core',
                callback_prefix='https://%s:%s/ovirt-engine/' % (
                    self.environment[osetupcons.RenameEnv.FQDN],
                    engine_port,
                ),
            ),
        )

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                update sso_clients
                   set notification_callback = %(notification_callback)s
                 where client_id = %(client_id)s
            """,
            args=dict(
                notification_callback='https://%s:%s/%s' % (
                    self.environment[osetupcons.RenameEnv.FQDN],
                    engine_port,
                    'ovirt-engine/services/sso-callback',
                ),
                client_id='ovirt-engine-core',
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
