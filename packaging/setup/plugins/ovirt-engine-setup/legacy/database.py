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


"""Database legacy migration."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        before=[
            osetupcons.Stages.DB_CONNECTION_SETUP,
        ],
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
    )
    def _setup(self):
        if os.path.exists(
            osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE
        ):
            self.logger.debug('Existing database pgpass found')
            with open(
                osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE,
                'r',
            ) as f:
                for l in f:
                    l = l.rstrip('\n')
                    if ':%s:' % 'postgres' not in l:
                        d = l.split(':')
                        if len(d) == 5:
                            self.environment.update({
                                osetupcons.DBEnv.HOST: d[0],
                                osetupcons.DBEnv.PORT: int(d[1]),
                                osetupcons.DBEnv.SECURED: None,
                                osetupcons.DBEnv.SECURED_HOST_VALIDATION: None,
                                osetupcons.DBEnv.DATABASE: (
                                    d[2] if d[2] != '*'
                                    else
                                    osetupcons.Defaults.DEFAULT_DB_DATABASE
                                ),
                                osetupcons.DBEnv.USER: d[3],
                                osetupcons.DBEnv.PASSWORD: d[4],
                                osetupcons.DBEnv.NEW_DATABASE: False,
                            })
                            dbovirtutils = database.OvirtUtils(plugin=self)
                            dbovirtutils.tryDatabaseConnect()
                            if dbovirtutils.isNewDatabase():
                                raise RuntimeError(
                                    _(
                                        'Unexpected empty database '
                                        'during upgrade'
                                    )
                                )
                            break


# vim: expandtab tabstop=4 shiftwidth=4
