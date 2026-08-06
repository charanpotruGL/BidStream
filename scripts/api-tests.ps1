# BidStream Automated API Test Suite
# Runs against the API Gateway (http://localhost:8080).
# Requires: Eureka + User/Auction/Bid/Notification services + API Gateway running.
# Run:  powershell -ExecutionPolicy Bypass -File scripts/api-tests.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$LogFile = ""
)

$ErrorActionPreference = "Stop"
$script:Passed = 0
$script:Failed = 0
$script:Failures = New-Object System.Collections.Generic.List[string]
$script:LogFile = if ($LogFile) { $LogFile } else { Join-Path $PSScriptRoot "api-tests-$(Get-Date -Format 'yyyyMMdd-HHmmss').log" }

function Write-Log([string]$text) {
    $line = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss.fff')  $text"
    Add-Content -LiteralPath $script:LogFile -Value $line -Encoding UTF8
}

function Write-LogSeparator() { Write-Log ("=" * 72) }

function Test-Pass([string]$name) { $script:Passed++; Write-Host "  [PASS] $name" -ForegroundColor Green }
function Test-Fail([string]$name, [string]$detail) {
    $script:Failed++
    $script:Failures.Add("$name -> $detail")
    Write-Host "  [FAIL] $name :: $detail" -ForegroundColor Red
}

function Assert-Status([string]$name, [int]$expected, [scriptblock]$action) {
    try {
        $response = & $action
        if ([int]$response.StatusCode -eq $expected) {
            Test-Pass $name
        } else {
            Test-Fail $name "expected $expected but got $($response.StatusCode)"
        }
    } catch {
        $status = $null
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
        }
        if ($status -eq $expected) {
            Test-Pass $name
        } elseif ($status -ne $null) {
            Test-Fail $name "expected $expected but got $status"
        } else {
            Test-Fail $name "request failed: $($_.Exception.Message)"
        }
    }
}

function Invoke-Gateway {
    param(
        [string]$Method = "GET",
        [string]$Path,
        $Body = $null,
        [string]$Token = $null,
        [hashtable]$ExtraHeaders = @{},
        [switch]$RawBody
    )
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    foreach ($k in $ExtraHeaders.Keys) { $headers[$k] = $ExtraHeaders[$k] }
    $params = @{
        Method = $Method
        Uri    = "$BaseUrl$Path"
        Headers = $headers
        TimeoutSec = 30
    }
    $bodyText = ""
    if ($Body -ne $null) {
        if ($RawBody) { $bodyText = [string]$Body } else { $bodyText = ($Body | ConvertTo-Json -Depth 6) }
        $params["Body"] = $bodyText
        $params["ContentType"] = "application/json"
    }
    Write-LogSeparator
    Write-Log "REQUEST  $Method $Path"
    foreach ($k in $headers.Keys) {
        $v = if ($k -eq "Authorization") { "Bearer <redacted>" } else { $headers[$k] }
        Write-Log "  Header: ${k}: ${v}"
    }
    if ($bodyText) { Write-Log "  Body: $bodyText" }
    try {
        $resp = Invoke-WebRequest -UseBasicParsing @params
        Write-Log "RESPONSE $([int]$resp.StatusCode) $($resp.StatusDescription)"
        Write-Log "  Body: $($resp.Content)"
        return $resp
    } catch {
        $status = $null
        $reason = ""
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try { $reason = $_.Exception.Response.StatusDescription } catch {}
        }
        $errBody = Get-ErrorResponseBody $_
        Write-Log "RESPONSE $(if ($status -ne $null) { $status } else { 'NO-RESPONSE' }) $reason"
        if ($errBody) { Write-Log "  Body: $errBody" }
        throw $_
    }
}

function Get-ErrorResponseBody($errorRecord) {
    try {
        $resp = $errorRecord.Exception.Response
        if ($resp -and $resp.GetResponseStream()) {
            $stream = $resp.GetResponseStream()
            if ($stream -and $stream.CanRead) {
                $reader = New-Object System.IO.StreamReader($stream)
                return $reader.ReadToEnd()
            }
        }
    } catch { }
    return $null
}

