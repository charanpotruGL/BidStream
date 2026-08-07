-- Idempotent seed for notifications1 (BidStream).
-- Bid-related notifications are seeded here; auction lifecycle
-- notifications (AUCTION_CREATED/STARTED/CLOSED) are produced live by
-- the Kafka event replay in scripts/publish-seed-events.ps1.
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
