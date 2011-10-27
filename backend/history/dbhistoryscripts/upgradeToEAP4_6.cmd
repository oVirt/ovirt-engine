@echo off

if "%1%" == "-h" goto Syntax
if "%1%" == "--help" goto Syntax

SET sqlServer="%1%"
if "%1%" == "" set sqlServer=.\sqlexpress

SET dbname=%2%
if "%2%" == "" set dbname=engine_history

SET maindb=%3%
if "%3%" == "" set maindb=engine

SET user=%4%
SET pass=%5%


SET dbcmd=-b -S %sqlServer% -d %dbname% 
if not "%user%" == "" set dbcmd=-b -S %sqlServer% -d %dbname% -U %user% -P %pass% 

for %%v in (%0) do set MyPath=%%~dpv
pushd "%MyPath%"


echo server - %sqlServer%
echo dbname - %dbname%

REM -------------------------------------------------------------------------------
REM Upgrading to version 4.6 (2.3)
REM -------------------------------------------------------------------------------

REM Skip INT 2 UUID conversion if already done, trying to select a column that is added
REM after the INT 2 UUID conversion is completed, if column is already there , skip the 
REM drop constrains, int2uuid and create constrains steps.
sqlcmd  %dbcmd% -Q"declare @i int; select top 1 @i = vds_id___old from vds_configuration"
if %ERRORLEVEL% EQU 0 goto UPGRADE
sqlcmd  %dbcmd% -i DropConstrains.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
sqlcmd  %dbcmd% -v db=%maindb% -i upgrade.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
sqlcmd  %dbcmd% -i CreateConstrains.sql
if %ERRORLEVEL% NEQ 0 goto ERROR

:UPGRADE
sqlcmd  %dbcmd% -i upgradeToEAP4_6.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
sqlcmd  %dbcmd% -i insert_enum_values.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
sqlcmd  %dbcmd% -i insert_period_table_values.sql
if %ERRORLEVEL% NEQ 0 goto ERROR
echo refreeshing Views and SPs...
call .\refreshStoredProcedures.cmd %1 %2 %3 %4 %5
if %ERRORLEVEL% NEQ 0 goto ERROR
sqlcmd  %dbcmd% -i create_tags_upgrade.sql

REM Cleanup : for future use
REM sqlcmd  %dbcmd% -i Cleanup.sql

echo Done.

popd

goto:EOF


:Syntax
echo upgradeINT2UUID.cmd script-path [server] [dbname] [user] [password] [debug]
echo     server      - the sql server to access (default = .\sqlexpress)
echo     dbname      - the database name to access/create (default = engine_history)
echo     maindb      - the main engine database name (default = engine)
echo	 user        - the dbo user name (default is to use integrated authentication)
echo	 password    - the dbo password (default is to use integrated authentication)


goto:EOF

:ERROR
exit /b 1