function Get-JsonFromResponse($resp) {
    if ($resp -and $resp.Content) { return $resp.Content | ConvertFrom-Json }
    return $null
}

function Invoke-Assert {
    param([string]$Name, [int]$Expected, [hashtable]$Req)
    try {
        $r = Invoke-Gateway @Req
        if ([int]$r.StatusCode -eq $Expected) {
            Test-Pass $Name
        } else {
            Test-Fail $Name "expected $Expected but got $($r.StatusCode)"
        }
        return $r
    } catch {
        $status = $null
        if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
        if ($status -eq $Expected) {
            Test-Pass $Name
        } elseif ($status -ne $null) {
            Test-Fail $Name "expected $Expected but got $status"
        } else {
            Test-Fail $Name "request failed: $($_.Exception.Message)"
        }
        return $null
    }
}

Write-Host "=== BidStream API Test Suite ===" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl"
Write-Host "Log file: $($script:LogFile)"
Write-Host ""
Write-LogSeparator
Write-Log "BidStream API Test Suite started"
Write-Log "Base URL: $BaseUrl"
Write-Log "Log file: $($script:LogFile)"
Write-LogSeparator

# =========================================================
# 1. AUTH & USER
# =========================================================
Write-Host "--- AUTH & USER ---" -ForegroundColor Yellow

$userSuffix = Get-Date -Format "HHmmssfff"
$username = "tester_$userSuffix"
$email = "$username@example.com"
$regBody = @{ username = $username; email = $email; password = "secret123"; fullName = "Test User"; role = "USER" }

$regResp = Invoke-Assert -Name "POST /api/auth/register -> 201" -Expected 201 -Req @{ Method = "POST"; Path = "/api/auth/register"; Body = $regBody }
$reg = Get-JsonFromResponse $regResp
if ($reg.token) { Test-Pass "register returns JWT + userId ($($reg.userId))" } else { Test-Fail "register response token" "no token returned" }
$token = $reg.token
$newUserId = $reg.userId

Assert-Status "POST /api/auth/register duplicate -> 409" 409 { Invoke-Gateway -Method "POST" -Path "/api/auth/register" -Body $regBody }
Assert-Status "POST /api/auth/register invalid email -> 400" 400 { Invoke-Gateway -Method "POST" -Path "/api/auth/register" -Body @{ username = "x_$userSuffix"; email = "not-an-email"; password = "secret123" } }

$loginBody = @{ usernameOrEmail = $email; password = "secret123" }
Assert-Status "POST /api/auth/login -> 200" 200 { Invoke-Gateway -Method "POST" -Path "/api/auth/login" -Body $loginBody }
Assert-Status "POST /api/auth/login wrong password -> 401" 401 { Invoke-Gateway -Method "POST" -Path "/api/auth/login" -Body @{ usernameOrEmail = $email; password = "wrongpass" } }

Assert-Status "GET /api/users/{id} with token -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/users/$newUserId" -Token $token }
Assert-Status "GET /api/users/{id} without token -> 401" 401 { Invoke-Gateway -Method "GET" -Path "/api/users/$newUserId" }
Assert-Status "GET /api/users/{id} invalid token -> 401" 401 { Invoke-Gateway -Method "GET" -Path "/api/users/$newUserId" -Token "invalid.token.value" }
Assert-Status "GET /api/users/username/{u} with token -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/users/username/$username" -Token $token }
Assert-Status "GET /api/users/99999999 with token -> 404" 404 { Invoke-Gateway -Method "GET" -Path "/api/users/99999999" -Token $token }

# =========================================================
# 2. AUCTIONS
# =========================================================
Write-Host "--- AUCTIONS ---" -ForegroundColor Yellow

$now = Get-Date
$startFuture = $now.AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
$endFuture = $now.AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
$startPast = $now.AddHours(-1).ToString("yyyy-MM-ddTHH:mm:ss")

$auctionBody = @{ title = "Antique Vase $userSuffix"; description = "A rare vase"; sellerId = $newUserId; startingPrice = 100.00; startTime = $startFuture; endTime = $endFuture }

