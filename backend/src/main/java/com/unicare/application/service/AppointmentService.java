package com.unicare.application.service;

import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;

import java.util.List;

/**
 * Service xử lý các nghiệp vụ lịch hẹn khám bệnh (dùng chung cho Sinh viên và Bác sĩ).
 */
public interface AppointmentService {

    /**
     * Hủy lịch hẹn khám và hoàn trả slot cho ca khám cũ.
     *
     * @param appointmentId   ID của lịch hẹn cần hủy
     * @param request         Thông tin lý do hủy
     * @param currentUsername Tên đăng nhập của người thực hiện
     * @return Thông tin lịch hẹn sau khi hủy
     */
    AppointmentResponse cancelAppointment(Long appointmentId, CancelAppointmentRequest request, String currentUsername);

    /**
     * Dời lịch hẹn sang ca khám mới, hoàn trả slot ca cũ và trừ slot ca mới trong cùng một transaction.
     *
     * @param appointmentId   ID của lịch hẹn cần dời
     * @param request         Thông tin ca khám mới và lý do dời lịch
     * @param currentUsername Tên đăng nhập của người thực hiện
     * @return Thông tin lịch hẹn sau khi dời lịch
     */
    AppointmentResponse rescheduleAppointment(Long appointmentId, RescheduleAppointmentRequest request, String currentUsername);

    /**
     * Lấy danh sách lịch hẹn của sinh viên đang đăng nhập.
     *
     * @param currentUsername Tên đăng nhập của sinh viên
     * @param status          Trạng thái lọc (CONFIRMED, COMPLETED, CANCELED, NO_SHOW, hoặc null)
     * @return Danh sách lịch hẹn tương ứng
     */
    List<AppointmentResponse> getStudentAppointments(String currentUsername, String status);

    /**
     * Lấy chi tiết lịch hẹn theo ID.
     *
     * @param appointmentId   ID của lịch hẹn
     * @param currentUsername Tên đăng nhập của người dùng
     * @return Thông tin chi tiết lịch hẹn
     */
    AppointmentResponse getAppointmentById(Long appointmentId, String currentUsername);
}

