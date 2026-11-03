/**
 * DTO trả về thông tin chi tiết của ca khám (Shift).
 */
package com.unicare.application.dto.response;

import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class ShiftResponse {

    private Long id;
    private Long doctorId;
    private Long staffId;
    private String doctorUsername;
    private String doctorName;
    private String staffCode;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ServiceType serviceType;
    private ServiceType shiftType;
    private String room;
    private Integer maxQuantity;
    private Integer maxSlots;
    private Integer bookedQuantity;
    private Integer bookedSlots;
    private Integer availableSlots;
    private ShiftStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ShiftResponse() {
    }

    public static ShiftResponse fromEntity(Shift shift) {
        if (shift == null) {
            return null;
        }
        ShiftResponse response = new ShiftResponse();
        response.setId(shift.getId());

        MedicalStaff staff = shift.getStaff();
        if (staff != null) {
            response.setStaffId(staff.getId());
            response.setDoctorId(staff.getId());
            response.setStaffCode(staff.getStaffCode());
            User user = staff.getUser();
            if (user != null) {
                response.setDoctorUsername(user.getUsername());
                response.setDoctorName(user.getFullName());
            }
        }

        response.setShiftDate(shift.getShiftDate());
        response.setStartTime(shift.getStartTime());
        response.setEndTime(shift.getEndTime());
        response.setServiceType(shift.getShiftType());
        response.setShiftType(shift.getShiftType());
        response.setRoom(shift.getRoom());

        int max = shift.getMaxSlots() != null ? shift.getMaxSlots() : 0;
        int booked = shift.getBookedSlots() != null ? shift.getBookedSlots() : 0;
        response.setMaxSlots(max);
        response.setMaxQuantity(max);
        response.setBookedSlots(booked);
        response.setBookedQuantity(booked);
        response.setAvailableSlots(Math.max(0, max - booked));

        response.setStatus(shift.getStatus());
        response.setCreatedAt(shift.getCreatedAt());
        response.setUpdatedAt(shift.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
        this.staffId = doctorId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
        this.doctorId = staffId;
    }

    public String getDoctorUsername() {
        return doctorUsername;
    }

    public void setDoctorUsername(String doctorUsername) {
        this.doctorUsername = doctorUsername;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        this.shiftType = serviceType;
    }

    public ServiceType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ServiceType shiftType) {
        this.shiftType = shiftType;
        this.serviceType = shiftType;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
        this.maxSlots = maxQuantity;
    }

    public Integer getMaxSlots() {
        return maxSlots;
    }

    public void setMaxSlots(Integer maxSlots) {
        this.maxSlots = maxSlots;
        this.maxQuantity = maxSlots;
    }

    public Integer getBookedQuantity() {
        return bookedQuantity;
    }

    public void setBookedQuantity(Integer bookedQuantity) {
        this.bookedQuantity = bookedQuantity;
        this.bookedSlots = bookedQuantity;
    }

    public Integer getBookedSlots() {
        return bookedSlots;
    }

    public void setBookedSlots(Integer bookedSlots) {
        this.bookedSlots = bookedSlots;
        this.bookedQuantity = bookedSlots;
    }

    public Integer getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
