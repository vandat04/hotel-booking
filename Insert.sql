USE HotelDB;
GO
--All password: 123123
INSERT INTO Users (username, password_hash, provider, email, email_verified, full_name, phone, avatar_url, role, status, created_at, updated_at)
VALUES
-- ADMIN
('admin_user', '$2a$10$OwEVq/j00c/HhMfHUkJ9JuMVCZGv3XrmXYCWApMqtFO1HdHWN.SdG', 'LOCAL', 'admin@example.com', 1, 'Admin User', '0901000001', 'https://example.com/avatar/admin.png', 'ADMIN', 1, GETDATE(), GETDATE()),
-- CLEANER
('cleaner_user', '$2a$10$OwEVq/j00c/HhMfHUkJ9JuMVCZGv3XrmXYCWApMqtFO1HdHWN.SdG', 'LOCAL', 'cleaner@example.com', 1, 'Cleaner User', '0901000003', 'https://example.com/avatar/cleaner.png', 'CLEANER', 1, GETDATE(), GETDATE()),
-- RECEPTIONIST
('reception_user', '$2a$10$OwEVq/j00c/HhMfHUkJ9JuMVCZGv3XrmXYCWApMqtFO1HdHWN.SdG', 'LOCAL', 'reception@example.com', 1, 'Receptionist User', '0901000004', 'https://example.com/avatar/reception.png', 'RECEPTIONIST', 1, GETDATE(), GETDATE());
GO

INSERT INTO SalaryOfRole (role, salary, created_at, updated_at)
VALUES
('CLEANER', 7000000.00, GETDATE(), GETDATE()),      -- Lương CLEANER: 7 triệu
('RECEPTIONIST', 12000000.00, GETDATE(), GETDATE()); -- Lương RECEPTIONIST: 12 triệu
GO

INSERT INTO SlotWork (start_time, end_time)
VALUES
-- SLOT 1: 0h-8h
('00:00:00', '08:00:00'),
-- SLOT 2: 8h-16h
('08:00:00', '16:00:00'),
-- SLOT 3: 16h-24h
('16:00:00', '23:59:59'); -- SQL Server không cho 24:00:00 nên dùng 23:59:59
GO

INSERT INTO RoomTypes (name, price_hour, price_day, description, status)
VALUES
('Standard', 500000.00, 2000000.00, 'Standard room, suitable for 1-2 guests', 1),
('Deluxe', 800000.00, 3500000.00, 'Deluxe room with nice view, suitable for 2-3 guests', 1),
('Suite', 1500000.00, 6000000.00, 'Luxury suite with full amenities, suitable for small groups', 1),
('VIP', 2500000.00, 10000000.00, 'VIP room with complete amenities, for premium guests', 1),
('GOD', 25000000.00, 10000000.00, 'GOD room with billions', 1);
GO

INSERT INTO RoomPicture (room_type_id, ima_url, public_id)
VALUES
-- Standard Room
(1, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'standard1'),
(1, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'standard2'),
-- Deluxe Room
(2, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'deluxe1'),
(2, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'deluxe2'),
-- Suite Room
(3, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'suite1'),
(3, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'suite2'),
-- VIP Room
(4, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'vip1'),
(4, 'https://res.cloudinary.com/do8uakd0l/image/upload/v1774416942/hotel/room-types/pmdhqiqd5pvwuv7wc7pm.jpg', 'vip2');
GO

INSERT INTO RoomItems (room_type_id, name, quantity, price, status)
VALUES
-- Standard Room Items
(1, 'Single Bed', 2, 50000.00, 1),
(1, 'Wardrobe', 1, 20000.00, 1),
(1, 'Desk', 1, 15000.00, 1),

-- Deluxe Room Items
(2, 'Double Bed', 1, 80000.00, 1),
(2, 'Wardrobe', 1, 25000.00, 1),
(2, 'Desk', 1, 20000.00, 1),
(2, 'TV', 1, 500000.00, 1),

-- Suite Room Items
(3, 'King Bed', 1, 120000.00, 1),
(3, 'Sofa', 1, 60000.00, 1),
(3, 'Wardrobe', 1, 30000.00, 1),
(3, 'Desk', 1, 20000.00, 1),
(3, 'TV', 1, 70000.00, 1),
(3, 'Mini Bar', 1, 40000.00, 1),

-- VIP Room Items
(4, 'King Bed', 1, 150000.00, 1),
(4, 'Sofa', 2, 70000.00, 1),
(4, 'Wardrobe', 2, 35000.00, 1),
(4, 'Desk', 1, 25000.00, 1),
(4, 'TV', 2, 80000.00, 1),
(4, 'Mini Bar', 1, 50000.00, 1),
(4, 'Jacuzzi', 1, 200000.00, 1);
GO

INSERT INTO Rooms (room_number, type_id, status)
VALUES
-- Standard Rooms
('S101', 1, 'AVAILABLE'),
('S102', 1, 'AVAILABLE'),
('S103', 1, 'OCCUPIED'),

-- Deluxe Rooms
('D201', 2, 'AVAILABLE'),
('D202', 2, 'OCCUPIED'),
('D203', 2, 'CLEANING'),

-- Suite Rooms
('SU301', 3, 'AVAILABLE'),
('SU302', 3, 'OCCUPIED'),

-- VIP Rooms
('V401', 4, 'AVAILABLE'),
('V402', 4, 'MAINTENANCE');
GO
