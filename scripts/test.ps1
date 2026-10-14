param([string]$JdkHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'

if (!$JdkHome) {
    throw 'Set JAVA_HOME to JDK 17+ or pass -JdkHome.'
}

$projectRoot = Split-Path $PSScriptRoot -Parent
$testClasses = Join-Path $projectRoot 'build\host-tests'
New-Item -ItemType Directory -Force $testClasses | Out-Null

& (Join-Path $JdkHome 'bin\javac.exe') --release 11 -d $testClasses `
    (Join-Path $projectRoot 'app\src\main\java\app\still\focus\Rules.java') `
    (Join-Path $projectRoot 'tests\RulesTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }

& (Join-Path $JdkHome 'bin\java.exe') -cp $testClasses app.still.focus.RulesTest
if ($LASTEXITCODE -ne 0) { throw 'Policy tests failed' }
