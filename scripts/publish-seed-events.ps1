# publish-seed-events.ps1
# ------------------------------------------------------------------
# Replays auction lifecycle events (auction-created / auction-started /
# auction-closed) for the SQL-seeded auctions so that:
#   * BidProcessingService's in-memory AuctionContextCache is warmed
#     (seeded ACTIVE auctions become biddable via the API), and
#   * NotificationService generates the AUCTION_CREATED/STARTED/CLOSED
#     notifications (deduplicated) for the seeded data.
#
# Run AFTER scripts/db-reset.sql + scripts/db-seed.sql, and after the
# bid service has (re)started so its cache starts clean.
#
# Ordering matters: auction-created must be consumed before
# auction-started for each auction (the started listener preserves the
# existing context but builds a degraded one when created is missing).
# The phased pauses below make that ordering deterministic.
# ------------------------------------------------------------------
$ErrorActionPreference = 'Stop'

$kafkaBin = 'C:\kafka_2.12-2.7.1\bin\windows'
$producer = Join-Path $kafkaBin 'kafka-console-producer.bat'
if (-not (Test-Path -LiteralPath $producer)) { throw "Producer not found: $producer" }

$tmp = Join-Path $env:TEMP 'bidstream-seed-events'
New-Item -ItemType Directory -Force -Path $tmp | Out-Null

function TS([int]$days, [int]$hours = 0) {
    (Get-Date).AddDays($days).AddHours($hours).ToString('yyyy-MM-ddTHH:mm:ss')
}

$auctions = @(
    @{ id = 1;  title = 'Sony Alpha A7 III Mirrorless Camera';      seller = 2; sp = 1200.00;  startDays = 2;  endDays = 9;  status = 'PENDING' }
    @{ id = 2;  title = 'MacBook Pro 14-inch M3 (2023)';            seller = 2; sp = 1800.00;  startDays = -3; endDays = 4;  status = 'ACTIVE';  winBid = 6;  winBidder = 6; final = 2200.00 }
    @{ id = 3;  title = 'Canon EF 70-200mm f/2.8L IS III Lens';     seller = 2; sp = 900.00;   startDays = -1; endDays = 6;  status = 'ACTIVE' }
    @{ id = 4;  title = 'Omega Seamaster 300 Diver Watch';          seller = 3; sp = 3200.00;  startDays = -5; endDays = 2;  status = 'ACTIVE';  winBid = 8;  winBidder = 7; final = 3350.00 }
    @{ id = 5;  title = 'Rolex Datejust 41 Blue Dial Watch';        seller = 3; sp = 6500.00;  startDays = -10; endDays = -3; status = 'CLOSED';  winBid = 18; winBidder = 9; final = 6900.00 }
    @{ id = 6;  title = '1969 Ford Mustang Mach 1 Classic';         seller = 4; sp = 28000.00; startDays = -14; endDays = -7; status = 'CLOSED';  winBid = 20; winBidder = 6; final = 31000.00 }
    @{ id = 7;  title = 'Tesla Model 3 Long Range 2021';            seller = 4; sp = 31000.00; startDays = -12; endDays = -2; status = 'EXPIRED' }
    @{ id = 8;  title = 'Bose QuietComfort 45 Headphones';          seller = 5; sp = 250.00;   startDays = -6; endDays = 1;  status = 'ACTIVE';  winBid = 11; winBidder = 8; final = 280.00 }
    @{ id = 9;  title = 'Patek Philippe Nautilus 5711 Watch';       seller = 3; sp = 45000.00; startDays = 5;  endDays = 12; status = 'PENDING' }
    @{ id = 10; title = 'Leica Q2 Digital Camera';                  seller = 2; sp = 4200.00;  startDays = -8; endDays = -2; status = 'CLOSED';  winBid = 22; winBidder = 8; final = 4500.00 }
    @{ id = 11; title = 'Ducati Monster 937 Motorcycle';            seller = 4; sp = 9800.00;  startDays = -2; endDays = 5;  status = 'ACTIVE';  winBid = 15; winBidder = 9; final = 10500.00 }
    @{ id = 12; title = 'Kenwood Chef XL Stand Mixer';              seller = 3; sp = 180.00;   startDays = -6; endDays = -1; status = 'CLOSED';  winBid = 23; winBidder = 7; final = 195.00 }
)

$created = @()
$started = @()
$closed  = @()

foreach ($a in $auctions) {
    $key = "$($a.id)"

    $createdEvent = @{
        auctionId     = $a.id
        title         = $a.title
        sellerId      = $a.seller
        startingPrice = $a.sp
        startTime     = TS $a.startDays
        endTime       = TS $a.endDays
        eventType     = 'AUCTION_CREATED'
    }
    $created += ($key + "`t" + ($createdEvent | ConvertTo-Json -Compress))

    if ($a.status -ne 'PENDING') {
        $startedEvent = @{
            auctionId = $a.id
            title     = $a.title
            sellerId  = $a.seller
            eventType = 'AUCTION_STARTED'
        }
        $started += ($key + "`t" + ($startedEvent | ConvertTo-Json -Compress))
    }

    if ($a.status -eq 'CLOSED' -or $a.status -eq 'EXPIRED') {
        $closedEvent = @{
            auctionId       = $a.id
            title           = $a.title
            sellerId        = $a.seller
            winningBidId    = $a.winBid
            winningBidderId = $a.winBidder
            finalPrice      = $a.final
            closedAt        = TS $a.endDays
            eventType       = 'AUCTION_CLOSED'
        }
        $closed += ($key + "`t" + ($closedEvent | ConvertTo-Json -Compress))
    }
}

function Send-Topic([string]$topic, [string[]]$lines) {
    if (-not $lines -or $lines.Count -eq 0) { return }
    $file = Join-Path $tmp ($topic + '.txt')
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file, ($lines -join "`n"), $utf8)
    Write-Host "Publishing $($lines.Count) events to topic '$topic' ..."
    cmd /c "type `"$file`" | `"$producer`" --bootstrap-server localhost:9092 --topic $topic --property parse.key=true"
    if ($LASTEXITCODE -ne 0) { throw "Producer failed for topic '$topic' (exit code $LASTEXITCODE)" }
}

Write-Host "Phase 1/3: auction-created ($($created.Count))"
Send-Topic 'auction-created' $created
Start-Sleep -Seconds 5

Write-Host "Phase 2/3: auction-started ($($started.Count))"
Send-Topic 'auction-started' $started
Start-Sleep -Seconds 5

Write-Host "Phase 3/3: auction-closed ($($closed.Count))"
Send-Topic 'auction-closed' $closed
Start-Sleep -Seconds 3

Write-Host "Done. Wait a few seconds for consumers, then verify caches/notifications."
