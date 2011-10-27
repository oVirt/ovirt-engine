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
if "%4%" == "" set user=sa

SET pass=%5%
if "%5%" == "" set pass=ENGINEadmin2009!

SET debug=%6%
if "%6%" == "" set debug='true'

SET dbcmd=-b -S %sqlServer% -d %dbname% 
if not "%user%" == "" set dbcmd=-b -S %sqlServer% -d %dbname% -U %user% -P %pass% 

for %%v in (%0) do set MyPath=%%~dpv
pushd "%MyPath%"

echo server - %sqlServer%
echo dbname - %dbname%

REM -------------------------------------------------------------------------------
REM Refreshing all views 
REM -------------------------------------------------------------------------------

sqlcmd  %dbcmd% -i create_views.sql
if %ERRORLEVEL% NEQ 0 goto ERROR

echo Creating Stored Procedures .....
sqlcmd  %dbcmd% -v db=%maindb% -i create_sp.sql
if %ERRORLEVEL% NEQ 0 goto ERROR

echo Creating Functions .....
sqlcmd  %dbcmd% -v db=%maindb% -i functions.sql
if %ERRORLEVEL% NEQ 0 goto ERROR

echo Done.

popd

goto:EOF


:Syntax
echo refrashStoredProcedures.cmd [server] [dbname] [user] [password] [debug]
echo	 script-path - the path to the sql scripts folder
echo     server      - the sql server to access (default = .\sqlexpress)
echo     dbname      - the database name to access/create (default = engine_history)
echo	 maindb	     - the main database name
echo	 user        - the dbo user name (default is to use integrated authentication)
echo	 password    - the dbo password (default is to use integrated authentication)
echo	 debug - true/false enables storing of exception data in DB

goto:EOF
:ERROR
exit /b 1
