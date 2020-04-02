#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


""" DB Async tasks handling plugin."""


import gettext
import time

from otopi import base
from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import database

from ovirt_setup_lib import dialog

from .async_tasks_map import ASYNC_TASKS_MAP


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


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
            self._dbstatement = dbstatement

        def _setEngineMode(self, maintenance, timeout=0):
            mode = (
                'MAINTENANCE' if maintenance
                else 'ACTIVE'
            )
            try:
                self._parent.logger.debug(
                    'Setting engine into {mode} mode'.format(
                        mode=mode,
                    )
                )

                vdcoption.VdcOption(
                    statement=self._dbstatement,
                ).updateVdcOptions(
                    options=(
                        {
                            'name': 'EngineMode',
                            'value': mode,
                        },
                    ),
                    ownConnection=True,
                )
            except Exception as e:
                self._parent.logger.debug(
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
            self._setEngineMode(
                maintenance=True,
            )

        def __exit__(self, exc_type, exc_value, traceback):
            self._setEngineMode(
                maintenance=False,
            )

    def _clearZombies(self):
        _args_base = (
            oenginecons.FileLocations.OVIRT_ENGINE_TASKCLEANER,
            '-l', self.environment[otopicons.CoreEnv.LOG_FILE_NAME],
            '-q',
        )
        envPwd = {
            'DBFUNC_DB_PGPASSFILE': self.environment[
                oenginecons.EngineDBEnv.PGPASS_FILE
            ]
        }

        # removing zombie commands if present
        _args_commands = _args_base + ('-r', '-Z',)
        rc, tasks, stderr = self.execute(
            args=_args_commands,
            raiseOnError=False,
            envAppend=envPwd,
        )
        if rc:
            raise RuntimeError(
                _(
                    'Failed to clear zombie commands. '
                    'Please access support in attempt to resolve '
                    'the problem\n'
                    'Error: {error}\n'
                ).format(
                    error=stderr
                )
            )

        # than remove zombie tasks ans related jobs and compensation entries
        _args_tasks = _args_base + ('-R', '-z', '-A',)
        rc, tasks, stderr = self.execute(
            args=_args_tasks,
            raiseOnError=False,
            envAppend=envPwd,
        )
        if rc:
            raise RuntimeError(
                _(
                    'Failed to clear zombie tasks. '
                    'Please access support in attempt to resolve '
                    'the problem\n'
                    'Error: {error}\n'
                ).format(
                    error=stderr
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

        def taskinfo(task):
            return ASYNC_TASKS_MAP.get(
                str(task['action_type']),
                ['Unknown', 'Unknown']
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
                    task_name=taskinfo(entry)[0],
                    task_desc=taskinfo(entry)[1],
                    started_at=entry['started_at'],
                    name=entry['name'],
                )
                for entry in tasks
            ]
        )

    def _getCommandEntitiesTableExists(self, dbstatement):

        command_entities = dbstatement.execute(
            statement="""
                select relname
                from pg_class
                where relname = 'command_entities'
            """,
            ownConnection=True,
            transaction=False,
        )

        return (
            [
                _(
                    'Relname:           {relname:30}'
                ).format(
                    relname=entry['relname'],
                )
                for entry in command_entities
            ]
        )

    def _getRunningCommands(self, dbstatement):

        if not self._getCommandEntitiesTableExists(dbstatement):
            return []

        commands = dbstatement.execute(
            statement="""
                select
                command_entities.command_type,
                command_entities.command_id,
                command_entities.created_at,
                command_entities.status
                from command_entities
                where command_entities.callback_enabled = 'true'
                  and command_entities.callback_notified = 'false'
            """,
            ownConnection=True,
            transaction=False,
        )

        return (
            [
                _(
                    'Command ID:           {command_id:30}\n'
                    'Command Type:         {command_type:30}\n'
                    'Created at:           {created_at:30}\n'
                    'Status:               {status:30}'
                ).format(
                    command_id=entry['command_id'],
                    command_type=entry['command_type'],
                    created_at=entry['created_at'],
                    status=entry['status'],
                )
                for entry in commands
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

    def _askUserToWaitForTasks(
        self,
        runningTasks,
        runningCommands,
        compensations,
    ):
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
                'The following commands have been '
                'found running in the system:\n'
                '{commands}'
            ).format(
                commands='\n'.join(runningCommands),
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
        if not dialog.queryBoolean(
            dialog=self.dialog,
            name='OVESETUP_WAIT_RUNNING_TASKS',
            note=_(
                'Would you like to try to wait for that?\n'
                '(Answering "no" will stop the upgrade (@VALUES@) '
            ),
            prompt=True,
        ):
            raise RuntimeError(
                _(
                    'Upgrade cannot be completed; asynchronious tasks or '
                    'commands or compensations are still running. Please '
                    'make sure that there are no running tasks before you '
                    'continue.'
                )
            )

    def _checkRunningTasks(self):
        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        return (
            self._getRunningTasks(dbstatement),
            self._getRunningCommands(dbstatement),
            self._getCompensations(dbstatement),
        )

    def _unlockAll(self):
        args = (
            oenginecons.FileLocations.OVIRT_ENGINE_UNLOCK_ENTITY,
            '-t', 'all',
            '-l', self.environment[otopicons.CoreEnv.LOG_FILE_NAME],
            '-i',
            '-f',
        )
        envPwd = {
            'DBFUNC_DB_PGPASSFILE': self.environment[
                oenginecons.EngineDBEnv.PGPASS_FILE
            ]
        }
        rc, tasks, stderr = self.execute(
            args=args,
            raiseOnError=False,
            envAppend=envPwd,
        )
        if rc:
            raise RuntimeError(
                _(
                    'Failed to unlock entities. '
                    'Please access support in attempt to resolve '
                    'the problem\n'
                    'Error: {error}\n'
                ).format(
                    error=stderr
                )
            )

    def _waitForTasksToClear(self):
        while True:
            (
                runningTasks,
                runningCommands,
                compensations,
            ) = self._checkRunningTasks()
            if (
                not runningTasks and
                not runningCommands and
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
                        oenginecons.AsyncTasksEnv.
                        CLEAR_TASKS_WAIT_PERIOD
                    ],
                    number=(
                        len(runningTasks) +
                        len(runningCommands) +
                        len(compensations)
                    ),
                )
            )
            if (
                not self.services.exists(
                    name=oenginecons.Const.ENGINE_SERVICE_NAME
                ) or not self.services.status(
                    name=oenginecons.Const.ENGINE_SERVICE_NAME
                )
            ):
                self.dialog.note(
                    text=_(
                        'Please note that the engine is currently down. you '
                        'may want to try to start it using the following '
                        'command:\n'
                        '# service {service} start\n'
                    ).format(
                        service=oenginecons.Const.ENGINE_SERVICE_NAME,
                    )
                )
            time.sleep(
                self.environment[
                    oenginecons.AsyncTasksEnv.
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
            oenginecons.AsyncTasksEnv.CLEAR_TASKS,
            True
        )
        self.environment.setdefault(
            oenginecons.AsyncTasksEnv.CLEAR_TASKS_WAIT_PERIOD,
            oenginecons.Defaults.DEFAULT_CLEAR_TASKS_WAIT_PERIOD
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_HIGH,
    )
    def _validateEnv(self):
        self._enabled = (
            self.environment[oenginecons.CoreEnv.ENABLE] and
            not self.environment[
                oenginecons.EngineDBEnv.NEW_DATABASE
            ] and
            self.environment[
                oenginecons.AsyncTasksEnv.CLEAR_TASKS
            ] and
            not self.environment[
                osetupcons.CoreEnv.DEVELOPER_MODE
            ]
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self._enabled,
        after=(
            oengcommcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
    )
    def _validateZombies(self):
        self.logger.info(
            _('Cleaning stale zombie tasks and commands')
        )
        self._clearZombies()

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LOW,
        condition=lambda self: self._enabled,
    )
    def _validateAsyncTasks(self):
        self.logger.info(
            _('Cleaning async tasks and compensations')
        )

        (
            runningTasks,
            runningCommands,
            compensations,
        ) = self._checkRunningTasks()

        dbstatement = database.Statement(
            dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
            environment=self.environment,
        )
        with self._engineInMaintenance(
            dbstatement=dbstatement,
            parent=self,
        ):
            if runningTasks or runningCommands or compensations:
                self._askUserToWaitForTasks(
                    runningTasks,
                    runningCommands,
                    compensations,
                )
                try:
                    self._waitForTasksToClear()
                except KeyboardInterrupt:
                    self.logger.error(
                        _(
                            'Upgrade cannot be completed; asynchronious tasks '
                            'or commands or compensations are still running. '
                            'Please make sure that there are no running tasks '
                            'before you continue.'
                        )
                    )
                    raise RuntimeError(
                        _('Upgrade cannot be completed due to running tasks.')
                    )
            self.logger.info(
                _('Unlocking existing entities')
            )
            self._unlockAll()


# vim: expandtab tabstop=4 shiftwidth=4
