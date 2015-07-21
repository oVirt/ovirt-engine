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


"""Dockerc plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_setup_lib import dialog
from ovirt_engine_setup.dockerc import constants as odockerccons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Dockerc plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            odockerccons.RemoveEnv.REMOVE_DOCKERC,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = not self.environment[
            osetupcons.CoreEnv.DEVELOPER_MODE
        ] and self.environment[
            odockerccons.RemoveEnv.REMOVE_DCLIST
        ]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=odockerccons.Stages.REMOVE_CUSTOMIZATION_DOCKERC,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_PRODUCT_OPTIONS,
        ),
        condition=lambda self: self._enabled,
    )
    def _customization(self):
        if self.environment[
            osetupcons.RemoveEnv.REMOVE_ALL
        ]:
            self.environment[
                odockerccons.RemoveEnv.REMOVE_DOCKERC
            ] = True
        else:
            if self.environment[
                odockerccons.RemoveEnv.REMOVE_DOCKERC
            ] is None:
                self.environment[
                    odockerccons.RemoveEnv.REMOVE_DOCKERC
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_REMOVE_DOCKERC',
                    note=_(
                        'Do you want to remove the Setup-deployed\n'
                        'Docker containers ({clist})?\n'
                        'Data will be lost\n'
                        '(@VALUES@) [@DEFAULT@]: '
                    ).format(
                        clist=self.environment[
                            odockerccons.RemoveEnv.REMOVE_DCLIST
                        ]
                    ),
                    prompt=True,
                    true=_('Yes'),
                    false=_('No'),
                    default=False,
                )
        if self.environment[
            odockerccons.RemoveEnv.REMOVE_DOCKERC
        ]:
            self.environment[
                odockerccons.ConfigEnv.DOCKERC_NEEDED
            ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: (
            self.environment[odockerccons.RemoveEnv.REMOVE_DOCKERC] and
            self._enabled
        ),
    )
    def _misc(self):
        import docker
        dcli = docker.Client(base_url='unix://var/run/docker.sock')

        if self.environment[
            odockerccons.RemoveEnv.REMOVE_DCLIST
        ]:
            rlist = [
                x.strip()
                for x in self.environment[
                    odockerccons.RemoveEnv.REMOVE_DCLIST
                ].split(',')
                if x
            ]
        else:
            rlist = []

        for cont in rlist:
            self.logger.info(_('Stopping {cname}').format(cname=cont))
            try:
                dcli.stop(
                    container=cont,
                    timeout=60,
                )
            except docker.errors.APIError as ex:
                if ex.response.status_code == 404:
                    self.logger.warning(
                        _(
                            'Unable to stop {cname} container'
                        ).format(
                            cname=cont,
                        )
                    )
                else:
                    raise ex
            self.logger.info(_('Removing {cname}').format(cname=cont))
            try:
                dcli.remove_container(
                    container=cont,
                )
            except docker.errors.APIError as ex:
                if ex.response.status_code == 404:
                    self.logger.warning(
                        _(
                            'Unable to remove {cname} container'
                        ).format(
                            cname=cont,
                        )
                    )
                else:
                    raise ex

        still_deployed = [
            str(name).lstrip('/')
            for d in dcli.containers(all=True)
            for name in d['Names']
        ]

        if still_deployed:
            self.logger.info(
                _(
                    'Keeping docker enabled and running cause other '
                    'containers are still present:\n'
                    '{clist}'
                ).format(
                    clist=', '.join(still_deployed),
                )
            )
        else:
            self.logger.info(_('Stopping and disabling docker'))
            if self.services.exists(
                name=odockerccons.Const.DOCKER_SERVICE_NANE
            ):
                self.services.startup(
                    name=odockerccons.Const.DOCKER_SERVICE_NANE,
                    state=False,
                )
                self.services.state(
                    name=odockerccons.Const.DOCKER_SERVICE_NANE,
                    state=False,
                )

        self.environment[
            odockerccons.RemoveEnv.REMOVE_DCLIST
        ] = ''

# vim: expandtab tabstop=4 shiftwidth=4
