/**
 * Unit test kiểm thử logic tính BMI, phân loại BMI, validate và phân quyền trong HealthProfileServiceImpl.
 */
package com.unicare.application.service;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.service.impl.HealthProfileServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.HealthRecord;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.BloodType;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.HealthRecordRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthProfileServiceImplTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HealthProfileServiceImpl healthProfileService;

    private User studentUser;
    private User otherStudentUser;
    private User doctorUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        studentUser = new User("24520579", "hashed_pwd", "Tăng Chấn Hồng", Role.SINH_VIEN, UserStatus.ACTIVE);
        studentUser.setId(2L);

        otherStudentUser = new User("24521234", "hashed_pwd", "Sinh Viên Khác", Role.SINH_VIEN, UserStatus.ACTIVE);
        otherStudentUser.setId(5L);

        doctorUser = new User("NV001", "hashed_pwd", "Bác Sĩ A", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        doctorUser.setId(3L);

        adminUser = new User("admin", "hashed_pwd", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        adminUser.setId(1L);
    }

    @Test
    @DisplayName("Tính BMI làm tròn 1 chữ số thập phân và phân loại chuẩn WHO (Gầy, Bình thường, Thừa cân, Béo phì)")
    void calculateAndClassifyBmi_AllCategories() {
        // 1. Gầy (< 18.5): 170cm, 50kg -> 50 / (1.7^2) = 17.301... -> 17.3
        double bmiUnderweight = healthProfileService.calculateBmi(170.0, 50.0);
        assertThat(bmiUnderweight).isEqualTo(17.3);
        assertThat(healthProfileService.classifyBmi(bmiUnderweight)).isEqualTo("Gầy");

        // 2. Bình thường (18.5 - 24.9): 170cm, 65kg -> 65 / 2.89 = 22.491... -> 22.5
        double bmiNormal = healthProfileService.calculateBmi(170.0, 65.0);
        assertThat(bmiNormal).isEqualTo(22.5);
        assertThat(healthProfileService.classifyBmi(bmiNormal)).isEqualTo("Bình thường");

        // 3. Thừa cân (25.0 - 29.9): 170cm, 78kg -> 78 / 2.89 = 26.989... -> 27.0
        double bmiOverweight = healthProfileService.calculateBmi(170.0, 78.0);
        assertThat(bmiOverweight).isEqualTo(27.0);
        assertThat(healthProfileService.classifyBmi(bmiOverweight)).isEqualTo("Thừa cân");

        // 4. Béo phì (>= 30.0): 165cm, 85kg -> 85 / 2.7225 = 31.221... -> 31.2
        double bmiObese = healthProfileService.calculateBmi(165.0, 85.0);
        assertThat(bmiObese).isEqualTo(31.2);
        assertThat(healthProfileService.classifyBmi(bmiObese)).isEqualTo("Béo phì");
    }

    @Test
    @DisplayName("Sinh viên tự tạo mới hồ sơ sức khỏe thành công và tự động tính BMI")
    void upsertHealthProfile_StudentSelfCreate_Success() {
        HealthProfileRequest request = new HealthProfileRequest(
                170.0,
                65.0,
                "o",
                "Dị ứng hải sản",
                "Không có",
                "Đã tiêm 3 mũi COVID-19"
        );
        request.setMedicalHistory("Khỏe mạnh");

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> {
            HealthRecord saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        HealthProfileResponse response = healthProfileService.upsertHealthProfile("24520579", null, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStudentId()).isEqualTo(2L);
        assertThat(response.getHeight()).isEqualTo(170.0);
        assertThat(response.getWeight()).isEqualTo(65.0);
        assertThat(response.getBloodType()).isEqualTo("O");
        assertThat(response.getBmi()).isEqualTo(22.5);
        assertThat(response.getBmiClassification()).isEqualTo("Bình thường");
        assertThat(response.getAllergies()).isEqualTo("Dị ứng hải sản");
        assertThat(response.getUnderlyingDiseases()).isEqualTo("Không có");
        assertThat(response.getVaccinationHistory()).isEqualTo("Đã tiêm 3 mũi COVID-19");
        assertThat(response.getMedicalHistory()).isEqualTo("Khỏe mạnh");
    }

    @Test
    @DisplayName("Bác sĩ xem và cập nhật hồ sơ sức khỏe của sinh viên thành công")
    void doctorGetAndUpsertStudentProfile_Success() {
        HealthRecord existing = new HealthRecord();
        existing.setId(10L);
        existing.setStudent(studentUser);
        existing.setHeight(BigDecimal.valueOf(170.0));
        existing.setWeight(BigDecimal.valueOf(60.0));
        existing.setBloodType("AB");
        existing.setBmi(BigDecimal.valueOf(20.8));
        existing.setBmiClassification("Bình thường");

        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.of(existing));
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthProfileResponse getRes = healthProfileService.getHealthProfile("NV001", 2L);
        assertThat(getRes).isNotNull();
        assertThat(getRes.getBloodType()).isEqualTo("AB");

        HealthProfileRequest updateReq = new HealthProfileRequest(
                172.0,
                76.0,
                "AB",
                "Không",
                "Viêm xoang",
                "Viêm gan B"
        );
        HealthProfileResponse updatedRes = healthProfileService.upsertHealthProfile("NV001", 2L, updateReq);
        assertThat(updatedRes.getHeight()).isEqualTo(172.0);
        assertThat(updatedRes.getWeight()).isEqualTo(76.0);
        assertThat(updatedRes.getBmi()).isEqualTo(25.7);
        assertThat(updatedRes.getBmiClassification()).isEqualTo("Thừa cân");
    }

    @Test
    @DisplayName("Sinh viên chưa có hồ sơ khi gọi GET trả về null")
    void getHealthProfile_WhenNotYetCreated_ReturnsNull() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());

        HealthProfileResponse response = healthProfileService.getHealthProfile("24520579", null);
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Chặn Sinh viên xem hoặc cập nhật hồ sơ của sinh viên khác (403 Forbidden)")
    void studentAccessOtherStudentProfile_ThrowsAccessDenied() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("24520579", 5L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chỉ được phép xem và cập nhật hồ sơ sức khỏe của chính mình");

        HealthProfileRequest request = new HealthProfileRequest(170.0, 60.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", 5L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Chặn Ban quản lý (BAN_QUAN_LY) xem hoặc cập nhật hồ sơ sức khỏe cá nhân (403 Forbidden)")
    void adminAccessProfile_ThrowsAccessDenied() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("admin", 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ chính sinh viên đó và Bác sĩ");
    }

    @Test
    @DisplayName("Bác sĩ không truyền studentId hoặc truyền ID không phải sinh viên ném lỗi")
    void doctorInvalidTargetStudent_ThrowsException() {
        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("NV001", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("studentId");

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> healthProfileService.getHealthProfile("NV001", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không phải là sinh viên");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> healthProfileService.getHealthProfile("NV001", 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Validate chặt chiều cao, cân nặng <= 0 hoặc vượt ngưỡng và nhóm máu sai")
    void validateInvalidMetricsAndBloodType_ThrowsBadRequest() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        HealthProfileRequest zeroHeight = new HealthProfileRequest(0.0, 60.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, zeroHeight))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Chiều cao phải lớn hơn 0");

        HealthProfileRequest negativeWeight = new HealthProfileRequest(170.0, -5.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, negativeWeight))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cân nặng phải lớn hơn 0");

        HealthProfileRequest tooTall = new HealthProfileRequest(350.0, 60.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, tooTall))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tối đa 300 cm");

        HealthProfileRequest tooHeavy = new HealthProfileRequest(170.0, 600.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, tooHeavy))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tối đa 500 kg");

        HealthProfileRequest invalidBlood = new HealthProfileRequest(170.0, 60.0, "XYZ", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, invalidBlood))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Nhóm máu chỉ chấp nhận");

        HealthProfileRequest blankBlood = new HealthProfileRequest(170.0, 60.0, "   ", null, null, null);
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        HealthProfileResponse savedWithNullBlood = healthProfileService.upsertHealthProfile("24520579", null, blankBlood);
        assertThat(savedWithNullBlood.getBloodType()).isNull();

        assertThat(BloodType.isValid(null)).isFalse();
        assertThat(BloodType.isValid("  ")).isFalse();
        assertThat(BloodType.isValid("AB")).isTrue();
    }

    @Test
    @DisplayName("Kiểm tra các trường hợp biên: chưa đăng nhập hoặc request null")
    void edgeCases_NullRequestOrUnauthenticated() {
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> healthProfileService.getHealthProfile(null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("   ", null))
                .isInstanceOf(AccessDeniedException.class);
    }
}
