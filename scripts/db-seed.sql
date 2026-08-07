\set ON_ERROR_STOP on
-- ============================================================
-- BidStream - Realistic seed data
-- Idempotent: safe to re-run (every INSERT uses ON CONFLICT DO NOTHING).
-- Uses NOW()-relative timestamps so the seed stays valid whenever run.
--
-- All cross-service ids are aligned by convention (no cross-db FKs):
--   auctions1.seller_id -> users1.id
--   bids1.auction_id    -> auctions1.id   | bids1.bidder_id -> users1.id
--   notifications1.user_id -> users1.id
--
-- After seeding, setval bumps identity sequences past seeded ids so
-- live inserts never collide.
--
-- Usage:
--   psql -U postgres -h localhost -d bidstream_users -f scripts\db-seed.sql
-- ============================================================

-- ------------------------------------------------------------------
-- bidstream_users.users1
-- Login password for ALL seed users:  BidStream@2026
-- ------------------------------------------------------------------
\c bidstream_users
INSERT INTO users1 (id, username, email, password, full_name, role, active, created_at) VALUES
(1,  'alex.morgan',        'alex.morgan@bidstream.demo',        '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Alex Morgan',        'ADMIN',  true,  NOW() - INTERVAL '30 days'),
(2,  'sophia.chen',        'sophia.chen@bidstream.demo',        '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Sophia Chen',        'SELLER', true,  NOW() - INTERVAL '28 days'),
(3,  'liam.bennett',       'liam.bennett@bidstream.demo',       '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Liam Bennett',       'SELLER', true,  NOW() - INTERVAL '25 days'),
(4,  'emma.davis',         'emma.davis@bidstream.demo',         '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Emma Davis',         'SELLER', true,  NOW() - INTERVAL '22 days'),
(5,  'noah.martin',        'noah.martin@bidstream.demo',        '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Noah Martin',        'SELLER', true,  NOW() - INTERVAL '20 days'),
(6,  'olivia.wilson',      'olivia.wilson@bidstream.demo',      '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Olivia Wilson',      'USER',   true,  NOW() - INTERVAL '18 days'),
(7,  'ethan.brown',        'ethan.brown@bidstream.demo',        '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Ethan Brown',        'USER',   true,  NOW() - INTERVAL '15 days'),
(8,  'ava.johnson',        'ava.johnson@bidstream.demo',        '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Ava Johnson',        'USER',   true,  NOW() - INTERVAL '12 days'),
(9,  'mason.garcia',       'mason.garcia@bidstream.demo',       '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Mason Garcia',       'USER',   true,  NOW() - INTERVAL '10 days'),
(10, 'isabella.rodriguez', 'isabella.rodriguez@bidstream.demo', '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Isabella Rodriguez', 'USER',   true,  NOW() - INTERVAL '8 days'),
(11, 'lucas.nguyen',       'lucas.nguyen@bidstream.demo',       '$2a$10$91hxKrf0qEgSlss907kote2fnnOLrgGXQXVkRPNfp7ZvXMDKJSmv2', 'Lucas Nguyen',       'USER',   false, NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('users1', 'id'), (SELECT COALESCE(MAX(id), 1) FROM users1));