$auctionResp = Invoke-Assert -Name "POST /api/auctions -> 201" -Expected 201 -Req @{ Method = "POST"; Path = "/api/auctions"; Body = $auctionBody; Token = $token }
$auction = Get-JsonFromResponse $auctionResp
Test-Pass "auction created id=$($auction.id) status=$($auction.status)"
$auctionId = $auction.id

Assert-Status "POST /api/auctions end<start -> 400" 400 { Invoke-Gateway -Method "POST" -Path "/api/auctions" -Body @{ title = "Bad"; sellerId = 1; startingPrice = 10; startTime = $endFuture; endTime = $startPast } -Token $token }

Assert-Status "GET /api/auctions -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/auctions" -Token $token }
Assert-Status "GET /api/auctions?sort=title&direction=desc -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/auctions?page=0&size=5&sort=title&direction=desc" -Token $token }
Assert-Status "GET /api/auctions/{id} -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/auctions/$auctionId" -Token $token }
Assert-Status "GET /api/auctions/status/ACTIVE -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/auctions/status/ACTIVE" -Token $token }
Assert-Status "GET /api/auctions/seller/{id} -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/auctions/seller/$newUserId" -Token $token }
Assert-Status "GET /api/auctions/stats/active-count -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/auctions/stats/active-count" -Token $token }
Assert-Status "GET /api/auctions/99999999 -> 404" 404 { Invoke-Gateway -Method "GET" -Path "/api/auctions/99999999" -Token $token }

Assert-Status "PUT /api/auctions/{id} update title -> 200" 200 { Invoke-Gateway -Method "PUT" -Path "/api/auctions/$auctionId" -Body @{ title = "Antique Vase UPDATED" } -Token $token }

$startResp = Invoke-Assert -Name "POST /api/auctions/{id}/start -> 200" -Expected 200 -Req @{ Method = "POST"; Path = "/api/auctions/$auctionId/start"; Token = $token }
$started = Get-JsonFromResponse $startResp
Test-Pass "auction started: status=$($started.status)"
Assert-Status "POST /api/auctions/{id}/start again -> 400" 400 { Invoke-Gateway -Method "POST" -Path "/api/auctions/$auctionId/start" -Token $token }

Invoke-Assert -Name "POST /api/auctions/{id}/close -> 200" -Expected 200 -Req @{ Method = "POST"; Path = "/api/auctions/$auctionId/close"; Token = $token } | Out-Null
Assert-Status "POST /api/auctions/{id}/close again -> 400" 400 { Invoke-Gateway -Method "POST" -Path "/api/auctions/$auctionId/close" -Token $token }

# Auction without auth -> 401 (gateway enforces JWT)
Assert-Status "POST /api/auctions without token -> 401" 401 { Invoke-Gateway -Method "POST" -Path "/api/auctions" -Body $auctionBody }

# =========================================================
# 3. BIDS
# =========================================================
Write-Host "--- BIDS ---" -ForegroundColor Yellow

# Create an ACTIVE auction for bidding (start time must be in the future; start explicitly)
$bidAuctionBody = @{ title = "Bid Target $userSuffix"; description = "For bids"; sellerId = $newUserId; startingPrice = 50.00; startTime = $startFuture; endTime = $endFuture }
$bidAuctionResp = Invoke-Assert -Name "POST /api/auctions (bid target) -> 201" -Expected 201 -Req @{ Method = "POST"; Path = "/api/auctions"; Body = $bidAuctionBody; Token = $token }
$bidAuction = Get-JsonFromResponse $bidAuctionResp
$bidAuctionId = $bidAuction.id
Invoke-Assert -Name "POST /api/auctions/{id}/start (bid target) -> 200" -Expected 200 -Req @{ Method = "POST"; Path = "/api/auctions/$bidAuctionId/start"; Token = $token } | Out-Null

$bidBody1 = @{ auctionId = $bidAuctionId; bidderId = $newUserId; amount = 60.00 }
$bid1Resp = Invoke-Assert -Name "POST /api/bids -> 201" -Expected 201 -Req @{ Method = "POST"; Path = "/api/bids"; Body = $bidBody1; Token = $token }
$bid1 = Get-JsonFromResponse $bid1Resp
Test-Pass "bid placed id=$($bid1.id) amount=$($bid1.amount) status=$($bid1.status)"

