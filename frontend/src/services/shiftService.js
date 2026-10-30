import axiosClient from "../api/axiosClient";

/**
 * Service gọi API quản lý ca khám (/api/shifts)
 */
export const shiftService = {
  /**
   * Lấy danh sách ca khám theo bộ lọc
   * @param {Object} filters
   * @param {string} [filters.date] YYYY-MM-DD
   * @param {string} [filters.fromDate] YYYY-MM-DD (Đầu tuần)
   * @param {string} [filters.toDate] YYYY-MM-DD (Cuối tuần)
   * @param {number|string} [filters.doctorId] ID của Bác sĩ
   * @param {string} [filters.serviceType] KHAM_DA_KHOA | TU_VAN_TAM_LY
   * @param {string} [filters.status] DANG_MO | DA_DAY | DA_HUY
   */
  getShifts: (filters = {}) => {
    const params = {};
    if (filters.date) params.date = filters.date;
    if (filters.fromDate) params.fromDate = filters.fromDate;
    if (filters.toDate) params.toDate = filters.toDate;
    if (filters.doctorId && filters.doctorId !== "ALL") params.doctorId = filters.doctorId;
    if (filters.serviceType && filters.serviceType !== "ALL") params.serviceType = filters.serviceType;
    if (filters.status && filters.status !== "ALL") params.status = filters.status;

    return axiosClient.get("/shifts", { params });
  },

  /**
   * Xem thông tin chi tiết một ca khám
   * @param {number|string} id
   */
  getShiftById: (id) => {
    return axiosClient.get(`/shifts/${id}`);
  },

  /**
   * Mở ca khám mới
   * @param {Object} data
   * @param {string} data.shiftDate YYYY-MM-DD
   * @param {string} data.startTime HH:mm
   * @param {string} data.endTime HH:mm
   * @param {number|string} data.doctorId
   * @param {string} data.serviceType KHAM_DA_KHOA | TU_VAN_TAM_LY
   * @param {string} [data.room]
   * @param {number} data.maxQuantity
   */
  createShift: (data) => {
    return axiosClient.post("/shifts", data);
  },

  /**
   * Cập nhật ca khám
   * @param {number|string} id
   * @param {Object} data
   */
  updateShift: (id, data) => {
    return axiosClient.put(`/shifts/${id}`, data);
  },

  /**
   * Xóa ca khám
   * @param {number|string} id
   */
  deleteShift: (id) => {
    return axiosClient.delete(`/shifts/${id}`);
  },
};

export default shiftService;

