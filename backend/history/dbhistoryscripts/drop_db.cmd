@echo off 

if "%1%" == "-h" goto Syntax
if "%1%" == "--help" goto Syntax


SET sqlServer=%1%
if "%1%" == "" set sqlServer=.\sqlexpress

SET dbname=%2%
if "%2%" == "" set dbname=engine_history 

echo server - %sqlServer%
echo dbname - %dbname%

echo droping db %dbname% .....
sqlcmd -b -S %sqlServer%  -i drop_db.sql -v dbname=%dbname%
if %ERRORLEVEL% NEQ 0 goto ERROR

echo Done.
goto:EOF



:Syntax
echo drop_vdc_db.cmd [server] [dbname]
echo     server - the sql server to access (default = .\sqlexpress)
echo     dbname - the database name to access/create (default = engine_history)
goto:EOF
:ERROR
exit /b 1
