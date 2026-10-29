/**
 * DTO trả về thông tin chi tiết của ca khám (Shift).
 */
package com.unicare.application.dto.response;

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
    private String doctorUsername;
    private String doctorName;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ServiceType serviceType;
    private String room;
    private Integer maxQuantity;
    private Integer bookedQuantity;
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
        User doctor = shift.getDoctor();
        if (doctor != null) {
            response.setDoctorId(doctor.getId());
            response.setDoctorUsername(doctor.getUsername());
            response.setDoctorName(doctor.getFullName());
        }
        response.setShiftDate(shift.getShiftDate());
        response.setStartTime(shift.getStartTime());
        response.setEndTime(shift.getEndTime());
        response.setServiceType(shift.getServiceType());
        response.setRoom(shift.getRoom());
        int maxQty = shift.getMaxQuantity() != null ? shift.getMaxQuantity() : 0;
        int bookedQty = shift.getBookedQuantity() != null ? shift.getBookedQuantity() : 0;
        response.setMaxQuantity(maxQty);
        response.setBookedQuantity(bookedQty);
        response.setAvailableSlots(Math.max(0, maxQty - bookedQty));
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
    }

    public Integer getBookedQuantity() {
        return bookedQuantity;
    }

    public void setBookedQuantity(Integer bookedQuantity) {
        this.bookedQuantity = bookedQuantity;
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
