package com.unicare.application.service;

import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.PrescribedMedicineResponse;
import com.unicare.application.service.impl.MedicalCheckupServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.*;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalCheckupServiceImplTest {

    @Mock
    private MedicalCheckupRepository medicalCheckupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalCheckupServiceImpl medicalCheckupService;

    private User studentUser;
    private User otherStudentUser;
    private User doctorUser;
    private User otherDoctorUser;
    private User adminUser;

    private Student student;
    private Student otherStudent;
    private MedicalStaff doctorStaff;
    private MedicalStaff otherDoctorStaff;

    private MedicalCheckup draftCheckup;
    private MedicalCheckup completedCheckup;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id(10L)
                .username("sv001")
                .fullName("Nguyễn Văn Sinh Viên")
                .role(Role.SINH_VIEN)
                .status(UserStatus.ACTIVE)
                .build();

        student = Student.builder()
                .id(10L)
                .user(studentUser)
                .studentCode("24521234")
                .build();
        studentUser.setStudent(student);

        otherStudentUser = User.builder()
                .id(11L)
                .username("sv002")
                .fullName("Trần Thị Khác")
                .role(Role.SINH_VIEN)
                .status(UserStatus.ACTIVE)
                .build();

        otherStudent = Student.builder()
                .id(11L)
                .user(otherStudentUser)
                .studentCode("24520579")
                .build();
        otherStudentUser.setStudent(otherStudent);

        doctorUser = User.builder()
                .id(20L)
                .username("bs001")
                .fullName("Bác sĩ Nguyễn Văn A")
                .role(Role.NHAN_VIEN_Y_TE)
                .status(UserStatus.ACTIVE)
                .build();

        doctorStaff = MedicalStaff.builder()
                .id(20L)
                .user(doctorUser)
                .staffCode("NV001")
                .title("Bác sĩ chuyên khoa I")
                .specialty("KHAM_DA_KHOA")
                .build();
        doctorUser.setMedicalStaff(doctorStaff);

        otherDoctorUser = User.builder()
                .id(21L)
                .username("bs002")
                .fullName("Bác sĩ Lê Khác Ca")
                .role(Role.NHAN_VIEN_Y_TE)
                .status(UserStatus.ACTIVE)
                .build();

        otherDoctorStaff = MedicalStaff.builder()
                .id(21L)
                .user(otherDoctorUser)
                .staffCode("NV002")
                .title("Bác sĩ đa khoa")
                .specialty("KHAM_DA_KHOA")
                .build();
        otherDoctorUser.setMedicalStaff(otherDoctorStaff);

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .fullName("Ban Quản Lý UniCare")
                .role(Role.BAN_QUAN_LY)
                .status(UserStatus.ACTIVE)
                .build();

        PrescribedMedicine sampleMed = PrescribedMedicine.builder()
                .id(100L)
                .medicineName("Paracetamol 500mg")
                .quantity(10)
                .dosageInstruction("Uống 1 viên khi sốt > 38.5 độ")
                .build();

        draftCheckup = MedicalCheckup.builder()
                .id(1L)
                .checkupCode("PK2611040001")
                .student(student)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .isEmergency(false)
                .symptoms("Sốt nhẹ, ho khan")
                .diagnosisNotes("Cảm cúm thông thường")
                .treatmentSuggestions("Nghỉ ngơi, uống nhiều nước")
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .prescribedMedicines(new ArrayList<>(List.of(sampleMed)))
                .build();
        sampleMed.setMedicalCheckup(draftCheckup);

        completedCheckup = MedicalCheckup.builder()
                .id(2L)
                .checkupCode("PK2611040002")
                .student(student)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .isEmergency(false)
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now().minusHours(2))
                .completedAt(OffsetDateTime.now().minusHours(1))
                .prescribedMedicines(new ArrayList<>(List.of(sampleMed)))
                .build();
    }

    // =========================================================================
    // 1. Tests for getCheckupDetail
    // =========================================================================

    @Test
    @DisplayName("Sinh viên xem chi tiết phiếu khám của chính mình: Thành công và trả về đầy đủ đơn thuốc")
    void getCheckupDetail_Success_ByStudent_OwnCheckup() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        MedicalCheckupDetailResponse response = medicalCheckupService.getCheckupDetail(1L, "sv001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCheckupCode()).isEqualTo("PK2611040001");
        assertThat(response.getStudentId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getPrescribedMedicines()).hasSize(1);
        assertThat(response.getPrescribedMedicines().get(0).getMedicineName()).isEqualTo("Paracetamol 500mg");
    }

    @Test
    @DisplayName("Bác sĩ xem chi tiết phiếu khám của sinh viên: Thành công để phục vụ tra cứu tiền sử bệnh")
    void getCheckupDetail_Success_ByDoctor() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        MedicalCheckupDetailResponse response = medicalCheckupService.getCheckupDetail(1L, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDoctorName()).isEqualTo("Bác sĩ Nguyễn Văn A");
    }

    @Test
    @DisplayName("Sinh viên cố tình xem phiếu khám của sinh viên khác: Chặn với AccessDeniedException")
    void getCheckupDetail_Forbidden_ByStudent_OtherStudentCheckup() {
        when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        assertThatThrownBy(() -> medicalCheckupService.getCheckupDetail(1L, "sv002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình");
    }

    @Test
    @DisplayName("Ban Quản lý cố xem chi tiết phiếu khám: Chặn với AccessDeniedException vì tính riêng tư bệnh án")
    void getCheckupDetail_Forbidden_ByAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        assertThatThrownBy(() -> medicalCheckupService.getCheckupDetail(1L, "admin"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Ban quản lý không có quyền truy cập hồ sơ bệnh án chi tiết của sinh viên");
    }

    @Test
    @DisplayName("Xem chi tiết phiếu khám không tồn tại: Ném ResourceNotFoundException")
    void getCheckupDetail_NotFound() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(medicalCheckupRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalCheckupService.getCheckupDetail(999L, "sv001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy phiếu khám với ID: 999");
    }

    // =========================================================================
    // 2. Tests for updatePrescription
    // =========================================================================

    @Test
    @DisplayName("Bác sĩ phụ trách cập nhật đơn thuốc khi phiếu khám ở trạng thái DRAFT: Thành công")
    void updatePrescription_Success_WhenDraft() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 10, "Uống 1 viên khi sốt"),
                        new PrescribedMedicineRequest("Vitamin C 500mg", 20, "Uống 1 viên mỗi sáng sau ăn")
                ))
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.updatePrescription(1L, request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getPrescribedMedicines()).hasSize(2);
        assertThat(response.getPrescribedMedicines())
                .extracting(PrescribedMedicineResponse::getMedicineName)
                .containsExactly("Paracetamol 500mg", "Vitamin C 500mg");

        verify(medicalCheckupRepository).save(draftCheckup);
    }

    @Test
    @DisplayName("Trường hợp không kê thuốc : Cập nhật thành công danh sách rỗng")
    void updatePrescription_Success_WhenNoMedicinesPrescribed() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(new ArrayList<>())
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.updatePrescription(1L, request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getPrescribedMedicines()).isEmpty();
        verify(medicalCheckupRepository).save(draftCheckup);
    }

    @Test
    @DisplayName("Cố tình sửa đơn thuốc khi phiếu khám đã COMPLETED: Bị khóa Read-only, ném BadRequestException")
    void updatePrescription_Failed_WhenCompleted() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(completedCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Kháng sinh Amoxicillin", 14, "Uống ngày 2 viên")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(2L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Phiếu khám đã hoàn tất (COMPLETED), đơn thuốc đã bị khóa vĩnh viễn và không thể chỉnh sửa");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bác sĩ không phụ trách phiếu khám cố tình sửa đơn thuốc: Ném AccessDeniedException")
    void updatePrescription_Forbidden_WhenNotAssignedDoctor() {
        when(userRepository.findByUsername("bs002")).thenReturn(Optional.of(otherDoctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Kháng sinh Amoxicillin", 14, "Uống ngày 2 viên")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ bác sĩ phụ trách ca khám mới có quyền kê đơn thuốc cho phiếu khám này");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tài khoản Sinh viên cố tình gọi kê đơn thuốc: Ném AccessDeniedException")
    void updatePrescription_Forbidden_WhenStudentRole() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 5, "Uống khi sốt")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "sv001"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền kê đơn thuốc");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kê đơn thuốc trùng tên trong cùng một lần kê: Ném BadRequestException")
    void updatePrescription_Failed_DuplicateMedicineName() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 10, "Uống 1 viên khi sốt"),
                        new PrescribedMedicineRequest("  paracetamol 500mg  ", 5, "Uống thêm nếu đau đầu")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("bị trùng lặp trong đơn thuốc");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kê đơn thuốc có số lượng không hợp lệ (<= 0): Ném BadRequestException")
    void updatePrescription_Failed_InvalidQuantity() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 0, "Uống khi sốt")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số lượng thuốc phải lớn hơn hoặc bằng 1");
    }

    @Test
    @DisplayName("Kê đơn thuốc có tên rỗng: Ném BadRequestException")
    void updatePrescription_Failed_BlankMedicineName() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("   ", 10, "Uống khi sốt")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tên thuốc không được để trống");
    }
}
