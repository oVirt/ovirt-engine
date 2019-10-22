#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""vmconsole proxy configuration plugin."""

import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons

from ovirt_setup_lib import dialog
from ovirt_setup_lib import hostname as osetuphostname


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


def _base_url_from_env(env):
    sslFlag = env[
        oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
    ]
    proxyFlag = env[
        oengcommcons.ConfigEnv.JBOSS_AJP_PORT
    ]

    if sslFlag:
        proto = 'https'
        if proxyFlag:
            port = env[
                oengcommcons.ConfigEnv.HTTPS_PORT
            ]
        else:
            port = env[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
            ]
    else:
        proto = 'http'
        if proxyFlag:
            port = env[
                oengcommcons.ConfigEnv.HTTP_PORT
            ]
        else:
            port = env[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
            ]

    return "{proto}://{fqdn}:{port}/ovirt-engine/".format(
        proto=proto,
        fqdn=env[osetupcons.ConfigEnv.FQDN],
        port=port,
    )


@util.export
class Plugin(plugin.PluginBase):
    """vmconsole proxy configuration plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG,
            None
        )
        self.environment.setdefault(
            oenginecons.ConfigEnv.ENGINE_FQDN,
            None
        )
        self.environment.setdefault(
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_PORT,
            ovmpcons.Defaults.DEFAULT_VMCONSOLE_PROXY_PORT
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            oenginecons.Stages.CORE_ENABLE,
        ),
    )
    def _customization(self):
        if self.environment[oenginecons.CoreEnv.ENABLE]:
            if self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ] is None:
                self.environment[
                    ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_CONFIG_VMCONSOLE_PROXY',
                    note=_(
                        'Configure VM Console Proxy on this host '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                )
            if self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ]:
                self.environment[
                    ovmpcons.ConfigEnv.VMCONSOLE_PROXY_STOP_NEEDED
                ] = True
        else:
            self.logger.info(_(
                'Deploying VM Console Proxy on a separate '
                'host is not supported'
            ))
            self.environment[ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG] = False

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
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
    )
    def _customizationNetwork(self):
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
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-vmconsole-proxy',
                'directory': 'vmconsole-proxy'
            },
        ])
        self.environment[
            osetupcons.NetEnv.FIREWALLD_SUBST
        ].update({
            '@VMCONSOLE_PROXY_PORT@': self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_PORT
            ],
        })

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
    )
    def _miscConfigVMConsoleHelper(self):
        content = (
            'ENGINE_CA={engine_apache_ca_cert}\n'
            'ENGINE_VERIFY_HOST={engine_verify_host}\n'
            'ENGINE_BASE_URL={engine_base_url}\n'
            'TOKEN_CERTIFICATE={certificate}\n'
            'TOKEN_KEY={key}\n'
        ).format(
            engine_apache_ca_cert=(
                oengcommcons.FileLocations.OVIRT_ENGINE_PKI_APACHE_CA_CERT
            ),
            # we skip host verification only if it is localhost
            engine_verify_host=self.environment[
                osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION
            ],
            engine_base_url=_base_url_from_env(self.environment),
            certificate=(
                ovmpcons.FileLocations.
                OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_CERT
            ),
            key=(
                ovmpcons.FileLocations.
                OVIRT_ENGINE_PKI_VMCONSOLE_PROXY_HELPER_KEY
            ),
        )

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    ovmpcons.FileLocations.
                    VMCONSOLE_PROXY_HELPER_VARS_SETUP
                ),
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
            ]
        ),
    )
    def _miscConfigVMConsoleProxy(self):
        with open(ovmpcons.FileLocations.OVIRT_VMCONSOLE_PROXY_CONFIG) as f:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=ovmpcons.FileLocations.VMCONSOLE_CONFIG,
                    content=f.read(),
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
