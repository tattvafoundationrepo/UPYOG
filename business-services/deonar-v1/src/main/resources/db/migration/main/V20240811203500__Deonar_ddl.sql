-- Create table if not EXISTS eg_deonar_arrival
CREATE TABLE IF NOT EXISTS eg_deonar_arrival (
    id SERIAL PRIMARY KEY,
    arrivalId VARCHAR(255) UNIQUE,
    importPermission VARCHAR(255),
    stakeholderId INT NOT NULL,
    dateOfArrival BIGINT NOT NULL,
    timeOfArrival VARCHAR(25) NOT NULL,
    vehicleNumber VARCHAR(255),
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50),
    CONSTRAINT fk_stakeholderId FOREIGN KEY (stakeholderId)
        REFERENCES eg_deonar_stakeholder (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Create table if not EXISTS eg_deonar_animal_at_arrival
CREATE TABLE IF NOT EXISTS eg_deonar_animal_at_arrival (
    id SERIAL PRIMARY KEY,
    arrivalId INT NOT NULL,
    animalTypeId INT NOT NULL,
    count INT NOT NULL,
    createdAt BIGINT NOT NULL,
    updatedAt BIGINT,
    createdBy VARCHAR(50),
    updatedBy VARCHAR(50),
    CONSTRAINT fk_arrivalId FOREIGN KEY (arrivalId)
        REFERENCES eg_deonar_arrival (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_animalTypeId FOREIGN KEY (animalTypeId)
        REFERENCES eg_deonar_animal_type (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);