import axiosClient from "./axiosClient.js";
import {
  INITIAL_MOCK_APPOINTMENTS,
  createMockAppointmentsForDate,
  getMockAvailableShiftsForDate,
} from "../components/doctor/mockDoctorAppointments.js";
import { todayInVietnam } from "../utils/appointments.js";

const STORAGE_KEY = "unicare_doctor_appointments_mock";
let inMemoryStore = null;

function loadStoredAppointments(targetDate) {
  let list = [];
  if (typeof localStorage !== "undefined") {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed) && parsed.length > 0) list = parsed;
      }
    } catch (e) {
      console.warn("Could not read appointments from localStorage", e);
    }
  } else if (inMemoryStore) {
    list = inMemoryStore;
  }

  const effectiveDate = targetDate || todayInVietnam();

  if (list.length === 0) {
    list = createMockAppointmentsForDate(effectiveDate);
    saveStoredAppointments(list);
    return list;
  }

  // Đảm bảo targetDate luôn có các ca hẹn mẫu để người dùng kiểm thử
  const hasTargetDate = list.some((item) => (item.shiftDate || item.shift_date) === effectiveDate);
  if (!hasTargetDate) {
    const newItems = createMockAppointmentsForDate(effectiveDate).map((item, idx) => ({
      ...item,
      id: list.length + idx + 1,
    }));
    list = [...list, ...newItems];
    saveStoredAppointments(list);
  }

  return list;
}

function saveStoredAppointments(appointments) {
  if (typeof localStorage !== "undefined") {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(appointments));
    } catch (e) {
      console.warn("Could not save appointments to localStorage", e);
    }
  } else {
    inMemoryStore = appointments;
  }
}

