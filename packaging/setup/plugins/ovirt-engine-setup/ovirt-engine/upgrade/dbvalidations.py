#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


""" DB validations plugin."""


import gettext
import re

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons

from ovirt_setup_lib import dialog


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """ DB validations plugin."""

    def _dbUtil(self, fix=False):

        args = [
            oenginecons.FileLocations.OVIRT_ENGINE_DB_VALIDATOR,
            '--user={user}'.format(
                user=self.environment[
                    oenginecons.EngineDBEnv.USER
                ],
            ),
            '--host={host}'.format(
                host=self.environment[
                    oenginecons.EngineDBEnv.HOST
                ],
            ),
            '--port={port}'.format(
                port=self.environment[
                    oenginecons.EngineDBEnv.PORT
                ],
            ),
            '--database={database}'.format(
                database=self.environment[
                    oenginecons.EngineDBEnv.DATABASE
                ],
            ),
            '--log={logfile}'.format(
                logfile=self.environment[
                    otopicons.CoreEnv.LOG_FILE_NAME
                ],
            ),
        ]
        if fix:
            args.append('--fix')

        return self.execute(
            args,
            raiseOnError=False,
            envAppend={
                'DBFUNC_DB_PGPASSFILE': self.environment[
                    oenginecons.EngineDBEnv.PGPASS_FILE
                ]
            },
        )

    def _checkDb(self):

        rc, stdout, stderr = self._dbUtil()
        if rc != 0:
            exceptTextBase = _(
                'Failed checking Engine database: '
                'an exception occurred while validating the Engine'
                ' database, please check the logs for getting more info'
            )
            stderrLines = stderr
            for stderrLine in stderrLines:
                if re.match('^ERROR: ', stderrLine):
                    break
            else:
                stderrLine = stderrLines[0]

            if stderrLine != '':
                exceptText = _(
                    '{base}:\n{output}\n'.format(
                        base=exceptTextBase,
                        output=stderrLine,
                    )
                )
            else:
                exceptText = _(
                    '{base}.\n'.format(
                        base=exceptTextBase,
                    )
                )

            raise RuntimeError(exceptText)

        return (stdout, rc)

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            oenginecons.EngineDBEnv.FIX_DB_VIOLATIONS,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LOW,
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
        condition=lambda self: (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ]
        ),
    )
    def _validation(self):
        self.logger.info(
            _('Checking the Engine database consistency')
        )
        violations, issues_found = self._checkDb()
        if issues_found:
            if self.environment[
                oenginecons.EngineDBEnv.FIX_DB_VIOLATIONS
            ] is None:
                self.logger.warn(
                    _(
                        'The following inconsistencies were found '
                        'in Engine database: {violations}. '
                    ).format(
                        violations=violations,
                    ),
                )
                self.environment[
                    oenginecons.EngineDBEnv.FIX_DB_VIOLATIONS
                ] = dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_FIX_DB_VALIDATIONS',
                    note=_(
                        'Would you like to automatically clear '
                        'inconsistencies before upgraing?\n'
                        '(Answering no will stop the upgrade): '
                    ),
                    prompt=True,
                )

            if not self.environment[
                oenginecons.EngineDBEnv.FIX_DB_VIOLATIONS
            ]:
                raise RuntimeError(
                    _(
                        'Upgrade aborted, database integrity '
                        'cannot be established.'
                    )
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_EARLY_MISC,
        condition=lambda self: self.environment[
            oenginecons.EngineDBEnv.FIX_DB_VIOLATIONS
        ],
    )
    def _misc(self):
        self.logger.info(
            _('Fixing Engine database inconsistencies')
        )
        self._dbUtil(fix=True)


# vim: expandtab tabstop=4 shiftwidth=4
