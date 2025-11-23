-- Add columns for advance payment tracking
ALTER TABLE egbs_demand_v1 ADD COLUMN IF NOT EXISTS isadvance BOOLEAN DEFAULT FALSE;
ALTER TABLE egbs_demand_v1 ADD COLUMN IF NOT EXISTS advanceindex INTEGER DEFAULT 0;

-- Add columns to audit table as well
ALTER TABLE egbs_demand_v1_audit ADD COLUMN IF NOT EXISTS isadvance BOOLEAN DEFAULT FALSE;
ALTER TABLE egbs_demand_v1_audit ADD COLUMN IF NOT EXISTS advanceindex INTEGER DEFAULT 0;

-- Drop existing unique constraint
DROP INDEX IF EXISTS uk_egbs_demand_v1_consumercode_businessservice;

-- Create new unique constraint including advanceindex
-- This allows multiple demands for same period if advanceindex differs
CREATE UNIQUE INDEX uk_egbs_demand_v1_consumercode_businessservice
ON egbs_demand_v1 (consumercode, tenantid, taxperiodfrom, taxperiodto, businessservice, advanceindex)
WHERE status='ACTIVE';

-- Add index for faster advance demand lookups
CREATE INDEX IF NOT EXISTS idx_egbs_demand_v1_advance
ON egbs_demand_v1 (businessservice, consumercode, isadvance)
WHERE isadvance = TRUE;

COMMENT ON COLUMN egbs_demand_v1.isadvance IS 'Flag indicating if this is an advance payment demand';
COMMENT ON COLUMN egbs_demand_v1.advanceindex IS 'Sequence index for advance demands (0 for regular, 1+ for advance)';