-- ------------------------------------------------------------------
-- bidstream_auctions.auctions1
-- Statuses: PENDING (scheduled), ACTIVE (biddable now),
--           CLOSED (ended with winner), EXPIRED (ended with no winner)
-- Auction #6 is CLOSED with end_time > 5 days ago -> eligible for
-- the nightly cleanup (deleteOldAuctions).
-- ------------------------------------------------------------------
\c bidstream_auctions
INSERT INTO auctions1 (id, title, description, seller_id, starting_price, current_price, highest_bid_id, highest_bidder_id, start_time, end_time, status, created_at) VALUES
(1,  'Sony Alpha A7 III Mirrorless Camera', 'Full-frame mirrorless camera body with 24.2MP sensor, includes box and original accessories.', 2, 1200.00, 1200.00, NULL, NULL, NOW() + INTERVAL '2 days',  NOW() + INTERVAL '9 days',  'PENDING', NOW() - INTERVAL '1 day'),
(2,  'MacBook Pro 14-inch M3 (2023)',       'Apple MacBook Pro 14 with M3 chip, 16GB RAM, 512GB SSD. Battery health 92%.',              2, 1800.00, 2200.00, 6,    6,    NOW() - INTERVAL '3 days', NOW() + INTERVAL '4 days',  'ACTIVE',  NOW() - INTERVAL '4 days'),
(3,  'Canon EF 70-200mm f/2.8L IS III Lens', 'Professional telephoto zoom lens in excellent condition with hood, caps and case.',      2, 900.00,  900.00,  NULL, NULL, NOW() - INTERVAL '1 day',  NOW() + INTERVAL '6 days',  'ACTIVE',  NOW() - INTERVAL '2 days'),
(4,  'Omega Seamaster 300 Diver Watch',     'Automatic dive watch, 41mm, includes papers and box, serviced 2025.',                      3, 3200.00, 3350.00, 8,    7,    NOW() - INTERVAL '5 days', NOW() + INTERVAL '2 days',  'ACTIVE',  NOW() - INTERVAL '6 days'),
(5,  'Rolex Datejust 41 Blue Dial Watch',   'Datejust 41 with fluted bezel and jubilee bracelet, full set with warranty card.',        3, 6500.00, 6900.00, 18,   9,    NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days', 'CLOSED',  NOW() - INTERVAL '11 days'),
(6,  '1969 Ford Mustang Mach 1 Classic',    'Restored 1969 Mustang Mach 1, 351 Windsor V8, 4-speed, California car.',                4, 28000.00, 31000.00, 20,   6,    NOW() - INTERVAL '14 days', NOW() - INTERVAL '7 days', 'CLOSED',  NOW() - INTERVAL '15 days'),
(7,  'Tesla Model 3 Long Range 2021',       '2021 Tesla Model 3 Long Range AWD, 45,000 miles, autopilot, no accidents.',              4, 31000.00, 31000.00, NULL, NULL, NOW() - INTERVAL '12 days', NOW() - INTERVAL '2 days', 'EXPIRED', NOW() - INTERVAL '13 days'),
(8,  'Bose QuietComfort 45 Headphones',     'Wireless noise-cancelling headphones, comes with case and cables.',                      5, 250.00,  280.00,  11,   8,    NOW() - INTERVAL '6 days', NOW() + INTERVAL '1 day',  'ACTIVE',  NOW() - INTERVAL '7 days'),
(9,  'Patek Philippe Nautilus 5711 Watch',  'Nautilus 5711 in stainless steel, blue dial, box and papers included.',                 3, 45000.00, 45000.00, NULL, NULL, NOW() + INTERVAL '5 days',  NOW() + INTERVAL '12 days', 'PENDING', NOW() - INTERVAL '1 day'),
(10, 'Leica Q2 Digital Camera',             'Compact full-frame camera with 28mm Summilux lens, excellent condition.',               2, 4200.00, 4500.00, 22,   8,    NOW() - INTERVAL '8 days', NOW() - INTERVAL '2 days', 'CLOSED',  NOW() - INTERVAL '9 days'),
(11, 'Ducati Monster 937 Motorcycle',       '2022 Ducati Monster 937, 3,200 miles, full service history, clean title.',             4, 9800.00, 10500.00, 15,   9,    NOW() - INTERVAL '2 days', NOW() + INTERVAL '5 days',  'ACTIVE',  NOW() - INTERVAL '3 days'),
(12, 'Kenwood Chef XL Stand Mixer',         'Chef XL KVL8300S stand mixer, rarely used, all attachments included.',                   3, 180.00,  195.00,  23,   7,    NOW() - INTERVAL '6 days', NOW() - INTERVAL '1 day',  'CLOSED',  NOW() - INTERVAL '7 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('auctions1', 'id'), (SELECT COALESCE(MAX(id), 1) FROM auctions1));

-- ------------------------------------------------------------------
-- bidstream_bids.bids1
-- Status rules (mirror live app behavior):
--   ACTIVE auction: top bid = PLACED, older = OUTBID
--   CLOSED auction: winner = WINNING, others = LOST
--   PENDING/EXPIRED auctions: no bids
-- Edge cases covered: no-bid auctions (1,3,7,9), multi-bid auctions
-- (2,5,8,11), single-bid winner (12), active top (2,4,8,11).
-- ------------------------------------------------------------------
\c bidstream_bids
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

-- ------------------------------------------------------------------
-- bidstream_notifications.notifications1
-- Bid-related notifications (BID_PLACED / BID_OUTBID) are seeded here.
-- Auction lifecycle notifications (AUCTION_CREATED / AUCTION_STARTED /
-- AUCTION_CLOSED) are generated live from the Kafka event replay in
-- publish-seed-events.ps1.
-- ------------------------------------------------------------------
\c bidstream_notifications
INSERT INTO notifications1 (id, user_id, notification_type, title, message, read, created_at) VALUES
(1,  6,  'BID_PLACED', 'Bid Placed',       'Your bid of $2200.00 was placed on auction ''MacBook Pro 14-inch M3 (2023)''.',                             false, NOW() - INTERVAL '5 hours'),
(2,  8,  'BID_PLACED', 'Bid Placed',       'Your bid of $3350.00 was placed on auction ''Omega Seamaster 300 Diver Watch''.',                            false, NOW() - INTERVAL '3 days'),
(3,  8,  'BID_PLACED', 'Bid Placed',       'Your bid of $280.00 was placed on auction ''Bose QuietComfort 45 Headphones''.',                            false, NOW() - INTERVAL '2 days'),
(4,  9,  'BID_PLACED', 'Bid Placed',       'Your bid of $10500.00 was placed on auction ''Ducati Monster 937 Motorcycle''.',                             false, NOW() - INTERVAL '6 hours'),
(5,  7,  'BID_PLACED', 'Bid Placed',       'Your bid of $195.00 was placed on auction ''Kenwood Chef XL Stand Mixer''.',                                true,  NOW() - INTERVAL '5 days'),
(6,  6,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''MacBook Pro 14-inch M3 (2023)'' has been outbid.',                                  true,  NOW() - INTERVAL '2 days'),
(7,  7,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''MacBook Pro 14-inch M3 (2023)'' has been outbid.',                                  true,  NOW() - INTERVAL '3 days'),
(8,  8,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''MacBook Pro 14-inch M3 (2023)'' has been outbid.',                                  false, NOW() - INTERVAL '1 day'),
(9,  8,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''Omega Seamaster 300 Diver Watch'' has been outbid.',                                 false, NOW() - INTERVAL '4 days'),
(10, 7,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''Bose QuietComfort 45 Headphones'' has been outbid.',                                true,  NOW() - INTERVAL '4 days'),
(11, 6,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''Bose QuietComfort 45 Headphones'' has been outbid.',                                false, NOW() - INTERVAL '2 days'),
(12, 10, 'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''Ducati Monster 937 Motorcycle'' has been outbid.',                                 false, NOW() - INTERVAL '1 day'),
(13, 6,  'BID_OUTBID', 'You''ve Been Outbid', 'Your bid on auction ''Ducati Monster 937 Motorcycle'' has been outbid.',                                 true,  NOW() - INTERVAL '2 days'),
(14, 2,  'INFO',       'Weekly Digest',      'Your active listings received 6 bids this week. Review your auctions dashboard.',                            false, NOW() - INTERVAL '1 day'),
(15, 3,  'INFO',       'Auction Success',    'Your auction ''Rolex Datejust 41 Blue Dial Watch'' closed at $6900.00.',                                   true,  NOW() - INTERVAL '3 days'),
(16, 5,  'INFO',       'Listing Tip',        'Add high-resolution photos to ''Bose QuietComfort 45 Headphones'' to increase bidder interest.',          false, NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('notifications1', 'id'), (SELECT COALESCE(MAX(id), 1) FROM notifications1));
