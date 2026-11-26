/**
 * Interface định nghĩa các nghiệp vụ quản lý Ca khám (/api/shifts).
 */
package com.unicare.application.service;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.DoctorOptionResponse;
import com.unicare.application.dto.response.FacilityResponse;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;

import java.time.LocalDate;
import java.util.List;

public interface ShiftService {

    List<FacilityResponse> getFacilities();

    List<ShiftResponse> getShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            ShiftStatus status,
            String room,
            Long facilityId
    );

    default List<ShiftResponse> getShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            ShiftStatus status,
            String room
    ) {
        return getShifts(date, fromDate, toDate, doctorId, serviceType, status, room, null);
    }

    default List<ShiftResponse> getShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            ShiftStatus status
    ) {
        return getShifts(date, fromDate, toDate, doctorId, serviceType, status, null, null);
    }

    List<ShiftResponse> getAvailableShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            String room,
            Long facilityId
    );

    default List<ShiftResponse> getAvailableShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            String room
    ) {
        return getAvailableShifts(date, fromDate, toDate, doctorId, serviceType, room, null);
    }

    default List<ShiftResponse> getAvailableShifts(
            LocalDate date,
            ServiceType serviceType
    ) {
        return getAvailableShifts(date, null, null, null, serviceType, null, null);
    }

    List<DoctorOptionResponse> getAvailableDoctors();

    ShiftResponse getShiftById(Long id);

    ShiftResponse createShift(ShiftRequest request, String currentUsername);

    ShiftResponse updateShift(Long id, ShiftRequest request, String currentUsername);

    void deleteShift(Long id, String currentUsername);

    default void deleteShift(Long id) {
        deleteShift(id, null);
    }
}
