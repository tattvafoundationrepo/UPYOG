-- Add glcode and saccode columns to egbs_demanddetail_v1 table

-- Add glcode column
ALTER TABLE egbs_demanddetail_v1
ADD COLUMN IF NOT EXISTS glcode character varying(250);

-- Add saccode column
ALTER TABLE egbs_demanddetail_v1
ADD COLUMN IF NOT EXISTS saccode character varying(250);

-- Add comments
COMMENT ON COLUMN egbs_demanddetail_v1.glcode IS 'General Ledger Code for accounting';
COMMENT ON COLUMN egbs_demanddetail_v1.saccode IS 'Service Accounting Code for accounting';

-- Add glcode and saccode columns to egbs_demanddetail_v1_audit table

-- Add glcode column to audit table
ALTER TABLE egbs_demanddetail_v1_audit
ADD COLUMN IF NOT EXISTS glcode character varying(250);

-- Add saccode column to audit table
ALTER TABLE egbs_demanddetail_v1_audit
ADD COLUMN IF NOT EXISTS saccode character varying(250);

-- Add comments to audit table
COMMENT ON COLUMN egbs_demanddetail_v1_audit.glcode IS 'General Ledger Code for accounting';
COMMENT ON COLUMN egbs_demanddetail_v1_audit.saccode IS 'Service Accounting Code for accounting';
