
IF EXISTS(SELECT * FROM sys.databases WHERE name = '$(dbname)') then
   ALTER DATABASE "$(dbname)"
   SET SINGLE_USER
   WITH ROLLBACK IMMEDIATE;
   DROP DATABASE "$(dbname)";
end if;


