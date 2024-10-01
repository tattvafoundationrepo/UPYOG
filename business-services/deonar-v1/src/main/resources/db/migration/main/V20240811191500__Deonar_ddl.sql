-- Create table if not EXISTS eg_deonar_animal_type
CREATE TABLE IF NOT EXISTS eg_deonar_animal_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50)
);

-- Create table if not EXISTS eg_deonar_stakeholders_type
CREATE TABLE IF NOT EXISTS eg_deonar_stakeholders_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50)
);

-- Create table if not EXISTS eg_deonar_stakeholder_animal_type_mapping
CREATE TABLE IF NOT EXISTS eg_deonar_stakeholder_animal_type_mapping (
    id SERIAL PRIMARY KEY,
    stakeholderTypeId INT NOT NULL,
    animalTypeId INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50),
    CONSTRAINT fk_stakeholderTypeId FOREIGN KEY (stakeholderTypeId)
        REFERENCES eg_deonar_stakeholders_type (id),
    CONSTRAINT fk_animalTypeId FOREIGN KEY (animalTypeId)
        REFERENCES eg_deonar_animal_type (id)
);

-- Create table if not EXISTS eg_deonar_stakeholder
CREATE TABLE IF NOT EXISTS eg_deonar_stakeholder (
    id SERIAL PRIMARY KEY,
    stakeholderName VARCHAR(50) NOT NULL,
    address1 VARCHAR(255),
    address2 VARCHAR(255),
    pinCode INT,
    mobileNumber BIGINT,
    contactNumber BIGINT,
    email VARCHAR(50),
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50)
);

-- Create table if not EXISTS eg_deonar_stakeholder_type_mapping
CREATE TABLE IF NOT EXISTS eg_deonar_stakeholder_type_mapping (
    id SERIAL PRIMARY KEY,
    stakeholderTypeId INT NOT NULL,
    stakeholderId INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50),
    CONSTRAINT fk_stakeholderTypeId FOREIGN KEY (stakeholderTypeId)
        REFERENCES eg_deonar_stakeholders_type (id),
    CONSTRAINT fk_stakeholderId FOREIGN KEY (stakeholderId)
        REFERENCES eg_deonar_stakeholder (id)
);

-- Create table if not EXISTS eg_deonar_licence
CREATE TABLE IF NOT EXISTS eg_deonar_licence (
    id SERIAL PRIMARY KEY,
    licenceNumber VARCHAR(255) NOT NULL,
    registrationNumber VARCHAR(255) NOT NULL,
    validFromDate BIGINT NOT NULL,
    validToDate BIGINT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50)
);

-- Create table if not EXISTS eg_deonar_stakeholder_licence_mapping
CREATE TABLE IF NOT EXISTS eg_deonar_stakeholder_licence_mapping (
    id SERIAL PRIMARY KEY,
    licenceId INT NOT NULL,
    stakeholderId INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50),
    CONSTRAINT fk_licenceId FOREIGN KEY (licenceId)
        REFERENCES eg_deonar_licence (id),
    CONSTRAINT fk_stakeholderId FOREIGN KEY (stakeholderId)
        REFERENCES eg_deonar_stakeholder (id)
);