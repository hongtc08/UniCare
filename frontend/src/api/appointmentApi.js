import axiosClient from "./axiosClient";
import { normalizeShift, requiredReason } from "../utils/appointments";

function unwrap(response) {
  if (response?.success === false) throw new Error(response.message || "Thao tác không thành công.");
  if (!response || !Object.hasOwn(response, "data")) throw new Error("Máy chủ trả dữ liệu không đúng định dạng.");
  return response.data;
}

function list(response) {
  const data = unwrap(response);
  if (!Array.isArray(data)) throw new Error("Máy chủ trả danh sách không đúng định dạng.");
  return data;
}

function saved(response) {
  const data = unwrap(response);
  if (!data?.id) throw new Error("Máy chủ chưa trả thông tin lịch hẹn đã lưu. Hãy kiểm tra Lịch hẹn của tôi trước khi thử lại.");
  return data;
}

export const appointmentApi = {
  async facilities(signal) {
    return list(await axiosClient.get("/facilities", { signal, timeout: 20000 }));
  },
  async shifts(params, signal) {
    return list(await axiosClient.get("/shifts", { params, signal, timeout: 20000 })).map(normalizeShift);
  },
  async mine(signal) {
    return list(await axiosClient.get("/appointments/my-appointments", { signal, timeout: 20000 }));
  },
  async create(shiftId, reasonSymptoms) {
    try {
      return saved(await axiosClient.post("/appointments", { shiftId, reasonSymptoms: reasonSymptoms.trim() }, { timeout: 20000 }));
    } catch (error) {
      if ([404, 405].includes(error?.response?.status)) throw new Error("Chức năng tạo lịch hẹn chưa sẵn sàng trên máy chủ. Vui lòng thử lại sau.", { cause: error });
      throw error;
    }
  },
  async cancel(id, reason) {
    const message = requiredReason(reason);
    if (message) throw new Error(message);
    return saved(await axiosClient.put(`/appointments/${id}/cancel`, { reason: reason.trim() }, { timeout: 20000 }));
  },
  async reschedule(id, newShiftId, reason) {
    const message = requiredReason(reason);
    if (message) throw new Error(message);
    return saved(await axiosClient.put(`/appointments/${id}/reschedule`, { newShiftId, reason: reason.trim() }, { timeout: 20000 }));
  },
};
