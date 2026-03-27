CREATE DATABASE HotelDB;
GO

USE HotelDB;
GO

CREATE TABLE Users (
    id INT IDENTITY PRIMARY KEY,
    -- Login truyền thống
    username NVARCHAR(50) UNIQUE,
    password_hash NVARCHAR(255),
    -- OAuth
    provider NVARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id NVARCHAR(100),
    email NVARCHAR(100) NOT NULL UNIQUE,
    email_verified BIT DEFAULT 0,
    -- Profile
    full_name NVARCHAR(100),
    phone NVARCHAR(20),
    avatar_url NVARCHAR(255),
    -- Role (dùng INT cho đúng logic)
    role NVARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- ADMIN, CUSTOMER, CLEANER, RECEPTIONIST
    -- Status
    status INT DEFAULT 1, -- 1:ACTIVE, 2:INACTIVE, 3:BANNED
    -- Audit
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME
);
GO

ALTER TABLE Users
ADD CONSTRAINT UQ_User UNIQUE(phone);

--Khi User quên mật khẩu, gửi mã OTP về cho User qua email có thời hạn xử dụng 5p hoặc trước khi hết hạn
CREATE TABLE ResetPasswordOTP (
    id INT IDENTITY PRIMARY KEY,
    user_id INT NOT NULL,
    otp_code NVARCHAR(6) NOT NULL, -- Đổi tên cho rõ nghĩa
    expired_at DATETIME NOT NULL,
    is_used BIT DEFAULT 0,

    CONSTRAINT FK_ResetOTP_Users FOREIGN KEY (user_id)
    REFERENCES Users(id)
);

ALTER TABLE ResetPasswordOTP
ADD CONSTRAINT UQ_ResetOTP_User UNIQUE(user_id);

CREATE TABLE InvalidTokens (
    id INT IDENTITY PRIMARY KEY,
    token NVARCHAR(500),
    expiry_time DATETIME
);

--II. Tính lương: 
-- 1. Điểm danh mỗi ngày trong Attendance
-- 2. Lưu lỗi phạt từng ngày vào trong Penalties
-- 3. tính lương cuối ngày SalaryRecords = Attendance - Penalties
-- 4. Mỗi tháng Salaries = tổng SalaryRecords trong 1 tháng

CREATE TABLE SalaryOfRole (
    id INT IDENTITY PRIMARY KEY,
    role NVARCHAR(20) NOT NULL UNIQUE, -- ADMIN, CLEANER, RECEPTIONIST
    salary DECIMAL(12,2) NOT NULL,     -- lương cơ bản cho role
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME
);
GO

--SLOT 1: 0h-8h
--SLOT 2: 8h-16h
--SLOT 3: 16h-24h
CREATE TABLE SlotWork(
	id INT IDENTITY PRIMARY KEY,
    start_time time,
    end_time time
);
GO

CREATE TABLE Attendance (
    id INT IDENTITY PRIMARY KEY,
    user_id INT NOT NULL,
    work_date DATE, -- ngày làm
    check_in TIME,
    check_out TIME,
    status INT DEFAULT 1,  -- 1: đi làm, 2: nghỉ, 3: đi trễ
    slot_id INT,
    CONSTRAINT FK_Att_User
    FOREIGN KEY (user_id) REFERENCES Users(id),
    CONSTRAINT UQ_Attendance UNIQUE(user_id, work_date)
);
GO

ALTER TABLE Attendance
ADD CONSTRAINT FK_Attendance_Slot
FOREIGN KEY (slot_id) REFERENCES SlotWork(id);
GO

--CHECK-IN trễ, CHECK-OUT sớm : mõi lỗi phạt 100k
CREATE TABLE Penalties (
    id INT IDENTITY PRIMARY KEY,
    user_id INT NOT NULL,
    work_date DATE NOT NULL, -- ngày bị phạt
    amount DECIMAL(12,2) NOT NULL, -- số tiền phạt (luôn là số dương)
    reason NVARCHAR(255),  -- ví dụ: đi trễ, làm sai, nghỉ không phép
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Penalty_User 
    FOREIGN KEY (user_id) REFERENCES Users(id)
);
GO

