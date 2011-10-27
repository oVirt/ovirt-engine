use master
go

BEGIN
   RESTORE DATABASE [$(dbname)] FROM DISK = N'$(restore_file)' WITH REPLACE
END
GO