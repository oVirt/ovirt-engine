/************************************************************************************************
Drop stuff created by taskcleaner_sp.sql
************************************************************************************************/
DROP FUNCTION IF EXISTS DeleteAllCommands();
DROP FUNCTION IF EXISTS DeleteAllCommandsWithRunningTasks();
DROP FUNCTION IF EXISTS DeleteAllCommandsWithZombieTasks();
DROP FUNCTION IF EXISTS DeleteAllEntitySnapshot();
DROP FUNCTION IF EXISTS DeleteAllJobs();
DROP FUNCTION IF EXISTS DeleteAsyncTaskByCommandId(v_command_id UUID);
DROP FUNCTION IF EXISTS DeleteAsyncTasksZombies();
DROP FUNCTION IF EXISTS DeleteAsyncTaskZombiesByCommandId(v_command_id UUID);
DROP FUNCTION IF EXISTS DeleteAsyncTaskZombiesByTaskId(v_task_id UUID);
DROP FUNCTION IF EXISTS DeleteEntitySnapshotByTaskId(v_task_id UUID);
DROP FUNCTION IF EXISTS DeleteEntitySnapshotByZombieTaskId(v_task_id UUID);
DROP FUNCTION IF EXISTS DeleteEntitySnapshotZombies();
DROP FUNCTION IF EXISTS DeleteJobStepsByCommandId(v_command_id UUID);
DROP FUNCTION IF EXISTS DeleteJobStepsByTaskId(v_task_id UUID);
DROP FUNCTION IF EXISTS DeleteJobStepsByZombieCommandId(v_command_id UUID);
DROP FUNCTION IF EXISTS DeleteJobStepsZombies();
DROP FUNCTION IF EXISTS GetAllCommands();
DROP FUNCTION IF EXISTS GetAllCommandsWithRunningTasks();
DROP FUNCTION IF EXISTS GetAllCommandsWithZombieTasks();
DROP FUNCTION IF EXISTS GetAsyncTasksZombies();
