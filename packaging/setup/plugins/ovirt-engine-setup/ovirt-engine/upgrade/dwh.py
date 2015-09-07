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


"""dwh plugin."""


import gettext
import time

from otopi import plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database, dwh_history_timekeeping


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """dwh plugin."""

    RETRIES = 30
    DELAY = 2

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    def _update_DisconnectDwh(self, value):
        vdcoption.VdcOption(
            statement=self._statement,
        ).updateVdcOptions(
            options=(
                {
                    'name': 'DisconnectDwh',
                    'value': value,
                },
            ),
            # Can't use ownConnection=False. The shared connection is
            # created only after the db is updated, and we want to stop
            # dwhd before that.
            # ownConnection=True means we are not rolled back on error,
            # if one happens between setting DisconnectDwh to 1 and
            # setting it back to 0, so an attempt to start dwh before
            # fixing the problem and finishing the engine upgrade will
            # fail, which is ok.
            ownConnection=True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self._remote_dwh_stopped = False

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        before=(
            oengcommcons.Stages.DB_SCHEMA,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[oenginecons.EngineDBEnv.NEW_DATABASE]
        ),
    )
    def _misc_stop_remote_dwh(self):
        self._statement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        self._dwh_host = dwh_history_timekeeping.getValueFromTimekeeping(
            statement=self._statement,
            name=dwh_history_timekeeping.DB_KEY_HOSTNAME
        )
        if dwh_history_timekeeping.dwhIsUp(self._statement):
            self.logger.info(
                _(
                    'Stopping DWH service on host {hostname}...'
                ).format(
                    hostname=self._dwh_host,
                )
            )
            try:
                self._update_DisconnectDwh('1')
                retries = self.RETRIES
                while dwh_history_timekeeping.dwhIsUp(
                    self._statement
                ) and retries > 0:
                    retries -= 1
                    self.logger.debug(
                        'Waiting for remote dwh to die, %s retries left' %
                        retries
                    )
                    time.sleep(self.DELAY)
            finally:
                self._update_DisconnectDwh('0')
            if dwh_history_timekeeping.dwhIsUp(self._statement):
                self.logger.error(
                    _(
                        '{service} is currently running.\n'
                        'Its hostname is {hostname}.\n'
                        'Please stop it before running Setup.'
                    ).format(
                        service=oengcommcons.DWHConst.ENGINE_DWHD_SERVICE_NAME,
                        hostname=self._dwh_host,
                    )
                )
                raise RuntimeError(_('{service} is currently running').format(
                    service=oengcommcons.DWHConst.ENGINE_DWHD_SERVICE_NAME,
                ))
            self.logger.info(_('Stopped DWH'))
            self._remote_dwh_stopped = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_SUMMARY,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_SUMMARY,
        ),
        condition=lambda self: self._remote_dwh_stopped,
    )
    def _closeup(self):
        self.dialog.note(
            text=_(
                "Please start the DWH service on the host '{hostname}'."
            ).format(
                hostname=self._dwh_host,
            ),
        )


# vim: expandtab tabstop=4 shiftwidth=4
