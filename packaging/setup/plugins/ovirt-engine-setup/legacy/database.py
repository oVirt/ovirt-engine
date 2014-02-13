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


from otopi import constants as otopicons
from otopi import filetransaction
from otopi import util
from otopi import plugin


from ovirt_engine import configfile


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
        before=(
            osetupcons.Stages.LEGACY_CORE_INIT,
        ),
    )
    def _init(self):
        self.environment[
            osetupcons.CoreEnv.LEGACY_PG_CREDS_FOUND
        ] = False
        if os.path.exists(
            osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE
        ):
            self.logger.debug('Existing database pgpass found')
            config = configfile.ConfigFile([
                osetupcons.FileLocations.LEGACY_OVIRT_ENGINE_SYSCONFIG
            ])
            legacy_user = config.get('ENGINE_DB_USER')
            self.logger.debug('legacy ENGINE_DB_USER: %s' % legacy_user)

            #
            # we need client side psql library
            # version as at least in rhel for 8.4
            # the password within pgpassfile is
            # not escaped.
            # the simplest way is to checkout psql
            # utility version.
            #
            # we are at too early stage to use commands
            # so we hardcode path in this case.
            #
            psql = self.command.get('psql', optional=True)
            if not psql:
                psql = '/usr/bin/psql'
            rc, stdout, stderr = self.execute(
                args=(
                    psql,
                    '-V',
                ),
            )
            plainPassword = ' 8.' in stdout[0]
            with open(
                osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE,
                'r',
            ) as f:
                for l in f:
                    if plainPassword:
                        d = l.rstrip('\n').split(':', 4)
                    else:
                        if l and l[-1] != '\n':
                            l += '\n'
                        d = []
                        escape = False
                        s = ''
                        for c in l:
                            if escape:
                                escape = False
                                s += c
                            else:
                                if c == ':' or c == '\n':
                                    d.append(s)
                                    s = ''
                                elif c == '\\':
                                    escape = True
                                else:
                                    s += c
                    if len(d) == 5 and d[3] == legacy_user:
                        self._dbenv = {
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
                        }
                        self.environment[
                            osetupcons.CoreEnv.LEGACY_PG_CREDS_FOUND
                        ] = True
                        break

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        before=(
            osetupcons.Stages.DB_CONNECTION_SETUP,
        ),
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
    )
    def _setup(self):
        self.environment.update(self._dbenv)
        self.environment[
            otopicons.CoreEnv.LOG_FILTER
        ].append(
            self.environment[osetupcons.DBEnv.PASSWORD]
        )
        dbovirtutils = database.OvirtUtils(plugin=self)
        dbovirtutils.tryDatabaseConnect()
        if dbovirtutils.isNewDatabase():
            raise RuntimeError(
                _('Unexpected empty database during upgrade')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            osetupcons.CoreEnv.UPGRADE_FROM_LEGACY
        ],
    )
    def _misc(self):
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=(
                    osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE
                ),
                content='',
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
