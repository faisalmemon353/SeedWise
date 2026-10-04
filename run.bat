@echo off
echo Compiling SeedWise App...
if not exist out mkdir out
powershell -Command "$javaFiles = Get-ChildItem -Path 'src\main' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName; javac -encoding UTF-8 --module-path lib --add-modules javafx.controls,javafx.media -d out -cp 'lib/*' $javaFiles"
if errorlevel 1 goto error

echo Compilation successful! Starting SeedWise App...
java "-Djava.library.path=lib" --module-path lib --add-modules javafx.controls,javafx.media -cp "out;lib/*" ui.MainApp
goto end

:error
echo.
echo Compilation failed. Please check your Java version (JDK 17+ required).
pause

:end
