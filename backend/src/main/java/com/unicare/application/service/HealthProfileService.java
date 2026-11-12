/**
 * Interface định nghĩa các nghiệp vụ quản lý hồ sơ sức khỏe sinh học, thông tin cá nhân sinh viên và tính BMI.
 */
package com.unicare.application.service;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.request.StudentPersonalInfoRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.dto.response.StudentMedicalOverviewResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;

public interface HealthProfileService {

    /**
     * Tra cứu Hồ sơ Y tế Tổng hợp của Sinh viên phục vụ Bác sĩ trong ca khám.
     * Chỉ Nhân viên y tế (NHAN_VIEN_Y_TE) mới được phép truy cập; chặn Ban quản lý (BAN_QUAN_LY) và Sinh viên.
     */
    StudentMedicalOverviewResponse getStudentMedicalOverview(String currentUsername, Long targetStudentId);

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
     * Xem thông tin cá nhân & học vụ của sinh viên (Họ tên, Ngày sinh, Giới tính, SĐT, Email, Lớp, Khoa).
     */
    StudentPersonalInfoResponse getStudentPersonalInfo(String currentUsername, Long targetStudentId);

    /**
     * Cập nhật thông tin cá nhân của sinh viên đang đăng nhập (hoặc Bác sĩ cập nhật theo studentId).
     */
    StudentPersonalInfoResponse updateStudentPersonalInfo(
            String currentUsername,
            Long targetStudentId,
            StudentPersonalInfoRequest request
    );

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
