$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$connector = Join-Path $projectRoot "lib\mysql-connector-j-26.7.0.jar"
$sourceRoot = Join-Path $projectRoot "src\main\java"
$outputRoot = Join-Path $projectRoot "out"

if ([string]::IsNullOrWhiteSpace($env:APP_PORT)) {
    $requestedPort = 8080
} elseif (-not [int]::TryParse($env:APP_PORT, [ref]$requestedPort)) {
    throw "APP_PORT debe ser un numero entre 1 y 65535."
}

if ($requestedPort -lt 1 -or $requestedPort -gt 65535) {
    throw "APP_PORT debe ser un numero entre 1 y 65535."
}

function Test-PortAvailable([int]$Port) {
    $listener = $null
    try {
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
        $listener.Start()
        return $true
    } catch [System.Net.Sockets.SocketException] {
        return $false
    } finally {
        if ($null -ne $listener) {
            $listener.Stop()
        }
    }
}

$port = $requestedPort
while ($port -le 65535 -and -not (Test-PortAvailable $port)) {
    $port++
}
if ($port -gt 65535) {
    throw "No hay puertos TCP disponibles desde $requestedPort hasta 65535."
}

$env:APP_PORT = "$port"
if ($port -ne $requestedPort) {
    Write-Host "El puerto $requestedPort esta ocupado. Se utilizara el puerto $port."
} else {
    Write-Host "Puerto disponible: $port."
}

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
