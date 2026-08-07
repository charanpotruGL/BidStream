-- Idempotent seed for bids1 (BidStream).
-- ACTIVE auctions: top bid = PLACED, older = OUTBID.
-- CLOSED auctions: winner = WINNING, others = LOST.
INSERT INTO bids1 (id, auction_id, bidder_id, amount, status, created_at, version) VALUES
(1,  2,  6,  1850.00, 'OUTBID',  NOW() - INTERVAL '3 days' + INTERVAL '2 hours', 0),
(2,  2,  7,  1950.00, 'OUTBID',  NOW() - INTERVAL '3 days' + INTERVAL '5 hours', 0),
(3,  2,  6,  2050.00, 'OUTBID',  NOW() - INTERVAL '2 days', 0),
(4,  2,  8,  2100.00, 'OUTBID',  NOW() - INTERVAL '1 day',  0),
(5,  2,  9,  2150.00, 'OUTBID',  NOW() - INTERVAL '1 day' + INTERVAL '8 hours', 0),
(6,  2,  6,  2200.00, 'PLACED',  NOW() - INTERVAL '5 hours', 0),
(7,  4,  8,  3300.00, 'OUTBID',  NOW() - INTERVAL '4 days', 0),
(8,  4,  7,  3350.00, 'PLACED',  NOW() - INTERVAL '3 days', 0),
(9,  8,  7,  260.00,  'OUTBID',  NOW() - INTERVAL '5 days', 0),
(10, 8,  6,  265.00,  'OUTBID',  NOW() - INTERVAL '4 days', 0),
(11, 8,  8,  280.00,  'PLACED',  NOW() - INTERVAL '2 days', 0),
(12, 11, 9,  9900.00, 'OUTBID',  NOW() - INTERVAL '2 days' + INTERVAL '3 hours', 0),
(13, 11, 6,  10000.00, 'OUTBID', NOW() - INTERVAL '2 days' + INTERVAL '9 hours', 0),
(14, 11, 10, 10200.00, 'OUTBID', NOW() - INTERVAL '1 day', 0),
(15, 11, 9,  10500.00, 'PLACED', NOW() - INTERVAL '6 hours', 0),
(16, 5,  6,  6600.00, 'LOST',    NOW() - INTERVAL '9 days', 0),
(17, 5,  8,  6700.00, 'LOST',    NOW() - INTERVAL '8 days', 0),
(18, 5,  9,  6900.00, 'WINNING', NOW() - INTERVAL '7 days', 0),
(19, 6,  8,  29500.00, 'LOST',   NOW() - INTERVAL '12 days', 0),
(20, 6,  6,  31000.00, 'WINNING', NOW() - INTERVAL '11 days', 0),
(21, 10, 7,  4350.00, 'LOST',    NOW() - INTERVAL '7 days', 0),
(22, 10, 8,  4500.00, 'WINNING', NOW() - INTERVAL '6 days', 0),
(23, 12, 7,  195.00, 'WINNING', NOW() - INTERVAL '5 days', 0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('bids1', 'id'), (SELECT COALESCE(MAX(id), 1) FROM bids1));
