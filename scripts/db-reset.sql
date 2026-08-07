\set ON_ERROR_STOP on
-- ============================================================
-- BidStream - Full database reset
-- Drops legacy/orphaned tables, truncates the active application
-- tables and resets identity sequences.
--
-- Safe to run while services are stopped. Idempotent.
--
-- Usage:
--   psql -U postgres -h localhost -d bidstream_users -f scripts\db-reset.sql
-- ============================================================

\c bidstream_users
DROP TABLE IF EXISTS user3;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS flyway_schema_history;
TRUNCATE TABLE users1 RESTART IDENTITY;

\c bidstream_auctions
DROP TABLE IF EXISTS auctions;
DROP TABLE IF EXISTS flyway_schema_history;
TRUNCATE TABLE auctions1 RESTART IDENTITY;

\c bidstream_bids
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS flyway_schema_history;
TRUNCATE TABLE bids1 RESTART IDENTITY;

\c bidstream_notifications
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS flyway_schema_history;
TRUNCATE TABLE notifications1 RESTART IDENTITY;
