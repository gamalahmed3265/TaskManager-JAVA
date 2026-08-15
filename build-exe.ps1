Write-Host "Building Fat JAR using local Maven..." -ForegroundColor Cyan
$env:PATH = "$PWD\.maven\apache-maven-3.9.6\bin;" + $env:PATH
$env:MAVEN_OPTS = "-Dmaven.repo.local=.m2repo"
mvn clean package

if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Creating standalone executable using jpackage..." -ForegroundColor Cyan
if (Test-Path ".\app-output") {
    Remove-Item -Recurse -Force ".\app-output"
}

jpackage --type app-image --input target --dest app-output --name TaskManager --main-jar daily-task-manager-1.0.0.jar --main-class com.taskmanager.Launcher

if ($LASTEXITCODE -eq 0) {
    Write-Host "Success! The executable is located in:" -ForegroundColor Green
    Write-Host "$PWD\app-output\TaskManager\TaskManager.exe" -ForegroundColor Yellow
} else {
    Write-Host "jpackage failed!" -ForegroundColor Red
}
