const DAY = 86400000;
const BHYT_CARD_PATTERN = /^(?:\d{10}|[A-Z]{2}\d{13})$/;

function calendarDay(value) {
  if (typeof value !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return NaN;
  const date = new Date(`${value}T00:00:00Z`);
  return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === value ? date.getTime() / DAY : NaN;
}

export function getInsuranceStatus(validFrom, validUntil, now = new Date()) {
  const today = new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Ho_Chi_Minh", year: "numeric", month: "2-digit", day: "2-digit" }).format(now);
  const start = calendarDay(validFrom);
  const end = calendarDay(validUntil);
  const current = calendarDay(today);
  const daysRemaining = end - current;
  if (![start, end, current].every(Number.isFinite) || start > end) return { state: "unknown", label: "Chưa có thông tin hiệu lực" };
  if (current < start) return { state: "pending", label: "Chưa đến hạn hiệu lực", daysRemaining };
  if (daysRemaining < 0) return { state: "expired", label: "Đã hết hạn - Cần gia hạn", daysRemaining };
  if (daysRemaining < 30) return { state: "expiring", label: "Sắp hết hạn - Cần gia hạn", daysRemaining };
  return { state: "active", label: "Đang hiệu lực", daysRemaining };
}

export function formatInsuranceDate(value) {
  return Number.isFinite(calendarDay(value)) ? value.split("-").reverse().join("/") : "Chưa cập nhật";
}

export function fromInsuranceResponse(rawData, fallback = {}) {
  const data = Array.isArray(rawData) ? rawData[0] : rawData;
  return {
    id: data?.id ?? fallback?.id ?? null,
    studentId: data?.studentId ?? fallback?.studentId ?? null,
    cardNumber: data?.cardNumber ?? fallback?.cardNumber ?? "",
    socialInsuranceNumber: data?.socialInsuranceNumber ?? fallback?.socialInsuranceNumber ?? "",
    validFrom: data?.effectiveDate ?? data?.issueDate ?? data?.validFrom ?? fallback?.validFrom ?? "",
    validUntil: data?.expirationDate ?? data?.validUntil ?? fallback?.validUntil ?? "",
    initialProvider: data?.initialHospital ?? data?.registeredHospital ?? data?.initialProvider ?? fallback?.initialProvider ?? "",
    issuedAt: data?.issueAt ?? data?.issuedAt ?? fallback?.issuedAt ?? "",
    benefits: data?.benefits ?? fallback?.benefits ?? "",
  };
}

export function toInsuranceRequest(draft, studentId) {
  const cardNumber = (draft.cardNumber ?? "").trim().toUpperCase();
  const effectiveDate = (draft.validFrom ?? draft.effectiveDate ?? "").trim();
  const expirationDate = (draft.validUntil ?? draft.expirationDate ?? "").trim();
  const initialHospital = (draft.initialProvider ?? draft.initialHospital ?? "").trim();
  const parsedStudentId = Number(studentId ?? draft.studentId);
  if (!Number.isSafeInteger(parsedStudentId) || parsedStudentId <= 0) {
    throw new Error("Không xác định được sinh viên. Vui lòng tải lại trang hoặc đăng nhập lại.");
  }

  return {
    cardNumber,
    studentId: parsedStudentId,
    effectiveDate,
    expirationDate,
    initialHospital,
    issueAt: (draft.issuedAt ?? "").trim(),
  };
}

export function validateInsuranceDraft(draft) {
  const cardNumber = (draft.cardNumber ?? "").trim().toUpperCase();
  if (!BHYT_CARD_PATTERN.test(cardNumber)) {
    return "Mã thẻ BHYT không đúng định dạng chuẩn Việt Nam (gồm 10 chữ số hoặc 15 ký tự: 2 chữ cái đầu và 13 chữ số, VD: SV4790123456789).";
  }
  const effectiveDate = (draft.validFrom ?? draft.effectiveDate ?? "").trim();
  const expirationDate = (draft.validUntil ?? draft.expirationDate ?? "").trim();
  const start = calendarDay(effectiveDate);
  const end = calendarDay(expirationDate);
  if (!Number.isFinite(start) || !Number.isFinite(end)) {
    return "Vui lòng nhập đầy đủ ngày bắt đầu hiệu lực và ngày hết hạn hợp lệ.";
  }
  if (start > end) {
    return "Ngày bắt đầu hiệu lực phải trước hoặc bằng ngày hết hạn.";
  }
  const initialHospital = (draft.initialProvider ?? draft.initialHospital ?? "").trim();
  if (!initialHospital) {
    return "Nơi đăng ký khám chữa bệnh ban đầu không được để trống.";
  }
  if (initialHospital.length > 150) {
    return "Nơi đăng ký khám chữa bệnh ban đầu không được vượt quá 150 ký tự.";
  }
  return "";
}
