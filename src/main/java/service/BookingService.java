package service;

import dao.BookingDao;
import dao.RoomDao;
import dao.ServiceDao;
import dao.impl.BookingDaoImpl;
import dao.impl.RoomDaoImpl;
import dao.impl.ServiceDaoImpl;
import model.Booking;
import model.BookingDetail;
import model.Room;
import model.Service;
import util.DateUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BookingService {
    private final BookingDao bookingDao = new BookingDaoImpl();
    private final RoomDao roomDao = new RoomDaoImpl();
    private final ServiceDao serviceDao = new ServiceDaoImpl();

    public int createBooking(int employeeId,
            int roomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int expectedAttendees,
            Map<Integer, Integer> equipmentRequests,
            Map<Integer, Integer> serviceRequests) {
        validateBookingInput(startTime, endTime, expectedAttendees);

        Room room = roomDao.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong voi id = " + roomId));

        if (!room.isActive()) {
            throw new IllegalArgumentException("Phong khong con hoat dong.");
        }

        if (room.getCapacity() < expectedAttendees) {
            throw new IllegalArgumentException("Suc chua phong khong du cho so nguoi du kien.");
        }

        validateRequestedServices(serviceRequests);

        Booking booking = new Booking();
        booking.setEmployeeId(employeeId);
        booking.setRoomId(roomId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        return bookingDao.createPendingBookingWithDetails(booking, equipmentRequests, serviceRequests);
    }

    public List<BookingDetail> getPendingBookings() {
        return bookingDao.findPendingBookings();
    }

    public List<BookingDetail> getAllBookings() {
        return bookingDao.findAllBookings();
    }

    public List<BookingDetail> getBookingsByEmployee(int employeeId) {
        return bookingDao.findByEmployee(employeeId);
    }

    public List<BookingDetail> getBookingsBySupportStaff(int supportStaffId) {
        return bookingDao.findBySupportStaff(supportStaffId);
    }

    public void approveBooking(int bookingId, int supportStaffId) {
        Optional<Booking> bookingOpt = bookingDao.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay booking.");
        }
        if (!"PENDING".equals(bookingOpt.get().getBookingStatus())) {
            throw new IllegalArgumentException("Chi duoc duyet booking dang PENDING.");
        }

        if (!bookingDao.approvePendingBooking(bookingId, supportStaffId)) {
            throw new IllegalStateException("Khong the duyet booking.");
        }
    }

    public void rejectBooking(int bookingId) {
        bookingDao.rejectPendingBooking(bookingId);
    }

    public void cancelPendingBooking(int bookingId, int employeeId) {
        bookingDao.cancelPendingBooking(bookingId, employeeId);
    }

    public void updatePreparationStatus(int bookingId, int supportStaffId, String prepStatus) {
        if (!"PREPARING".equals(prepStatus)
                && !"READY".equals(prepStatus)
                && !"MISSING_EQUIPMENT".equals(prepStatus)) {
            throw new IllegalArgumentException("Trang thai chuan bi khong hop le.");
        }

        if (!bookingDao.updatePreparationStatus(bookingId, supportStaffId, prepStatus)) {
            throw new IllegalArgumentException("Khong the cap nhat. Kiem tra booking/su phan cong.");
        }
    }

    public List<Service> getAllServices() {
        return serviceDao.findAll();
    }

    public void createService(String serviceName, double price, String unit) {
        validateService(serviceName, price);

        Service service = new Service();
        service.setServiceName(serviceName.trim());
        service.setPrice(price);
        service.setUnit(unit);
        serviceDao.create(service);
    }

    public void updateService(int serviceId, String serviceName, double price, String unit) {
        validateService(serviceName, price);

        Service service = serviceDao.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay dich vu can cap nhat."));

        service.setServiceName(serviceName.trim());
        service.setPrice(price);
        service.setUnit(unit);
        serviceDao.update(service);
    }

    public void deleteService(int serviceId) {
        if (!serviceDao.delete(serviceId)) {
            throw new IllegalArgumentException("Khong tim thay dich vu de xoa.");
        }
    }

    private void validateBookingInput(LocalDateTime startTime, LocalDateTime endTime, int expectedAttendees) {
        if (!DateUtil.isValidRange(startTime, endTime)) {
            throw new IllegalArgumentException("Thoi gian bat dau phai nho hon thoi gian ket thuc.");
        }
        if (!DateUtil.isFuture(startTime)) {
            throw new IllegalArgumentException("Khong duoc dat phong voi thoi gian bat dau trong qua khu.");
        }
        if (expectedAttendees <= 0) {
            throw new IllegalArgumentException("So nguoi du kien phai lon hon 0.");
        }
    }

    private void validateService(String serviceName, double price) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Ten dich vu khong duoc de trong.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Don gia dich vu khong duoc am.");
        }
    }

    private void validateRequestedServices(Map<Integer, Integer> serviceRequests) {
        if (serviceRequests.isEmpty()) {
            return;
        }

        for (Integer serviceId : serviceRequests.keySet()) {
            if (serviceDao.findById(serviceId).isEmpty()) {
                throw new IllegalArgumentException("Dich vu id = " + serviceId + " khong ton tai.");
            }
        }
    }

    /**
     * Lấy danh sách phòng trống trong khoảng thời gian
     * 
     * @param startTime Thời gian bắt đầu
     * @param endTime   Thời gian kết thúc
     * @return Danh sách ID phòng trống
     */
    public List<Integer> findAvailableRoomIds(LocalDateTime startTime, LocalDateTime endTime) {
        return bookingDao.findAvailableRoomIds(startTime, endTime);
    }
}
