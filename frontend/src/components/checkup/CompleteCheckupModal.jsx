import { AlertTriangle, Lock, X, CheckCircle2, User, Pill } from "lucide-react";

/**
 * Modal Xác nhận Hoàn tất ca khám & Khóa Chỉ đọc vĩnh viễn (FE2).
 *
 * @param {Object} props
 * @param {boolean} props.isOpen
 * @param {Function} props.onClose
 * @param {Function} props.onConfirm
 * @param {boolean} props.submitting
 * @param {Object} [props.checkupInfo]
 */
export default function CompleteCheckupModal({
  isOpen,
  onClose,
  onConfirm,
  submitting = false,
  checkupInfo = {},
}) {
  if (!isOpen) return null;

  const { studentName, studentCode, medicinesCount = 0 } = checkupInfo;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-fadeIn"
      role="dialog"
      aria-modal="true"
      aria-labelledby="complete-modal-title"
    >
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200/80 max-w-md w-full overflow-hidden transform transition-all">
        {/* HEADER */}
        <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-amber-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center shrink-0">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h3 id="complete-modal-title" className="text-sm font-bold text-slate-800">
                Xác nhận Hoàn tất Ca khám
              </h3>
              <p className="text-[11px] text-slate-500">
                Khóa cứng phiếu khám & đơn thuốc vĩnh viễn
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
        <div className="p-5 space-y-4 text-xs text-slate-600">
          {/* CẢNH BÁO */}
          <div className="p-3.5 rounded-xl bg-amber-50/80 border border-amber-200/80 flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <p className="font-bold text-amber-900">Lưu ý trước khi hoàn tất:</p>
              <p className="text-amber-800 leading-relaxed text-[11px]">
                Sau khi hoàn tất, phiếu khám và đơn thuốc sẽ không thể chỉnh sửa hoặc xóa.
              </p>
            </div>
          </div>

          {/* TÓM TẮT CA KHÁM */}
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-1.5">
                <User className="w-3.5 h-3.5" />
                Sinh viên:
              </span>
              <span className="font-bold text-slate-800">
                {studentName || "Sinh viên"}{" "}
                {studentCode ? `(${studentCode})` : ""}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-1.5">
                <Pill className="w-3.5 h-3.5" />
                Đơn thuốc:
              </span>
              <span className="font-semibold text-slate-700">
                {medicinesCount > 0 ? (
                  <span className="text-emerald-700 font-bold">
                    {medicinesCount} loại thuốc kê đơn
                  </span>
                ) : (
                  <span className="text-slate-500 italic">Không kê thuốc</span>
                )}
              </span>
            </div>
          </div>

          <p className="text-[11px] text-slate-500 italic text-center">
            Bạn có chắc chắn muốn hoàn tất và khóa hồ sơ ca khám này không?
          </p>
        </div>

        {/* FOOTER CÁC NÚT BẤM */}
        <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-end gap-2.5">
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-lg transition-colors disabled:opacity-50"
          >
            Hủy bỏ
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={submitting}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:opacity-50 rounded-lg transition-colors shadow-xs"
          >
            {submitting ? (
              <>
                <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                <span>Đang xử lý & khóa...</span>
              </>
            ) : (
              <>
                <CheckCircle2 className="w-4 h-4" />
                <span>Xác nhận hoàn tất</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
