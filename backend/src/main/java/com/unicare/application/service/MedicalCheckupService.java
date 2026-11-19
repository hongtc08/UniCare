package com.unicare.application.service;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.CreateTransferRecordRequest;
import com.unicare.application.dto.request.UpdateEmergencyProgressRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.EmergencyCasePageResponse;
import com.unicare.application.dto.response.EmergencyStatsResponse;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.dto.response.TransferReferralPrintResponse;

import java.time.LocalDate;
import java.util.List;

public interface MedicalCheckupService {

    /**
     * Tra cứu nhanh danh sách Sinh viên theo MSSV, Họ tên hoặc Mã/Tên lớp phục vụ Tiếp nhận khẩn cấp / cấp cứu.
     */
    List<StudentPersonalInfoResponse> searchStudentsForIntake(String keyword, String currentUsername);

    /**
     * Tiếp nhận sinh viên (từ lịch hẹn CONFIRMED hoặc Tiếp nhận khẩn cấp/cấp cứu)
     * và lập Phiếu khám trên bảng phẳng medical_checkups.
     */
    MedicalCheckupDetailResponse createCheckup(CreateMedicalCheckupRequest request, String currentUsername);

    /**
     * Cập nhật nội dung chuyên môn của Phiếu khám (triệu chứng, chẩn đoán, biện pháp sơ cứu, nhóm bệnh dịch tễ)
     * và quản lý trạng thái DRAFT / COMPLETED (khóa cứng Read-only vĩnh viễn khi COMPLETED).
     */
    MedicalCheckupDetailResponse updateCheckup(Long checkupId, UpdateMedicalCheckupRequest request, String currentUsername);

    /**
     * Hoàn tất ca khám: chuyển trạng thái phiếu sang COMPLETED, đổi lịch hẹn sang COMPLETED,
     * ghi nhận completed_at và khóa cứng Read-only vĩnh viễn.
     */
    MedicalCheckupDetailResponse completeCheckup(Long checkupId, String currentUsername);

    /**
     * Xóa phiếu khám nháp (DRAFT). Chặn cứng vĩnh viễn nếu phiếu đã COMPLETED.
     */
    void deleteCheckup(Long checkupId, String currentUsername);

    /**
     * Lấy chi tiết Phiếu khám theo mã lịch hẹn (appointmentId).
     */
    MedicalCheckupDetailResponse getCheckupByAppointment(Long appointmentId, String currentUsername);

    /**
     * Lấy chi tiết Phiếu khám kèm Đơn thuốc theo ID.
     */
    MedicalCheckupDetailResponse getCheckupDetail(Long checkupId, String currentUsername);

    /**
     * Bác sĩ kê đơn / cập nhật danh sách thuốc kê đơn đi kèm Phiếu khám.
     */
    MedicalCheckupDetailResponse updatePrescription(Long checkupId, UpdatePrescriptionRequest request, String currentUsername);

    /**
     * Lấy dòng thời gian (Timeline) toàn bộ lịch sử khám bệnh đã hoàn tất (Đa khoa, Tâm lý, Định kỳ)
     * kèm đơn thuốc và hồ sơ chuyển viện (nếu có) của chính Sinh viên đang đăng nhập.
     */
    List<MedicalCheckupDetailResponse> getStudentMedicalHistory(String currentUsername, Long requestedStudentId);

    /**
     * Lập Hồ sơ Chuyển viện trên bảng transfer_records (POST /api/medical-records/{id}/transfer):
     * Lưu thông tin chuyển tuyến, khóa chỉ đọc hồ sơ chuyển viện và tự động kết thúc ca khám tại trường.
     */
    MedicalCheckupDetailResponse createTransferRecord(Long checkupId, CreateTransferRecordRequest request, String currentUsername);

    /**
     * Kết xuất dữ liệu chuẩn / bản in để xuất Giấy giới thiệu chuyển viện (GET /api/medical-records/{id}/transfer/print).
     */
    TransferReferralPrintResponse getTransferReferralPaper(Long checkupId, String currentUsername);

    /**
     * Lấy danh sách ca cấp cứu đã lưu, hỗ trợ bộ lọc và phân trang (GET /api/medical-records/emergencies).
     */
    EmergencyCasePageResponse getEmergencyCases(
            String keyword,
            String emergencyLevel,
            String emergencyType,
            String currentStep,
            String status,
            LocalDate date,
            Integer page,
            Integer size,
            String currentUsername
    );

    /**
     * Cập nhật tiến độ (bước xử lý hiện tại) của ca cấp cứu khi chuyển sang sơ cứu, chuyển viện hoặc kết thúc.
     */
    MedicalCheckupDetailResponse updateEmergencyProgress(Long checkupId, UpdateEmergencyProgressRequest request, String currentUsername);

    /**
     * Lấy số liệu thống kê ca cấp cứu: đang xử lý, đang sơ cứu, đang chuyển viện và đã kết thúc hôm nay.
     */
    EmergencyStatsResponse getEmergencyStats(String currentUsername);
}
