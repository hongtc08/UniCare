/**
 * Interface định nghĩa các nghiệp vụ quản lý Ca khám (/api/shifts).
 */
package com.unicare.application.service;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.DoctorOptionResponse;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;

import java.time.LocalDate;
import java.util.List;

public interface ShiftService {

    List<ShiftResponse> getShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            ShiftStatus status,
            String room
    );

    default List<ShiftResponse> getShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            ShiftStatus status
    ) {
        return getShifts(date, fromDate, toDate, doctorId, serviceType, status, null);
    }

    List<DoctorOptionResponse> getAvailableDoctors();

    ShiftResponse getShiftById(Long id);

    ShiftResponse createShift(ShiftRequest request, String currentUsername);

    ShiftResponse updateShift(Long id, ShiftRequest request, String currentUsername);

    void deleteShift(Long id);
}
