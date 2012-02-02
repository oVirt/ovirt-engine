
BEGIN
   BACKUP DATABASE "$(dbname)" TO DISK = N'$(backup_file)' WITH NOFORMAT,NOINIT,NAME = N'$(dbname)-Full Database Backup',SKIP,NOREWIND, 
   NOUNLOAD,STATS = 10;
END;

