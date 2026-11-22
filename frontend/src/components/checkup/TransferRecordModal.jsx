import { useState, useEffect } from "react";
import {
  Ambulance,
  AlertTriangle,
  X,
  Building2,
  Clock,
  Lock,
  Send,
} from "lucide-react";
import { TRANSFER_REASON_CATEGORIES } from "../../api/medicalRecordApi";
import { validateTransferForm } from "../../utils/checkupValidation";

const REASON_OPTIONS = [
  { value: TRANSFER_REASON_CATEGORIES.QUA_TAI, label: "Quá tải" },
  { value: TRANSFER_REASON_CATEGORIES.THIEU_THIET_BI, label: "Thiếu thiết bị / thuốc chuyên khoa" },
  { value: TRANSFER_REASON_CATEGORIES.CAP_CUU_NANG, label: "Cấp cứu nặng vượt khả năng chuyên môn" },
];

function toLocalDateTimeInputValue(date = new Date()) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`;
}

/**
 * Modal Lập Hồ sơ Chuyển viện (BE3 - POST /api/medical-records/{id}/transfer).
 */
export default function TransferRecordModal({
  isOpen,
  onClose,
  onSubmit,
  submitting = false,
  apiError = null,
  initialValues = {},
  studentInfo = {},
}) {
  const [form, setForm] = useState({
    targetHospital: "",
    reasonCategory: TRANSFER_REASON_CATEGORIES.CAP_CUU_NANG,
    transferReason: "",
    currentCondition: "",
    firstAidNotes: "",
    refusalNotes: "",
    transferredAt: toLocalDateTimeInputValue(),
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      setForm({
        targetHospital: "",
        reasonCategory: TRANSFER_REASON_CATEGORIES.CAP_CUU_NANG,
        transferReason: "",
        currentCondition: initialValues.currentCondition || "",
        firstAidNotes: initialValues.firstAidNotes || "",
        refusalNotes: "",
        transferredAt: toLocalDateTimeInputValue(),
      });
      setErrors({});
    }
  }, [isOpen, initialValues.currentCondition, initialValues.firstAidNotes]);

  if (!isOpen) return null;

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => {
        const next = { ...prev };
        delete next[field];
        return next;
      });
    }
  };

  const handleFormSubmit = (e) => {
    e.preventDefault();
    if (submitting) return;

    const validation = validateTransferForm(form);
    if (!validation.isValid) {
      setErrors(validation.errors);
      return;
    }

    const transferredAtIso = form.transferredAt
      ? new Date(form.transferredAt).toISOString()
      : new Date().toISOString();

    onSubmit({
      targetHospital: form.targetHospital.trim(),
      reasonCategory: form.reasonCategory,
      transferReason: form.transferReason.trim(),
      currentCondition: form.currentCondition.trim(),
      firstAidNotes: form.firstAidNotes.trim(),
      refusalNotes: form.refusalNotes?.trim() || null,
      transferredAt: transferredAtIso,
    });
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="transfer-modal-title"
    >
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200/80 max-w-2xl w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* HEADER */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-red-50/50 shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-red-100 text-red-600 flex items-center justify-center shrink-0">
              <Ambulance className="w-5 h-5" />
            </div>
            <div>
              <h3 id="transfer-modal-title" className="text-sm font-bold text-slate-800">
                Lập Hồ sơ Chuyển viện Tuyến trên
              </h3>
              <p className="text-[11px] text-slate-500">
                {studentInfo.studentName
                  ? `Sinh viên: ${studentInfo.studentName}${
                      studentInfo.studentCode ? ` (${studentInfo.studentCode})` : ""
                    }`
                  : "Ghi nhận thông tin chuyển viện và xuất giấy giới thiệu"}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors disabled:opacity-50"
            title="Đóng"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* BODY */}
        <form onSubmit={handleFormSubmit} className="flex flex-col flex-1 overflow-hidden">
          <div className="p-5 space-y-4 overflow-y-auto flex-1">
            {/* CẢNH BÁO NGHIỆP VỤ */}
            <div className="p-3.5 rounded-xl bg-amber-50/90 border border-amber-200 flex items-start gap-3">
              <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
              <div className="space-y-0.5 text-xs">
                <p className="font-bold text-amber-900 flex items-center gap-1.5">
                  <Lock className="w-3.5 h-3.5" />
                  Lưu ý quan trọng khi Xác nhận chuyển viện:
                </p>
                <p className="text-amber-800 leading-relaxed text-[11px]">
                  Sau khi bấm <span className="font-bold">Xác nhận chuyển viện</span>, hồ sơ chuyển
                  viện sẽ được <span className="font-bold underline">khóa chỉ đọc (Read-only)</span>{" "}
                  và hệ thống sẽ <span className="font-bold">tự động kết thúc ca khám tại trường</span>.
                  Bạn sẽ không thể chỉnh sửa lại hồ sơ sau khi xác nhận.
                </p>
              </div>
            </div>

            {/* LỖI TỪ BE NẾU CÓ */}
            {apiError && (
              <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-xs text-red-700 font-medium">
                {apiError}
              </div>
            )}

            {/* HÀNG 1: BỆNH VIỆN TUYẾN TRÊN & PHÂN LOẠI LÝ DO */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label
                  htmlFor="targetHospital"
                  className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
                >
                  Bệnh viện tuyến trên <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Building2 className="w-4 h-4 text-slate-400 absolute left-3 top-2.5 pointer-events-none" />
                  <input
                    id="targetHospital"
                    type="text"
                    maxLength={150}
                    value={form.targetHospital}
                    onChange={(e) => handleChange("targetHospital", e.target.value)}
                    disabled={submitting}
                    placeholder="VD: Bệnh viện Bạch Mai, BV Đại học Y..."
                    className={`w-full pl-9 pr-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
                      errors.targetHospital
                        ? "border-red-400 bg-red-50/30 text-red-900"
                        : "border-slate-300 bg-white text-slate-800 focus:border-blue-500"
                    }`}
                  />
                </div>
                {errors.targetHospital && (
                  <p className="mt-1 text-xs text-red-600 font-medium">{errors.targetHospital}</p>
                )}
              </div>

              <div>
                <label
                  htmlFor="reasonCategory"
                  className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
                >
                  Phân loại lý do <span className="text-red-500">*</span>
                </label>
                <select
                  id="reasonCategory"
                  value={form.reasonCategory}
                  onChange={(e) => handleChange("reasonCategory", e.target.value)}
                  disabled={submitting}
                  className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
                    errors.reasonCategory
                      ? "border-red-400 bg-red-50/30 text-red-900"
                      : "border-slate-300 bg-white text-slate-800 focus:border-blue-500"
                  }`}
                >
                  {REASON_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
                {errors.reasonCategory && (
                  <p className="mt-1 text-xs text-red-600 font-medium">{errors.reasonCategory}</p>
                )}
              </div>
            </div>

            {/* THỜI GIAN CHUYỂN */}
            <div>
              <label
                htmlFor="transferredAt"
                className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
              >
                Thời gian chuyển viện
              </label>
              <div className="relative max-w-xs">
                <Clock className="w-4 h-4 text-slate-400 absolute left-3 top-2.5 pointer-events-none" />
                <input
                  id="transferredAt"
                  type="datetime-local"
                  value={form.transferredAt}
                  onChange={(e) => handleChange("transferredAt", e.target.value)}
                  disabled={submitting}
                  className="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-slate-300 bg-white text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                />
              </div>
            </div>

            {/* LÝ DO CHI TIẾT */}
            <div>
              <label
                htmlFor="transferReason"
                className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
              >
                Lý do chuyển viện chi tiết <span className="text-red-500">*</span>
              </label>
              <textarea
                id="transferReason"
                rows={2}
                value={form.transferReason}
                onChange={(e) => handleChange("transferReason", e.target.value)}
                disabled={submitting}
                placeholder="Mô tả rõ lý do chuyên môn cần chuyển tuyến trên..."
                className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
                  errors.transferReason
                    ? "border-red-400 bg-red-50/30 text-red-900"
                    : "border-slate-300 bg-white text-slate-800 focus:border-blue-500"
                }`}
              />
              {errors.transferReason && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.transferReason}</p>
              )}
            </div>

            {/* TÌNH TRẠNG HIỆN TẠI */}
            <div>
              <label
                htmlFor="currentCondition"
                className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
              >
                Tình trạng hiện tại của sinh viên <span className="text-red-500">*</span>
              </label>
              <textarea
                id="currentCondition"
                rows={2}
                value={form.currentCondition}
                onChange={(e) => handleChange("currentCondition", e.target.value)}
                disabled={submitting}
                placeholder="Ý thức, dấu hiệu sinh tồn, triệu chứng hiện tại lúc chuyển viện..."
                className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
                  errors.currentCondition
                    ? "border-red-400 bg-red-50/30 text-red-900"
                    : "border-slate-300 bg-white text-slate-800 focus:border-blue-500"
                }`}
              />
              {errors.currentCondition && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.currentCondition}</p>
              )}
            </div>

            {/* BIỆN PHÁP SƠ CỨU */}
            <div>
              <label
                htmlFor="firstAidNotes"
                className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
              >
                Biện pháp sơ cứu đã thực hiện <span className="text-red-500">*</span>
              </label>
              <textarea
                id="firstAidNotes"
                rows={2}
                value={form.firstAidNotes}
                onChange={(e) => handleChange("firstAidNotes", e.target.value)}
                disabled={submitting}
                placeholder="Các biện pháp sơ cứu, thuốc đã dùng tại trạm y tế trước khi chuyển..."
                className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
                  errors.firstAidNotes
                    ? "border-red-400 bg-red-50/30 text-red-900"
                    : "border-slate-300 bg-white text-slate-800 focus:border-blue-500"
                }`}
              />
              {errors.firstAidNotes && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.firstAidNotes}</p>
              )}
            </div>

            {/* GHI CHÚ TỪ CHỐI (TÙY CHỌN) */}
            <div>
              <label
                htmlFor="refusalNotes"
                className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
              >
                Ghi chú từ chối (nếu có)
              </label>
              <textarea
                id="refusalNotes"
                rows={2}
                value={form.refusalNotes}
                onChange={(e) => handleChange("refusalNotes", e.target.value)}
                disabled={submitting}
                placeholder="Ghi nhận nếu sinh viên / người nhà từ chối chuyển viện hoặc yêu cầu đặc biệt..."
                className="w-full px-3 py-2 text-sm rounded-lg border border-slate-300 bg-white text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              />
            </div>
          </div>

          {/* FOOTER */}
          <div className="px-5 py-3.5 bg-slate-50 border-t border-slate-100 flex items-center justify-end gap-2.5 shrink-0">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-lg transition-colors disabled:opacity-50"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-red-600 hover:bg-red-700 active:bg-red-800 disabled:opacity-50 rounded-lg transition-colors shadow-xs"
            >
              {submitting ? (
                <>
                  <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Đang xác nhận...</span>
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" />
                  <span>Xác nhận chuyển viện</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
