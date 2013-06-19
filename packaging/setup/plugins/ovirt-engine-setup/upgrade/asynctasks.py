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


""" DB Async tasks handling plugin."""


import time
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import base


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database


from async_tasks_map import ASYNC_TASKS_MAP


@util.export
class Plugin(plugin.PluginBase):
    """ DB Async tasks handling plugin."""

    class _engineInMaintenance(base.Base):

        def __init__(
            self,
            parent,
            dbstatement,
        ):
            self._parent = parent
            self._origTimeout = 0
            self._dbstatement = dbstatement

        def _getCurrentTimeout(self):
            return self._dbstatement.getVDCOptions(
                name='AsyncTaskZombieTaskLifeInMinutes',
            )

        def _setEngineMode(self, maintenance, timeout=0):
            mode = (
                'MAINTENANCE' if maintenance
                else 'ACTIVE'
            )
            try:
                self.logger.debug(
                    'Setting engine into {mode} mode'.format(
                        value=mode,
                    )
                )
                self._dbstatement.updateVDCOptions(
                    {
                        'name': 'EngineMode',
                        'value': mode,
                    },
                    {
                        'name': 'AsyncTaskZombieTaskLifeInMinutes',
                        'value': timeout,
                    },
                )
            except Exception as e:
                self.logger.debug(
                    'Cannot set engine mode',
                    exc_info=True,
                )
                raise RuntimeError(
                    _(
                        'Failed to set engine to {mode} mode.\n'
                        'Error: {error}\n'
                        'Please check that engine is in correct state '
                        'and try running upgrade again.'
                    ).format(
                        mode=mode,
                        error=e,
                    )
                )

        def __enter__(self):
            self._origTimeout = self._getCurrentTimeout()
            self._setEngineMode(
                maintenance=True,
            )

            self._parent.services.state(
                name=self.environment[
                    osetupcons.Const.ENGINE_SERVICE_NAME
                ],
                state=True,
            )

        def __exit__(self, exc_type, exc_value, traceback):
            self._parent.services.state(
                name=self.environment[
                    osetupcons.Const.ENGINE_SERVICE_NAME
                ],
                state=False,
            )
            self._setEngineMode(
                maintenance=False,
                timeout=self._origTimeout,
            )

    def _clearZombieTasks(self):
        rc, tasks, stderr = self.execute(
            args=(
                osetupcons.FileLocations.OVIRT_ENGINE_TASKCLEANER,
                '-u', self.environment[osetupcons.DBEnv.USER],
                '-s', self.environment[osetupcons.DBEnv.HOST],
                '-p', str(self.environment[osetupcons.DBEnv.PORT]),
                '-d', self.environment[osetupcons.DBEnv.DATABASE],
                '-R',
                '-A',
                '-J',
                '-q',
            ),
            raiseOnError=False,
            envAppend={
                'ENGINE_PGPASS': self.environment[
                    osetupcons.DBEnv.PGPASS_FILE
                ]
            },
        )

        if rc:
            raise RuntimeError(
                _(
                    'Failed to clear zombie tasks. Please access support '
                    'in attempt to resolve the problem'
                )
            )

    def _getRunningTasks(self, dbstatement):

        tasks = dbstatement.execute(
            statement="""
                select
                async_tasks.action_type,
                async_tasks.task_id,
                async_tasks.started_at,
                storage_pool.name
                from async_tasks, storage_pool
                where async_tasks.storage_pool_id = storage_pool.id
            """,
            ownConnection=True,
            transaction=False,
        )

        return (
            [
                _(
                    'Task ID:           {task_id:30}\n'
                    'Task Name:         {task_name:30}\n'
                    'Task Description:  {task_desc:30}\n'
                    'Started at:        {started_at:30}\n'
                    'DC Name:           {name:30}'
                ).format(
                    task_id=entry['task_id'],
                    task_name=ASYNC_TASKS_MAP[entry['action_type']][0],
                    task_desc=ASYNC_TASKS_MAP[entry['action_type']][1],
                    started_at=entry['started_at'],
                    name=entry['name'],
                )
                for entry in tasks
            ]
        )

    def _getCompensations(self, dbstatement):

        compensations = dbstatement.execute(
            statement="""
                select command_type, entity_type
                from business_entity_snapshot
            """,
            ownConnection=True,
            transaction=False,
        )

        return (
            [
                '{command_type:30} {entity_type:30}'.format(**entry)
                for entry in compensations
            ]
        )

    def _askUserToStopTasks(self, runningTasks, compensations):
        self.dialog.note(
            text=_(
                'The following system tasks have been '
                'found running in the system:\n'
                '{tasks}'
            ).format(
                tasks='\n'.join(runningTasks),
            )
        )
        self.dialog.note(
            text=_(
                'The following compensations have been '
                'found running in the system:\n'
                '{compensations}'
            ).format(
                compensations='\n'.join(compensations),
            )
        )
        if not self.dialog.queryBoolean(
            dialog=self.dialog,
            name='OVESETUP_STOP_RUNNING_TASKS',
            note=_(
                'Would you like to try to stop these tasks automatically?\n'
                '(Answering "no" will stop the upgrade (@VALUES@) '
            ),
            prompt=True,
        ):
            raise RuntimeError(
                _(
                    'Upgrade cannot be completed; asynchronious tasks or '
                    'compensations are still running. Please make sure '
                    'that there are no running tasks before you '
                    'continue.'
                )
            )

    def _checkRunningTasks(self):
        dbstatement = database.Statement(self.environment)
        return (
            self._getRunningTasks(dbstatement),
            self._getCompensations(dbstatement),
        )

    def _waitForTasksToClear(self, dbstatement):
        with self._engineInMaintenance(
            dbstatement=dbstatement,
            parent=self,
        ):
            while True:
                runningTasks, compensations = self._checkRunningTasks()
                if (
                    not runningTasks and
                    not compensations
                ):
                    break

                self.dialog.note(
                    text=_(
                        'Waiting for the completion of {number} '
                        'running tasks during the next '
                        '{cleanup_wait} seconds.\n'
                        'Press Ctrl+C to interrupt. '
                    ).format(
                        cleanup_wait=self.environment[
                            osetupcons.AsyncTasksEnv.
                            CLEAR_TASKS_WAIT_PERIOD
                        ],
                        number=len(runningTasks + compensations),
                    )
                )
                time.sleep(
                    self.environment[
                        osetupcons.AsyncTasksEnv.
                        CLEAR_TASKS_WAIT_PERIOD
                    ]
                )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.AsyncTasksEnv.CLEAR_TASKS,
            True
        )
        self.environment.setdefault(
            osetupcons.AsyncTasksEnv.CLEAR_TASKS_WAIT_PERIOD,
            osetupcons.Defaults.DEFAULT_CLEAR_TASKS_WAIT_PERIOD
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self._enabled = self.environment[
            osetupcons.CoreEnv.ACTION
        ] == osetupcons.Const.ACTION_UPGRADE

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _validateEnv(self):
        self._enabled = (
            self._enabled and
            self.environment[
                osetupcons.AsyncTasksEnv.CLEAR_TASKS
            ]
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
        after=[
            osetupcons.Stages.DB_CREDENTIALS_AVAILABLE,
        ],
    )
    def _validateZombies(self):
        self.logger.info(
            _('Cleaning stale zombie tasks')
        )
        self._clearZombieTasks()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LOW,
        condition=lambda self: self._enabled,
    )
    def _validateAsyncTasks(self):
        self.logger.info(
            _('Cleaning async tasks and compensations')
        )
        runningTasks, compensations = self._checkRunningTasks()
        if runningTasks or compensations:
            self._askUserToStopTasks(runningTasks, compensations)
            dbstatement = database.Statement(self.environment)
            try:
                self._waitForTasksToClear(dbstatement)
            except KeyboardInterrupt:
                self.logger.error(
                    _(
                        'Upgrade cannot be completed; asynchronious tasks '
                        'or compensations are still running. Please make '
                        'sure that there are no running tasks before you '
                        'continue.'
                    )
                )
                raise RuntimeError(
                    _('Upgrade cannot be completed due to running tasks.')
                )


# vim: expandtab tabstop=4 shiftwidth=4
