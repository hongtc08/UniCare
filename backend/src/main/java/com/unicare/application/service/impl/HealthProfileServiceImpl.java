/**
 * Lớp triển khai nghiệp vụ quản lý hồ sơ sức khỏe, kiểm tra phân quyền và tự động tính BMI.
 */
package com.unicare.application.service.impl;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.service.HealthProfileService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.HealthRecord;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.BloodType;
import com.unicare.domain.enums.Role;
import com.unicare.domain.repository.HealthRecordRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
public class HealthProfileServiceImpl implements HealthProfileService {

    public static final String BMI_UNDERWEIGHT = "Gầy";
    public static final String BMI_NORMAL = "Bình thường";
    public static final String BMI_OVERWEIGHT = "Thừa cân";
    public static final String BMI_OBESE = "Béo phì";

    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    public HealthProfileServiceImpl(HealthRecordRepository healthRecordRepository, UserRepository userRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthProfileResponse getHealthProfile(String currentUsername, Long targetStudentId) {
        User targetStudent = resolveAndAuthorizeTargetStudent(currentUsername, targetStudentId);

        return healthRecordRepository.findByStudentId(targetStudent.getId())
                .map(HealthProfileResponse::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional
    public HealthProfileResponse upsertHealthProfile(
            String currentUsername,
            Long targetStudentId,
            HealthProfileRequest request
    ) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu hồ sơ sức khỏe không được để trống");
        }

        Long effectiveStudentId = targetStudentId != null ? targetStudentId : request.getStudentId();
        User targetStudent = resolveAndAuthorizeTargetStudent(currentUsername, effectiveStudentId);

        validatePhysicalMetrics(request.getHeight(), request.getWeight());
        String normalizedBloodType = normalizeAndValidateBloodType(request.getBloodType());

        double bmiValue = calculateBmi(request.getHeight(), request.getWeight());
        String bmiClassification = classifyBmi(bmiValue);

        HealthRecord record = healthRecordRepository.findByStudentId(targetStudent.getId())
                .orElseGet(() -> {
                    HealthRecord newRecord = new HealthRecord();
                    newRecord.setStudent(targetStudent);
                    return newRecord;
                });

        record.setHeight(BigDecimal.valueOf(request.getHeight()).setScale(1, RoundingMode.HALF_UP));
        record.setWeight(BigDecimal.valueOf(request.getWeight()).setScale(1, RoundingMode.HALF_UP));
        record.setBloodType(normalizedBloodType);
        record.setBmi(BigDecimal.valueOf(bmiValue).setScale(1, RoundingMode.HALF_UP));
        record.setBmiClassification(bmiClassification);
        record.setAllergies(cleanText(request.getAllergies()));
        record.setUnderlyingDiseases(cleanText(request.getUnderlyingDiseases()));
        record.setVaccinationHistory(cleanText(request.getVaccinationHistory()));
        if (request.getMedicalHistory() != null) {
            record.setMedicalHistory(cleanText(request.getMedicalHistory()));
        }
        record.setUpdatedAt(OffsetDateTime.now());

        HealthRecord savedRecord = healthRecordRepository.save(record);
        return HealthProfileResponse.fromEntity(savedRecord);
    }

    @Override
    public double calculateBmi(double heightCm, double weightKg) {
        validatePhysicalMetrics(heightCm, weightKg);
        double heightMeters = heightCm / 100.0;
        double rawBmi = weightKg / (heightMeters * heightMeters);
        return BigDecimal.valueOf(rawBmi)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public String classifyBmi(double bmi) {
        double roundedBmi = BigDecimal.valueOf(bmi)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        if (roundedBmi < 18.5) {
            return BMI_UNDERWEIGHT;
        }
        if (roundedBmi < 25.0) {
            return BMI_NORMAL;
        }
        if (roundedBmi < 30.0) {
            return BMI_OVERWEIGHT;
        }
        return BMI_OBESE;
    }

    private User resolveAndAuthorizeTargetStudent(String currentUsername, Long targetStudentId) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }

        User currentUser = userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đang đăng nhập: " + currentUsername));

        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (targetStudentId != null && !isMatchingStudentIdentity(currentUser, targetStudentId)) {
                throw new AccessDeniedException("Sinh viên chỉ được phép xem và cập nhật hồ sơ sức khỏe của chính mình");
            }
            return currentUser;
        }

        if (currentUser.getRole() == Role.NHAN_VIEN_Y_TE) {
            if (targetStudentId == null) {
                throw new BadRequestException("Bác sĩ vui lòng truyền mã ID sinh viên (studentId) để xem hoặc cập nhật hồ sơ sức khỏe");
            }
            User targetStudent = userRepository.findById(targetStudentId)
                    .or(() -> userRepository.findByUsername(String.valueOf(targetStudentId)))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với mã ID/MSSV: " + targetStudentId));
            if (targetStudent.getRole() != Role.SINH_VIEN) {
                throw new BadRequestException("Tài khoản mục tiêu (ID: " + targetStudentId + ") không phải là sinh viên");
            }
            return targetStudent;
        }

        throw new AccessDeniedException("Chỉ chính sinh viên đó và Bác sĩ mới được phép xem hoặc cập nhật hồ sơ sức khỏe");
    }

    private boolean isMatchingStudentIdentity(User student, Long targetStudentId) {
        if (targetStudentId.equals(student.getId())) {
            return true;
        }
        return String.valueOf(targetStudentId).equalsIgnoreCase(student.getUsername());
    }

    private void validatePhysicalMetrics(Double height, Double weight) {
        if (height == null || height <= 0) {
            throw new BadRequestException("Chiều cao phải lớn hơn 0");
        }
        if (weight == null || weight <= 0) {
            throw new BadRequestException("Cân nặng phải lớn hơn 0");
        }
        if (height > 300.0) {
            throw new BadRequestException("Chiều cao không hợp lệ (tối đa 300 cm)");
        }
        if (weight > 500.0) {
            throw new BadRequestException("Cân nặng không hợp lệ (tối đa 500 kg)");
        }
    }

    private String normalizeAndValidateBloodType(String bloodType) {
        if (bloodType == null || bloodType.isBlank()) {
            return null;
        }
        String normalized = bloodType.trim().toUpperCase();
        if (!BloodType.isValid(normalized)) {
            throw new BadRequestException("Nhóm máu chỉ chấp nhận các giá trị: A, B, AB, O");
        }
        return normalized;
    }

    private String cleanText(String text) {
        return text == null ? null : text.trim();
    }
}
