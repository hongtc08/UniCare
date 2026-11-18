import axiosClient from "./axiosClient";

export const emergencyIntakeApi = {
  async searchStudents(keyword, signal) {
    const response = await axiosClient.get("/medical-records/students/search", {
      params: { keyword },
      signal,
    });
    if (response?.success !== true || !Array.isArray(response.data)) {
      throw new Error(response?.message || "Không đọc được kết quả tra cứu sinh viên.");
    }
    return response.data;
  },

  async create(studentId, symptoms) {
    const response = await axiosClient.post("/medical-records", {
      studentId,
      isEmergency: true,
      status: "DRAFT",
      symptoms: symptoms.trim() || null,
    });
    if (response?.success !== true || response.data?.id == null) {
      throw new Error(response?.message || "Chưa nhận được mã phiếu từ máy chủ.");
    }
    return response.data;
  },
};
