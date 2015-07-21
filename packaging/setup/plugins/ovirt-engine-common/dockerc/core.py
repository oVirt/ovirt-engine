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


"""dockerc plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.dockerc import constants as odockerccons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """dockerc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            odockerccons.RemoveEnv.REMOVE_DCLIST,
            None
        )
        self.environment.setdefault(
            odockerccons.ConfigEnv.DOCKERC_DAEMON,
            None
        )
        self.environment.setdefault(
            odockerccons.ConfigEnv.DOCKERC_NEEDED,
            False
        )
        self.environment.setdefault(
            odockerccons.ConfigEnv.DOCKERC_CTAG,
            odockerccons.Const.DEFAULT_CTAG
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ].append(odockerccons)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_PRODUCT_OPTIONS,
            odockerccons.Stages.DOCKERC_CUSTOMIZE,
            odockerccons.Stages.REMOVE_CUSTOMIZATION_DOCKERC,
        ),
        condition=lambda self: (
            self.environment[
                odockerccons.ConfigEnv.DOCKERC_NEEDED
            ] and
            (
                self.environment[
                    odockerccons.ConfigEnv.DOCKERC_CINDER
                ] or
                self.environment[
                    odockerccons.ConfigEnv.DOCKERC_GLANCE
                ]
            ) and
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ]
        ),
    )
    def _customization(self):

        if self.services.status(
            name=odockerccons.Const.DOCKER_SERVICE_NANE,
        ):
            self.logger.info(_('Found a running docker daemon'))
        else:
            self.logger.info(_('Unable to find an active docker daemon'))
            if self.environment[
                odockerccons.ConfigEnv.DOCKERC_DAEMON
            ] is None:
                self.environment[
                    odockerccons.ConfigEnv.DOCKERC_DAEMON
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_CONFIG_DOCKERC_DAEMON',
                    note=_(
                        'To continue with this setup the docker daemon '
                        'should be active.\n'
                        'Would you like to start it and continue with Setup? '
                        '(@VALUES@) [@DEFAULT@]: '
                    ),
                    prompt=True,
                    default=True,
                )
            if self.environment[
                odockerccons.ConfigEnv.DOCKERC_DAEMON
            ]:
                self.logger.info(_('Starting Docker'))
                self.services.state(
                    name=odockerccons.Const.DOCKER_SERVICE_NANE,
                    state=True,
                )
            else:
                raise RuntimeError(
                    _('Docker daemon is required to complete this setup')
                )


# vim: expandtab tabstop=4 shiftwidth=4
