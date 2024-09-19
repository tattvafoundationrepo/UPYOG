          
    
    ALTER TABLE IF EXISTS eg_bmc_userdocument DROP COLUMN IF EXISTS documentnumber;

    ALTER TABLE IF EXISTS eg_bmc_userdocument
    ADD COLUMN documentnumber character varying(255);