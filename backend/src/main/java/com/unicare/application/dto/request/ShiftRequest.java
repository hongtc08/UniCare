/**
 * DTO nhận dữ liệu yêu cầu tạo mới hoặc cập nhật ca khám (/api/shifts).
 */
package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftRequest {

    @JsonAlias({"nhanVienYTeId", "bacSiId", "doctor_id"})
    private Long doctorId;

    @NotNull(message = "Ngày khám không được để trống")
    @JsonAlias({"ngayKham", "date", "shift_date"})
    private LocalDate shiftDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @JsonAlias({"gioBatDau", "start_time"})
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @JsonAlias({"gioKetThuc", "end_time"})
    private LocalTime endTime;

    @NotNull(message = "Loại dịch vụ không được để trống (chỉ nhận KHAM_DA_KHOA hoặc TU_VAN_TAM_LY)")
    @JsonAlias({"loaiCaKham", "loaiDichVu", "service_type"})
    private ServiceType serviceType;

    @Size(max = 100, message = "Phòng khám không được vượt quá 100 ký tự")
    @JsonAlias({"phongKham"})
    private String room;

    @NotNull(message = "Số lượng tối đa không được để trống")
    @Min(value = 1, message = "Số lượng tối đa phải từ 1 sinh viên trở lên")
    @Max(value = 500, message = "Số lượng tối đa không được vượt quá 500")
    @JsonAlias({"soLuongToiDa", "max_quantity"})
    private Integer maxQuantity;

    @JsonAlias({"trangThaiCa"})
    private ShiftStatus status;

    public ShiftRequest() {
    }

    public ShiftRequest(
            Long doctorId,
            LocalDate shiftDate,
            LocalTime startTime,
            LocalTime endTime,
            ServiceType serviceType,
            String room,
            Integer maxQuantity
    ) {
        this.doctorId = doctorId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceType = serviceType;
        this.room = room;
        this.maxQuantity = maxQuantity;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }
}
