@ECHO off

echo [0m -------------------------
echo [0m ^| J                     ^|
echo [0m ^| A                     ^|
echo [0m ^| C                     ^|
echo [0m ^| K   [31m        (   [0m      ^|
echo [0m ^|     [31m       (#   [0m      ^|
echo [0m ^|     [31m     ###    [0m      ^|
echo [0m ^|     [31m   ##  #,   [0m      ^|
echo [0m ^|     [31m   #/ ##    [0m      ^|
echo [0m ^|     [31m    #  ##   [0m      ^|
echo [0m ^| [34m   ************   *[0m   ^|
echo [0m ^| [34m    **********  ** [0m   ^|
echo [0m ^| [34m     ********,     [0m   ^|
echo [0m ^| [34m *****,    ,***** *[0m K ^|
echo [0m ^|                     C ^|
echo [0m ^|                     A ^|
echo [0m ^|                     J ^|
echo [0m -------------------------

where /q java
IF ERRORLEVEL 1 (
    ECHO Java is not installed or not in your PATH. Jack depends on Java being installed.
    EXIT /B 1
)

where /q mvn
IF ERRORLEVEL 1 (
    ECHO Maven is not installed or not in your PATH. Jack depends on Maven being installed.
    EXIT /B 1
)

