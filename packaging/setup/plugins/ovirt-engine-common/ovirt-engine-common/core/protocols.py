#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Protocols plugin."""


import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Protocols plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ConfigEnv.HTTP_PORT,
            oengcommcons.Defaults.DEFAULT_NETWORK_HTTP_PORT
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.HTTPS_PORT,
            oengcommcons.Defaults.DEFAULT_NETWORK_HTTPS_PORT
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_HTTP_PORT,
            oengcommcons.Defaults.DEFAULT_NETWORK_JBOSS_HTTP_PORT
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_HTTPS_PORT,
            oengcommcons.Defaults.DEFAULT_NETWORK_JBOSS_HTTPS_PORT
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT,
            oengcommcons.Defaults.DEFAULT_NETWORK_JBOSS_AJP_PORT
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_DEBUG_ADDRESS,
            oengcommcons.Defaults.DEFAULT_NETWORK_JBOSS_DEBUG_ADDRESS
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT,
            None
        )
        self.environment.setdefault(
            oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[
                oengcommcons.ConfigEnv.JBOSS_AJP_PORT
            ] = None
            self.environment[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
            ] = self.environment[
                oengcommcons.ConfigEnv.JBOSS_HTTP_PORT
            ]
            self.environment[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
            ] = self.environment[
                oengcommcons.ConfigEnv.JBOSS_HTTPS_PORT
            ]
        if self.environment[
            oengcommcons.ConfigEnv.JBOSS_AJP_PORT
        ] is None:
            self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
            ] = self.environment[
                oengcommcons.ConfigEnv.JBOSS_HTTP_PORT
            ]
            self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
            ] = self.environment[
                oengcommcons.ConfigEnv.JBOSS_HTTPS_PORT
            ]
        else:
            self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTP_PORT
            ] = self.environment[
                oengcommcons.ConfigEnv.HTTP_PORT
            ]
            self.environment[
                oengcommcons.ConfigEnv.PUBLIC_HTTPS_PORT
            ] = self.environment[
                oengcommcons.ConfigEnv.HTTPS_PORT
            ]


# vim: expandtab tabstop=4 shiftwidth=4
