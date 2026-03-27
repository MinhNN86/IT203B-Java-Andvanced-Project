CREATE DATABASE IF NOT EXISTS prj_meeting_java_05;
USE prj_meeting_java_05;

-- 1. Bảng Người dùng (Users)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('EMPLOYEE', 'SUPPORT_STAFF', 'ADMIN') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Phòng họp (Rooms)
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    location VARCHAR(100),
    fixed_equipment TEXT, -- Mô tả thiết bị cố định
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. Bảng Thiết bị di động (Equipments)
CREATE TABLE equipments (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_name VARCHAR(100) NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') DEFAULT 'ACTIVE'
);

-- 4. Bảng Dịch vụ đi kèm (Services) - Hỗ trợ tính năng Nâng cao 2 (Chi phí)
CREATE TABLE services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Đơn giá
    unit VARCHAR(50) -- Đơn vị tính (chai, người, gói...)
);

-- 5. Bảng Đặt phòng (Bookings) - Bảng trung tâm
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    room_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    booking_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
    support_staff_id INT, -- Người được Admin phân công hỗ trợ
    prep_status ENUM('PENDING', 'PREPARING', 'READY', 'MISSING_EQUIPMENT') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(user_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    FOREIGN KEY (support_staff_id) REFERENCES users(user_id)
);

-- 6. Bảng Chi tiết Thiết bị mượn (Booking_Equipments)
CREATE TABLE booking_equipments (
    booking_id INT,
    equipment_id INT,
    quantity INT NOT NULL,
    PRIMARY KEY (booking_id, equipment_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipments(equipment_id)
);

-- 7. Bảng Chi tiết Dịch vụ (Booking_Services)
CREATE TABLE booking_services (
    booking_id INT,
    service_id INT,
    quantity INT NOT NULL,
    price_at_booking DECIMAL(10, 2) NOT NULL, -- Lưu giá tại thời điểm đặt để làm báo cáo sau này không bị sai lệch nếu giá gốc thay đổi
    PRIMARY KEY (booking_id, service_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(service_id)
);

-- ==========================================================
-- CÁC BẢNG DÀNH CHO KHÔNG GIAN PHÁT TRIỂN NÂNG CAO (Tuỳ chọn)
-- ==========================================================

-- 8. Bảng Đánh giá (Feedbacks)
CREATE TABLE feedbacks (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- 9. Bảng Lịch cá nhân (Personal_Schedules)
CREATE TABLE personal_schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    note VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);