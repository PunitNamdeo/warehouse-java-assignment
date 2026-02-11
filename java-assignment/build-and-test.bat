@echo off
cd /d "C:\Users\c plus\Downloads\fcs-interview-code-assignment-main\java-assignment"
echo ======== COMPILING PROJECT ========
call mvn clean compile -q
if errorlevel 1 (
    echo COMPILATION FAILED
    mvn clean compile
    exit /b 1
) else (
    echo COMPILATION SUCCESSFUL
)

echo.
echo ======== RUNNING TESTS ========
call mvn test -q
if errorlevel 1 (
    echo TESTS FAILED
    mvn test
    exit /b 1
) else (
    echo ALL TESTS PASSED
)

echo.
echo ======== GENERATING JACOCO REPORT ========
call mvn jacoco:report -q
echo JACOCO REPORT GENERATED at target/site/jacoco/index.html

echo.
echo ======== BUILD COMPLETE ========
