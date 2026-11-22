import { useState, useEffect, useCallback, useRef } from "react";
import { Printer, X, AlertCircle, RefreshCw, FileText } from "lucide-react";
import { medicalRecordApi } from "../../api/medicalRecordApi";

function formatDate(dateStr) {
  if (!dateStr) return "—";
  const d = new Date(dateStr);
  return Number.isNaN(d.getTime()) ? dateStr : d.toLocaleDateString("vi-VN");
}

function formatDateTime(dateStr) {
  if (!dateStr) return "—";
  const d = new Date(dateStr);
  return Number.isNaN(d.getTime()) ? dateStr : d.toLocaleString("vi-VN");
}

/**
 * Modal Xem trước & In Giấy giới thiệu chuyển viện (BE3 - GET /api/medical-records/{id}/transfer/print).
 */
export default function TransferReferralPrintModal({ isOpen, onClose, checkupId }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const originalTitleRef = useRef(null);

  const restoreDocumentTitle = useCallback(() => {
    if (originalTitleRef.current !== null) {
      document.title = originalTitleRef.current;
      originalTitleRef.current = null;
    }
  }, []);

  useEffect(() => {
    if (!isOpen) {
      restoreDocumentTitle();
    }
    return () => {
      restoreDocumentTitle();
    };
  }, [isOpen, restoreDocumentTitle]);

  const fetchPrintData = useCallback(async () => {
    if (!checkupId) return;
    try {
      setLoading(true);
      setError(null);
      const res = await medicalRecordApi.getTransferReferralPrint(checkupId);
      setData(res);
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err?.message ||
          "Không thể tải dữ liệu Giấy giới thiệu chuyển viện."
      );
    } finally {
      setLoading(false);
    }
  }, [checkupId]);

  useEffect(() => {
    if (isOpen && checkupId) {
      fetchPrintData();
    }
  }, [isOpen, checkupId, fetchPrintData]);

  if (!isOpen) return null;

  const handlePrint = () => {
    if (originalTitleRef.current === null) {
      originalTitleRef.current = document.title;
    }

    const recordCode = (data?.recordCode || "").trim();
    const studentCode = (data?.studentCode || "").trim();
    const titleParts = ["Giay_Chuyen_Vien", recordCode, studentCode].filter(Boolean);
    document.title = titleParts.join("_");

    const handleAfterPrint = () => {
      restoreDocumentTitle();
      window.removeEventListener("afterprint", handleAfterPrint);
    };

    window.addEventListener("afterprint", handleAfterPrint);
    window.print();
  };

  const transferDate = data?.transferredAt ? new Date(data.transferredAt) : new Date();

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/50 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="transfer-print-modal-title"
    >
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200/80 max-w-4xl w-full max-h-[92vh] flex flex-col overflow-hidden">
        {/* HEADER MODAL (ẨN KHI IN) */}
        <div className="px-5 py-3.5 border-b border-slate-200 bg-slate-50 flex items-center justify-between gap-3 shrink-0 no-print">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center shrink-0">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <h3 id="transfer-print-modal-title" className="text-sm font-bold text-slate-800">
                Giấy Giới Thiệu Chuyển Viện
              </h3>
              <p className="text-[11px] text-slate-500">
                {data?.recordCode
                  ? `Mã hồ sơ: ${data.recordCode} · Chỉ đọc`
                  : "Xem trước và in mẫu giấy chuyển viện chuẩn hành chính"}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {data && !loading && (
              <button
                type="button"
                onClick={handlePrint}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors shadow-xs"
              >
                <Printer className="w-4 h-4" />
                <span>In giấy chuyển viện</span>
              </button>
            )}
            <button
              type="button"
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 rounded-lg transition-colors"
              title="Đóng"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* BODY MODAL */}
        <div className="p-4 sm:p-6 overflow-y-auto flex-1 bg-slate-100/70">
          {loading ? (
            <div className="py-20 flex flex-col items-center justify-center gap-3 text-slate-500 text-sm">
              <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
              <span>Đang tải dữ liệu giấy giới thiệu chuyển viện...</span>
            </div>
          ) : error ? (
            <div className="p-5 bg-red-50 border border-red-200 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 text-red-700 text-sm">
              <div className="flex items-start gap-3">
                <AlertCircle className="w-5 h-5 shrink-0 mt-0.5 text-red-600" />
                <div>
                  <p className="font-bold">Không thể tải bản in</p>
                  <p className="text-xs mt-0.5">{error}</p>
                </div>
              </div>
              <button
                type="button"
                onClick={fetchPrintData}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-white border border-red-300 text-red-700 rounded-lg hover:bg-red-50 shrink-0"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                Thử lại
              </button>
            </div>
          ) : data ? (
            /* TỜ GIẤY GIỚI THIỆU CHUYỂN VIỆN (VÙNG IN) */
            <div
              id="transfer-referral-print-sheet"
              className="transfer-print-area bg-white border border-slate-200 shadow-sm rounded-xl p-5 sm:p-8 text-slate-900 space-y-5 text-xs sm:text-[13px] leading-relaxed"
            >
              {/* QUỐC HIỆU & ĐƠN VỊ GIỚI THIỆU */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-4 border-b border-slate-300">
                <div className="text-center sm:text-left">
                  <p className="font-bold uppercase text-slate-800">
                    {data.referringUnitName || "TRẠM Y TẾ TRƯỜNG ĐẠI HỌC - UNICARE"}
                  </p>
                  <p className="text-[11px] text-slate-600 mt-0.5">
                    Mã hồ sơ chuyển viện: <span className="font-bold">{data.recordCode || "—"}</span>
                  </p>
                  <p className="text-[11px] text-slate-600">
                    Mã phiếu khám: <span className="font-semibold">{data.checkupCode || "—"}</span>
                  </p>
                </div>
                <div className="text-center">
                  <p className="font-bold uppercase text-slate-900">
                    CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM
                  </p>
                  <p className="font-semibold text-slate-700 underline underline-offset-4">
                    Độc lập - Tự do - Hạnh phúc
                  </p>
                </div>
              </div>

              {/* TIÊU ĐỀ */}
              <div className="text-center space-y-1">
                <h1 className="text-lg sm:text-xl font-bold uppercase tracking-wide text-slate-900">
                  GIẤY GIỚI THIỆU CHUYỂN VIỆN
                </h1>
                <p className="text-xs text-slate-600">
                  Kính gửi:{" "}
                  <span className="font-bold text-slate-900 uppercase">
                    {data.targetHospital || "—"}
                  </span>
                </p>
              </div>

              {/* I. THÔNG TIN SINH VIÊN */}
              <section className="space-y-2">
                <h2 className="text-xs font-bold uppercase text-slate-800 border-b border-slate-200 pb-1">
                  I. Thông tin hành chính Sinh viên
                </h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1.5">
                  <p>
                    <span className="text-slate-500">Họ và tên:</span>{" "}
                    <span className="font-bold uppercase">{data.studentName || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">MSSV:</span>{" "}
                    <span className="font-semibold">{data.studentCode || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Ngày sinh:</span>{" "}
                    <span>{formatDate(data.dateOfBirth)}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Giới tính:</span>{" "}
                    <span>{data.gender || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Lớp / Khoa:</span>{" "}
                    <span>
                      {[data.className || data.classCode, data.facultyName]
                        .filter(Boolean)
                        .join(" - ") || "—"}
                    </span>
                  </p>
                  <p>
                    <span className="text-slate-500">Số điện thoại:</span>{" "}
                    <span>{data.studentPhone || "—"}</span>
                  </p>
                </div>
              </section>

              {/* II. THÔNG TIN BHYT */}
              <section className="space-y-2">
                <h2 className="text-xs font-bold uppercase text-slate-800 border-b border-slate-200 pb-1">
                  II. Thông tin Bảo hiểm Y tế (BHYT)
                </h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1.5">
                  <p>
                    <span className="text-slate-500">Số thẻ BHYT:</span>{" "}
                    <span className="font-semibold">{data.insuranceCardNumber || "Không có"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Nơi ĐK KCB ban đầu:</span>{" "}
                    <span>{data.insuranceInitialHospital || "—"}</span>
                  </p>
                  <p className="sm:col-span-2">
                    <span className="text-slate-500">Thời hạn thẻ:</span>{" "}
                    <span>
                      {data.insuranceEffectiveDate || data.insuranceExpirationDate
                        ? `${formatDate(data.insuranceEffectiveDate)} đến ${formatDate(
                            data.insuranceExpirationDate
                          )}`
                        : "—"}
                    </span>
                  </p>
                </div>
              </section>

              {/* III. SINH HIỆU & TIỀN SỬ */}
              <section className="space-y-2">
                <h2 className="text-xs font-bold uppercase text-slate-800 border-b border-slate-200 pb-1">
                  III. Chỉ số Sinh hiệu & Tiền sử Y tế
                </h2>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <p>
                    <span className="text-slate-500">Chiều cao:</span>{" "}
                    <span>{data.height != null ? `${data.height} cm` : "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Cân nặng:</span>{" "}
                    <span>{data.weight != null ? `${data.weight} kg` : "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">BMI:</span>{" "}
                    <span>{data.bmi != null ? data.bmi : "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Nhóm máu:</span>{" "}
                    <span>{data.bloodType || "—"}</span>
                  </p>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1.5">
                  <p>
                    <span className="text-slate-500">Tiền sử dị ứng:</span>{" "}
                    <span>{data.allergies || "Không ghi nhận"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Bệnh lý nền:</span>{" "}
                    <span>{data.underlyingDiseases || "Không ghi nhận"}</span>
                  </p>
                </div>
              </section>

              {/* IV. TÓM TẮT LÂM SÀNG & ĐƠN THUỐC */}
              <section className="space-y-2">
                <h2 className="text-xs font-bold uppercase text-slate-800 border-b border-slate-200 pb-1">
                  IV. Tóm tắt Lâm sàng & Xử trí tại trường
                </h2>
                <div className="space-y-1.5">
                  <p>
                    <span className="text-slate-500">Triệu chứng lâm sàng:</span>{" "}
                    <span>{data.symptoms || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Chẩn đoán ban đầu:</span>{" "}
                    <span className="font-semibold">{data.diagnosisNotes || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Hướng xử trí / Sơ cứu ban đầu:</span>{" "}
                    <span>{data.treatmentSuggestions || "—"}</span>
                  </p>
                </div>

                {Array.isArray(data.prescribedMedicines) && data.prescribedMedicines.length > 0 && (
                  <div className="pt-2 overflow-x-auto">
                    <p className="text-[11px] font-bold text-slate-700 mb-1">
                      Thuốc đã kê / sử dụng tại Trạm Y tế:
                    </p>
                    <table className="w-full border-collapse border border-slate-300 text-xs">
                      <thead>
                        <tr className="bg-slate-50">
                          <th className="border border-slate-300 px-2 py-1 text-left w-10">STT</th>
                          <th className="border border-slate-300 px-2 py-1 text-left">Tên thuốc</th>
                          <th className="border border-slate-300 px-2 py-1 text-center w-20">SL</th>
                          <th className="border border-slate-300 px-2 py-1 text-left">Liều dùng</th>
                        </tr>
                      </thead>
                      <tbody>
                        {data.prescribedMedicines.map((med, idx) => (
                          <tr key={med.id || idx}>
                            <td className="border border-slate-300 px-2 py-1 text-center">
                              {idx + 1}
                            </td>
                            <td className="border border-slate-300 px-2 py-1 font-medium">
                              {med.medicineName}
                            </td>
                            <td className="border border-slate-300 px-2 py-1 text-center">
                              {med.quantity}
                            </td>
                            <td className="border border-slate-300 px-2 py-1">
                              {med.dosageInstruction}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </section>

              {/* V. THÔNG TIN CHUYỂN VIỆN */}
              <section className="space-y-2">
                <h2 className="text-xs font-bold uppercase text-slate-800 border-b border-slate-200 pb-1">
                  V. Thông tin Chuyển tuyến
                </h2>
                <div className="space-y-1.5">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1.5">
                    <p>
                      <span className="text-slate-500">Bệnh viện tuyến trên:</span>{" "}
                      <span className="font-bold">{data.targetHospital || "—"}</span>
                    </p>
                    <p>
                      <span className="text-slate-500">Phân loại lý do:</span>{" "}
                      <span className="font-semibold">
                        {data.reasonCategoryLabel || data.reasonCategory || "—"}
                      </span>
                    </p>
                  </div>
                  <p>
                    <span className="text-slate-500">Lý do chuyển viện chi tiết:</span>{" "}
                    <span>{data.transferReason || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Tình trạng hiện tại lúc chuyển:</span>{" "}
                    <span>{data.currentCondition || "—"}</span>
                  </p>
                  <p>
                    <span className="text-slate-500">Biện pháp sơ cứu đã thực hiện:</span>{" "}
                    <span>{data.firstAidNotes || "—"}</span>
                  </p>
                  {data.refusalNotes && (
                    <p>
                      <span className="text-slate-500">Ghi chú từ chối:</span>{" "}
                      <span className="italic">{data.refusalNotes}</span>
                    </p>
                  )}
                  <p>
                    <span className="text-slate-500">Thời gian chuyển viện:</span>{" "}
                    <span className="font-semibold">{formatDateTime(data.transferredAt)}</span>
                  </p>
                </div>
              </section>

              {/* CHỮ KÝ BÁC SĨ */}
              <div className="pt-4 grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div className="text-center text-[11px] text-slate-500 hidden sm:block" />
                <div className="text-center space-y-1">
                  <p className="italic text-slate-600">
                    Ngày {transferDate.getDate()} tháng {transferDate.getMonth() + 1} năm{" "}
                    {transferDate.getFullYear()}
                  </p>
                  <p className="font-bold uppercase text-slate-800">Bác sĩ ký chuyển viện</p>
                  <p className="text-[11px] text-slate-500 italic">(Ký và ghi rõ họ tên)</p>
                  <div className="h-14" />
                  <p className="font-bold text-slate-900">
                    {data.doctorTitle ? `${data.doctorTitle} ` : ""}
                    {data.doctorName || "—"}
                  </p>
                  {(data.doctorSpecialty || data.staffCode) && (
                    <p className="text-[11px] text-slate-600">
                      {[data.doctorSpecialty, data.staffCode ? `Mã NV: ${data.staffCode}` : null]
                        .filter(Boolean)
                        .join(" · ")}
                    </p>
                  )}
                </div>
              </div>
            </div>
          ) : null}
        </div>

        {/* FOOTER MODAL (ẨN KHI IN) */}
        <div className="px-5 py-3.5 bg-slate-50 border-t border-slate-200 flex items-center justify-end gap-2.5 shrink-0 no-print">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-lg transition-colors"
          >
            Đóng
          </button>
          {data && !loading && (
            <button
              type="button"
              onClick={handlePrint}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors shadow-xs"
            >
              <Printer className="w-4 h-4" />
              <span>In giấy chuyển viện</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
