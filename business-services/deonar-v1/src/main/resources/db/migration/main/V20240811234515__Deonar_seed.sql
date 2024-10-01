-- Insert data into eg_deonar_collection_type
TRUNCATE TABLE eg_deonar_collection_type RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_collection_type (name, createdAt, createdBy, updatedAt, updatedBy) VALUES
('Entry Fee', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Stabling Fee', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Removal Fee', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Slaughtering Charge', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Weighing Charge', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Washing Charge', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Parking Fee', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Penality', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Inspection', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1);

-- Insert data into eg_deonar_stable_unit
TRUNCATE TABLE eg_deonar_stable_unit RESTART IDENTITY CASCADE;
INSERT INTO eg_deonar_stable_unit (name, createdAt, createdBy, updatedAt, updatedBy) VALUES
('Stable 1', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Stable 2', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Stable 3', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Stable 4', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1),
('Stable 5', EXTRACT(EPOCH FROM NOW()) * 1000, 1, EXTRACT(EPOCH FROM NOW()) * 1000, 1);
