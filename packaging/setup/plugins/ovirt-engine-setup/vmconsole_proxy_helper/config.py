#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015 Red Hat, Inc.
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


"""vmconsole proxy configuration plugin."""

import os.path

import gettext

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import hostname as osetuphostname
from ovirt_engine_setup import dialog
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons


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


def _existsUserGroup(log, user, group):
    try:
        osetuputil.getUid(user)
    except (KeyError, IndexError):
        log.warn(_('User {user} does not exist.'.format(user=user)))
        return False

    try:
        osetuputil.getGid(group)
    except (KeyError, IndexError):
        log.warn(_('Group {group} does not exist.'.format(group=group)))
        return False

    return True


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
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_PORT,
            None
        )
        self.environment.setdefault(
            ovmpcons.EngineConfigEnv.ENGINE_FQDN,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=ovmpcons.Stages.CONFIG_VMCONSOLE_ENGINE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
    )
    def _customization(self):
        # TODO:
        # Currently engine-setup does the configuration when
        # ovirt-vmconsole-proxy is installed, and it is installed in the
        # same host as Engine. This is ensured checking for ovirt-vmconsole
        # user/group above.
        #
        # We should not depend (and we don't yet) on ovirt-vmconsole-proxy in
        # rpm spec because the serial console support is optional:
        # if it is there and it isenabled, then will be configured,
        # and skipped in any other case.
        #
        # Manual setup instructions will be provided on the feature page:
        # http://www.ovirt.org/Features/Serial_Console
        if not _existsUserGroup(
            self.logger,
            ovmpcons.Const.OVIRT_VMCONSOLE_USER,
            ovmpcons.Const.OVIRT_VMCONSOLE_GROUP
        ):
            self.logger.warning(
                _(
                    'VM Console Proxy seems not installed on this host. '
                    'Disabled configuration.'
                )
            )
            enabled = False

        elif self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ] is None:
            enabled = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONFIG_VMCONSOLE_PROXY',
                note=_(
                    'Configure VM Console Proxy on this host '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=True,
            )

        self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ] = enabled

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
            envkey=ovmpcons.EngineConfigEnv.ENGINE_FQDN,
            whichhost=_('the engine'),
            supply_default=True,
        )

        while self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_PORT
        ] is None:
            try:
                port = osetuputil.parsePort(
                    self.dialog.queryString(
                        name='VMCONSOLE_PROXY_HOST',
                        note=_(
                            'Engine vmconsole port [@DEFAULT@]: '
                        ),
                        prompt=True,
                        default=(
                            ovmpcons.Defaults.
                            DEFAULT_VMCONSOLE_PROXY_PORT
                        ),
                    )
                )
                self.environment[
                    ovmpcons.ConfigEnv.VMCONSOLE_PROXY_PORT
                ] = port
            except ValueError:
                self.logger.warning(
                    _(
                        'Unable to parse the given port.'
                    )
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            ovmpcons.Stages.CONFIG_VMCONSOLE_ENGINE_CUSTOMIZATION,
        ),
    )
    def _customizationFirewall(self):
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
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=ovmpcons.Stages.CONFIG_VMCONSOLE_PROXY_CUSTOMIZATION,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
            ovmpcons.Stages.CONFIG_VMCONSOLE_ENGINE_CUSTOMIZATION,
        ),
    )
    def customizationVMConsoleProxy(self):
        if self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ]:
            configd_path = self.dialog.queryString(
                name='VMCONSOLE_PROXY_CONFIGD_PATH',
                note=_(
                    'ovirt-vmconsole-proxy config '
                    'directory path [@DEFAULT@]: '
                ),
                prompt=True,
                default=ovmpcons.FileLocations.OVIRT_VMCONSOLE_PROXY_CONFIGD
            )

            self.environment[
                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIGD_PATH
            ] = configd_path

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
    )
    def _miscConfigVMConsoleHelper(self):
        content = (
            'ENGINE_BASE_URL={engine_base_url}\n'
            'TOKEN_CERTIFICATE={certificate}\n'
            'TOKEN_KEY={key}\n'
        ).format(
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
                    OVIRT_ENGINE_VMCONSOLE_PROXY_CONFIG_SETUP
                ),
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
    )
    def _miscConfigVMConsoleProxy(self):

        content = (
            "[proxy]\n"
            "key_list = exec {helper} --version {version} keys\n"
            "console_list = exec {helper} --version {version} "
            "consoles --entityid {entityid}\n"
        ).format(
            helper=(
                ovmpcons.FileLocations.OVIRT_VMCONSOLE_PROXY_ENGINE_HELPER
            ),
            version='{version}',  # pass through to vmconsole
            entityid='{entityid}'  # ditto
        )

        # shortcut
        conf_file = os.path.join(
            ovmpcons.FileLocations.ENGINE_VMCONSOLE_PROXY_DIR,
            ovmpcons.FileLocations.
            OVIRT_VMCONSOLE_PROXY_CONFIG_ENGINE_SETUP_FILE
        )

        # make a copy in engine's dir as reference
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=conf_file,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )

        if not self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=os.path.join(
                        self.environment[
                            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIGD_PATH
                        ],
                        ovmpcons.FileLocations.
                        OVIRT_VMCONSOLE_PROXY_CONFIG_ENGINE_SETUP_FILE
                    ),
                    content=content,
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )
        else:
            self.dialog.note(
                _(
                    'Manual intervention is required, because '
                    'setup was run under unprivileged user.\n'
                    'Cannot copy ovirt-vmconsole config snippet '
                    'into the proper directory.\n'
                    'Please make sure to run as root:\n'
                    '\n'
                    '{cp_cmd}'.format(
                        cp_cmd='cp -p {conf_file} {vmconsole_confdir}'.format(
                            conf_file=conf_file,
                            vmconsole_confdir=self.environment[
                                ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIGD_PATH
                            ],
                        )
                    )
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
