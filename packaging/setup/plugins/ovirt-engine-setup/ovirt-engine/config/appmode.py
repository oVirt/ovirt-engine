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


"""Application mode plugin."""


import gettext

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """Application mode plugin."""

    class ApplicationMode(object):
        VirtOnly = 1
        GlusterOnly = 2
        AllModes = 255

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.APPLICATION_MODE,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
            osetupcons.Stages.CONFIG_APPLICATION_MODE_AVAILABLE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
        ),
        condition=lambda self: self.environment[
            oenginecons.EngineDBEnv.NEW_DATABASE
        ],
        name=oenginecons.Stages.APPMODE_ALLOWED,
    )
    def _customization_enable(self):
        self._enabled = self.environment[oenginecons.CoreEnv.ENABLE]

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=(
            oengcommcons.Stages.DIALOG_TITLES_E_ENGINE,
        ),
        after=(
            oengcommcons.Stages.DIALOG_TITLES_S_ENGINE,
            oenginecons.Stages.APPMODE_ALLOWED,
        ),
        condition=lambda self: self._enabled,
        name=osetupcons.Stages.CONFIG_APPLICATION_MODE_AVAILABLE,
    )
    def _customization(self):
        if self.environment[
            osetupcons.ConfigEnv.APPLICATION_MODE
        ] is None:
            self.environment[
                osetupcons.ConfigEnv.APPLICATION_MODE
            ] = self.dialog.queryString(
                name='OVESETUP_CONFIG_APPLICATION_MODE',
                note=_('Application mode (@VALUES@) [@DEFAULT@]: '),
                prompt=True,
                validValues=(
                    'Virt',
                    'Gluster',
                    'Both',
                ),
                caseSensitive=False,
                default=(
                    oenginecons.Defaults.
                    DEFAULT_CONFIG_APPLICATION_MODE
                ),
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self._enabled,
    )
    def _misc(self):

        v = self.environment[osetupcons.ConfigEnv.APPLICATION_MODE]

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select inst_update_service_type(
                    %(clusterId)s,
                    %(virt)s,
                    %(gluster)s
                )
            """,
            args=dict(
                clusterId=vdcoption.VdcOption(
                    statement=self.environment[
                        oenginecons.EngineDBEnv.STATEMENT
                    ]
                ).getVdcOption(name='AutoRegistrationDefaultClusterID'),
                virt=(v in ('both', 'virt')),
                gluster=(v == 'gluster'),
            ),
        )

        if v == 'virt':
            mode = self.ApplicationMode.VirtOnly
        elif v == 'gluster':
            mode = self.ApplicationMode.GlusterOnly
        elif v == 'both':
            mode = self.ApplicationMode.AllModes
        else:
            raise RuntimeError(
                _('Selected application mode \'{v}\' is not allowed').format(
                    v=v,
                )
            )

        self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
            statement="""
                select fn_db_update_config_value(
                    'ApplicationMode',
                    %(mode)s,
                    'general'
                )
            """,
            args=dict(
                mode=str(mode),
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
