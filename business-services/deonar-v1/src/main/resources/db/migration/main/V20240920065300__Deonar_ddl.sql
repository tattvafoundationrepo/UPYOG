-- Create table if not EXISTS eg_deonar_inspection_unit
CREATE TABLE IF NOT EXISTS eg_deonar_inspection_unit(
id SERIAL PRIMARY KEY,
inspectionUnit VARCHAR(255) NOT NULL UNIQUE,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(255) NOT NULL,
updatedBy VARCHAR(255)
);

-- Create table if not EXISTS eg_deonar_animal
CREATE TABLE IF NOT EXISTS eg_deonar_animal(
id SERIAL PRIMARY KEY,
animalType INT NOT NULL,
animalTokenNum VARCHAR(255) NOT NULL UNIQUE,
status VARCHAR(50),
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(255) NOT NULL,
updatedBy VARCHAR(255),
CONSTRAINT fk_animalType FOREIGN KEY (animalType)
REFERENCES eg_deonar_animal_type (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
);

-- Create table if not EXISTS eg_deonar_inspection
CREATE TABLE IF NOT EXISTS eg_deonar_inspection(
id SERIAL PRIMARY KEY,
inspectionDate BIGINT NOT NULL UNIQUE,
inspectionTime VARCHAR(25) NOT NULL,
arrivalId INT NOT NULL,
stakeHolderId INT NOT NULL,
inspectionUnitId INT NOT NULL,
employeeId VARCHAR(255) NOT NULL,
animalId INT NOT NULL,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(255) NOT NULL,
updatedBy VARCHAR(255),
CONSTRAINT fk_inspectionUnitId FOREIGN KEY (inspectionUnitId)
REFERENCES eg_deonar_inspection_unit (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE,
CONSTRAINT fk_animalId FOREIGN KEY (animalId)
REFERENCES eg_deonar_animal (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE,
CONSTRAINT fk_arrivalId FOREIGN KEY (arrivalId)
REFERENCES eg_deonar_arrival (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE,
CONSTRAINT fk_stakeHolderId FOREIGN KEY (stakeHolderId)
REFERENCES eg_deonar_stakeHolder (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE,
CONSTRAINT fk_employeeId FOREIGN KEY (employeeId)
REFERENCES public.eg_hrms_employee(uuid)
  ON UPDATE CASCADE
  ON DELETE CASCADE

);

-- Create table if not EXISTS eg_deonar_inspection_indicators
CREATE TABLE IF NOT EXISTS eg_deonar_inspection_indicators(
id SERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL UNIQUE,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(255) NOT NULL,
updatedBy VARCHAR(255)
);


-- Create table if not EXISTS eg_deonar_inspection_detail
CREATE TABLE IF NOT EXISTS eg_deonar_inspection_detail(
id SERIAL PRIMARY KEY,
inspectionId INT NOT NULL,
inspectionIndicatorId INT NOT NULL,
inspectionIndicatorValue VARCHAR(255) ,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(255) NOT NULL,
updatedBy VARCHAR(255),
CONSTRAINT fk_inspectionId FOREIGN KEY (inspectionId)
REFERENCES eg_deonar_inspection (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE,
CONSTRAINT fk_inspectionIndicatorId FOREIGN KEY (inspectionIndicatorId)
REFERENCES eg_deonar_inspection_indicators (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
);




