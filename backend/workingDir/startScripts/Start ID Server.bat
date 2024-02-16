@echo off
color A
:start
java -cp ./lib/*; app/vatov/idserver/MainKt
if errorlevel 1 (
	timeout /t 10 /nobreak
	goto start
)
pause