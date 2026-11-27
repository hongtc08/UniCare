import axiosClient from "./axiosClient.js";

/**
 * Unwrap response bọc bởi ApiResponse<T> của Backend.
 */
function unwrap(response) {
  if (response?.success === false) {
    throw new Error(response.message || "Thao tác không thành công.");
  }
  return response?.data !== undefined ? response.data : response;
}

function buildQueryParams({ period, fromDate, toDate } = {}) {
  const queryParams = {};
  if (period && String(period).trim()) {
    queryParams.period = String(period).trim();
  }
  if (fromDate && String(fromDate).trim()) {
    queryParams.fromDate = String(fromDate).trim();
  }
  if (toDate && String(toDate).trim()) {
    queryParams.toDate = String(toDate).trim();
  }
  return queryParams;
}

/**
 * Service API tra cứu lịch sử DASS-21 và Nhật ký tâm trạng của Sinh viên dành cho Bác sĩ / Chuyên gia tâm lý (BE3 - SP6-03).
 * Endpoints:
 * 1. GET /api/doctor/students/{studentId}/mental-health-history
 * 2. GET /api/doctor/students/{studentId}/dass21-history
 * 3. GET /api/doctor/students/{studentId}/mood-logs
 */
export const doctorMentalHealthApi = {
  /**
   * Lấy tổng hợp lịch sử kết quả DASS-21 (chuỗi điểm + danh sách) và nhật ký tâm trạng của sinh viên.
   * GET /api/doctor/students/{studentId}/mental-health-history
   * @param {number|string} studentId
   * @param {Object} [params]
   * @param {"WEEK"|"MONTH"|"ALL"|"CUSTOM"} [params.period]
   * @param {string} [params.fromDate] YYYY-MM-DD
   * @param {string} [params.toDate] YYYY-MM-DD
   * @param {AbortSignal} [signal]
   */
  async getStudentMentalHealthHistory(studentId, params = {}, signal) {
    if (!studentId) {
      throw new Error("Thiếu mã sinh viên (studentId).");
    }
    const response = await axiosClient.get(
      `/doctor/students/${encodeURIComponent(studentId)}/mental-health-history`,
      {
        params: buildQueryParams(params),
        signal,
      }
    );
    return unwrap(response);
  },

  /**
   * Lấy riêng lịch sử kết quả các lần test DASS-21 của sinh viên.
   * GET /api/doctor/students/{studentId}/dass21-history
   * @param {number|string} studentId
   * @param {Object} [params]
   * @param {string} [params.fromDate] YYYY-MM-DD
   * @param {string} [params.toDate] YYYY-MM-DD
   * @param {AbortSignal} [signal]
   */
  async getStudentDass21History(studentId, params = {}, signal) {
    if (!studentId) {
      throw new Error("Thiếu mã sinh viên (studentId).");
    }
    const response = await axiosClient.get(
      `/doctor/students/${encodeURIComponent(studentId)}/dass21-history`,
      {
        params: buildQueryParams({
          fromDate: params.fromDate,
          toDate: params.toDate,
        }),
        signal,
      }
    );
    const result = unwrap(response);
    return Array.isArray(result) ? result : [];
  },

  /**
   * Lấy riêng lịch sử nhật ký tâm trạng của sinh viên.
   * GET /api/doctor/students/{studentId}/mood-logs
   * @param {number|string} studentId
   * @param {Object} [params]
   * @param {"WEEK"|"MONTH"|"ALL"|"CUSTOM"} [params.period]
   * @param {string} [params.fromDate] YYYY-MM-DD
   * @param {string} [params.toDate] YYYY-MM-DD
   * @param {AbortSignal} [signal]
   */
  async getStudentMoodLogs(studentId, params = {}, signal) {
    if (!studentId) {
      throw new Error("Thiếu mã sinh viên (studentId).");
    }
    const response = await axiosClient.get(
      `/doctor/students/${encodeURIComponent(studentId)}/mood-logs`,
      {
        params: buildQueryParams(params),
        signal,
      }
    );
    return unwrap(response);
  },
};
