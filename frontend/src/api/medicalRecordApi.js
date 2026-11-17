import axiosClient from "./axiosClient.js";

/**
 * Helper unwrap response bọc bởi ApiResponse<T>
 */
function unwrap(response) {
  if (response?.success === false) {
    throw new Error(response.message || "Thao tác không thành công.");
  }
  return response?.data !== undefined ? response.data : response;
}

/**
 * Service API quản lý Phiếu khám bệnh và Kê đơn thuốc điện tử (Medical Checkups & Prescriptions).
 * Kết nối các endpoint chuẩn của Spring Boot MedicalCheckupController & DiseaseGroupController:
 * 1. POST   /api/medical-records
 * 2. PUT    /api/medical-records/{id}
 * 3. PUT    /api/medical-records/{id}/prescriptions
 * 4. PUT    /api/medical-records/{id}/complete
 * 5. GET    /api/medical-records/{id}
 * 6. GET    /api/medical-records/by-appointment/{appointmentId}
 * 7. DELETE /api/medical-records/{id}
 * 8. GET    /api/medical-records/students/search
 * 9. GET    /api/disease-groups
 * 10. GET   /api/doctor/students/{studentId}/medical-overview
 */
export const medicalRecordApi = {
  /**
   * Tiếp nhận sinh viên (từ lịch hẹn CONFIRMED hoặc khẩn cấp) và khởi tạo Phiếu khám
   * POST /api/medical-records
   * @param {Object} payload
   * @param {number} [payload.appointmentId]
   * @param {number} [payload.studentId]
   * @param {string} [payload.studentCode]
   * @param {boolean} [payload.isEmergency]
   * @param {string} [payload.checkupType] KHAM_DA_KHOA | TU_VAN_TAM_LY
   * @param {string} [payload.symptoms]
   * @param {string} [payload.diagnosisNotes]
   * @param {string} [payload.treatmentSuggestions]
   * @param {number} [payload.diseaseGroupId]
   * @param {string} [payload.status] DRAFT | COMPLETED
   */
  async createCheckup(payload) {
    const response = await axiosClient.post("/medical-records", payload);
    return unwrap(response);
  },

  /**
   * Cập nhật thông tin chuyên môn của Phiếu khám
   * PUT /api/medical-records/{id}
   * @param {number|string} id
   * @param {Object} payload
   * @param {string} [payload.checkupType]
   * @param {boolean} [payload.isEmergency]
   * @param {string} [payload.periodicResult]
   * @param {string} [payload.symptoms]
   * @param {string} [payload.diagnosisNotes]
   * @param {string} [payload.treatmentSuggestions]
   * @param {number} [payload.diseaseGroupId]
   * @param {string} [payload.status] DRAFT | COMPLETED
   */
  async updateCheckup(id, payload) {
    const response = await axiosClient.put(`/medical-records/${encodeURIComponent(id)}`, payload);
    return unwrap(response);
  },

  /**
   * Cập nhật danh sách thuốc kê đơn đi kèm Phiếu khám
   * PUT /api/medical-records/{id}/prescriptions
   * @param {number|string} id
   * @param {Array<{medicineName: string, quantity: number, dosageInstruction: string}>} medicines
   */
  async updatePrescriptions(id, medicines = []) {
    const formattedMedicines = (medicines || []).map((m) => ({
      medicineName: (m.medicineName || "").trim(),
      quantity: Number(m.quantity) || 1,
      dosageInstruction: (m.dosageInstruction || "").trim(),
    }));

    const response = await axiosClient.put(
      `/medical-records/${encodeURIComponent(id)}/prescriptions`,
      { medicines: formattedMedicines }
    );
    return unwrap(response);
  },

  /**
   * Hoàn tất ca khám trực tiếp: chuyển trạng thái sang COMPLETED và khóa Read-only
   * PUT /api/medical-records/{id}/complete
   * @param {number|string} id
   */
  async completeCheckup(id) {
    const response = await axiosClient.put(`/medical-records/${encodeURIComponent(id)}/complete`);
    return unwrap(response);
  },

  /**
   * Lấy chi tiết Phiếu khám kèm Đơn thuốc theo ID
   * GET /api/medical-records/{id}
   * @param {number|string} id
   */
  async getCheckupDetail(id) {
    const response = await axiosClient.get(`/medical-records/${encodeURIComponent(id)}`);
    return unwrap(response);
  },

  /**
   * Lấy Phiếu khám theo mã lịch hẹn (appointmentId)
   * GET /api/medical-records/by-appointment/{appointmentId}
   * @param {number|string} appointmentId
   */
  async getCheckupByAppointment(appointmentId) {
    const response = await axiosClient.get(
      `/medical-records/by-appointment/${encodeURIComponent(appointmentId)}`
    );
    return unwrap(response);
  },

  /**
   * Xóa phiếu khám đang ở trạng thái DRAFT
   * DELETE /api/medical-records/{id}
   * @param {number|string} id
   */
  async deleteCheckup(id) {
    const response = await axiosClient.delete(`/medical-records/${encodeURIComponent(id)}`);
    return unwrap(response);
  },

  /**
   * Tra cứu danh sách sinh viên phục vụ tiếp nhận khẩn cấp
   * GET /api/medical-records/students/search?keyword=...
   * @param {string} keyword
   */
  async searchStudents(keyword = "") {
    const response = await axiosClient.get("/medical-records/students/search", {
      params: { keyword: keyword.trim() || undefined },
    });
    const result = unwrap(response);
    return Array.isArray(result) ? result : [];
  },

  /**
   * Lấy danh sách nhóm bệnh dịch tễ
   * GET /api/disease-groups?search=...
   * @param {string} [search]
   */
  async getDiseaseGroups(search) {
    const queryParams = {};
    if (search && search.trim()) {
      queryParams.search = search.trim();
    }
    const response = await axiosClient.get("/disease-groups", {
      params: queryParams,
    });
    const result = unwrap(response);
    return Array.isArray(result) ? result : [];
  },

  /**
   * Tra cứu hồ sơ y tế tổng hợp của Sinh viên (BE3)
   * GET /api/doctor/students/{studentId}/medical-overview
   * @param {number|string} studentId
   */
  async getStudentMedicalOverview(studentId) {
    const response = await axiosClient.get(
      `/doctor/students/${encodeURIComponent(studentId)}/medical-overview`
    );
    return unwrap(response);
  },
};
