 
IF EXISTS(SELECT * FROM sys.databases WHERE name='$(dbname)')
BEGIN
    ALTER DATABASE $(dbname) 
       SET SINGLE_USER 
       WITH ROLLBACK IMMEDIATE

    DROP DATABASE [$(dbname)]
END
GO
