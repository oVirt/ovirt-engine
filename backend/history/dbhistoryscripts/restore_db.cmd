@echo off

if "%1%" == "-h" goto Syntax
if "%1%" == "--help" goto Syntax

SET restore_file=%1%

SET sqlServer=%2%
if "%2%" == "" set sqlServer=.\sqlexpress

SET dbname=%3%
if "%3%" == "" set dbname=engine_history

for %%v in (%0) do set MyPath=%%~dpv
pushd "%MyPath%"

SET user=%4%
if "%4%" == "" set user=sa

SET pass=%5%
if "%5%" == "" set pass=ENGINEadmin2009!


echo server - %sqlServer%
echo dbname - %dbname%


echo restoring the database ...
sqlcmd  -b -S %sqlServer% -U %user% -P %pass% -d %dbname% -i restore_db.sql -v dbname=%dbname% restore_file=%restore_file%
if %ERRORLEVEL% NEQ 0 goto ERROR
echo Done.

popd

goto:EOF



:Syntax
echo backup_vdc_db.cmd  [file_name] [server] [dbname] [user] [pass] 
echo	 file_name - the backup file name
echo     server - the sql server to access (default = .\sqlexpress)
echo     dbname - the database name to access/create (default = engine_history)
echo	 user - the database administrator user name
echo	 pass - the database administrator password


goto:EOF

:ERROR
exit /b 1



