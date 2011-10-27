set nocount on
/*
use master
go

-- Set auto update statistics async ON

declare @flag bit
select @flag = is_auto_update_stats_async_on  from sys.databases where name = 'engine_history'
if (@flag = 0)
begin
	ALTER DATABASE engine SET AUTO_UPDATE_STATISTICS ON
	ALTER DATABASE engine SET AUTO_UPDATE_STATISTICS_ASYNC ON 
end
go

--Upgrade Database increment size(only if value is lower ...)
--No need to update initial size in upgrade

use engine_history
go

declare @db_growth_size int
declare @db_log_growth_size int
declare @initial_growth_size int
set @initial_growth_size = 10

-- get current values for engine
select @db_growth_size = [growth]/128 from sys.database_files where [name] = 'engine_history'
select @db_log_growth_size = [growth]/128 from sys.database_files where [name] = 'engine_history_log'

if (@db_growth_size < @initial_growth_size) 
begin
	alter database engine_history
	modify file (name = engine_history ,  FILEGROWTH  = 10)
end

if (@db_log_growth_size < @initial_growth_size) 
begin
	alter database engine_history
	modify file (name = engine_history_log ,  FILEGROWTH  = 10)
end
go
*/


