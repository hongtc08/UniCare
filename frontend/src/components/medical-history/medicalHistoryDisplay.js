const dateFormat = new Intl.DateTimeFormat("vi-VN", {
  dateStyle: "short",
  timeZone: "Asia/Ho_Chi_Minh",
});
const dateTimeFormat = new Intl.DateTimeFormat("vi-VN", {
  dateStyle: "short",
  timeStyle: "short",
  timeZone: "Asia/Ho_Chi_Minh",
});

export function medicalDate(value, withTime = false) {
  if (!value) return "Chưa ghi nhận";
  const date = new Date(value);
  if (!Number.isFinite(date.getTime())) return "Chưa ghi nhận";
  return (withTime ? dateTimeFormat : dateFormat).format(date);
}

export function checkupDate(record) {
  return medicalDate(record.shiftDate || record.createdAt || record.completedAt);
}

export function checkupDateKey(record) {
  if (record.shiftDate) return record.shiftDate;
  const value = record.createdAt || record.completedAt;
  const date = new Date(value);
  if (!value || !Number.isFinite(date.getTime())) return "";
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Ho_Chi_Minh", year: "numeric", month: "2-digit", day: "2-digit",
  }).format(date);
}

export function checkupType(record) {
  if (record.isPeriodic || record.timelineCategory === "PERIODIC") return "Khám định kỳ";
  if (record.timelineCategory === "PSYCHOLOGICAL") return "Tư vấn tâm lý";
  return "Khám đa khoa";
}

export const checkupTypes = ["Khám đa khoa", "Khám định kỳ", "Tư vấn tâm lý"];

const periodicLabels = { PASSED: "Đạt", DAT: "Đạt", FAILED: "Không đạt", KHONG_DAT: "Không đạt" };

export function periodicResult(record) {
  return periodicLabels[record.periodicResult?.trim().toUpperCase()] || "Chưa ghi nhận";
}
