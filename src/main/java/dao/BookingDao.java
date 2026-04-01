package dao;

import model.Booking;
import model.BookingDetail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for Booking data access operations
 */
public interface BookingDao {
    /**
     * Tạo booking mới với trạng thái PENDING và chi tiết thiết bị/dịch vụ
     * 
     * @param booking           Thông tin booking cơ bản
     * @param equipmentRequests Map của equipment_id -> quantity
     * @param serviceRequests   Map của service_id -> quantity
     * @return ID của booking vừa tạo
     */
    int createPendingBookingWithDetails(Booking booking,
            Map<Integer, Integer> equipmentRequests,
            Map<Integer, Integer> serviceRequests);

    /**
     * Tìm booking theo ID
     * 
     * @param bookingId ID của booking cần tìm
     * @return Optional chứa Booking nếu tìm thấy, rỗng nếu không
     */
    Optional<Booking> findById(int bookingId);

    /**
     * Lấy danh sách booking đang chờ duyệt
     * 
     * @return Danh sách BookingDetail có trạng thái PENDING
     */
    List<BookingDetail> findPendingBookings();

    /**
     * Lấy tất cả booking
     * 
     * @return Danh sách tất cả BookingDetail
     */
    List<BookingDetail> findAllBookings();

    /**
     * Lấy danh sách booking của một nhân viên
     * 
     * @param employeeId ID của nhân viên
     * @return Danh sách BookingDetail của nhân viên
     */
    List<BookingDetail> findByEmployee(int employeeId);

    /**
     * Lấy danh sách booking được giao cho một support staff
     * 
     * @param supportStaffId ID của support staff
     * @return Danh sách BookingDetail được giao cho support staff
     */
    List<BookingDetail> findBySupportStaff(int supportStaffId);

    /**
     * Duyệt booking đang chờ
     * 
     * @param bookingId      ID của booking cần duyệt
     * @param supportStaffId ID của support staff được giao
     * @return true nếu duyệt thành công, false nếu không
     */
    boolean approvePendingBooking(int bookingId, int supportStaffId);

    /**
     * Từ chối booking đang chờ và giải phóng thiết bị
     * 
     * @param bookingId ID của booking cần từ chối
     * @return true nếu từ chối thành công
     */
    boolean rejectPendingBooking(int bookingId);

    /**
     * Hủy booking đang chờ bởi nhân viên
     * 
     * @param bookingId  ID của booking cần hủy
     * @param employeeId ID của nhân viên yêu cầu hủy
     * @return true nếu hủy thành công
     */
    boolean cancelPendingBooking(int bookingId, int employeeId);

    /**
     * Cập nhật trạng thái chuẩn bị của booking
     * 
     * @param bookingId      ID của booking
     * @param supportStaffId ID của support staff
     * @param prepStatus     Trạng thái mới (PREPARING, READY, COMPLETED)
     * @return true nếu cập nhật thành công
     */
    boolean updatePreparationStatus(int bookingId, int supportStaffId, String prepStatus);

    /**
     * Lấy danh sách phòng trống trong khoảng thời gian
     *
     * @param startTime Thời gian bắt đầu
     * @param endTime   Thời gian kết thúc
     * @return Danh sách ID phòng trống
     */
    List<Integer> findAvailableRoomIds(LocalDateTime startTime, LocalDateTime endTime);
}
