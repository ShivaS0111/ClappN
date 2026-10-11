# Staging / local smoke against a running ClappN API.
# Requires: PowerShell 5+, curl.exe (Windows 10+).
#
# Example:
#   $env:STAGING_BASE_URL = "http://localhost:9090"
#   $env:STAGING_EMAIL = "owner@example.com"
#   $env:STAGING_PASSWORD = "secret"
#   $env:STAGING_STORE_ID = "1"
#   $env:STAGING_BUSINESS_ID = "1"
#   $env:STAGING_PAYMENT_GATEWAY = "RAZORPAY"   # optional
#   .\smoke\staging-smoke.ps1
#
# Or Maven (same env vars):
#   .\mvnw.cmd -Pstaging-smoke "-Dtest=StagingSmokeIT" test

$ErrorActionPreference = "Stop"

function Require-Env([string]$Name) {
    $v = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($v)) {
        throw "Missing env $Name"
    }
    return $v.Trim()
}

$base = (Require-Env "STAGING_BASE_URL").TrimEnd("/")
$email = Require-Env "STAGING_EMAIL"
$password = Require-Env "STAGING_PASSWORD"
$storeId = Require-Env "STAGING_STORE_ID"
$businessId = Require-Env "STAGING_BUSINESS_ID"
$skipOrder = ($env:STAGING_SKIP_ORDER -eq "true")
$gateway = $env:STAGING_PAYMENT_GATEWAY
$currency = if ($env:STAGING_CURRENCY) { $env:STAGING_CURRENCY } else { "INR" }

Write-Host "== health =="
$health = curl.exe -sS "$base/actuator/health"
Write-Host $health

Write-Host "== login =="
$loginBody = @{ username = $email; password = $password } | ConvertTo-Json -Compress
$loginRaw = curl.exe -sS -X POST "$base/api/auth/login" -H "Content-Type: application/json" -d $loginBody
$login = $loginRaw | ConvertFrom-Json
if (-not $login.success) { throw "login failed: $loginRaw" }
$token = $login.data.tokenInfo.token
Write-Host "JWT ok"

$hdrAuth = @("Authorization: Bearer $token", "X-Store-Id: $storeId", "X-Business-Id: $businessId")

Write-Host "== me/context =="
$me = curl.exe -sS "$base/api/me/context" -H $hdrAuth[0] -H $hdrAuth[1] -H $hdrAuth[2]
Write-Host $me

if ($skipOrder) {
    Write-Host "STAGING_SKIP_ORDER=true — done."
    exit 0
}

Write-Host "== store-product =="
$productsRaw = curl.exe -sS "$base/api/store-product" -H $hdrAuth[0] -H $hdrAuth[1] -H $hdrAuth[2]
$products = $productsRaw | ConvertFrom-Json
if (-not $products.success -or -not $products.data -or $products.data.Count -lt 1) {
    throw "No store products. Assign catalog first. Response: $productsRaw"
}
$p = $products.data | Select-Object -First 1
$itemId = if ($p.businessProductId) { $p.businessProductId } else { $p.id }

Write-Host "== place order (price resolved server-side) =="
$orderBody = @{
    storeId = [long]$storeId
    items   = @(
        @{
            itemType = 1
            itemId   = [long]$itemId
            quantity = 1
            itemName = "smoke-item"
        }
    )
} | ConvertTo-Json -Depth 5 -Compress

$orderRaw = curl.exe -sS -X POST "$base/api/orders/new" `
    -H "Content-Type: application/json" -H $hdrAuth[0] -H $hdrAuth[1] -H $hdrAuth[2] -d $orderBody
$order = $orderRaw | ConvertFrom-Json
if (-not $order.success) { throw "order failed: $orderRaw" }
$orderId = $order.data.id
$charged = [double]$order.data.totalAmount
$amountMinor = [long][math]::Round($charged * 100)
Write-Host "orderId=$orderId charged=$charged amountMinor=$amountMinor"

if ([string]::IsNullOrWhiteSpace($gateway)) {
    Write-Host "No STAGING_PAYMENT_GATEWAY — smoke complete (auth+order)."
    exit 0
}

Write-Host "== payment initiate ($gateway) =="
$payBody = @{
    orderId  = [long]$orderId
    amount   = $amountMinor
    currency = $currency
    gateway  = $gateway.ToUpperInvariant()
} | ConvertTo-Json -Compress

$payRaw = curl.exe -sS -X POST "$base/api/payments/initiate" `
    -H "Content-Type: application/json" -H $hdrAuth[0] -H $hdrAuth[1] -H $hdrAuth[2] -d $payBody
Write-Host $payRaw
Write-Host "Smoke complete."
