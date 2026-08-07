-- Idempotent seed for users1 (BidStream). Login password: BidStream@2026
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
