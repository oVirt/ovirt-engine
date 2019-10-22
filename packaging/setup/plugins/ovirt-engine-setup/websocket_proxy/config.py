#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""websocket proxy plugin."""


import gettext
import os

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.websocket_proxy import constants as owspcons

from ovirt_setup_lib import dialog
from ovirt_setup_lib import hostname as osetuphostname


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """websocket proxy plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._needStart = False
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG,
            None
        )
        self.environment.setdefault(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_PORT,
            osetupcons.Defaults.DEFAULT_WEBSOCKET_PROXY_PORT
        )
        self.environment.setdefault(
            owspcons.ConfigEnv.WEBSOCKET_PROXY_HOST,
            'localhost'
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ENGINE_FQDN,
            None
        )
        self.environment.setdefault(
            oenginecons.CoreEnv.ENABLE,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_LATE_SETUP,
        condition=lambda self: not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ],
    )
    def _late_setup_service_state(self):
        self._needStart = self.services.status(
            name='ovirt-websocket-proxy',
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=owspcons.Stages.CONFIG_WEBSOCKET_PROXY_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def _customization(self):

        if self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ] is None:
            self.environment[
                owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONFIG_WEBSOCKET_PROXY',
                note=_(
                    'Configure WebSocket Proxy on this host '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=True,
            )
        self._enabled = self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ]
        if self._enabled:
            self.environment[
                owspcons.ConfigEnv.WEBSOCKET_PROXY_STOP_NEEDED
            ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
            oengcommcons.Stages.NETWORK_OWNERS_CONFIG_CUSTOMIZED,
        ),
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ),
        condition=lambda self: self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ],
    )
    def _customization_network(self):
        osetuphostname.Hostname(
            plugin=self,
        ).getHostname(
            envkey=oenginecons.ConfigEnv.ENGINE_FQDN,
            whichhost=_('the engine'),
            supply_default=False,
            validate_syntax=True,
            system=True,
            dns=False,
            local_non_loopback=self.environment[
                osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION
            ],
            reverse_dns=self.environment[
                osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION
            ],
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            owspcons.ConfigEnv.WEBSOCKET_PROXY_CONFIG
        ],
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            owspcons.Stages.CONFIG_WEBSOCKET_PROXY_CUSTOMIZATION,
        ),
    )
    def _customization_firewall(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-websocket-proxy',
                'directory': 'websocket-proxy'
            },
        ])
        self.environment[
            osetupcons.NetEnv.FIREWALLD_SUBST
        ].update({
            '@WEBSOCKET_PROXY_PORT@': self.environment[
                owspcons.ConfigEnv.WEBSOCKET_PROXY_PORT
            ],
        })

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self._enabled,
        after=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
    )
    def _check_separate(self):
        self.logger.info(_('Configuring WebSocket Proxy'))
        if (
            not os.path.exists(
                owspcons.FileLocations.
                OVIRT_ENGINE_PKI_ENGINE_CERT
            )
        ):
            self.dialog.note(
                text=_(
                    "\n"
                    "ATTENTION\n"
                    "\n"
                    "Manual actions are required on the engine host\n"
                    "in order to enroll certs for this host "
                    "and configure the engine about it.\n"
                )
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=owspcons.Stages.REMOTE_VDC,
        condition=lambda self: (
            self._enabled and
            not os.path.exists(
                owspcons.FileLocations.
                OVIRT_ENGINE_PKI_ENGINE_CERT
            )
        ),
        after=(
            oenginecons.Stages.CA_AVAILABLE,
        ),
    )
    def _misc_VDC(self):
        self.dialog.note(
            text=_(
                "\nPlease execute this command on the engine host: \n"
                "   engine-config -s WebSocketProxy={fqdn}:{port}\n"
                "and than restart the engine service to make it effective\n\n"
            ).format(
                fqdn=self.environment[osetupcons.ConfigEnv.FQDN],
                port=self.environment[
                    owspcons.ConfigEnv.WEBSOCKET_PROXY_PORT
                ],
            ),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self._enabled,
        ),
    )
    def _misc_config(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    owspcons.FileLocations.
                    OVIRT_ENGINE_WEBSOCKET_PROXY_CONFIG_SETUP
                ),
                content=(
                    "PROXY_PORT={port}\n"
                    "SSL_CERTIFICATE={certificate}\n"
                    "SSL_KEY={key}\n"
                    "CERT_FOR_DATA_VERIFICATION={engine_cert}\n"
                    "SSL_ONLY=True\n"
                ).format(
                    port=self.environment[
                        owspcons.ConfigEnv.WEBSOCKET_PROXY_PORT
                    ],
                    certificate=(
                        owspcons.FileLocations.
                        OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_CERT
                    ),
                    key=(
                        owspcons.FileLocations.
                        OVIRT_ENGINE_PKI_WEBSOCKET_PROXY_KEY
                    ),
                    engine_cert=(
                        owspcons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CERT
                    ),
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        condition=lambda self: (
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ] and (
                self._needStart or
                self._enabled
            )
        ),
    )
    def _closeup(self):
        for state in (False, True):
            self.services.state(
                name=owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
                state=state,
            )
        self.services.startup(
            name=owspcons.Const.WEBSOCKET_PROXY_SERVICE_NAME,
            state=True,
        )


# vim: expandtab tabstop=4 shiftwidth=4
