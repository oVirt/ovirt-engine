#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Filter secrets plugin."""


from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Filter secrets plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        name=osetupcons.Stages.SECRETS_FILTERED_FROM_SETUP_ATTRS_MODULES,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
    )
    def _boot(self):
        secret_keys = []
        consts = []
        for constobj in self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ]:
            consts.extend(constobj.__dict__['__osetup_attrs__'])
        for c in consts:
            for k in c.__dict__.values():
                if (
                    hasattr(k, '__osetup_attrs__') and
                    k.__osetup_attrs__['is_secret']
                ):
                    k = k.fget(None)
                    secret_keys.append(k)

        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].extend(
            secret_keys
        )


# vim: expandtab tabstop=4 shiftwidth=4
