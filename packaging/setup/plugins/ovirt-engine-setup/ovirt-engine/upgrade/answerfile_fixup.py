#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Answer file fixup plugin."""


import gettext
import glob
import os

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Temporary fixup for invalid permission of past answer files."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):
        for f in glob.glob(
            os.path.join(
                osetupcons.FileLocations.OVIRT_SETUP_ANSWERS_DIR,
                '*.conf',
            )
        ):
            try:
                os.chmod(f, 0o600)
            except Exception:
                self.logger.debug(
                    "Cannot modify permission for '%s'",
                    f,
                    exc_info=True,
                )


# vim: expandtab tabstop=4 shiftwidth=4
