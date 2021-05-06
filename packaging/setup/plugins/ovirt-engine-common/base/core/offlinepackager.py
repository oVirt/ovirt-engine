#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Fake packager for offline mode"""


import gettext

from otopi import constants as otopicons
from otopi import packager
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase, packager.PackagerBase):
    """Offline packager."""
    def install(self, packages, ignoreErrors=False):
        pass

    def update(self, packages, ignoreErrors=False):
        pass

    def queryPackages(self, patterns=None):
        if patterns == ['vdsm']:
            return [
                {
                    'operation': 'installed',
                    'display_name': 'vdsm',
                    'name': 'vdsm',
                    'version': '999.9.9',
                    'release': '1',
                    'epoch': '0',
                    'arch': 'noarch',
                },
            ]
        else:
            return []

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        after=(
            otopicons.Stages.PACKAGERS_DETECTION,
        ),
    )
    def _init(self):
        if self.environment.setdefault(
            osetupcons.CoreEnv.OFFLINE_PACKAGER,
            (
                self.environment[osetupcons.CoreEnv.DEVELOPER_MODE] or
                not osetuputil.is_ovirt_packaging_supported_distro()
            ),
        ):
            self.logger.debug('Registering offline packager')
            self.context.registerPackager(packager=self)


# vim: expandtab tabstop=4 shiftwidth=4
