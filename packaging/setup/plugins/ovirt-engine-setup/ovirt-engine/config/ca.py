#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""CA plugin."""


import gettext

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine import util as outil

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """CA plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.CONFIG_DB_ENCRYPTION_AVAILABLE,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
            oenginecons.Stages.CA_AVAILABLE,
            oenginecons.Stages.QEMU_CA_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        uninstall_files = []
        self.environment[
            osetupcons.CoreEnv.REGISTER_UNINSTALL_GROUPS
        ].createGroup(
            group='ca_config',
            description='PKI configuration',
            optional=True,
        ).addFiles(
            'ca_config',
            uninstall_files,
        )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    oenginecons.FileLocations.
                    OVIRT_ENGINE_SERVICE_CONFIG_PKI
                ),
                mode=0o640,
                owner=self.environment[oengcommcons.SystemEnv.USER_ROOT],
                group=self.environment[osetupcons.SystemEnv.GROUP_ENGINE],
                enforcePermissions=True,
                content=(
                    'ENGINE_PKI="{pki_dir}"\n'
                    'ENGINE_PKI_CA="{ca}"\n'
                    'ENGINE_PKI_ENGINE_CERT="{engine_cert}"\n'
                    'ENGINE_PKI_TRUST_STORE="{trust_store}"\n'
                    'ENGINE_PKI_TRUST_STORE_PASSWORD=' + (
                        '"{trust_store_password}"\n'
                    ) +
                    'ENGINE_PKI_ENGINE_STORE="{engine_store}"\n'
                    'ENGINE_PKI_ENGINE_STORE_PASSWORD=' + (
                        '"{engine_store_password}"\n'
                    ) +
                    'ENGINE_PKI_ENGINE_STORE_ALIAS="{engine_store_alias}"\n'
                    'ENGINE_HTTPS_PKI_TRUST_STORE=' + (
                        '"${{ENGINE_PKI_TRUST_STORE}}"\n'
                    ) +
                    'ENGINE_HTTPS_PKI_TRUST_STORE_PASSWORD=' + (
                        '"${{ENGINE_PKI_TRUST_STORE_PASSWORD}}"\n'
                    )
                ).format(
                    pki_dir=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKIDIR
                    ),
                    ca=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CA_CERT
                    ),
                    engine_cert=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CERT
                    ),
                    trust_store=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_TRUST_STORE
                    ),
                    trust_store_password=outil.escape(
                        oenginecons.Const.PKI_PASSWORD,
                        '"\\$',
                    ),
                    engine_store=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_STORE
                    ),
                    engine_store_password=outil.escape(
                        oenginecons.Const.PKI_PASSWORD,
                        '"\\$',
                    ),
                    engine_store_alias='1',
                ),
                modifiedList=uninstall_files,
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
