#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
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


"""post install config file plugin."""


import gettext

from otopi import constants as otopicons
from otopi import common, filetransaction, plugin, util

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
        content = ['[environment:default]']
        consts = []
        for constobj in self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ]:
            consts.extend(constobj.__dict__['__osetup_attrs__'])
        for c in consts:
            for k in c.__dict__.values():
                if hasattr(k, '__osetup_attrs__'):
                    if k.__osetup_attrs__['postinstallfile']:
                        k = k.fget(None)
                        if k in self.environment:
                            v = self.environment[k]
                            content.append(
                                '%s=%s:%s' % (
                                    k,
                                    common.typeName(v),
                                    '\n'.join(v) if isinstance(v, list)
                                    else v,
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
