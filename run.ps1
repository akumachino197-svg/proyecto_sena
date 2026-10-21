$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$connector = Join-Path $projectRoot "lib\mysql-connector-j-26.7.0.jar"
$sourceRoot = Join-Path $projectRoot "src\main\java"
$outputRoot = Join-Path $projectRoot "out"

if (-not (Test-Path $connector)) {
    throw "No se encontro el conector JDBC: $connector"
}

$sources = Get-ChildItem $sourceRoot -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
if ($sources.Count -eq 0) {
    throw "No se encontraron archivos Java en $sourceRoot"
}

New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
javac -encoding UTF-8 -cp $connector -d $outputRoot $sources
if ($LASTEXITCODE -ne 0) {
    throw "La compilacion fallo. Revisa los errores anteriores."
}

java -cp "$outputRoot;$connector" com.sena.empleados.Main
