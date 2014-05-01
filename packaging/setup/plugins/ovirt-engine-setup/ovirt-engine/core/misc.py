#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2014 Red Hat, Inc.
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


"""Misc plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import dialog
from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


@util.export
class Plugin(plugin.PluginBase):
    """Misc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(oenginecons.CoreEnv.ENABLE, None)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=oenginecons.Stages.CORE_ENABLE,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
        ),
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _customization(self):
        if self.environment[oenginecons.CoreEnv.ENABLE] is None:
            if not self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]:
                # we are upgrading from 3.4 that doesn't ask about it
                # but we need to upgrade the engine cause has been configured
                # in the past
                # It can be removed in future release ( > 3.5)
                self.environment[oenginecons.CoreEnv.ENABLE] = True
            else:
                self.environment[
                    oenginecons.CoreEnv.ENABLE
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_ENGINE_ENABLE',
                    note=_(
                        'Configure Engine on this host '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                )
        if self.environment[oenginecons.CoreEnv.ENABLE]:
            self.environment[oengcommcons.ApacheEnv.ENABLE] = True


# vim: expandtab tabstop=4 shiftwidth=4
