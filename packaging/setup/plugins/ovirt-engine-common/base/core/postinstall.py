#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""post install config file plugin."""


import gettext

from otopi import common
from otopi import constants as otopicons
from otopi import filetransaction
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """post install config file plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[osetupcons.CoreEnv.GENERATE_POSTINSTALL] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        priority=plugin.Stages.PRIORITY_LAST,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.GENERATE_POSTINSTALL
        ],
    )
    def _misc(self):
        self.logger.info(
            _("Generating post install configuration file '{name}'").format(
                name=osetupcons.FileLocations.OVIRT_SETUP_POST_INSTALL_CONFIG,
            )
        )
        content = [u'[environment:default]']
        consts = []
        for constobj in self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ]:
            consts.extend(constobj.__dict__['__osetup_attrs__'])
        for c in consts:
            for key in c.__dict__.values():
                if hasattr(key, '__osetup_attrs__'):
                    if key.__osetup_attrs__['postinstallfile']:
                        key = key.fget(None)
                        if key in self.environment:
                            value = self.environment[key]
                            content.append(
                                u'{key}={type}:{value}'.format(
                                    key=key,
                                    type=common.typeName(value),
                                    value=(
                                        u'\n '.join(value)
                                        # We want the next lines to be
                                        # indented, so that
                                        # configparser will treat them
                                        # as a single multi-line value.
                                        # So we join with '\n '.
                                        if isinstance(value, list)
                                        else value
                                    ),
                                )
                            )
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.OVIRT_SETUP_POST_INSTALL_CONFIG,
                content=content,
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
