# Meeting Manager System

Hệ thống quản lý đặt phòng họp với đầy đủ tính năng quản lý phòng, thiết bị, dịch vụ và người dùng.

## 📋 Mục lục

- [Tổng quan](#tổng-quan)
- [Tính năng](#tính-năng)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Cài đặt](#cài-đặt)
- [Sử dụng](#sử dụng)
- [Cơ sở dữ liệu](#cơ-sở-dữ-liệu)
- [Tài liệu API](#tài-liệu-api)

## Tổng quan

Meeting Manager là một ứng dụng console-based cho phép quản lý đặt phòng họp trong tổ chức. Hệ thống hỗ trợ:

- Quản lý phòng họp với thông tin chi tiết
- Quản lý thiết bị và dịch vụ
- Đặt phòng với yêu cầu thiết bị/dịch vụ
- Quy trình duyệt booking qua admin
- Quản lý người dùng với nhiều vai trò

## Tính năng

### 🏢 Quản lý phòng họp
- Thêm, sửa, xóa (deactivate) phòng họp
- Thông tin: tên, sức chứa, vị trí, thiết bị cố định
- Trạng thái active/inactive

### 🔧 Quản lý thiết bị
- Thêm, sửa, xóa thiết bị
- Theo dõi tổng số lượng và số lượng khả dụng
- Trạng thái: ACTIVE, MAINTENANCE, INACTIVE
- Tự động giữ chỗ khi booking

### 📦 Quản lý dịch vụ
- Thêm, sửa, xóa dịch vụ
- Định giá theo đơn vị
- Lưu giá tại thời điểm booking

### 👥 Quản lý người dùng
- **EMPLOYEE**: Đặt phòng, xem booking của mình
- **SUPPORT_STAFF**: Xem booking được giao, cập nhật trạng thái chuẩn bị
- **ADMIN**: Quản lý toàn bộ hệ thống, duyệt booking

### 📅 Quản lý booking
- Tạo booking với yêu cầu thiết bị/dịch vụ
- Trạng thái: PENDING, APPROVED, REJECTED, CANCELLED
- Kiểm tra trùng lịch tự động
- Quy trình duyệt qua admin
- Hủy booking bởi nhân viên (chỉ PENDING)

## Cấu trúc dự án

```
meetingManager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── dao/              # Data Access Objects
│   │   │   │   ├── BaseDao.java
│   │   │   │   ├── BookingDao.java
│   │   │   │   ├── EquipmentDao.java
│   │   │   │   ├── RoomDao.java
│   │   │   │   ├── ServiceDao.java
│   │   │   │   └── UserDao.java
│   │   │   ├── model/            # Domain models
│   │   │   │   ├── Booking.java
│   │   │   │   ├── BookingDetail.java
│   │   │   │   ├── Equipment.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── Service.java
│   │   │   │   └── User.java
│   │   │   ├── service/          # Business logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── BookingService.java
│   │   │   │   ├── EquipmentService.java
│   │   │   │   └── RoomService.java
│   │   │   ├── presentation/     # UI layer
│   │   │   │   ├── AdminView.java
│   │   │   │   ├── EmployeeView.java
│   │   │   │   ├── SupportView.java
│   │   │   │   └── MenuManager.java
│   │   │   ├── util/             # Utilities
│   │   │   │   ├── DateUtil.java
│   │   │   │   ├── DBConnection.java
│   │   │   │   ├── InputValidator.java
│   │   │   │   └── SecurityUtil.java
│   │   │   └── Main.java         # Entry point
│   │   └── resources/
│   │       └── init.sql          # Database schema
├── build.gradle                  # Gradle build config
└── README.md
```

## Cài đặt

### Yêu cầu
- Java 17 hoặc cao hơn
- Gradle 9.0
- MySQL 8.0 hoặc cao hơn

### Các bước cài đặt

1. **Clone repository**
   ```bash
   git clone <repository-url>
   cd meetingManager
   ```

2. **Cấu hình database**
   - Tạo database MySQL mới:
     ```sql
     CREATE DATABASE meeting_manager;
     ```
   - Chạy script khởi tạo:
     ```bash
     mysql -u root -p meeting_manager < src/main/resources/init.sql
     ```

3. **Cấu hình kết nối database**
   - Mở file `src/main/java/util/DBConnection.java`
   - Cập nhật thông tin kết nối:
     ```java
     private static final String URL = "jdbc:mysql://localhost:3306/meeting_manager";
     private static final String USER = "your_username";
     private static final String PASSWORD = "your_password";
     ```

4. **Build project**
   ```bash
   ./gradlew build
   ```

5. **Chạy ứng dụng**
   ```bash
   ./gradlew run
   ```

## Sử dụng

### Đăng nhập

Khi khởi động, bạn sẽ thấy màn hình đăng nhập:

```
===== MEETING MANAGER =====
1. Dang nhap
2. Thoat
Chon: 1
```

Nhập username và password để đăng nhập.

### Tài khoản mặc định

Sau khi chạy `init.sql`, các tài khoản sau sẽ được tạo:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| support | support123 | SUPPORT_STAFF |
| employee | employee123 | EMPLOYEE |

### Màn hình Admin

```
===== ADMIN MENU =====
1. Quan ly phong hop (CRUD)
2. Quan ly thiet bi (CRUD)
3. Quan ly dich vu (CRUD)
4. Quan ly user (CRUD)
5. Duyet/Tu choi booking PENDING
6. Xem tat ca booking
7. Tao tai khoan SUPPORT/ADMIN
0. Dang xuat
```

### Màn hình Employee

```
===== EMPLOYEE MENU =====
1. Dat phong
2. Xem booking cua toi
3. Cap nhat profile
0. Dang xuat
```

### Màn hình Support Staff

```
===== SUPPORT MENU =====
1. Xem booking duoc giao
2. Cap nhat trang thai chuan bi
3. Cap nhat profile
0. Dang xuat
```

## Cơ sở dữ liệu

### Schema chính

#### users
- `user_id`: ID người dùng
- `username`: Tên đăng nhập
- `password`: Mật khẩu (đã hash)
- `role`: Vai trò (EMPLOYEE, SUPPORT_STAFF, ADMIN)
- `full_name`: Họ tên đầy đủ
- `email`: Email
- `phone`: Số điện thoại
- `department`: Phòng ban
- `created_at`: Ngày tạo

#### rooms
- `room_id`: ID phòng
- `room_name`: Tên phòng
- `capacity`: Sức chứa
- `location`: Vị trí
- `fixed_equipment`: Thiết bị cố định
- `is_active`: Trạng thái active

#### equipments
- `equipment_id`: ID thiết bị
- `equipment_name`: Tên thiết bị
- `total_quantity`: Tổng số lượng
- `available_quantity`: Số lượng khả dụng
- `status`: Trạng thái (ACTIVE, MAINTENANCE, INACTIVE)

#### services
- `service_id`: ID dịch vụ
- `service_name`: Tên dịch vụ
- `price`: Đơn giá
- `unit`: Đơn vị tính

#### bookings
- `booking_id`: ID booking
- `employee_id`: ID nhân viên
- `room_id`: ID phòng
- `support_staff_id`: ID support staff
- `start_time`: Thời gian bắt đầu
- `end_time`: Thời gian kết thúc
- `booking_status`: Trạng thái (PENDING, APPROVED, REJECTED, CANCELLED)
- `prep_status`: Trạng thái chuẩn bị (PENDING, PREPARING, READY, COMPLETED)
- `created_at`: Ngày tạo

#### booking_equipments
- `booking_id`: ID booking
- `equipment_id`: ID thiết bị
- `quantity`: Số lượng

#### booking_services
- `booking_id`: ID booking
- `service_id`: ID dịch vụ
- `quantity`: Số lượng
- `price_at_booking`: Giá tại thời điểm booking

## Tài liệu API

### DAO Layer

#### BookingDao
- `createPendingBookingWithDetails()`: Tạo booking mới với thiết bị/dịch vụ
- `findById()`: Tìm booking theo ID
- `findPendingBookings()`: Lấy danh sách booking chờ duyệt
- `findAllBookings()`: Lấy tất cả booking
- `findByEmployee()`: Lấy booking của nhân viên
- `findBySupportStaff()`: Lấy booking của support staff
- `approvePendingBooking()`: Duyệt booking
- `rejectPendingBooking()`: Từ chối booking
- `cancelPendingBooking()`: Hủy booking
- `updatePreparationStatus()`: Cập nhật trạng thái chuẩn bị

#### EquipmentDao
- `findAll()`: Lấy tất cả thiết bị
- `findById()`: Tìm thiết bị theo ID
- `create()`: Tạo thiết bị mới
- `update()`: Cập nhật thiết bị
- `delete()`: Xóa thiết bị
- `reserveForBooking()`: Giữ chỗ thiết bị cho booking
- `releaseForBooking()`: Giải phóng thiết bị

#### RoomDao
- `findAll()`: Lấy tất cả phòng
- `findById()`: Tìm phòng theo ID
- `create()`: Tạo phòng mới
- `update()`: Cập nhật phòng
- `deactivate()`: Vô hiệu hóa phòng

#### ServiceDao
- `findAll()`: Lấy tất cả dịch vụ
- `findById()`: Tìm dịch vụ theo ID
- `create()`: Tạo dịch vụ mới
- `update()`: Cập nhật dịch vụ
- `delete()`: Xóa dịch vụ

#### UserDao
- `findByUsername()`: Tìm user theo username
- `findById()`: Tìm user theo ID
- `createUser()`: Tạo user mới
- `updateProfile()`: Cập nhật profile
- `findByRole()`: Lấy user theo role
- `getAllUsers()`: Lấy tất cả user
- `updateUser()`: Cập nhật user
- `deleteUser()`: Xóa user

## Quy trình booking

1. **Employee đặt phòng**
   - Chọn phòng, thời gian
   - Chọn thiết bị cần thiết
   - Chọn dịch vụ cần thiết
   - Booking được tạo với trạng thái PENDING

2. **Admin duyệt booking**
   - Xem danh sách booking PENDING
   - Chọn booking để duyệt/từ chối
   - Nếu duyệt: Gán support staff, chuyển sang APPROVED
   - Nếu từ chối: Giải phóng thiết bị, chuyển sang REJECTED

3. **Support Staff chuẩn bị**
   - Xem booking được giao (APPROVED)
   - Cập nhật trạng thái chuẩn bị: PREPARING → READY → COMPLETED

4. **Employee hủy booking**
   - Chỉ có thể hủy khi đang PENDING
   - Thiết bị được giải phóng tự động

## Tác giả

Meeting Manager System - IT203B Project

## Giấy phép

Dự án được tạo ra cho mục đích học tập.
