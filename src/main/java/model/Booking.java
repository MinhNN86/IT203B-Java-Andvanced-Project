package model;

import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int employeeId;
    private int roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bookingStatus;
    private Integer supportStaffId;
    private String prepStatus;
    private LocalDateTime createdAt;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Integer getSupportStaffId() {
        return supportStaffId;
    }

    public void setSupportStaffId(Integer supportStaffId) {
        this.supportStaffId = supportStaffId;
    }

    public String getPrepStatus() {
        return prepStatus;
    }

    public void setPrepStatus(String prepStatus) {
        this.prepStatus = prepStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
