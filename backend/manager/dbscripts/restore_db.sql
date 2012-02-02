use master;


BEGIN
   RESTORE DATABASE "$(dbname)" FROM DISK = N'$(restore_file)' WITH REPLACE;
END;

