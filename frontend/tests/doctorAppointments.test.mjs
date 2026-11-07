import test from "node:test";
import assert from "node:assert/strict";
import { INITIAL_MOCK_APPOINTMENTS, MOCK_AVAILABLE_SHIFTS } from "../src/components/doctor/mockDoctorAppointments.js";
import { doctorAppointmentApi } from "../src/api/doctorAppointmentApi.js";

test("doctor appointments mock data contains 100% required DB fields", () => {
  assert.ok(INITIAL_MOCK_APPOINTMENTS.length >= 4, "Should have mock appointments");

  for (const item of INITIAL_MOCK_APPOINTMENTS) {
    // Appointment identification
    assert.ok(item.id, "appointment id must exist");
    assert.ok(item.appointment_code || item.appointmentCode, "appointmentCode must exist");

    // Student fields
    assert.ok(item.student_code || item.studentCode, "studentCode must exist");
    assert.ok(item.full_name || item.studentName, "studentName must exist");
    assert.ok(item.class_code || item.classCode, "classCode must exist");
    assert.ok(item.faculty_name || item.facultyName, "facultyName must exist");
    assert.ok(item.gender, "gender must exist");
    assert.ok(item.date_of_birth || item.dateOfBirth, "dateOfBirth must exist");
    assert.ok(item.phone_number || item.studentPhone, "phoneNumber must exist");

    // Shift fields
    assert.ok(item.shift_date || item.shiftDate, "shiftDate must exist");
    assert.ok(item.start_time || item.startTime, "startTime must exist");
    assert.ok(item.end_time || item.endTime, "endTime must exist");
    assert.ok(item.shift_type || item.shiftType, "shiftType must exist");
    assert.ok(["KHAM_DA_KHOA", "TU_VAN_TAM_LY"].includes(item.shift_type || item.shiftType), "valid shiftType");
    assert.ok(item.room, "room must exist");

    // Status
    assert.ok(["CONFIRMED", "COMPLETED", "CANCELED", "NO_SHOW"].includes(item.status), "valid status");

    // Health records
    const hr = item.health_records;
    assert.ok(hr, "health_records must exist");
    assert.ok(typeof hr.height === "number", "height must be number");
    assert.ok(typeof hr.weight === "number", "weight must be number");
    assert.ok(typeof hr.bmi === "number", "bmi must be number");
    assert.ok(hr.bmi_classification || hr.bmiClassification, "bmiClassification must exist");
    assert.ok(hr.blood_type || hr.bloodType, "bloodType must exist");
    assert.ok(hr.allergies !== undefined, "allergies must exist");
    assert.ok(hr.underlying_diseases || hr.underlyingDiseases !== undefined, "underlyingDiseases must exist");
  }
});

test("doctor appointments filter by status and shiftType", async () => {
  // Reset data
  doctorAppointmentApi.resetMockData();

  // Test filter status CONFIRMED
  const confirmed = await doctorAppointmentApi.getDoctorAppointments({ status: "CONFIRMED" });
  assert.ok(confirmed.length > 0);
  assert.ok(confirmed.every((item) => item.status === "CONFIRMED"));

  // Test filter status COMPLETED
  const completed = await doctorAppointmentApi.getDoctorAppointments({ status: "COMPLETED" });
  assert.ok(completed.length > 0);
  assert.ok(completed.every((item) => item.status === "COMPLETED"));

  // Test filter status CANCELED
  const canceled = await doctorAppointmentApi.getDoctorAppointments({ status: "CANCELED" });
  assert.ok(canceled.length > 0);
  assert.ok(canceled.every((item) => item.status === "CANCELED"));

  // Test filter status NO_SHOW
  const noShow = await doctorAppointmentApi.getDoctorAppointments({ status: "NO_SHOW" });
  assert.ok(noShow.length > 0);
  assert.ok(noShow.every((item) => item.status === "NO_SHOW"));

  // Test filter serviceType KHAM_DA_KHOA
  const general = await doctorAppointmentApi.getDoctorAppointments({ shiftType: "KHAM_DA_KHOA" });
  assert.ok(general.length > 0);
  assert.ok(general.every((item) => (item.shiftType || item.shift_type) === "KHAM_DA_KHOA"));

  // Test filter serviceType TU_VAN_TAM_LY
  const psychology = await doctorAppointmentApi.getDoctorAppointments({ shiftType: "TU_VAN_TAM_LY" });
  assert.ok(psychology.length > 0);
  assert.ok(psychology.every((item) => (item.shiftType || item.shift_type) === "TU_VAN_TAM_LY"));

  // Test search by studentCode
  const byCode = await doctorAppointmentApi.getDoctorAppointments({ search: "20240318" });
  assert.ok(byCode.length > 0);
  assert.equal(byCode[0].studentCode || byCode[0].student_code, "20240318");

  // Test search by studentName
  const byName = await doctorAppointmentApi.getDoctorAppointments({ search: "Ngọc Mai" });
  assert.ok(byName.length > 0);
  assert.ok((byName[0].studentName || byName[0].full_name).includes("Ngọc Mai"));
});

test("doctor cancel appointment requires reason and sets canceled_by DOCTOR", async () => {
  doctorAppointmentApi.resetMockData();

  // Empty reason should throw
  await assert.rejects(
    () => doctorAppointmentApi.cancelAppointment(1, ""),
    /Lý do hủy lịch hẹn không được để trống/
  );

  // Reason exceeding 500 chars should throw
  await assert.rejects(
    () => doctorAppointmentApi.cancelAppointment(1, "A".repeat(501)),
    /Lý do hủy lịch hẹn không được vượt quá 500 ký tự/
  );

  // Valid cancel
  const canceled = await doctorAppointmentApi.cancelAppointment(1, "Bác sĩ có ca cấp cứu đột xuất");
  assert.equal(canceled.status, "CANCELED");
  assert.equal(canceled.canceledBy || canceled.canceled_by, "DOCTOR");
  assert.equal(
    canceled.cancelRescheduleReason || canceled.cancel_reschedule_reason,
    "Bác sĩ có ca cấp cứu đột xuất"
  );
});

test("doctor reschedule appointment requires shiftId and reason, updates shift details", async () => {
  doctorAppointmentApi.resetMockData();

  // Missing shiftId should throw
  await assert.rejects(
    () => doctorAppointmentApi.rescheduleAppointment(2, null, "Lý do dời"),
    /Vui lòng chọn ca khám mới/
  );

  // Missing reason should throw
  await assert.rejects(
    () => doctorAppointmentApi.rescheduleAppointment(2, 301, "   "),
    /Lý do dời lịch hẹn không được để trống/
  );

  // Valid reschedule
  const rescheduled = await doctorAppointmentApi.rescheduleAppointment(
    2,
    301,
    "Bác sĩ đổi ca trực sáng"
  );
  assert.equal(rescheduled.shiftId || rescheduled.shift_id, 301);
  assert.equal(rescheduled.status, "CONFIRMED");
  assert.ok(
    (rescheduled.cancelRescheduleReason || rescheduled.cancel_reschedule_reason).includes("Bác sĩ đổi ca trực sáng")
  );
});

test("available shifts for SlotPicker returns only open shifts with available slots", async () => {
  const shifts = await doctorAppointmentApi.getAvailableShifts();
  assert.ok(shifts.length > 0);

  for (const s of shifts) {
    assert.equal(s.status, "OPEN");
    const booked = Number(s.bookedSlots ?? s.booked_slots ?? 0);
    const max = Number(s.maxSlots ?? s.max_slots ?? 0);
    assert.ok(booked < max, `booked (${booked}) must be less than max (${max})`);
  }
});
