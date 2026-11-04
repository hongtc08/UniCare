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
   * @param {number|string} [filters.doctorId] ID của Bác sĩ / Nhân viên y tế (staff_id)
   * @param {number|string} [filters.staffId] Alias chuẩn theo DB (staff_id)
   * @param {string} [filters.serviceType] KHAM_DA_KHOA | TU_VAN_TAM_LY (shift_type)
   * @param {string} [filters.shiftType] KHAM_DA_KHOA | TU_VAN_TAM_LY
   * @param {string} [filters.status] OPEN | FULL | ENDED (hoặc alias cũ CON_TRONG | DA_DAY | DA_HUY)
   * @param {string} [filters.room] Tên phòng khám
   */
  getShifts: (filters = {}) => {
    const params = {};
    if (filters.date) params.date = filters.date;
    if (filters.fromDate) params.fromDate = filters.fromDate;
    if (filters.toDate) params.toDate = filters.toDate;
    const staffId = filters.staffId || filters.doctorId;
    if (staffId && staffId !== "ALL") params.doctorId = staffId;
    const type = filters.shiftType || filters.serviceType;
    if (type && type !== "ALL") params.serviceType = type;
    if (filters.status && filters.status !== "ALL") params.status = filters.status;
    if (filters.room && filters.room !== "ALL") params.room = filters.room;

    return axiosClient.get("/shifts", { params });
  },

  /**
   * Lấy danh sách Bác sĩ / Nhân viên y tế hoạt động cho Dropdown
   * GET /api/shifts/doctors
   */
  getDoctors: () => {
    return axiosClient.get("/shifts/doctors");
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
   * Chuẩn hóa payload hỗ trợ cả schema DB mới (staff_id/staffId, shift_type/shiftType, max_slots/maxSlots, room, status)
   * và các alias tương thích ngược (doctorId, serviceType, maxQuantity).
   * @param {Object} data
   * @param {string} data.shiftDate YYYY-MM-DD
   * @param {string} data.startTime HH:mm
   * @param {string} data.endTime HH:mm
   * @param {number|string} [data.staffId]
   * @param {number|string} [data.doctorId]
   * @param {string} [data.shiftType] KHAM_DA_KHOA | TU_VAN_TAM_LY
   * @param {string} [data.serviceType]
   * @param {string} [data.room]
   * @param {number} [data.maxSlots]
   * @param {number} [data.maxQuantity]
   * @param {string} [data.status] OPEN | FULL | ENDED
   */
  createShift: (data) => {
    const payload = {
      ...data,
      staffId: data.staffId ?? data.doctorId,
      doctorId: data.doctorId ?? data.staffId,
      shiftType: data.shiftType ?? data.serviceType,
      serviceType: data.serviceType ?? data.shiftType,
      maxSlots: data.maxSlots ?? data.maxQuantity,
      maxQuantity: data.maxQuantity ?? data.maxSlots,
      status: data.status || "OPEN",
    };
    return axiosClient.post("/shifts", payload);
  },

  /**
   * Cập nhật ca khám
   * @param {number|string} id
   * @param {Object} data
   */
  updateShift: (id, data) => {
    const payload = {
      ...data,
      staffId: data.staffId ?? data.doctorId,
      doctorId: data.doctorId ?? data.staffId,
      shiftType: data.shiftType ?? data.serviceType,
      serviceType: data.serviceType ?? data.shiftType,
      maxSlots: data.maxSlots ?? data.maxQuantity,
      maxQuantity: data.maxQuantity ?? data.maxSlots,
    };
    return axiosClient.put(`/shifts/${id}`, payload);
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
