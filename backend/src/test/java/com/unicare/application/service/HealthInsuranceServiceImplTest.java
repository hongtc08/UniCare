package com.unicare.application.service;

import com.unicare.application.dto.request.HealthInsuranceRequest;
import com.unicare.application.dto.response.HealthInsuranceResponse;
import com.unicare.application.service.impl.HealthInsuranceServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.HealthInsurance;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.HealthInsuranceStatus;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.HealthInsuranceRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthInsuranceServiceImplTest {

        @Mock
        private HealthInsuranceRepository healthInsuranceRepository;

        @Mock
        private StudentRepository studentRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private HealthInsuranceServiceImpl healthInsuranceService;

        private User adminUser;
        private User studentUser;
        private Student student;
        private HealthInsurance sampleInsurance;

        @BeforeEach
        void setUp() {
                adminUser = new User("admin01", "hashed_pwd", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
                adminUser.setId(1L);

                studentUser = new User("sv22120001", "hashed_pwd", "Nguyễn Văn Sinh", Role.SINH_VIEN,
                                UserStatus.ACTIVE);
                studentUser.setId(10L);

                student = new Student(10L, studentUser, "sv22120001", null);

                sampleInsurance = HealthInsurance.builder()
                                .id(100L)
                                .cardNumber("SV4791234567890")
                                .student(student)
                                .effectiveDate(LocalDate.now().minusMonths(6))
                                .expirationDate(LocalDate.now().plusMonths(6))
                                .initialHospital("Bệnh viện Đa khoa Khu vực Thủ Đức")
                                .issueAt("TP. Hồ Chí Minh")
                                .build();
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thành công theo chuẩn mới 10 chữ số")
        void createHealthInsurance_Success_NewFormat10Digits() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567",
                                10L,
                                LocalDate.now(),
                                LocalDate.now().plusYears(1),
                                "Bệnh viện Quân Dân Y Miền Đông");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(false);
                when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
                when(healthInsuranceRepository.existsByStudentId(10L)).thenReturn(false);
                when(healthInsuranceRepository.save(any(HealthInsurance.class))).thenAnswer(invocation -> {
                        HealthInsurance h = invocation.getArgument(0);
                        h.setId(101L);
                        return h;
                });

                HealthInsuranceResponse response = healthInsuranceService.createHealthInsurance(request, "admin01");

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(101L);
                assertThat(response.getCardNumber()).isEqualTo("0791234567");
                assertThat(response.getStatus()).isEqualTo(HealthInsuranceStatus.DANG_HIEU_LUC.name());
                verify(healthInsuranceRepository).save(any(HealthInsurance.class));
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thành công theo chuẩn cũ 15 ký tự (2 chữ cái + 13 chữ số)")
        void createHealthInsurance_Success_OldFormat15Chars() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "sv4791234567890",
                                10L,
                                LocalDate.now(),
                                LocalDate.now().plusMonths(2),
                                "Bệnh viện Thống Nhất");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("SV4791234567890")).thenReturn(false);
                when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
                when(healthInsuranceRepository.existsByStudentId(10L)).thenReturn(false);
                when(healthInsuranceRepository.save(any(HealthInsurance.class))).thenAnswer(invocation -> {
                        HealthInsurance h = invocation.getArgument(0);
                        h.setId(102L);
                        return h;
                });

                HealthInsuranceResponse response = healthInsuranceService.createHealthInsurance(request, "admin01");

                assertThat(response.getCardNumber()).isEqualTo("SV4791234567890"); // đã chuẩn hóa in hoa
                assertThat(response.getStatus()).isEqualTo(HealthInsuranceStatus.DANG_HIEU_LUC.name());
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thành công có thông tin nơi cấp thẻ (issueAt)")
        void createHealthInsurance_Success_WithIssueAt() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567",
                                10L,
                                LocalDate.now(),
                                LocalDate.now().plusYears(1),
                                "Bệnh viện Quân Dân Y Miền Đông",
                                "BHXH TP. Thủ Đức");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(false);
                when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
                when(healthInsuranceRepository.existsByStudentId(10L)).thenReturn(false);
                when(healthInsuranceRepository.save(any(HealthInsurance.class))).thenAnswer(invocation -> {
                        HealthInsurance h = invocation.getArgument(0);
                        h.setId(103L);
                        return h;
                });

                HealthInsuranceResponse response = healthInsuranceService.createHealthInsurance(request, "admin01");

                assertThat(response).isNotNull();
                assertThat(response.getIssueAt()).isEqualTo("BHXH TP. Thủ Đức");
                assertThat(response.getStatus()).isEqualTo(HealthInsuranceStatus.DANG_HIEU_LUC.name());
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thất bại khi mã thẻ sai định dạng (không phải 10 số hoặc 15 ký tự chữ/số)")
        void createHealthInsurance_InvalidFormat_ThrowsBadRequest() {
                HealthInsuranceRequest request9Digits = new HealthInsuranceRequest(
                                "123456789", 10L, LocalDate.now(), LocalDate.now().plusMonths(1), "BV A");
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request9Digits, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Mã thẻ BHYT không đúng định dạng chuẩn Việt Nam");

                HealthInsuranceRequest request16Chars = new HealthInsuranceRequest(
                                "SV479123456789012", 10L, LocalDate.now(), LocalDate.now().plusMonths(1), "BV A");
                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request16Chars, "admin01"))
                                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thất bại khi trùng mã thẻ đã có trên hệ thống (409)")
        void createHealthInsurance_DuplicateCardNumber_ThrowsConflict() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now(), LocalDate.now().plusMonths(1), "BV A");
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(true);

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "admin01"))
                                .isInstanceOf(DuplicateResourceException.class)
                                .hasMessageContaining("đã tồn tại");
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thất bại khi sinh viên đã có thẻ BHYT (409)")
        void createHealthInsurance_DuplicateStudent_ThrowsConflict() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now(), LocalDate.now().plusMonths(1), "BV A");
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(false);
                when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
                when(healthInsuranceRepository.existsByStudentId(10L)).thenReturn(true);

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "admin01"))
                                .isInstanceOf(DuplicateResourceException.class)
                                .hasMessageContaining("Sinh viên này đã có thông tin thẻ BHYT");
        }

        @Test
        @DisplayName("Thêm thẻ BHYT thất bại khi ngày bắt đầu > ngày hết hạn (400)")
        void createHealthInsurance_EffectiveDateAfterExpiration_ThrowsBadRequest() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now().plusDays(10), LocalDate.now(), "BV A");
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Ngày bắt đầu hiệu lực phải trước hoặc bằng ngày hết hạn");
        }

        @Test
        @DisplayName("Sinh viên cố tình thêm thẻ cho sinh viên khác bị từ chối quyền (403)")
        void createHealthInsurance_StudentUnauthorized_ThrowsAccessDenied() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 999L, LocalDate.now(), LocalDate.now().plusMonths(1), "BV A");
                when(userRepository.findByUsername("sv22120001")).thenReturn(Optional.of(studentUser));

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "sv22120001"))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("chỉ được phép tạo thẻ BHYT cho chính mình");
        }

        @Test
        @DisplayName("Lấy danh sách thẻ BHYT với vai trò Quản lý và tính trạng thái động thành công")
        void getHealthInsurances_AsAdmin_Success() {
                HealthInsurance expiringSoonInsurance = HealthInsurance.builder()
                                .id(200L)
                                .cardNumber("0799999999")
                                .student(student)
                                .effectiveDate(LocalDate.now().minusMonths(11))
                                .expirationDate(LocalDate.now().plusDays(15)) // còn 15 ngày -> Sắp hết hạn (Vàng)
                                .initialHospital("BV B")
                                .build();

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.searchHealthInsurances(""))
                                .thenReturn(List.of(sampleInsurance, expiringSoonInsurance));

                List<HealthInsuranceResponse> responses = healthInsuranceService.getHealthInsurances(null, null,
                                "admin01");

                assertThat(responses).hasSize(2);
                assertThat(responses.get(0).getStatus()).isEqualTo(HealthInsuranceStatus.DANG_HIEU_LUC.name());
                assertThat(responses.get(1).getStatus()).isEqualTo(HealthInsuranceStatus.SAP_HET_HAN.name());
        }

        @Test
        @DisplayName("Lọc danh sách thẻ BHYT theo trạng thái động (SAP_HET_HAN)")
        void getHealthInsurances_FilterByStatus() {
                HealthInsurance expiringSoonInsurance = HealthInsurance.builder()
                                .id(200L)
                                .cardNumber("0799999999")
                                .student(student)
                                .effectiveDate(LocalDate.now().minusMonths(11))
                                .expirationDate(LocalDate.now().plusDays(15))
                                .initialHospital("BV B")
                                .build();

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.searchHealthInsurances(""))
                                .thenReturn(List.of(sampleInsurance, expiringSoonInsurance));

                List<HealthInsuranceResponse> filtered = healthInsuranceService.getHealthInsurances(null, "SAP_HET_HAN",
                                "admin01");

                assertThat(filtered).hasSize(1);
                assertThat(filtered.get(0).getCardNumber()).isEqualTo("0799999999");
                assertThat(filtered.get(0).getStatus()).isEqualTo(HealthInsuranceStatus.SAP_HET_HAN.name());
        }

        @Test
        @DisplayName("Sinh viên chỉ xem được thẻ BHYT của chính mình khi gọi lấy danh sách")
        void getHealthInsurances_AsStudent_ReturnsOnlyOwnCard() {
                when(userRepository.findByUsername("sv22120001")).thenReturn(Optional.of(studentUser));
                when(healthInsuranceRepository.findByStudentIdWithDetails(10L))
                                .thenReturn(Optional.of(sampleInsurance));

                List<HealthInsuranceResponse> responses = healthInsuranceService.getHealthInsurances(null, null,
                                "sv22120001");

                assertThat(responses).hasSize(1);
                assertThat(responses.get(0).getCardNumber()).isEqualTo("SV4791234567890");
        }

        @Test
        @DisplayName("Sinh viên chưa có thẻ BHYT trả về danh sách rỗng")
        void getHealthInsurances_AsStudent_NoCard_ReturnsEmpty() {
                when(userRepository.findByUsername("sv22120001")).thenReturn(Optional.of(studentUser));
                when(healthInsuranceRepository.findByStudentIdWithDetails(10L))
                                .thenReturn(Optional.empty());

                List<HealthInsuranceResponse> responses = healthInsuranceService.getHealthInsurances(null, null,
                                "sv22120001");

                assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Lấy chi tiết thẻ BHYT theo ID thành công")
        void getHealthInsuranceById_Success() {
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleInsurance));

                HealthInsuranceResponse response = healthInsuranceService.getHealthInsuranceById(100L, "admin01");

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(100L);
                assertThat(response.getStudentCode()).isEqualTo("sv22120001");
        }

        @Test
        @DisplayName("Lấy chi tiết thẻ BHYT theo ID thất bại khi không tìm thấy (404)")
        void getHealthInsuranceById_NotFound_ThrowsException() {
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> healthInsuranceService.getHealthInsuranceById(999L, "admin01"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy thẻ BHYT với mã ID");
        }

        @Test
        @DisplayName("Sinh viên không được xem chi tiết thẻ BHYT của sinh viên khác (403)")
        void getHealthInsuranceById_StudentAccessDenied() {
                User otherStudentUser = new User("sv22120002", "pwd", "Trần Văn B", Role.SINH_VIEN, UserStatus.ACTIVE);
                otherStudentUser.setId(99L);
                Student otherStudent = new Student(99L, otherStudentUser, "sv22120002", null);

                HealthInsurance otherInsurance = HealthInsurance.builder()
                                .id(999L)
                                .cardNumber("0791112223")
                                .student(otherStudent)
                                .effectiveDate(LocalDate.now())
                                .expirationDate(LocalDate.now().plusYears(1))
                                .initialHospital("BV C")
                                .build();

                when(userRepository.findByUsername("sv22120001")).thenReturn(Optional.of(studentUser));
                when(healthInsuranceRepository.findByIdWithDetails(999L)).thenReturn(Optional.of(otherInsurance));

                assertThatThrownBy(() -> healthInsuranceService.getHealthInsuranceById(999L, "sv22120001"))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("chỉ được phép xem thông tin thẻ BHYT của chính mình");
        }

        @Test
        @DisplayName("Lấy chi tiết thẻ BHYT theo studentId thành công (Admin)")
        void getHealthInsuranceByStudentId_Success_AsAdmin() {
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByStudentIdWithDetails(10L))
                                .thenReturn(Optional.of(sampleInsurance));

                HealthInsuranceResponse response = healthInsuranceService.getHealthInsuranceByStudentId(10L, "admin01");

                assertThat(response).isNotNull();
                assertThat(response.getStudentId()).isEqualTo(10L);
                assertThat(response.getCardNumber()).isEqualTo("SV4791234567890");
        }

        @Test
        @DisplayName("Lấy chi tiết thẻ BHYT theo studentId thất bại khi không tìm thấy (404)")
        void getHealthInsuranceByStudentId_NotFound_ThrowsException() {
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByStudentIdWithDetails(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> healthInsuranceService.getHealthInsuranceByStudentId(999L, "admin01"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy thẻ BHYT cho sinh viên");
        }

        @Test
        @DisplayName("Sinh viên không được lấy thẻ BHYT của sinh viên khác qua studentId (403)")
        void getHealthInsuranceByStudentId_StudentAccessDenied() {
                when(userRepository.findByUsername("sv22120001")).thenReturn(Optional.of(studentUser));

                assertThatThrownBy(() -> healthInsuranceService.getHealthInsuranceByStudentId(99L, "sv22120001"))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("chỉ được phép xem thông tin thẻ BHYT của chính mình");
        }

        @Test
        @DisplayName("Cập nhật thẻ BHYT thành công")
        void updateHealthInsurance_Success() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567",
                                10L,
                                LocalDate.now(),
                                LocalDate.now().plusYears(2),
                                "Bệnh viện Đa khoa Sài Gòn");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleInsurance));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCaseAndIdNot("0791234567", 100L))
                                .thenReturn(false);
                when(healthInsuranceRepository.save(any(HealthInsurance.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                HealthInsuranceResponse response = healthInsuranceService.updateHealthInsurance(100L, request,
                                "admin01");

                assertThat(response.getCardNumber()).isEqualTo("0791234567");
                assertThat(response.getInitialHospital()).isEqualTo("Bệnh viện Đa khoa Sài Gòn");
                verify(healthInsuranceRepository).save(sampleInsurance);
        }

        @Test
        @DisplayName("Cập nhật thẻ BHYT thành công có nơi cấp thẻ issueAt")
        void updateHealthInsurance_Success_WithIssueAt() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567",
                                10L,
                                LocalDate.now(),
                                LocalDate.now().plusYears(1),
                                "Bệnh viện Đa khoa Sài Gòn",
                                "BHXH Quận 1");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleInsurance));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCaseAndIdNot("0791234567", 100L))
                                .thenReturn(false);
                when(healthInsuranceRepository.save(any(HealthInsurance.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                HealthInsuranceResponse response = healthInsuranceService.updateHealthInsurance(100L, request,
                                "admin01");

                assertThat(response.getIssueAt()).isEqualTo("BHXH Quận 1");
        }

        @Test
        @DisplayName("Cập nhật thẻ BHYT thất bại khi không tìm thấy thẻ (404)")
        void updateHealthInsurance_NotFound_ThrowsException() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now(), LocalDate.now().plusYears(1), "BV A");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> healthInsuranceService.updateHealthInsurance(999L, request, "admin01"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy thẻ BHYT với mã ID");
        }

        @Test
        @DisplayName("Cập nhật thẻ BHYT thất bại khi trùng số thẻ với thẻ khác (409)")
        void updateHealthInsurance_DuplicateCardNumber_ThrowsConflict() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now(), LocalDate.now().plusYears(1), "BV A");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleInsurance));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCaseAndIdNot("0791234567", 100L))
                                .thenReturn(true);

                assertThatThrownBy(() -> healthInsuranceService.updateHealthInsurance(100L, request, "admin01"))
                                .isInstanceOf(DuplicateResourceException.class)
                                .hasMessageContaining("đã tồn tại trên hệ thống");
        }

        @Test
        @DisplayName("Xóa thẻ BHYT thành công")
        void deleteHealthInsurance_Success() {
                when(healthInsuranceRepository.findById(100L)).thenReturn(Optional.of(sampleInsurance));

                healthInsuranceService.deleteHealthInsurance(100L, "admin01");

                verify(healthInsuranceRepository).delete(sampleInsurance);
        }

        @Test
        @DisplayName("Xóa thẻ BHYT thất bại khi không tìm thấy thẻ (404)")
        void deleteHealthInsurance_NotFound_ThrowsException() {
                when(healthInsuranceRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> healthInsuranceService.deleteHealthInsurance(999L, "admin01"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy thẻ BHYT với mã ID");
        }

        @Test
        @DisplayName("Thao tác thất bại khi username rỗng hoặc null (403)")
        void resolveCurrentUser_BlankUsername_ThrowsAccessDenied() {
                assertThatThrownBy(() -> healthInsuranceService.getHealthInsurances(null, null, null))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("Yêu cầu đăng nhập");

                assertThatThrownBy(() -> healthInsuranceService.getHealthInsurances(null, null, "   "))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("Yêu cầu đăng nhập");
        }

        @Test
        @DisplayName("Thao tác thất bại khi không tìm thấy tài khoản (404)")
        void resolveCurrentUser_UserNotFound_ThrowsException() {
                when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> healthInsuranceService.getHealthInsurances(null, null, "unknown"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy tài khoản");
        }

        @Test
        @DisplayName("Admin tạo thẻ BHYT thất bại khi không truyền studentId (400)")
        void createHealthInsurance_AdminMissingStudentId_ThrowsBadRequest() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", null, LocalDate.now(), LocalDate.now().plusYears(1), "BV A");
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Vui lòng cung cấp mã ID sinh viên");
        }

        @Test
        @DisplayName("Tự động tạo bản ghi Student nếu sinh viên chưa có trong bảng Student")
        void createHealthInsurance_StudentAutoCreatedFromUser() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now(), LocalDate.now().plusYears(1), "BV A");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(false);
                when(studentRepository.findById(10L)).thenReturn(Optional.empty());
                when(userRepository.findById(10L)).thenReturn(Optional.of(studentUser));
                when(studentRepository.save(any(Student.class))).thenReturn(student);
                when(healthInsuranceRepository.existsByStudentId(10L)).thenReturn(false);
                when(healthInsuranceRepository.save(any(HealthInsurance.class))).thenReturn(sampleInsurance);

                HealthInsuranceResponse response = healthInsuranceService.createHealthInsurance(request, "admin01");

                assertThat(response).isNotNull();
                verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("Tạo thẻ BHYT thất bại khi studentId không tồn tại trong hệ thống (404)")
        void createHealthInsurance_StudentNotFoundInUserRepo_ThrowsException() {
                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 999L, LocalDate.now(), LocalDate.now().plusYears(1), "BV A");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(false);
                when(studentRepository.findById(999L)).thenReturn(Optional.empty());
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "admin01"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy sinh viên với mã ID");
        }

        @Test
        @DisplayName("Tạo thẻ BHYT thất bại khi studentId trỏ tới user không phải role SINH_VIEN (400)")
        void createHealthInsurance_UserNotStudentRole_ThrowsBadRequest() {
                User doctorUser = new User("bs01", "pwd", "Bác sĩ", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
                doctorUser.setId(20L);

                HealthInsuranceRequest request = new HealthInsuranceRequest(
                                "0791234567", 20L, LocalDate.now(), LocalDate.now().plusYears(1), "BV A");

                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));
                when(healthInsuranceRepository.existsByCardNumberIgnoreCase("0791234567")).thenReturn(false);
                when(studentRepository.findById(20L)).thenReturn(Optional.empty());
                when(userRepository.findById(20L)).thenReturn(Optional.of(doctorUser));

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(request, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("không phải là sinh viên");
        }

        @Test
        @DisplayName("Tạo thẻ BHYT thất bại khi request null hoặc ngày hiệu lực null (400)")
        void createHealthInsurance_NullRequestOrDates_ThrowsBadRequest() {
                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(null, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Dữ liệu thẻ BHYT không được để trống");

                HealthInsuranceRequest nullEffective = new HealthInsuranceRequest(
                                "0791234567", 10L, null, LocalDate.now().plusYears(1), "BV A");
                when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(nullEffective, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Ngày bắt đầu hiệu lực không được để trống");

                HealthInsuranceRequest nullExpiration = new HealthInsuranceRequest(
                                "0791234567", 10L, LocalDate.now(), null, "BV A");

                assertThatThrownBy(() -> healthInsuranceService.createHealthInsurance(nullExpiration, "admin01"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Ngày hết hạn không được để trống");
        }
}
