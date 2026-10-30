-- Initial Schema Migration for cardroute-engine

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS card_spend_ledger (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    user_id VARCHAR(100) NOT NULL,
    card_id VARCHAR(50) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    mcc VARCHAR(10) NOT NULL,
    total_amount NUMERIC(15, 4) NOT NULL,
    accelerated_amount NUMERIC(15, 4) NOT NULL,
    gross_reward_earned NUMERIC(15, 4) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ledger_card_billing ON card_spend_ledger(card_id, billing_cycle);
CREATE INDEX IF NOT EXISTS idx_ledger_user_id ON card_spend_ledger(user_id);
CREATE INDEX IF NOT EXISTS idx_ledger_recorded_at ON card_spend_ledger(recorded_at);
