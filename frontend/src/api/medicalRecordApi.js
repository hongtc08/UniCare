import axiosClient from "./axiosClient.js";

/**
 * @typedef {"GENERAL" | "PSYCHOLOGICAL"} CheckupType
 * @typedef {"PASSED" | "FAILED" | null} PeriodicResult
 * @typedef {"QUA_TAI" | "THIEU_THIET_BI" | "CAP_CUU_NANG"} TransferReasonCategory
 * @typedef {"DRAFT" | "COMPLETED"} CheckupStatus
 *
 * @typedef {Object} TransferRecord
 * @property {number} id
 * @property {string} recordCode
 * @property {number} checkupId
 * @property {string} targetHospital
 * @property {TransferReasonCategory} reasonCategory
 * @property {string} transferReason
 * @property {string} currentCondition
 * @property {string} firstAidNotes
 * @property {string | null} refusalNotes
 * @property {string} transferredAt
 *
 * @typedef {Object} CreateTransferRecordPayload
 * @property {string} targetHospital
 * @property {TransferReasonCategory} reasonCategory
 * @property {string} transferReason
 * @property {string} currentCondition
 * @property {string} firstAidNotes
 * @property {string | null} [refusalNotes]
 * @property {string} [transferredAt]
 *
 * @typedef {Object} TransferReferralPrintResponse
 * @property {string} referringUnitName
 * @property {number} transferId
 * @property {string} recordCode
 * @property {number} checkupId
 * @property {string} checkupCode
 * @property {string} targetHospital
 * @property {TransferReasonCategory} reasonCategory
 * @property {string} reasonCategoryLabel
 * @property {string} transferReason
 * @property {string} currentCondition
 * @property {string} firstAidNotes
 * @property {string | null} refusalNotes
 * @property {string} transferredAt
 * @property {boolean} readOnly
 * @property {number | null} studentId
 * @property {string | null} studentCode
 * @property {string | null} studentName
 * @property {string | null} dateOfBirth
 * @property {string | null} gender
 * @property {string | null} studentPhone
 * @property {string | null} studentEmail
 * @property {string | null} classCode
 * @property {string | null} className
 * @property {string | null} facultyName
 * @property {string | null} insuranceCardNumber
 * @property {string | null} insuranceInitialHospital
 * @property {string | null} insuranceEffectiveDate
 * @property {string | null} insuranceExpirationDate
 * @property {number | null} height
 * @property {number | null} weight
 * @property {string | null} bloodType
 * @property {number | null} bmi
 * @property {string | null} allergies
 * @property {string | null} underlyingDiseases
 * @property {CheckupType | null} checkupType
 * @property {boolean | null} isEmergency
 * @property {string | null} emergencyLevel
 * @property {string | null} emergencyType
 * @property {string | null} symptoms
 * @property {string | null} diagnosisNotes
 * @property {string | null} treatmentSuggestions
 * @property {string | null} diseaseGroupName
 * @property {Array<{id?: number, medicineName: string, quantity: number, dosageInstruction: string}>} prescribedMedicines
 * @property {string | null} checkupCreatedAt
 * @property {string | null} checkupCompletedAt
 * @property {CheckupStatus | null} checkupStatus
 * @property {number | null} staffId
 * @property {string | null} staffCode
 * @property {string | null} doctorName
 * @property {string | null} doctorTitle
 * @property {string | null} doctorSpecialty
 *
 * @typedef {Object} MedicalCheckupPayload
 * @property {number} [appointmentId]
 * @property {number} [studentId]
 * @property {string} [studentCode]
 * @property {boolean} [isEmergency]
 * @property {CheckupType} [checkupType]
 * @property {boolean} [isPeriodic]
 * @property {PeriodicResult} [periodicResult]
 * @property {string | null} [symptoms]
 * @property {string | null} [diagnosisNotes]
 * @property {string | null} [treatmentSuggestions]
 * @property {number | null} [diseaseGroupId]
 * @property {CheckupStatus} [status]
 */

export const CHECKUP_TYPES = Object.freeze({
  GENERAL: "GENERAL",
  PSYCHOLOGICAL: "PSYCHOLOGICAL",
});

export const PERIODIC_RESULTS = Object.freeze({
  PASSED: "PASSED",
  FAILED: "FAILED",
});

export const TRANSFER_REASON_CATEGORIES = Object.freeze({
  QUA_TAI: "QUA_TAI",
  THIEU_THIET_BI: "THIEU_THIET_BI",
  CAP_CUU_NANG: "CAP_CUU_NANG",
});

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
 * Service API quản lý Phiếu khám bệnh, Kê đơn thuốc điện tử và Hồ sơ Chuyển viện.
 * Kết nối các endpoint chuẩn của Spring Boot MedicalCheckupController & DiseaseGroupController:
 * 1. POST   /api/medical-records
 * 2. PUT    /api/medical-records/{id}
 * 3. PUT    /api/medical-records/{id}/prescriptions
 * 4. PUT    /api/medical-records/{id}/complete
 * 5. GET    /api/medical-records/{id}
 * 6. GET    /api/medical-records/by-appointment/{appointmentId}
 * 7. DELETE /api/medical-records/{id}
 * 8. POST   /api/medical-records/{id}/transfer
 * 9. GET    /api/medical-records/{id}/transfer/print
 * 10. GET   /api/medical-records/students/search
 * 11. GET   /api/disease-groups
 * 12. GET   /api/doctor/students/{studentId}/medical-overview
 */
export const medicalRecordApi = {
  /**
   * Tiếp nhận sinh viên (từ lịch hẹn CONFIRMED hoặc khẩn cấp) và khởi tạo Phiếu khám
   * POST /api/medical-records
   * @param {MedicalCheckupPayload} payload
   */
  async createCheckup(payload) {
    const response = await axiosClient.post("/medical-records", payload);
    return unwrap(response);
  },

  /**
   * Cập nhật thông tin chuyên môn của Phiếu khám
   * PUT /api/medical-records/{id}
   * @param {number|string} id
   * @param {MedicalCheckupPayload} payload
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
   * Lập Hồ sơ Chuyển viện, khóa chỉ đọc và tự động kết thúc ca khám tại trường
   * POST /api/medical-records/{id}/transfer
   * @param {number|string} id
   * @param {CreateTransferRecordPayload} payload
   */
  async createTransferRecord(id, payload) {
    const response = await axiosClient.post(
      `/medical-records/${encodeURIComponent(id)}/transfer`,
      payload
    );
    return unwrap(response);
  },

  /**
   * Kết xuất dữ liệu chuẩn để xem / in Giấy giới thiệu chuyển viện
   * GET /api/medical-records/{id}/transfer/print
   * @param {number|string} id
   * @returns {Promise<TransferReferralPrintResponse>}
   */
  async getTransferReferralPrint(id) {
    const response = await axiosClient.get(
      `/medical-records/${encodeURIComponent(id)}/transfer/print`
    );
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

  /**
   * Tra cứu thông tin chi tiết Lịch hẹn theo ID
   * GET /api/appointments/{id}
   * @param {number|string} id
   */
  async getAppointmentById(id) {
    const response = await axiosClient.get(`/appointments/${encodeURIComponent(id)}`);
    return unwrap(response);
  },
};
