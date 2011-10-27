print 'Killing all processes using the databases engine_history'
-- Kill all processes using the databases engine_history

SET IMPLICIT_TRANSACTIONS OFF
go

DECLARE @SPId int
DECLARE @CMD nvarchar(max)
declare @sql_handle varbinary(max)
declare @text nvarchar(max)

DECLARE my_cursor CURSOR FAST_FORWARD FOR
SELECT SPId,sql_handle FROM MASTER..SysProcesses WHERE 
-- avoid the sql process
 status <> 'background' AND
-- get the user processes
 status in ('runnable','sleeping') AND
(DBId = DB_ID('engine_history')) AND SPId <> @@SPId

OPEN my_cursor

FETCH NEXT FROM my_cursor INTO @SPId,@sql_handle

WHILE @@FETCH_STATUS = 0
BEGIN
	set @CMD =  'KILL ' + CAST(@SPId AS varchar(5))
	exec (@CMD)
	SELECT @text = text FROM sys.dm_exec_sql_text(@sql_handle)

    print 'Processs ' + CAST(@SPId AS varchar(5)) + ' has been killed by Upgrade process ' + 
	' Executing :' + @text
	FETCH NEXT FROM my_cursor INTO @SPId,@sql_handle
END

CLOSE my_cursor
DEALLOCATE my_cursor 
go

SET IMPLICIT_TRANSACTIONS ON
go
