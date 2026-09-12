@echo off
chcp 65001 >nul 2>&1
setlocal

set "BUILD_DIR=build\classes"
set "LIBS=libs\json-20230618.jar;libs\jsoup-1.17.2.jar"
set "OUT_DIR=out\compiled"

if exist "%BUILD_DIR%" rmdir /S /Q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo Collecting source files...
for /R "src\de\hellbz\forge" %%f in (*.java) do (
    set "fp=%%~pf"
    setlocal enabledelayedexpansion
    set "t=!fp:test=!"
    if "!t!"=="!fp!" (
        set "n=!fp:NotInUse=!"
        if "!n!"=="!fp!" (
            echo %%f >> sources.txt
        )
    )
    endlocal
)

echo Compiling...
javac -encoding UTF-8 -cp "%LIBS%" -d "%BUILD_DIR%" @sources.txt
if %errorlevel% neq 0 (
    echo Compilation failed.
    del sources.txt 2>nul
    exit /b 1
)

echo Copying resources...
mkdir "%BUILD_DIR%\res"
xcopy /E /I /Y "res\*" "%BUILD_DIR%\res\" >nul 2>&1

echo Extracting libraries...
cd "%BUILD_DIR%"
jar xf "..\..\libs\json-20230618.jar"
jar xf "..\..\libs\jsoup-1.17.2.jar"
if exist "META-INF\maven" rmdir /S /Q "META-INF\maven" 2>nul
copy /Y "..\..\src\META-INF\MANIFEST.MF" "META-INF\MANIFEST.MF" >nul 2>&1
cd "..\.."

echo Building JAR...
jar cfm "%OUT_DIR%\minecraft_server.jar" "%BUILD_DIR%\META-INF\MANIFEST.MF" -C "%BUILD_DIR%" .

del sources.txt 2>nul
echo Done. Output: %OUT_DIR%\minecraft_server.jar