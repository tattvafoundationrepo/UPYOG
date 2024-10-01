-- Insert data into eg_deonar_stable_unit_shift
TRUNCATE TABLE eg_deonar_stable_unit_shift RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stable_unit_shift (stableUnitId, openTime, closeTime, createdAt, createdBy, updatedAt, updatedBy) VALUES
(1, '12:00 AM', '6:00 AM', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
(1, '6:00 AM', '12:00 PM', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
(1, '12:00 PM', '6:00 PM', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
(1, '6:00 PM', '12:00 AM', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1);

-- Insert data into eg_deonar_stable_unit_animal_mapping
TRUNCATE TABLE eg_deonar_stable_unit_animal_mapping RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stable_unit_animal_mapping (stableUnitShiftId, animalType, createdAt, createdBy, updatedAt, updatedBy) VALUES
(1, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
(2, 2, EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1);

-- Insert data into eg_deonar_stable_unit_charges
TRUNCATE TABLE eg_deonar_stable_unit_charges RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stable_unit_charges (stableUnitShiftId, charges, chargesTypeId, createdAt, createdBy, updatedAt, updatedBy) VALUES
(1, 20.00, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
(2, 30.00, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1);

-- Insert data into eg_deonar_stable_stakeholder
TRUNCATE TABLE eg_deonar_stable_stakeholder RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stable_stakeholder (stableUnitId, stakeholderId, createdAt, createdBy, updatedAt, updatedBy) VALUES
(1, 3, EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
(2, 4, EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1);