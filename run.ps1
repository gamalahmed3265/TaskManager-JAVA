$mavenVersion = "3.9.6"
$mavenZip = "apache-maven-$mavenVersion-bin.zip"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/$mavenZip"

if (-Not (Test-Path ".\.maven\apache-maven-$mavenVersion\bin\mvn.cmd")) {
    Write-Host "Setting up a local copy of Maven $mavenVersion..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path ".\.maven" | Out-Null
    
    Write-Host "Downloading Maven..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $mavenUrl -OutFile ".\.maven\$mavenZip"
    
    Write-Host "Extracting Maven..." -ForegroundColor Yellow
    Expand-Archive -Path ".\.maven\$mavenZip" -DestinationPath ".\.maven" -Force
}

Write-Host "Running Maven: clean javafx:run" -ForegroundColor Green
$env:PATH = "$PWD\.maven\apache-maven-$mavenVersion\bin;" + $env:PATH
$env:MAVEN_OPTS = "-Dmaven.repo.local=.m2repo"
mvn clean javafx:run
