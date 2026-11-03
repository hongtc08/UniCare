package com.unicare.domain.entity;

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

class ShiftTest {

    @Test
    @DisplayName("Kiểm tra khởi tạo và các getter, setter, compatibility methods của Shift")
    void shiftEntity_Complete() {
        User user = new User("bs01", "pwd", "BS. Nguyen", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        user.setId(10L);

        MedicalStaff staff = MedicalStaff.builder()
                .id(10L)
                .user(user)
                .staffCode("BS0010")
                .title("Bác sĩ")
                .specialty("Đa khoa")
                .build();

        LocalDate date = LocalDate.of(2026, 11, 15);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(10, 0);

        Shift shift = new Shift(staff, date, start, end, ServiceType.KHAM_DA_KHOA, "Phòng 101", 20);

        assertThat(shift.getStaff()).isEqualTo(staff);
        assertThat(shift.getDoctor()).isEqualTo(user);
        assertThat(shift.getShiftDate()).isEqualTo(date);
        assertThat(shift.getStartTime()).isEqualTo(start);
        assertThat(shift.getEndTime()).isEqualTo(end);
        assertThat(shift.getShiftType()).isEqualTo(ServiceType.KHAM_DA_KHOA);
        assertThat(shift.getServiceType()).isEqualTo(ServiceType.KHAM_DA_KHOA);
        assertThat(shift.getRoom()).isEqualTo("Phòng 101");
        assertThat(shift.getMaxSlots()).isEqualTo(20);
        assertThat(shift.getMaxQuantity()).isEqualTo(20);
        assertThat(shift.getBookedSlots()).isZero();
        assertThat(shift.getBookedQuantity()).isZero();
        assertThat(shift.getStatus()).isEqualTo(ShiftStatus.OPEN);

        // Compatibility setters
        shift.setServiceType(ServiceType.TU_VAN_TAM_LY);
        assertThat(shift.getShiftType()).isEqualTo(ServiceType.TU_VAN_TAM_LY);

        shift.setMaxQuantity(30);
        assertThat(shift.getMaxSlots()).isEqualTo(30);

        shift.setBookedQuantity(5);
        assertThat(shift.getBookedSlots()).isEqualTo(5);

        // Lifecycle methods
        shift.onCreate();
        assertThat(shift.getCreatedAt()).isNotNull();
        assertThat(shift.getUpdatedAt()).isNotNull();

        shift.onUpdate();
        assertThat(shift.getUpdatedAt()).isNotNull();

        // When staff is null
        Shift emptyShift = new Shift();
        assertThat(emptyShift.getDoctor()).isNull();

        emptyShift.setBookedSlots(null);
        emptyShift.setStatus(null);
        emptyShift.onCreate();
        assertThat(emptyShift.getBookedSlots()).isZero();
        assertThat(emptyShift.getStatus()).isEqualTo(ShiftStatus.OPEN);

        // Builder and AllArgs
        OffsetDateTime now = OffsetDateTime.now();
        Shift builtShift = Shift.builder()
                .id(99L)
                .staff(staff)
                .shiftDate(date)
                .startTime(start)
                .endTime(end)
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .room("P1")
                .maxSlots(15)
                .bookedSlots(2)
                .status(ShiftStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(builtShift.getId()).isEqualTo(99L);
        assertThat(builtShift.getMaxSlots()).isEqualTo(15);
    }
}
