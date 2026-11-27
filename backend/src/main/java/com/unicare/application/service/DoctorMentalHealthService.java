package com.unicare.application.service;

import com.unicare.application.dto.response.Dass21ResultResponse;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.StudentMentalHealthHistoryResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface nghiệp vụ tra cứu Lịch sử DASS-21 & Nhật ký Tâm trạng của Sinh viên
 * phục vụ Bác sĩ / Chuyên gia tâm lý trong ca khám / tư vấn.
 * Phân quyền nghiêm ngặt: Chỉ Role.NHAN_VIEN_Y_TE được phép truy cập; chặn tuyệt đối BAN_QUAN_LY và SINH_VIEN.
 */
public interface DoctorMentalHealthService {

    StudentMentalHealthHistoryResponse getStudentMentalHealthHistory(
            String currentUsername,
            Long targetStudentId,
            String period,
            LocalDate fromDate,
            LocalDate toDate
    );

    List<Dass21ResultResponse> getStudentDass21History(
            String currentUsername,
            Long targetStudentId,
            LocalDate fromDate,
            LocalDate toDate
    );

    MoodLogHistoryResponse getStudentMoodLogsForDoctor(
            String currentUsername,
            Long targetStudentId,
            String period,
            LocalDate fromDate,
            LocalDate toDate
    );
}
