#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
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


"""Engine plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """Engine plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.REMOVE,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
    )
    def _validation(self):
        if self.environment[
            osetupcons.CoreEnv.REMOVE
        ] is None:
            self.environment[
                osetupcons.CoreEnv.REMOVE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CORE_REMOVE',
                note=_(
                    'ovirt-engine is about to be removed, data will '
                    'be lost (@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                true=_('OK'),
                false=_('Cancel'),
                default=False,
            )

        if not self.environment[osetupcons.CoreEnv.REMOVE]:
            raise RuntimeError(
                _('Aborted by user')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        self.services.startup(
            name=osetupcons.Const.ENGINE_SERVICE_NAME,
            state=False,
        )


# vim: expandtab tabstop=4 shiftwidth=4
