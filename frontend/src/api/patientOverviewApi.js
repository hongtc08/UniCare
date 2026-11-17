import axiosClient from "./axiosClient";

export async function getPatientOverview(studentId, signal) {
  const response = await axiosClient.get(
    `/doctor/students/${encodeURIComponent(studentId)}/medical-overview`,
    { signal },
  );

  if (response?.success !== true || !response.data?.studentInfo) {
    throw new Error(response?.message || "Máy chủ chưa trả được hồ sơ sinh viên.");
  }

  return response.data;
}
