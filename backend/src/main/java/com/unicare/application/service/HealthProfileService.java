/**
 * Interface định nghĩa các nghiệp vụ quản lý hồ sơ sức khỏe sinh học và tính BMI.
 */
package com.unicare.application.service;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.response.HealthProfileResponse;

public interface HealthProfileService {

    /**
     * Xem hồ sơ sức khỏe sinh học của sinh viên.
     * Chỉ chính sinh viên đó và Bác sĩ (NHAN_VIEN_Y_TE) được phép xem.
     */
    HealthProfileResponse getHealthProfile(String currentUsername, Long targetStudentId);

    /**
     * Tạo mới hoặc cập nhật (Upsert) hồ sơ sức khỏe sinh học & tự động tính BMI.
     * Chỉ chính sinh viên đó và Bác sĩ (NHAN_VIEN_Y_TE) được phép cập nhật.
     */
    HealthProfileResponse upsertHealthProfile(String currentUsername, Long targetStudentId, HealthProfileRequest request);

    /**
     * Tính chỉ số BMI = Cân nặng / ((Chiều cao / 100)^2), làm tròn 1 chữ số thập phân.
     */
    double calculateBmi(double heightCm, double weightKg);

    /**
     * Phân loại chỉ số BMI theo ngưỡng WHO:
     * < 18.5: Gầy
     * 18.5 - 24.9: Bình thường
     * 25.0 - 29.9: Thừa cân
     * >= 30.0: Béo phì
     */
    String classifyBmi(double bmi);
}
