-- Insert data into eg_deonar_arrival
TRUNCATE TABLE eg_deonar_arrival RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_arrival (arrivalId, importPermission, stakeholderId, dateOfArrival, timeOfArrival, vehicleNumber, createdAt, createdBy, updatedAt, updatedBy) VALUES
('ARR1001', 'PERM1001', 1, EXTRACT(EPOCH FROM '2023-08-10'::timestamp) * 1000, '10:00:00', 'VH1001', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin'),
('ARR1002', 'PERM1002', 2, EXTRACT(EPOCH FROM '2023-08-11'::timestamp) * 1000, '11:00:00', 'VH1002', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin');

-- Insert data into eg_deonar_animal_at_arrival
TRUNCATE TABLE eg_deonar_animal_at_arrival RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_animal_at_arrival (arrivalId, animalTypeId, count, createdAt, createdBy, updatedAt, updatedBy) VALUES
((SELECT id FROM eg_deonar_arrival WHERE arrivalId = 'ARR1001'), (SELECT id FROM eg_deonar_animal_type WHERE name = 'Sheep'), 5, EXTRACT(EPOCH FROM NOW()) * 1000, 'admin', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin'),
((SELECT id FROM eg_deonar_arrival WHERE arrivalId = 'ARR1001'), (SELECT id FROM eg_deonar_animal_type WHERE name = 'Goat'), 10, EXTRACT(EPOCH FROM NOW()) * 1000, 'admin', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin'),
((SELECT id FROM eg_deonar_arrival WHERE arrivalId = 'ARR1002'), (SELECT id FROM eg_deonar_animal_type WHERE name = 'Pig'), 3, EXTRACT(EPOCH FROM NOW()) * 1000, 'admin', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin'),
((SELECT id FROM eg_deonar_arrival WHERE arrivalId = 'ARR1002'), (SELECT id FROM eg_deonar_animal_type WHERE name = 'Goat'), 7, EXTRACT(EPOCH FROM NOW()) * 1000, 'admin', EXTRACT(EPOCH FROM NOW()) * 1000, 'admin');