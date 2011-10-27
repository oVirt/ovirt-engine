
--Future (after process is verified):
--1) Remove *___old tables
--2) Remove *___old columns

print 'Clean up phase, removing all old tables & columns...'

declare @table varchar(128)
declare @statement nvarchar(max)
DECLARE DropOldTablesCursor CURSOR FOR
	select name from sys.objects where name like '%___old' 
OPEN DropOldTablesCursor

FETCH NEXT FROM DropOldTablesCursor INTO @table

WHILE @@FETCH_STATUS = 0
BEGIN
	set @statement = 'DROP TABLE ' + @table
    print 'Executing ' + @statement + '...'
    exec sp_executesql @statement
	FETCH NEXT FROM DropOldTablesCursor INTO @table
END
CLOSE DropOldTablesCursor
DEALLOCATE DropOldTablesCursor
go

declare @table varchar(128)
declare @col varchar(128)
declare @statement nvarchar(max)
DECLARE DropOldColumnsCursor CURSOR FOR
	select object_name(object_id) as [table], [name]  from sys.columns where name like '%___old'
OPEN DropOldColumnsCursor

FETCH NEXT FROM DropOldColumnsCursor INTO @table, @col

WHILE @@FETCH_STATUS = 0
BEGIN
	set @statement = 'ALTER TABLE ' + @table + ' DROP Column ' + @col
	print 'Executing ' + @statement + '...'
    exec sp_executesql @statement
	FETCH NEXT FROM DropOldColumnsCursor INTO  @table, @col
END
CLOSE DropOldColumnsCursor
DEALLOCATE DropOldColumnsCursor
go