export const doctorAppointmentApi = {
  /**
   * Lấy danh sách lịch hẹn của bác sĩ
   * GET /api/doctor/appointments
   * Hỗ trợ lọc theo date, shiftType (hoặc serviceType), status, và query tìm kiếm.
   * Tự động fallback về Mock data khi backend chưa có dữ liệu để người dùng luôn test được.
   */
  async getDoctorAppointments(params = {}, signal) {
    const { date, shiftType, serviceType, status, search, forceMock } = params;
    const effectiveType = shiftType || serviceType;
    const effectiveDate = date || todayInVietnam();

    if (!forceMock) {
      try {
        const queryParams = {};
        if (date) queryParams.date = date;
        if (effectiveType && effectiveType !== "ALL") queryParams.shiftType = effectiveType;
        if (status && status !== "ALL") queryParams.status = status;

        const response = await axiosClient.get("/doctor/appointments", {
          params: queryParams,
          signal,
          timeout: 4000,
        });

        if (response?.data && Array.isArray(response.data) && response.data.length > 0) {
          let results = response.data;
          if (search && search.trim()) {
            const q = search.trim().toLowerCase();
            results = results.filter(
              (item) =>
                (item.studentName || item.full_name || "").toLowerCase().includes(q) ||
                (item.studentCode || item.student_code || "").toLowerCase().includes(q)
            );
          }
          return results;
        }
      } catch (err) {
        console.info("Using local mock doctor appointments (API fallback):", err?.message);
      }
    }

    // Mock filtering logic - Luôn đảm bảo có dữ liệu ngày đang chọn để test
    let mockList = loadStoredAppointments(effectiveDate);

    if (effectiveDate) {
      mockList = mockList.filter((item) => (item.shiftDate || item.shift_date) === effectiveDate);
    }

    if (effectiveType && effectiveType !== "ALL") {
      mockList = mockList.filter((item) => (item.shiftType || item.shift_type) === effectiveType);
    }

    if (status && status !== "ALL") {
      mockList = mockList.filter((item) => item.status === status);
    }

    if (search && search.trim()) {
      const q = search.trim().toLowerCase();
      mockList = mockList.filter(
        (item) =>
          (item.studentName || item.full_name || "").toLowerCase().includes(q) ||
          (item.studentCode || item.student_code || "").toLowerCase().includes(q)
      );
    }

    return mockList;
  },

  /**
   * Bác sĩ hủy lịch hẹn
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

    try {
      const response = await axiosClient.put(
        `/doctor/appointments/${id}/cancel`,
        { reason: reason.trim() },
        { timeout: 8000 }
      );
      if (response?.data) return response.data;
    } catch (err) {
      console.info("Simulating cancel appointment in mock store:", err?.message);
    }

    // Mock store update
    const current = loadStoredAppointments();
    const index = current.findIndex((item) => String(item.id) === String(id));
    if (index === -1) {
      throw new Error("Không tìm thấy lịch hẹn cần hủy.");
    }

    const updated = {
      ...current[index],
      status: "CANCELED",
      canceled_by: "DOCTOR",
      canceledBy: "DOCTOR",
      cancel_reschedule_reason: reason.trim(),
      cancelRescheduleReason: reason.trim(),
    };

    current[index] = updated;
    saveStoredAppointments(current);
    return updated;
  },

  /**
   * Bác sĩ dời lịch hẹn
   * PUT /api/doctor/appointments/{id}/reschedule
   * Request body: { newShiftId: number, reason: "..." }
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

    try {
      const response = await axiosClient.put(
        `/doctor/appointments/${id}/reschedule`,
        { newShiftId: Number(newShiftId), reason: reason.trim() },
        { timeout: 8000 }
      );
      if (response?.data) return response.data;
    } catch (err) {
      console.info("Simulating reschedule appointment in mock store:", err?.message);
    }

    // Mock store update
    const current = loadStoredAppointments();
    const index = current.findIndex((item) => String(item.id) === String(id));
    if (index === -1) {
      throw new Error("Không tìm thấy lịch hẹn cần dời.");
    }

    const targetShifts = getMockAvailableShiftsForDate();
    const targetShift = targetShifts.find((s) => String(s.id) === String(newShiftId));
    const updated = {
      ...current[index],
      shift_id: Number(newShiftId),
      shiftId: Number(newShiftId),
      shift_date: targetShift?.shiftDate || targetShift?.shift_date || current[index].shift_date,
      shiftDate: targetShift?.shiftDate || targetShift?.shift_date || current[index].shiftDate,
      start_time: targetShift?.startTime || targetShift?.start_time || current[index].start_time,
      startTime: targetShift?.startTime || targetShift?.start_time || current[index].startTime,
      end_time: targetShift?.endTime || targetShift?.end_time || current[index].end_time,
      endTime: targetShift?.endTime || targetShift?.end_time || current[index].endTime,
      room: targetShift?.room || current[index].room,
      status: "CONFIRMED", // Sau khi dời ca mới, lịch quay lại trạng thái CONFIRMED
      cancel_reschedule_reason: `Đã dời sang ca mới (${reason.trim()})`,
      cancelRescheduleReason: `Đã dời sang ca mới (${reason.trim()})`,
    };

    current[index] = updated;
    saveStoredAppointments(current);
    return updated;
  },

  /**
   * Lấy danh sách ca khám mở còn slot trống
   * GET /api/shifts?status=OPEN&date=...
   */
  async getAvailableShifts(params = {}) {
    const { date, serviceType } = params;
    const targetDate = date || todayInVietnam();

    try {
      const queryParams = { status: "OPEN" };
      if (date) queryParams.date = date;
      if (serviceType && serviceType !== "ALL") queryParams.serviceType = serviceType;

      const response = await axiosClient.get("/shifts", {
        params: queryParams,
        timeout: 4000,
      });

      if (response?.data && Array.isArray(response.data) && response.data.length > 0) {
        // Lọc ca còn slot trống: bookedSlots < maxSlots
        const openShifts = response.data.filter((s) => {
          const booked = Number(s.bookedSlots ?? s.booked_slots ?? 0);
          const max = Number(s.maxSlots ?? s.max_slots ?? 0);
          return s.status === "OPEN" && booked < max;
        });
        if (openShifts.length > 0) return openShifts;
      }
    } catch (err) {
      console.info("Using mock available shifts for reschedule:", err?.message);
    }

    let shifts = getMockAvailableShiftsForDate(targetDate);
    if (serviceType && serviceType !== "ALL") {
      shifts = shifts.filter((s) => (s.shiftType || s.shift_type || s.serviceType) === serviceType);
    }
    return shifts.filter((s) => (s.bookedSlots ?? s.booked_slots ?? 0) < (s.maxSlots ?? s.max_slots ?? 0));
  },

  /**
   * Reset lại dữ liệu mock ban đầu cho ngày được chọn
   */
  resetMockData(targetDate = todayInVietnam()) {
    if (typeof localStorage !== "undefined") {
      localStorage.removeItem(STORAGE_KEY);
    }
    inMemoryStore = null;
    const fresh = createMockAppointmentsForDate(targetDate);
    saveStoredAppointments(fresh);
    return fresh;
  },
};
