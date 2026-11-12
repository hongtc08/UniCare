package com.unicare.application.service;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;

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
}
