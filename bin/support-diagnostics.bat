@echo OFF

SET CurrentDirectory=%~dp0
SET PowerShellScript=%CurrentDirectory%support-diagnostics.ps1

:loop
IF "%~1"=="" GOTO cont
IF /I "%~1"=="-H" SET H="%~2"
IF /I "%~1"=="-o" SET o="%~2"
IF /I "%~1"=="-n" SET n="%~2"
IF /I "%~1"=="-nc" SET nc=1
SHIFT & GOTO loop
:cont

SET Command="& '%PowerShellScript%' -H '%H%' -o '%o%' -n '%n%'"
IF "%nc%" == "1" (
	SET Command=%Command% -nc
)

powershell -NoProfile -ExecutionPolicy Bypass -Command %Command%

SET H=
SET o=
SET n=
SET nc=