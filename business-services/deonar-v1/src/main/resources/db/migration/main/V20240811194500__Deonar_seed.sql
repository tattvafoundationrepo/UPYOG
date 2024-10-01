-- Insert data into eg_deonar_animal_type
TRUNCATE TABLE eg_deonar_animal_type RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_animal_type (name, createdAt, createdBy) VALUES
('Sheep', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Goat', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Pig', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Catle', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin');

-- Insert data into eg_deonar_stakeholders_type
TRUNCATE TABLE eg_deonar_stakeholders_type RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stakeholders_type (name, createdAt, createdBy) VALUES
('Trader', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Shopkeeper', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Gawal', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Dawanwala', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('Helkari', EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin');

-- Insert data into eg_deonar_stakeholder
TRUNCATE TABLE eg_deonar_stakeholder RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stakeholder (stakeholderName, address1, pinCode, mobileNumber, email, createdAt, createdBy) VALUES
('John Doe', '1234 Market St', 94103, 1234567890, 'john.doe@example.com', EXTRACT(EPOCH FROM NOW())* 1000, 'admin'),
('Jane Smith', '5678 Mission St', 94110, 2345678901, 'jane.smith@example.com', EXTRACT(EPOCH FROM NOW())* 1000, 'admin'),
('Roni', '1235 Market St', 94103, 1234567892, 'roni@example.com', EXTRACT(EPOCH FROM NOW())* 1000, 'admin'),
('Raje', '5679 Mission St', 94110, 2345678902, 'raja@example.com', EXTRACT(EPOCH FROM NOW())* 1000, 'admin'),
('Alice Johnson', '1122 Broadway Ave', 94115, 3456789012,'alice.johnson@example.com', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin'),
('Bob Brown', '3344 Castro St', 94114, 4567890123,'bob.brown@example.com', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin');

-- Ensure the ids are correct and linked properly
-- Insert data into eg_deonar_stakeholder_animal_type_mapping
TRUNCATE TABLE eg_deonar_stakeholder_animal_type_mapping RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stakeholder_animal_type_mapping (stakeholderTypeId, animalTypeId, createdAt, createdBy) VALUES
(1, 1, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
(2, 2, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin');

-- Insert data into eg_deonar_stakeholder_type_mapping
TRUNCATE TABLE eg_deonar_stakeholder_type_mapping RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stakeholder_type_mapping (stakeholderTypeId, stakeholderId, createdAt, createdBy) VALUES
(1, 1, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
(2, 2, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin');

-- Insert data into eg_deonar_licence
TRUNCATE TABLE eg_deonar_licence RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_licence (licenceNumber, registrationNumber, validFromDate, validToDate, createdAt, createdBy) VALUES
('LIC12345', 'REG123',  EXTRACT(EPOCH FROM '2023-01-01'::timestamp) * 1000,  EXTRACT(EPOCH FROM '2028-01-01'::timestamp) * 1000, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
('LIC67890', 'REG456',  EXTRACT(EPOCH FROM '2023-01-02'::timestamp) * 1000,  EXTRACT(EPOCH FROM '2028-01-02'::timestamp) * 1000, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin');

-- Insert data into eg_deonar_stakeholder_licence_mapping
TRUNCATE TABLE eg_deonar_stakeholder_licence_mapping RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stakeholder_licence_mapping (licenceId, stakeholderId, createdAt, createdBy) VALUES
(1, 1, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin'),
(2, 2, EXTRACT(EPOCH FROM NOW())::BIGINT, 'admin');