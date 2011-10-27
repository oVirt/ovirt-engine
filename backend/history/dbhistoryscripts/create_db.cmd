@echo off

if "%1%" == "-h" goto Syntax
if "%1%" == "--help" goto Syntax


SET sqlServer=%1%
if "%1%" == "" set sqlServer=.\sqlexpress

SET dbname=%2%
if "%2%" == "" set dbname=engine_history 
SET maindb=engine

for %%v in (%0) do set MyPath=%%~dpv
pushd "%MyPath%"

SET user=%3%
if "%3%" == "" set user=sa

SET pass=%4%
if "%4%" == "" set pass=ENGINEadmin2009!

SET debug=%5%
if "%5%" == "" set debug='true'


echo server - %sqlServer%
echo dbname - %dbname%
echo Creating DB ...
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d Master -i create_db.sql -v dbname=%dbname%  
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - create_db.sql.

echo create tables ...
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d %dbname%  -i create_tables.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - create_tables.sql

echo create views ...
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d %dbname% -i create_views.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - create_views.sql

echo creating the Stored Procs .....
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d %dbname% -v debug=%debug%  db=%maindb% -i create_sp.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - create_sp.sql

echo creating the Functions .....
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d %dbname% -v debug=%debug%  db=%maindb% -i functions.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - functions.sql

echo Inserting enum values .....
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d %dbname% -v debug=%debug%  db=%maindb% -i insert_enum_values.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - insert_enum_values.sql

echo Inserting period table values .....
sqlcmd -b -S %sqlServer% -U %user% -P %pass% -d %dbname% -v debug=%debug%  db=%maindb% -i insert_period_table_values.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done - insert_period_table_values.sql

popd

goto:EOF



:Syntax
echo create_db.cmd [server] [dbname] [user] [pass] [debug]
echo     server - the sql server to access (default = .\sqlexpress)
echo     dbname - the database name to access/create (default = engine_history)
echo	 user - the database administrator user name
echo	 pass - the database administrator password
echo	 debug - true/false enables storing of exception data in DB
goto:EOF
:ERROR
exit /b 1
