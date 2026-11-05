// Isolated browser QA harness. Not imported by the production app or included in its build.
// No requests reach the real backend; all mutations affect this in-memory test dataset only.
import { createRoot } from "react-dom/client";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "../../src/context/AuthContext";
import MainLayout from "../../src/components/layout/MainLayout";
import StudentBooking from "../../src/pages/appointments/StudentBooking";
import MyAppointments from "../../src/pages/appointments/MyAppointments";
import axiosClient from "../../src/api/axiosClient";
import { todayInVietnam } from "../../src/utils/appointments";
import "../../src/global.css";
import "../../src/App.css";

const date = todayInVietnam(new Date(Date.now() + 2 * 86400000));
const scenario = new URLSearchParams(window.location.search).get("scenario");
const user = { username: "QA_STUDENT", fullName: "Sinh viên kiểm thử", role: "SINH_VIEN", userId: 9001 };
// A fake token is used solely to render the real shell. The adapter below intercepts every request.
localStorage.setItem("user", JSON.stringify(user));
localStorage.setItem("token", `test.${btoa(JSON.stringify({ sub: user.username, role: user.role, userId: user.userId, exp: Math.floor(Date.now() / 1000) + 86400 }))}.test`);
const shifts = [
  { id: 11, doctorId: 81, doctorName: "BS. Nguyễn Minh Anh", shiftDate: date, startTime: "09:00:00", endTime: "09:30:00", room: "Phòng khám 101", serviceType: "KHAM_DA_KHOA", maxSlots: 10, bookedSlots: 4, status: "OPEN" },
  { id: 12, doctorId: 81, doctorName: "BS. Nguyễn Minh Anh", shiftDate: date, startTime: "09:30:00", endTime: "10:00:00", room: "Phòng khám 101", serviceType: "KHAM_DA_KHOA", maxSlots: 10, bookedSlots: 10, status: "FULL" },
  { id: 13, doctorId: 81, doctorName: "BS. Nguyễn Minh Anh", shiftDate: date, startTime: "10:30:00", endTime: "11:00:00", room: "Phòng khám 101", serviceType: "KHAM_DA_KHOA", maxSlots: 10, bookedSlots: 1, status: "OPEN" },
  { id: 14, doctorId: 82, doctorName: "BS. Trần Quốc Huy", shiftDate: date, startTime: "14:00:00", endTime: "14:30:00", room: "Phòng khám 102", serviceType: "KHAM_DA_KHOA", maxSlots: 8, bookedSlots: 2, status: "OPEN" },
];
shifts.forEach((shift) => { shift.facilityId = 1; });
let appointments = [{ id: 1, appointmentCode: "QA-LH-001", shiftId: 14, shiftDate: date, startTime: "14:00:00", endTime: "14:30:00", doctorName: "BS. Trần Quốc Huy", shiftType: "KHAM_DA_KHOA", room: "Phòng khám 102", status: "CONFIRMED", reasonSymptoms: "Dữ liệu kiểm thử giao diện" }];
axiosClient.defaults.adapter = async (config) => {
  await new Promise((resolve) => setTimeout(resolve, 150));
  if (scenario === "error") throw new Error("Lỗi kết nối trong bộ kiểm thử. Thử lại để kiểm tra trạng thái lỗi.");
  if (scenario === "empty" && config.method === "get") return { data: { success: true, data: [] }, status: 200, statusText: "OK", headers: {}, config };
  let data;
  if (config.url === "/facilities") data = [{ id: 1, name: "Trung tâm Y tế Cơ sở Chính" }];
  else if (config.url === "/shifts") data = shifts.filter((shift) => (!config.params.facilityId || String(shift.facilityId) === String(config.params.facilityId)) && (!config.params.date || shift.shiftDate === config.params.date) && (!config.params.serviceType || shift.serviceType === config.params.serviceType) && (!config.params.fromDate || shift.shiftDate >= config.params.fromDate) && (!config.params.toDate || shift.shiftDate <= config.params.toDate));
  else if (config.url === "/appointments/my-appointments") data = appointments;
  else if (config.method === "post" && config.url === "/appointments") {
    const body = JSON.parse(config.data);
    const slot = shifts.find((shift) => shift.id === body.shiftId);
    slot.bookedSlots += 1;
    data = { ...slot, id: appointments.length + 1, shiftId: slot.id, appointmentCode: `QA-LH-00${appointments.length + 1}`, shiftType: slot.serviceType, reasonSymptoms: body.reasonSymptoms, status: "CONFIRMED" };
    appointments = [...appointments, data];
  } else if (config.method === "put") {
    const id = Number(config.url.split("/")[2]);
    const body = JSON.parse(config.data);
    const original = appointments.find((item) => item.id === id);
    data = config.url.endsWith("/cancel") ? { ...original, status: "CANCELED", cancelRescheduleReason: body.reason } : { ...original, ...shifts.find((shift) => shift.id === body.newShiftId), id, shiftId: body.newShiftId, status: "CONFIRMED", cancelRescheduleReason: body.reason };
    appointments = appointments.map((item) => item.id === id ? data : item);
  } else throw new Error(`Unexpected QA request: ${config.method} ${config.url}`);
  return { data: { success: true, data }, status: 200, statusText: "OK", headers: {}, config };
};

createRoot(document.getElementById("root")).render(<AuthProvider><MemoryRouter initialEntries={["/lich-kham"]}><Routes><Route element={<MainLayout />}><Route path="/lich-kham" element={<StudentBooking />} /><Route path="/lich-hen-cua-toi" element={<MyAppointments />} /></Route></Routes></MemoryRouter></AuthProvider>);