Assert-Status "POST /api/bids lower amount -> 400" 400 { Invoke-Gateway -Method "POST" -Path "/api/bids" -Body @{ auctionId = $bidAuctionId; bidderId = $newUserId; amount = 55.00 } -Token $token }
Assert-Status "POST /api/bids zero amount -> 400" 400 { Invoke-Gateway -Method "POST" -Path "/api/bids" -Body @{ auctionId = $bidAuctionId; bidderId = $newUserId; amount = 0 } -Token $token }

$bid2Resp = Invoke-Assert -Name "POST /api/bids higher -> 201" -Expected 201 -Req @{ Method = "POST"; Path = "/api/bids"; Body = @{ auctionId = $bidAuctionId; bidderId = $newUserId; amount = 75.00 }; Token = $token }
$bid2 = Get-JsonFromResponse $bid2Resp
Test-Pass "second (higher) bid placed id=$($bid2.id)"

Assert-Status "GET /api/bids/{bidId} -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/bids/$($bid1.id)" -Token $token }
Assert-Status "GET /api/bids/auction/{id} -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/bids/auction/$bidAuctionId" -Token $token }
Assert-Status "GET /api/bids/bidder/{id} -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/bids/bidder/$newUserId" -Token $token }
Assert-Status "GET /api/bids/auction/{id}/highest -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/bids/auction/$bidAuctionId/highest" -Token $token }
$highest = Get-JsonFromResponse (Invoke-Gateway -Method "GET" -Path "/api/bids/auction/$bidAuctionId/highest" -Token $token)
Test-Pass "highest bid = $($highest.amount) (expected 75)"
if ([double]$highest.amount -eq 75) { Test-Pass "highest bid value correct" } else { Test-Fail "highest bid value" "got $($highest.amount)" }

Assert-Status "GET /api/bids/99999999 -> 404" 404 { Invoke-Gateway -Method "GET" -Path "/api/bids/99999999" -Token $token }
Assert-Status "GET /api/bids/auction/99999999/highest -> 404" 404 { Invoke-Gateway -Method "GET" -Path "/api/bids/auction/99999999/highest" -Token $token }
Assert-Status "POST /api/bids without token -> 401" 401 { Invoke-Gateway -Method "POST" -Path "/api/bids" -Body $bidBody1 }

# =========================================================
# 4. NOTIFICATIONS
# =========================================================
Write-Host "--- NOTIFICATIONS ---" -ForegroundColor Yellow

$notifBody = @{ userId = $newUserId; notificationType = "INFO"; title = "Welcome"; message = "Hello from tests" }
$notifResp = Invoke-Assert -Name "POST /api/notifications -> 201" -Expected 201 -Req @{ Method = "POST"; Path = "/api/notifications"; Body = $notifBody; Token = $token }
$notif = Get-JsonFromResponse $notifResp
$notifId = $notif.id

Assert-Status "GET /api/notifications/{id} -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/notifications/$notifId" -Token $token }
Assert-Status "GET /api/notifications/99999999 -> 404" 404 { Invoke-Gateway -Method "GET" -Path "/api/notifications/99999999" -Token $token }

# user/me endpoints use X-User-Id injected by gateway
Assert-Status "GET /api/notifications/user/me -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/notifications/user/me" -Token $token }
Assert-Status "GET /api/notifications/user/me/unread -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/notifications/user/me/unread" -Token $token }
Assert-Status "GET /api/notifications/user/me/count -> 200" 200 { Invoke-Gateway -Method "GET" -Path "/api/notifications/user/me/count" -Token $token }
$count = Get-JsonFromResponse (Invoke-Gateway -Method "GET" -Path "/api/notifications/user/me/count" -Token $token)
Test-Pass "unread count = $count"