CREATE TABLE SalaryRecords (
    id INT IDENTITY PRIMARY KEY,
    user_id INT NOT NULL,
    work_date DATE DEFAULT GETDATE(), -- ngày phát sinh tiền
    amount DECIMAL(12,2) NOT NULL, -- số tiền (+ hoặc -)
    type NVARCHAR(50),  -- 'WORK', 'BONUS'
    note NVARCHAR(255),
    batch_id NVARCHAR(100),
    CONSTRAINT FK_Record_User 
    FOREIGN KEY (user_id) REFERENCES Users(id)
);
GO

CREATE TABLE Salaries (
    id INT IDENTITY PRIMARY KEY,
    user_id INT,
    salary_month INT,
    salary_year INT,
    total_salary DECIMAL(12,2),
    status INT DEFAULT 1,
    attendance INT,
    -- 1: chưa trả, 2: đã trả
    CONSTRAINT FK_Salary_User 
    FOREIGN KEY (user_id) REFERENCES Users(id),
    CONSTRAINT UQ_User_Month 
    UNIQUE (user_id, salary_month, salary_year)
);
GO

-- III. CRUD room
-- 1.Admin tạo Room Type: Giá thuê theo từng loại
-- 2. Tạo Room Items có trong loại phòng đó
-- 3. Tạo Room: Tạo hàng loạt hoặc từng cái
CREATE TABLE RoomTypes (
    id INT IDENTITY PRIMARY KEY,
    name NVARCHAR(100),
    price_hour DECIMAL(10,2),
    price_day DECIMAL(10,2),
    description NVARCHAR(255),
    status INT DEFAULT(1)
);
GO

CREATE TABLE RoomPicture (
    id INT IDENTITY PRIMARY KEY,
    room_type_id INT,
    ima_url NVARCHAR(255),
    public_id NVARCHAR(255), -- 🔥 bắt buộc
    FOREIGN KEY (room_type_id) REFERENCES RoomTypes(id)
);

CREATE TABLE RoomItems (
    id INT IDENTITY PRIMARY KEY,
    room_type_id INT,
    name NVARCHAR(100),
    quantity INT,
    price DECIMAL(10,2),
    status INT DEFAULT 1,
    FOREIGN KEY (room_type_id) REFERENCES RoomTypes(id)
);
GO

CREATE TABLE Rooms (
    id INT IDENTITY PRIMARY KEY,
    room_number NVARCHAR(20) UNIQUE,
    type_id INT,
    status NVARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE

    FOREIGN KEY (type_id) REFERENCES RoomTypes(id)
);
GO

-- IV. Booking
-- 1. Customer tạo Booking - Receptionist tạo offline - Đồng bộ từ OTA khác(Giả Lập) 
-- 2. Booking Extend nếu phòng còn đang trống
-- 3. Sau khi booking thành công thì phòng sẽ ở trạng thái hold ở khoảng thời gian đó, tránh double booking hoặc đè lịch
-- 4. Customer sẽ nhận EMAIL: Booking + RoomKey để có thể vào phòng, status sẽ được cập nhật khi booking ở trạng thái check-in
CREATE TABLE Bookings (
    id INT IDENTITY PRIMARY KEY,
    user_id INT,
    check_in DATETIME,
    check_out DATETIME,
    booking_type NVARCHAR(10), -- HOUR / DAY
    status NVARCHAR(20), -- BOOKED, CHECKED_IN, CHECKED_OUT, CANCELLED, PENDING_PAYMENT, FINISHED
    source NVARCHAR(20) DEFAULT 'DIRECT',-- DIRECT, OTA
    channel NVARCHAR(50), -- WALKIN, WEBSITE, AGODA, BOOKING...
    total_price DECIMAL(10,2),
    created_at DATETIME DEFAULT GETDATE(),
    note NVARCHAR(100),
    quantity INT,

    FOREIGN KEY (user_id) REFERENCES Users(id)
);
GO

CREATE TABLE RoomKey (
   id INT IDENTITY PRIMARY KEY,
   room_id INT,
   booking_id INT,
   qr_code NVARCHAR(100),
   number_code NVARCHAR(10),
   status INT DEFAULT 0,
   expired_at DATETIME,

   FOREIGN KEY (booking_id) REFERENCES Bookings(id),
   CONSTRAINT UQ_QR UNIQUE(qr_code),
   CONSTRAINT UQ_Number UNIQUE(number_code),
   FOREIGN KEY (room_id) REFERENCES Rooms(id)
); 
GO

