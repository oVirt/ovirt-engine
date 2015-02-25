/************************************************************************************************
 The following are helper SP for taskcleaner utility and are not exposed to the application DAOs
/This script deals with command_entities related SP
************************************************************************************************/


Create or replace FUNCTION GetAllCommandsWithRunningTasks() RETURNS SETOF COMMAND_ENTITIES STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM COMMAND_ENTITIES C
   WHERE EXISTS (SELECT * FROM ASYNC_TASKS A WHERE A.COMMAND_ID = C.COMMAND_ID);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION  GetAllCommands()
RETURNS SETOF COMMAND_ENTITIES STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM COMMAND_ENTITIES;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllCommandsWithZombieTasks()
RETURNS SETOF COMMAND_ENTITIES STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM COMMAND_ENTITIES C
   WHERE C.COMMAND_ID in (SELECT COMMAND_ID from GetAsyncTasksZombies());
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAllCommands()
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM COMMAND_ENTITIES;
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAllCommandsWithZombieTasks()
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM COMMAND_ENTITIES C
   WHERE C.COMMAND_ID in (SELECT COMMAND_ID from GetAsyncTasksZombies());
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAllCommandsWithRunningTasks()
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM COMMAND_ENTITIES C WHERE C.COMMAND_ID in (SELECT * FROM ASYNC_TASKS A WHERE A.COMMAND_ID = C.COMMAND_ID);
END; $procedure$
LANGUAGE plpgsql;
