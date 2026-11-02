package com.unicare.application.dto;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import com.unicare.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftDtoTest {

    @Test
    @DisplayName("Kiểm tra toàn bộ getters, setters của ShiftRequest")
    void shiftRequest_Coverage() {
        ShiftRequest req = new ShiftRequest();
        req.setDoctorId(10L);
        assertThat(req.getDoctorId()).isEqualTo(10L);
        assertThat(req.getStaffId()).isEqualTo(10L);

        req.setStaffId(20L);
        assertThat(req.getDoctorId()).isEqualTo(20L);
        assertThat(req.getStaffId()).isEqualTo(20L);

        LocalDate date = LocalDate.of(2026, 11, 20);
        req.setShiftDate(date);
        assertThat(req.getShiftDate()).isEqualTo(date);

        LocalTime start = LocalTime.of(8, 0);
        req.setStartTime(start);
        assertThat(req.getStartTime()).isEqualTo(start);

        LocalTime end = LocalTime.of(9, 30);
        req.setEndTime(end);
        assertThat(req.getEndTime()).isEqualTo(end);

        req.setServiceType(ServiceType.KHAM_DA_KHOA);
        assertThat(req.getServiceType()).isEqualTo(ServiceType.KHAM_DA_KHOA);
        assertThat(req.getShiftType()).isEqualTo(ServiceType.KHAM_DA_KHOA);

        req.setShiftType(ServiceType.TU_VAN_TAM_LY);
        assertThat(req.getServiceType()).isEqualTo(ServiceType.TU_VAN_TAM_LY);
        assertThat(req.getShiftType()).isEqualTo(ServiceType.TU_VAN_TAM_LY);

        req.setRoom("Phòng 205");
        assertThat(req.getRoom()).isEqualTo("Phòng 205");

        req.setMaxQuantity(15);
        assertThat(req.getMaxQuantity()).isEqualTo(15);
        assertThat(req.getMaxSlots()).isEqualTo(15);

        req.setMaxSlots(25);
        assertThat(req.getMaxQuantity()).isEqualTo(25);
        assertThat(req.getMaxSlots()).isEqualTo(25);

        req.setStatus(ShiftStatus.OPEN);
        assertThat(req.getStatus()).isEqualTo(ShiftStatus.OPEN);
    }

    @Test
    @DisplayName("Kiểm tra toàn bộ getters, setters và fromEntity của ShiftResponse")
    void shiftResponse_Coverage() {
        assertThat(ShiftResponse.fromEntity(null)).isNull();

        User user = new User("bs01", "pwd", "BS. Tran Van B", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        user.setId(5L);

        MedicalStaff staff = MedicalStaff.builder()
                .id(5L)
                .user(user)
                .staffCode("BS0005")
                .title("Bác sĩ")
                .specialty("Đa khoa")
                .build();

        Shift shift = Shift.builder()
                .id(1L)
                .staff(staff)
                .shiftDate(LocalDate.of(2026, 11, 10))
                .startTime(LocalTime.of(7, 30))
                .endTime(LocalTime.of(9, 0))
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .room("P101")
                .maxSlots(10)
                .bookedSlots(3)
                .status(ShiftStatus.OPEN)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        ShiftResponse res = ShiftResponse.fromEntity(shift);
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getDoctorId()).isEqualTo(5L);
        assertThat(res.getStaffId()).isEqualTo(5L);
        assertThat(res.getDoctorUsername()).isEqualTo("bs01");
        assertThat(res.getDoctorName()).isEqualTo("BS. Tran Van B");
        assertThat(res.getStaffCode()).isEqualTo("BS0005");
        assertThat(res.getMaxSlots()).isEqualTo(10);
        assertThat(res.getMaxQuantity()).isEqualTo(10);
        assertThat(res.getBookedSlots()).isEqualTo(3);
        assertThat(res.getBookedQuantity()).isEqualTo(3);
        assertThat(res.getAvailableSlots()).isEqualTo(7);
        assertThat(res.getServiceType()).isEqualTo(ServiceType.KHAM_DA_KHOA);
        assertThat(res.getShiftType()).isEqualTo(ServiceType.KHAM_DA_KHOA);
        assertThat(res.getRoom()).isEqualTo("P101");
        assertThat(res.getStatus()).isEqualTo(ShiftStatus.OPEN);
        assertThat(res.getCreatedAt()).isNotNull();
        assertThat(res.getUpdatedAt()).isNotNull();

        // Branches for null staff and null user
        Shift shiftNullStaff = new Shift();
        shiftNullStaff.setMaxSlots(null);
        shiftNullStaff.setBookedSlots(null);
        ShiftResponse resNullStaff = ShiftResponse.fromEntity(shiftNullStaff);
        assertThat(resNullStaff.getMaxSlots()).isZero();
        assertThat(resNullStaff.getBookedSlots()).isZero();
        assertThat(resNullStaff.getStaffId()).isNull();

        MedicalStaff staffNullUser = MedicalStaff.builder().id(20L).user(null).staffCode("BS0020").build();
        Shift shiftNullUser = Shift.builder().staff(staffNullUser).maxSlots(10).bookedSlots(2).build();
        ShiftResponse resNullUser = ShiftResponse.fromEntity(shiftNullUser);
        assertThat(resNullUser.getStaffId()).isEqualTo(20L);
        assertThat(resNullUser.getDoctorUsername()).isNull();

        // Direct setters & getters
        ShiftResponse direct = new ShiftResponse();
        direct.setId(2L);
        direct.setStaffId(6L);
        direct.setDoctorUsername("bs02");
        direct.setDoctorName("BS 2");
        direct.setStaffCode("BS0006");
        direct.setShiftDate(LocalDate.now());
        direct.setStartTime(LocalTime.MIN);
        direct.setEndTime(LocalTime.MAX);
        direct.setServiceType(ServiceType.TU_VAN_TAM_LY);
        direct.setShiftType(ServiceType.TU_VAN_TAM_LY);
        direct.setRoom("R2");
        direct.setMaxQuantity(5);
        direct.setMaxSlots(5);
        direct.setBookedQuantity(1);
        direct.setBookedSlots(1);
        direct.setAvailableSlots(4);
        direct.setStatus(ShiftStatus.FULL);
        OffsetDateTime dt = OffsetDateTime.now();
        direct.setCreatedAt(dt);
        direct.setUpdatedAt(dt);

        assertThat(direct.getId()).isEqualTo(2L);
        assertThat(direct.getDoctorId()).isEqualTo(6L);
        assertThat(direct.getStaffId()).isEqualTo(6L);
        assertThat(direct.getDoctorUsername()).isEqualTo("bs02");
        assertThat(direct.getDoctorName()).isEqualTo("BS 2");
        assertThat(direct.getStaffCode()).isEqualTo("BS0006");
        assertThat(direct.getRoom()).isEqualTo("R2");
        assertThat(direct.getStatus()).isEqualTo(ShiftStatus.FULL);
    }
}
