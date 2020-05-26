#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Config plugin."""


import gettext


from urllib.parse import urlparse
from urllib.parse import urlunparse

from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.vmconsole_proxy_helper import constants as ovmpcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Config plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._config = ovmpcons.FileLocations.VMCONSOLE_PROXY_HELPER_VARS_SETUP

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
    )
    def _setup(self):
        self.environment[
            osetupcons.RenameEnv.FILES_TO_BE_MODIFIED
        ].append(
            self._config
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            ovmpcons.ConfigEnv.VMCONSOLE_PROXY_CONFIG
        ],
    )
    def _misc(self):
        with open(self._config, 'r') as f:
            content = []
            key = 'ENGINE_BASE_URL'
            for line in f:
                line = line.rstrip('\n')
                if line.startswith('%s=' % key):
                    u = urlparse(line[len('%s=' % key):])
                    ulist = list(u)
                    ulist[1] = self.environment[osetupcons.RenameEnv.FQDN] + (
                        ':' + str(u.port) if u.port
                        else ''
                    )
                    line = '{key}={url}'.format(
                        key=key,
                        url=urlunparse(ulist),
                    )
                content.append(line)

        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=self._config,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
