package com.unicare.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedicalStaffTest {

    @Test
    @DisplayName("Kiểm tra getters, setters, builder của MedicalStaff")
    void medicalStaff_Coverage() {
        User user = new User();
        user.setId(7L);

        MedicalStaff staff = MedicalStaff.builder()
                .id(7L)
                .user(user)
                .staffCode("BS0007")
                .title("Bác sĩ chuyên khoa")
                .specialty("Tâm lý")
                .build();

        assertThat(staff.getId()).isEqualTo(7L);
        assertThat(staff.getUserId()).isEqualTo(7L);
        assertThat(staff.getUser()).isEqualTo(user);
        assertThat(staff.getStaffCode()).isEqualTo("BS0007");
        assertThat(staff.getTitle()).isEqualTo("Bác sĩ chuyên khoa");
        assertThat(staff.getSpecialty()).isEqualTo("Tâm lý");

        staff.setUserId(8L);
        assertThat(staff.getUserId()).isEqualTo(8L);
        staff.setStaffCode("BS0008");
        staff.setTitle("Điều dưỡng");
        staff.setSpecialty("Đa khoa");
        staff.setUser(null);

        MedicalStaff noArg = new MedicalStaff();
        noArg.setId(1L);
        assertThat(noArg.getId()).isEqualTo(1L);
    }
}
