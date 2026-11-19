import axiosClient from "./axiosClient";

export async function getStudentMedicalHistory(signal) {
  const response = await axiosClient.get("/student/medical-history", { signal });
  if (response?.success !== true || !Array.isArray(response.data)) {
    throw new Error(response?.message || "Máy chủ trả về lịch sử khám không đúng định dạng.");
  }
  return response.data;
}
