export const SERVICES = [
  { value: "KHAM_DA_KHOA", label: "Khám đa khoa", description: "Khám sức khỏe thể chất, triệu chứng và bệnh thông thường." },
  { value: "TU_VAN_TAM_LY", label: "Tư vấn tâm lý", description: "Trao đổi với chuyên gia về sức khỏe tinh thần." },
];

export const APPOINTMENT_STATUSES = {
  CONFIRMED: "Đã xác nhận", COMPLETED: "Đã hoàn tất", CANCELED: "Đã hủy", NO_SHOW: "Vắng mặt",
};

export function normalizeStatus(status) {
  return ({ DA_XAC_NHAN: "CONFIRMED", DA_HOAN_TAT: "COMPLETED", HOAN_TAT: "COMPLETED", DA_HUY: "CANCELED", CANCELLED: "CANCELED", VANG_MAT: "NO_SHOW" })[status] ?? status;
}

export function todayInVietnam(now = new Date()) {
  return new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Ho_Chi_Minh", year: "numeric", month: "2-digit", day: "2-digit" }).format(now);
}

export function formatDay(value, options = {}) {
  return value ? new Intl.DateTimeFormat("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", ...options }).format(new Date(`${value}T12:00:00+07:00`)) : "Chưa chọn ngày";
}

export const serviceLabel = (value) => SERVICES.find((item) => item.value === value)?.label ?? value ?? "Chưa chọn dịch vụ";
export const timeLabel = (value) => value?.slice(0, 5) ?? "—";

export function monthRange(month) {
  const [year, number] = month.split("-").map(Number);
  return { fromDate: `${month}-01`, toDate: `${month}-${new Date(year, number, 0).getDate()}` };
}

export function moveMonth(month, offset) {
  const [year, number] = month.split("-").map(Number);
  const date = new Date(year, number - 1 + offset, 1, 12);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

export function normalizeShift(shift) {
  const maxSlots = Number(shift.maxSlots ?? shift.maxQuantity ?? 0);
  const bookedSlots = Number(shift.bookedSlots ?? shift.bookedQuantity ?? 0);
  return {
    ...shift, doctorId: shift.doctorId ?? shift.staffId,
    serviceType: shift.serviceType ?? shift.shiftType,
    maxSlots, bookedSlots,
    availableSlots: Number(shift.availableSlots ?? Math.max(0, maxSlots - bookedSlots)),
  };
}

export function overlapsAppointment(shift, appointments, excludedId) {
  return appointments.some((appointment) =>
    String(appointment.id) !== String(excludedId) &&
    normalizeStatus(appointment.status) === "CONFIRMED" &&
    appointment.shiftDate === shift.shiftDate &&
    shift.startTime < appointment.endTime && shift.endTime > appointment.startTime,
  );
}

export function slotUnavailableReason(shift, appointments = [], { excludedAppointmentId, excludedShiftId, now = new Date() } = {}) {
  if (String(shift.id) === String(excludedShiftId)) return "Ca hiện tại";
  if (shift.status === "ENDED" || shift.status === "DA_HUY") return "Đã đóng";
  const startsAt = Date.parse(`${shift.shiftDate}T${shift.startTime}+07:00`);
  if (!Number.isFinite(startsAt) || startsAt <= now.getTime()) return "Đã qua giờ";
  if (shift.status === "FULL" || shift.status === "DA_DAY" || shift.availableSlots <= 0 || shift.bookedSlots >= shift.maxSlots) return "Đã kín lịch";
  if (overlapsAppointment(shift, appointments, excludedAppointmentId)) return "Trùng lịch";
  return "";
}

export function filterBookingShifts(shifts, { facilityId = "", onlyAvailable = false } = {}, appointments = [], now = new Date()) {
  return shifts.filter((slot) => (!facilityId || String(slot.facilityId) === String(facilityId)) && (!onlyAvailable || !slotUnavailableReason(slot, appointments, { now })));
}

export function canChangeAppointment(appointment, now = new Date()) {
  return normalizeStatus(appointment.status) === "CONFIRMED" && Date.parse(`${appointment.shiftDate}T${appointment.startTime}+07:00`) > now.getTime();
}

export function appointmentError(error, fallback = "Không tải được dữ liệu. Vui lòng thử lại.") {
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.code === "ECONNABORTED") return "Kết nối quá thời gian chờ. Vui lòng thử lại.";
  if (error?.request && !error?.response) return "Không kết nối được máy chủ. Kiểm tra kết nối và thử lại.";
  return error?.message || fallback;
}

export function requiredReason(value) {
  if (!value.trim()) return "Vui lòng nhập lý do trước khi xác nhận.";
  if (value.trim().length > 500) return "Lý do không được vượt quá 500 ký tự.";
  return "";
}
