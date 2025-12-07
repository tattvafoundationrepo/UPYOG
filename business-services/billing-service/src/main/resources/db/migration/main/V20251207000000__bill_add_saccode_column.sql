-- Add saccode column to egbs_billaccountdetail_v1 table
-- This column stores the Service Accounting Code (SAC) for bill account details

ALTER TABLE egbs_billaccountdetail_v1
ADD COLUMN IF NOT EXISTS saccode character varying(250);

COMMENT ON COLUMN egbs_billaccountdetail_v1.saccode
IS 'Service Accounting Code for the bill account detail';
