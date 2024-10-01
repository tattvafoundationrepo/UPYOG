-- Create table if not EXISTS for Collection Type
CREATE TABLE IF NOT EXISTS eg_deonar_collection_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy INT NOT NULL,
    updatedBy INT
);

-- Create table if not EXISTS for Stable Unit
CREATE TABLE IF NOT EXISTS eg_deonar_stable_unit (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy INT NOT NULL,
    updatedBy INT
);

-- Create table if not EXISTS for Stable Unit Shift
CREATE TABLE IF NOT EXISTS eg_deonar_stable_unit_shift (
    id SERIAL PRIMARY KEY,
    stableUnitId INT NOT NULL,
    openTime VARCHAR(255) NOT NULL,
    closeTime VARCHAR(255) NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy INT NOT NULL,
    updatedBy INT,
    CONSTRAINT fk_stableUnitId FOREIGN KEY (stableUnitId)
        REFERENCES eg_deonar_stable_unit (id)
);

-- Create table if not EXISTS for Stable Unit Animal Mapping
CREATE TABLE IF NOT EXISTS eg_deonar_stable_unit_animal_mapping (
    id SERIAL PRIMARY KEY,
    stableUnitShiftId INT NOT NULL,
    animalType INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy INT NOT NULL,
    updatedBy INT,
    CONSTRAINT fk_stableUnitShiftId FOREIGN KEY (stableUnitShiftId)
        REFERENCES eg_deonar_stable_unit_shift (id)
);

-- Create table if not EXISTS for Stable Unit Charges
CREATE TABLE IF NOT EXISTS eg_deonar_stable_unit_charges (
    id SERIAL PRIMARY KEY,
    stableUnitShiftId INT NOT NULL,
    charges DECIMAL NOT NULL,
    chargesTypeId INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy INT NOT NULL,
    updatedBy INT,
    CONSTRAINT fk_stableUnitShiftIdCharges FOREIGN KEY (stableUnitShiftId)
        REFERENCES eg_deonar_stable_unit_shift (id)
);

-- Create table if not EXISTS for Stable Stakeholder
CREATE TABLE IF NOT EXISTS eg_deonar_stable_stakeholder (
    id SERIAL PRIMARY KEY,
    stableUnitId INT NOT NULL,
    stakeholderId INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy INT NOT NULL,
    updatedBy INT,
    CONSTRAINT fk_stableUnitIdStakeholder FOREIGN KEY (stableUnitId)
        REFERENCES eg_deonar_stable_unit (id),
    CONSTRAINT fk_stakeholderId FOREIGN KEY (stakeholderId)
        REFERENCES eg_deonar_stakeholder (id)
);