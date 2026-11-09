import test from "node:test";
import assert from "node:assert/strict";
import { doctorAppointmentApi } from "../src/api/doctorAppointmentApi.js";
import axiosClient from "../src/api/axiosClient.js";

test("doctorAppointmentApi.getDoctorAppointments formats params and filters correctly", async () => {
  const originalGet = axiosClient.get;
  let capturedUrl = "";
  let capturedParams = null;

  try {
    axiosClient.get = async (url, options) => {
      capturedUrl = url;
      capturedParams = options?.params;
      return {
        success: true,
        data: [
          {
            id: 1,
            appointmentCode: "LH-001",
            studentCode: "20240318",
            studentName: "Phạm Ngọc Mai",
            status: "CONFIRMED",
            shiftType: "KHAM_DA_KHOA",
          },
          {
            id: 2,
            appointmentCode: "LH-002",
            studentCode: "20231042",
            studentName: "Trần Hoàng Nam",
            status: "COMPLETED",
            shiftType: "KHAM_DA_KHOA",
          },
        ],
      };
    };

    // 1. Test params mapping
    const result = await doctorAppointmentApi.getDoctorAppointments({
      date: "2026-11-08",
      shiftType: "KHAM_DA_KHOA",
      status: "CONFIRMED",
      keyword: "Ngọc Mai",
    });

    assert.equal(capturedUrl, "/doctor/appointments");
    assert.equal(capturedParams.date, "2026-11-08");
    assert.equal(capturedParams.shiftType, "KHAM_DA_KHOA");
    assert.equal(capturedParams.status, "CONFIRMED");
    assert.equal(capturedParams.keyword, "Ngọc Mai");

    // Check filter by keyword on returned list
    assert.equal(result.length, 1);
    assert.equal(result[0].studentName, "Phạm Ngọc Mai");
  } finally {
    axiosClient.get = originalGet;
  }
});

test("doctorAppointmentApi.getAvailableShifts falls back to /shifts with status=OPEN", async () => {
  const originalGet = axiosClient.get;
  const calls = [];

  try {
    axiosClient.get = async (url, options) => {
      calls.push({ url, params: options?.params });
      if (url === "/shifts/available") {
        const error = new Error("Not Found");
        error.response = { status: 404 };
        throw error;
      }
      if (url === "/shifts") {
        return {
          success: true,
          data: [
            { id: 101, status: "OPEN", maxSlots: 10, bookedSlots: 3 },
            { id: 102, status: "OPEN", maxSlots: 5, bookedSlots: 5 }, // Full
            { id: 103, status: "CLOSED", maxSlots: 10, bookedSlots: 0 },
          ],
        };
      }
      return { success: true, data: [] };
    };

    const available = await doctorAppointmentApi.getAvailableShifts({
      date: "2026-11-08",
      shiftType: "KHAM_DA_KHOA",
    });

    assert.equal(calls.length, 2);
    assert.equal(calls[0].url, "/shifts/available");
    assert.equal(calls[1].url, "/shifts");
    // Only id: 101 is OPEN and bookedSlots < maxSlots
    assert.equal(available.length, 1);
    assert.equal(available[0].id, 101);
  } finally {
    axiosClient.get = originalGet;
  }
});

test("doctorAppointmentApi.cancelAppointment validates reason and calls PUT", async () => {
  const originalPut = axiosClient.put;
  let capturedUrl = "";
  let capturedPayload = null;

  try {
    axiosClient.put = async (url, payload) => {
      capturedUrl = url;
      capturedPayload = payload;
      return { success: true, data: { id: 10, status: "CANCELED" } };
    };

    // Empty reason
    await assert.rejects(
      () => doctorAppointmentApi.cancelAppointment(10, ""),
      /Lý do hủy lịch hẹn không được để trống/
    );

    // Over 500 characters
    await assert.rejects(
      () => doctorAppointmentApi.cancelAppointment(10, "A".repeat(501)),
      /Lý do hủy lịch hẹn không được vượt quá 500 ký tự/
    );

    // Valid cancel
    const res = await doctorAppointmentApi.cancelAppointment(10, "Bác sĩ có việc đột xuất");
    assert.equal(capturedUrl, "/doctor/appointments/10/cancel");
    assert.deepEqual(capturedPayload, { reason: "Bác sĩ có việc đột xuất" });
    assert.equal(res.status, "CANCELED");
  } finally {
    axiosClient.put = originalPut;
  }
});

test("doctorAppointmentApi.rescheduleAppointment validates newShiftId and reason", async () => {
  const originalPut = axiosClient.put;
  let capturedUrl = "";
  let capturedPayload = null;

  try {
    axiosClient.put = async (url, payload) => {
      capturedUrl = url;
      capturedPayload = payload;
      return { success: true, data: { id: 15, shiftId: 205, status: "CONFIRMED" } };
    };

    // Missing shift ID
    await assert.rejects(
      () => doctorAppointmentApi.rescheduleAppointment(15, null, "Dời ca"),
      /Vui lòng chọn ca khám mới/
    );

    // Missing reason
    await assert.rejects(
      () => doctorAppointmentApi.rescheduleAppointment(15, 205, "   "),
      /Lý do dời lịch hẹn không được để trống/
    );

    // Valid reschedule
    const res = await doctorAppointmentApi.rescheduleAppointment(15, 205, "Chuyển ca chiều");
    assert.equal(capturedUrl, "/doctor/appointments/15/reschedule");
    assert.deepEqual(capturedPayload, { newShiftId: 205, reason: "Chuyển ca chiều" });
    assert.equal(res.shiftId, 205);
  } finally {
    axiosClient.put = originalPut;
  }
});
