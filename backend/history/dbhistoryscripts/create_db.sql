

IF NOT EXISTS(SELECT * FROM sys.databases WHERE name='$(dbname)')
BEGIN
    CREATE DATABASE [$(dbname)]
END
GO

--Update Database increment size(only if value is lower ...)

declare @db_growth_size int
declare @db_log_growth_size int
declare @initial_growth_size int
set @initial_growth_size = 50

-- get current values for engine_history
select @db_growth_size = [growth]/128 from sys.database_files where [name] = '$(dbname)'
select @db_log_growth_size = [growth]/128 from sys.database_files where [name] = '$(dbname)'

if (@db_growth_size < @initial_growth_size) 
begin
	alter database $(dbname)
	modify file (name = $(dbname) ,  FILEGROWTH  = 50)
end

if (@db_log_growth_size < @initial_growth_size) 
begin
	alter database $(dbname)
	modify file (name = $(dbname)_log ,  FILEGROWTH  = 50)
end
go