CREATE TABLE BookingExtend (
    id INT IDENTITY PRIMARY KEY,
    booking_id INT,
    old_check_out DATETIME,
    new_check_out DATETIME,
    extra_price DECIMAL(10,2),

    FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);
GO

CREATE TABLE RoomSchedules (
    id INT IDENTITY PRIMARY KEY,
    room_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    booking_id INT, -- liên kết booking nếu có
    status NVARCHAR(20) DEFAULT 'BOOKED', 
    -- BOOKED, OCCUPIED, BLOCKED
    CONSTRAINT FK_Schedule_Room 
    FOREIGN KEY (room_id) REFERENCES Rooms(id),
    CONSTRAINT FK_Schedule_Booking 
    FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);
GO

CREATE INDEX IX_RoomSchedule_Time
ON RoomSchedules(room_id, start_time, end_time);
GO

-- V. Payment
-- 1. Thanh toán bằng VNPAY
-- 2. Thanh toán 30% tiền cọc khi booking
-- 3. Thanh toán phần còn lại và phạt khi Cleaner check ra lỗi = DamagePenalties

CREATE TABLE Payments (
    id INT IDENTITY PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_type NVARCHAR(20) NOT NULL, -- DEPOSIT, FINAL, PENALTY, EXTEND
    source NVARCHAR(20) DEFAULT 'HOTEL', -- HOTEL, OTA
    method NVARCHAR(20), -- CASH, VNPAY
    status NVARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, FAILED, REFUNDED
    transaction_code NVARCHAR(100), -- mã giao dịch VNPAY
    created_at DATETIME DEFAULT GETDATE(),
    paid_at DATETIME,
    note NVARCHAR(255),
    vnp_TxnRef NVARCHAR(255),
    CONSTRAINT FK_Payment_Booking 
    FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);
GO

-- VI. Clean Task:
-- 1. Khi đến thời gian check-out trước 1 tiếng, Khách hàng sẽ nhận thông báo check-out
-- 2. Sau khi Customer check-out, Cleaner nhận thông báo dọn phòng và lưu task vào CleaningTask
-- 3. Khi check phòng, theo danh sách RoomItem đã thiết lập, nếu có cái nào hư thì lưu vào: CheckList, lưu ảnh vào PictureEvidence
-- 4. Tính tổng tiền phạt lưu vào Booking với status: PENALTY

CREATE TABLE CleaningTasks (
    id INT IDENTITY PRIMARY KEY,
    room_id INT,
    cleaner_id INT,
    booking_id INT,
    status NVARCHAR(20), -- PENDING, DOING, DONE
    created_at DATETIME DEFAULT GETDATE(),
    completed_at DATETIME,

    FOREIGN KEY (room_id) REFERENCES Rooms(id),
    FOREIGN KEY (cleaner_id) REFERENCES Users(id),
    FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);
GO

CREATE TABLE CheckList (
    id INT IDENTITY PRIMARY KEY,
    cleaning_task_id INT,
    item_id INT,
    expected_quantity INT,
    actual_quantity INT,
    amount DECIMAL(12,2), -- (expected_quantity - actual_quantity) * price trong RoomItems

    FOREIGN KEY (cleaning_task_id) REFERENCES CleaningTasks(id),
    FOREIGN KEY (item_id) REFERENCES RoomItems(id)
);
GO

CREATE TABLE PictureEvidence (
    id INT IDENTITY PRIMARY KEY,
    checklist_id INT,
    url NVARCHAR(255),

    FOREIGN KEY (checklist_id) REFERENCES CheckList(id)
);
GO

CREATE TABLE Notifications (
    id INT IDENTITY PRIMARY KEY,
    user_id INT,
    message NVARCHAR(255),
    is_read BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    role NVARCHAR(100),

    FOREIGN KEY (user_id) REFERENCES Users(id)
);
GO

CREATE TABLE Reviews (
    id INT IDENTITY PRIMARY KEY,
    booking_id INT,
    user_id INT,
    room_id INT,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Bookings(id),
    FOREIGN KEY (user_id) REFERENCES Users(id),
    FOREIGN KEY (room_id) REFERENCES Rooms(id)
);
GO