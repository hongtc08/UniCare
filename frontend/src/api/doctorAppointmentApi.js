import axiosClient from "./axiosClient.js";

/**
 * Service API quản lý danh sách lịch khám và thao tác nghiệp vụ của Bác sĩ (FE2).
 * Tích hợp chuẩn 100% với Spring Boot backend endpoints:
 * 1. GET /api/doctor/appointments
 * 2. GET /api/shifts/available (hoặc fallback /api/shifts?status=OPEN)
 * 3. PUT /api/doctor/appointments/{id}/cancel
 * 4. PUT /api/doctor/appointments/{id}/reschedule
 * 5. GET /api/health-profile/personal-info/{studentId} (Thông tin chi tiết sinh viên)
 * 6. GET /api/health-profile/{studentId} (Hồ sơ sức khỏe sinh học)
 */

export const doctorAppointmentApi = {
  /**
   * 1. Lấy danh sách lịch hẹn của bác sĩ
   * GET /api/doctor/appointments
   * @param {Object} params
   * @param {string} [params.date] Ngày khám (YYYY-MM-DD)
   * @param {string} [params.shiftType] KHAM_DA_KHOA | TU_VAN_TAM_LY
   * @param {string} [params.serviceType] Alias cho shiftType
   * @param {string} [params.status] CONFIRMED | COMPLETED | CANCELED | NO_SHOW
   * @param {string} [params.keyword] Tìm kiếm theo tên hoặc MSSV
   * @param {string} [params.search] Alias cho keyword
   * @param {AbortSignal} [signal]
   */
  async getDoctorAppointments(params = {}, signal) {
    const { date, shiftType, serviceType, status, keyword, search } = params;
    const effectiveType = shiftType || serviceType;
    const effectiveKeyword = keyword || search;

    const queryParams = {};
    if (date) queryParams.date = date;
    if (effectiveType && effectiveType !== "ALL") {
      queryParams.shiftType = effectiveType;
      queryParams.serviceType = effectiveType;
    }
    if (status && status !== "ALL") queryParams.status = status;
    if (effectiveKeyword && effectiveKeyword.trim()) {
      queryParams.keyword = effectiveKeyword.trim();
    }

    const response = await axiosClient.get("/doctor/appointments", {
      params: queryParams,
      signal,
    });

    // Backend bọc trong ApiResponse<List<AppointmentResponse>> { success, message, data: [...] }
    const rawList = Array.isArray(response)
      ? response
      : Array.isArray(response?.data)
      ? response.data
      : [];

    // Client-side search fallback nếu backend chưa hỗ trợ query param 'keyword'
    let results = rawList;
    if (effectiveKeyword && effectiveKeyword.trim()) {
      const q = effectiveKeyword.trim().toLowerCase();
      results = results.filter((item) => {
        const name = (item.studentName || item.full_name || "").toLowerCase();
        const code = (item.studentCode || item.student_code || "").toLowerCase();
        return name.includes(q) || code.includes(q);
      });
    }

    return results;
  },

  /**
   * Lấy thông tin cá nhân & học vụ của sinh viên
   * GET /api/health-profile/personal-info/{studentId}
   */
  async getStudentPersonalInfo(studentId) {
    if (!studentId) return null;
    try {
      const response = await axiosClient.get(`/health-profile/personal-info/${studentId}`);
      return response?.data || response;
    } catch (err) {
      console.warn("Could not fetch student personal info:", err?.message);
      return null;
    }
  },

  /**
   * Lấy hồ sơ sức khỏe sinh học của sinh viên
   * GET /api/health-profile/{studentId}
   */
  async getStudentHealthProfile(studentId) {
    if (!studentId) return null;
    try {
      const response = await axiosClient.get(`/health-profile/${studentId}`);
      return response?.data || response;
    } catch (err) {
      console.warn("Could not fetch student health profile:", err?.message);
      return null;
    }
  },

  /**
   * 2. Lấy danh sách ca khám mở còn slot trống cho SlotPicker
   * Thử gọi GET /api/shifts/available trước, nếu backend trả về 404 (chưa route),
   * tự động fallback sang GET /api/shifts?status=OPEN&date=...
   * @param {Object} params
   * @param {string} [params.date]
   * @param {string} [params.shiftType]
   * @param {string} [params.serviceType]
   */
  async getAvailableShifts(params = {}) {
    const { date, shiftType, serviceType } = params;
    const effectiveType = shiftType || serviceType;

    const queryParams = { status: "OPEN" };
    if (date) queryParams.date = date;
    if (effectiveType && effectiveType !== "ALL") {
      queryParams.shiftType = effectiveType;
      queryParams.serviceType = effectiveType;
    }

    try {
      const response = await axiosClient.get("/shifts/available", { params: queryParams });
      const list = Array.isArray(response)
        ? response
        : Array.isArray(response?.data)
        ? response.data
        : [];
      return list;
    } catch (err) {
      // Fallback về GET /api/shifts với status=OPEN nếu endpoint /available chưa định nghĩa
      if (err?.response?.status === 404 || err?.status === 404) {
        const fallbackRes = await axiosClient.get("/shifts", { params: queryParams });
        const list = Array.isArray(fallbackRes)
          ? fallbackRes
          : Array.isArray(fallbackRes?.data)
          ? fallbackRes.data
          : [];
        return list.filter((s) => {
          const booked = Number(s.bookedSlots ?? s.booked_slots ?? 0);
          const max = Number(s.maxSlots ?? s.max_slots ?? 0);
          return (s.status === "OPEN" || !s.status) && (max === 0 || booked < max);
        });
      }
      throw err;
    }
  },

  /**
   * 3. Bác sĩ hủy lịch hẹn
   * PUT /api/doctor/appointments/{id}/cancel
   * Request body: { reason: "..." }
   */
  async cancelAppointment(id, reason) {
    if (!reason || !reason.trim()) {
      throw new Error("Lý do hủy lịch hẹn không được để trống.");
    }
    if (reason.trim().length > 500) {
      throw new Error("Lý do hủy lịch hẹn không được vượt quá 500 ký tự.");
    }

    const payload = { reason: reason.trim() };
    const response = await axiosClient.put(`/doctor/appointments/${id}/cancel`, payload);
    return response?.data || response;
  },

  /**
   * 4. Bác sĩ dời lịch hẹn sang ca khám mới
   * PUT /api/doctor/appointments/{id}/reschedule
   * Request body: { newShiftId: Long, reason: "..." }
   */
  async rescheduleAppointment(id, newShiftId, reason) {
    if (!newShiftId) {
      throw new Error("Vui lòng chọn ca khám mới để dời lịch.");
    }
    if (!reason || !reason.trim()) {
      throw new Error("Lý do dời lịch hẹn không được để trống.");
    }
    if (reason.trim().length > 500) {
      throw new Error("Lý do dời lịch hẹn không được vượt quá 500 ký tự.");
    }

    const payload = {
      newShiftId: Number(newShiftId),
      reason: reason.trim(),
    };
    const response = await axiosClient.put(`/doctor/appointments/${id}/reschedule`, payload);
    return response?.data || response;
  },
};