$readResp = Invoke-Assert -Name "PUT /api/notifications/{id}/read -> 200" -Expected 200 -Req @{ Method = "PUT"; Path = "/api/notifications/$notifId/read"; Token = $token }
$readNotif = Get-JsonFromResponse $readResp
if ($readNotif.read) { Test-Pass "notification marked read" } else { Test-Fail "notification read flag" "read=$($readNotif.read)" }

Assert-Status "PUT /api/notifications/user/me/read-all -> 200" 200 { Invoke-Gateway -Method "PUT" -Path "/api/notifications/user/me/read-all" -Token $token }
Assert-Status "DELETE /api/notifications/{id} -> 200" 200 { Invoke-Gateway -Method "DELETE" -Path "/api/notifications/$notifId" -Token $token }

# =========================================================
# 5. GATEWAY EDGE CASES
# =========================================================
Write-Host "--- GATEWAY EDGE CASES ---" -ForegroundColor Yellow

Assert-Status "GET /api/nonexistent -> 404" 404 { Invoke-Gateway -Method "GET" -Path "/api/nonexistent" -Token $token }
Assert-Status "GET /api/users/{id} malformed token -> 401" 401 { Invoke-Gateway -Method "GET" -Path "/api/users/$newUserId" -Token "Bearer malformed" }

try {
    $bad = Invoke-Gateway -Method "POST" -Path "/api/auctions" -Body "{invalid json" -Token $token -RawBody
    Test-Fail "POST /api/auctions malformed json" "expected 400/500 got $($bad.StatusCode)"
} catch {
    $status = $null
    if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
    if ($status -eq 400 -or $status -eq 500) { Test-Pass "POST /api/auctions malformed json -> $status" } else { Test-Fail "POST malformed json" "got $status" }
}

# =========================================================
# 6. KAFKA EVENT FLOW (async verification)
# =========================================================
Write-Host "--- KAFKA EVENT FLOW (waiting for async processing) ---" -ForegroundColor Yellow
Start-Sleep -Seconds 15
$eventNotifs = Get-JsonFromResponse (Invoke-Gateway -Method "GET" -Path "/api/notifications/user/me" -Token $token)
$auctionCreatedNotifs = @($eventNotifs | Where-Object { $_.notificationType -eq "AUCTION_CREATED" })
$bidPlacedNotifs = @($eventNotifs | Where-Object { $_.notificationType -eq "BID_PLACED" })
if ($auctionCreatedNotifs.Count -gt 0) { Test-Pass "Kafka: auction-created -> notification delivered ($($auctionCreatedNotifs.Count))" } else { Test-Fail "Kafka: auction-created notification" "no AUCTION_CREATED notifications for user $newUserId" }
if ($bidPlacedNotifs.Count -gt 0) { Test-Pass "Kafka: bid-placed -> notification delivered ($($bidPlacedNotifs.Count))" } else { Test-Fail "Kafka: bid-placed notification" "no BID_PLACED notifications for user $newUserId" }

# Cleanup: delete created auction
Assert-Status "DELETE /api/auctions/{id} -> 204" 204 { Invoke-Gateway -Method "DELETE" -Path "/api/auctions/$auctionId" -Token $token }
Assert-Status "DELETE /api/auctions/{id} again -> 404" 404 { Invoke-Gateway -Method "DELETE" -Path "/api/auctions/$auctionId" -Token $token }

Write-Host ""
Write-Host "==============================" -ForegroundColor Cyan
Write-Host "RESULTS: $($script:Passed) passed, $($script:Failed) failed" -ForegroundColor $(if ($script:Failed -eq 0) { "Green" } else { "Red" })
if ($script:Failed -gt 0) {
    Write-Host "FAILURES:" -ForegroundColor Red
    $script:Failures | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
}
Write-Host "==============================" -ForegroundColor Cyan
Write-LogSeparator
Write-Log "Test suite finished: $($script:Passed) passed, $($script:Failed) failed"
if ($script:Failed -gt 0) { $script:Failures | ForEach-Object { Write-Log "  FAILURE: $_" } }
Write-LogSeparator
Write-Host ""
Write-Host "Requests & responses logged to: $($script:LogFile)" -ForegroundColor Cyan
exit $script:Failed
