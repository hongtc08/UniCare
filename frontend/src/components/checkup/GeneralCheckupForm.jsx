import {
  Stethoscope,
  AlertTriangle,
  Activity,
  ShieldCheck,
  HeartPulse,
  Brain,
  ClipboardCheck,
  CheckCircle2,
  XCircle,
} from "lucide-react";
import { PERIODIC_RESULTS } from "../../api/medicalRecordApi";
import { isPsychologicalCheckup } from "../../utils/checkupValidation";

/**
 * Form Phiếu khám bệnh (Đa khoa / Tư vấn Tâm lý / Khám Sức khỏe Định kỳ).
 * Tương thích với CreateMedicalCheckupRequest & UpdateMedicalCheckupRequest của Spring Boot.
 *
 * @param {Object} props
 * @param {Object} props.formData
 * @param {Function} props.onChange - (fieldName, value) => void
 * @param {Array} props.diseaseGroups - Danh sách nhóm bệnh [{ id, code, name }]
 * @param {boolean} [props.isReadOnly=false] - Trạng thái khóa cứng khi phiếu đã COMPLETED
 * @param {Object} [props.errors={}] - Đối tượng chứa lỗi validation
 * @param {string} [props.diseaseGroupNameFallback] - Tên nhóm bệnh trả về từ API chi tiết
 */
export default function GeneralCheckupForm({
  formData = {},
  onChange,
  diseaseGroups = [],
  isReadOnly = false,
  errors = {},
  diseaseGroupNameFallback = "",
}) {
  const isPsychological = isPsychologicalCheckup(formData.checkupType);
  const isPeriodic = !isPsychological && Boolean(formData.isPeriodic);

  const handleChange = (field, value) => {
    if (isReadOnly || !onChange) return;
    onChange(field, value);
  };

  const handleDiseaseGroupSelect = (e) => {
    if (isReadOnly || !onChange) return;
    const rawVal = e.target.value;
    const normalized = rawVal ? Number(rawVal) : null;
    onChange("diseaseGroupId", normalized);
  };

  // Tìm tên nhóm bệnh đang chọn để hiển thị ở chế độ Read-only
  const selectedDiseaseGroup = diseaseGroups.find(
    (g) => Number(g.id) === Number(formData.diseaseGroupId)
  );
  const displayDiseaseGroupName =
    selectedDiseaseGroup?.name ||
    diseaseGroupNameFallback ||
    (formData.diseaseGroupId ? `Mã nhóm #${formData.diseaseGroupId}` : "Chưa phân loại");

  const headerStyle = isPsychological
    ? {
        backgroundColor: "var(--service-psy-bg)",
        borderColor: "var(--service-psy-border)",
      }
    : undefined;

  const iconBoxStyle = isPsychological
    ? {
        backgroundColor: "#ffffff",
        color: "var(--service-psy-text)",
        border: "1px solid var(--service-psy-border)",
      }
    : undefined;

  const psyBadgeStyle = {
    backgroundColor: "var(--service-psy-bg)",
    color: "var(--service-psy-text)",
    borderColor: "var(--service-psy-border)",
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs overflow-hidden">
      {/* HEADER PHẦN KHÁM LÂM SÀNG / TƯ VẤN TÂM LÝ */}
      <div
        className={`px-5 py-4 border-b flex flex-wrap items-center justify-between gap-3 ${
          isPsychological ? "" : "border-slate-100 bg-slate-50/50"
        }`}
        style={headerStyle}
      >
        <div className="flex items-center gap-2.5">
          <div
            className={`p-2 rounded-lg ${
              isPsychological ? "" : "bg-blue-50 text-blue-600"
            }`}
            style={iconBoxStyle}
          >
            {isPsychological ? (
              <Brain className="w-5 h-5" />
            ) : (
              <Stethoscope className="w-5 h-5" />
            )}
          </div>
          <div>
            <h2
              className="text-sm font-bold text-slate-800"
              style={isPsychological ? { color: "var(--service-psy-text)" } : undefined}
            >
              {isPsychological
                ? "Nội dung Tư vấn Tâm lý"
                : isPeriodic
                ? "Nội dung Khám Sức khỏe Định kỳ"
                : "Nội dung Khám lâm sàng & Chẩn đoán"}
            </h2>
            <p className="text-[11px] text-slate-500">
              {isPsychological
                ? "Ghi nhận biểu hiện tâm lý, tóm tắt buổi tư vấn và đề xuất giải tỏa cho sinh viên"
                : isPeriodic
                ? "Đánh giá tình trạng sức khỏe tổng quát và kết luận Đạt / Không đạt định kỳ"
                : "Ghi nhận triệu chứng, xử trí sơ cứu ban đầu và kết luận của bác sĩ"}
            </p>
          </div>
        </div>

        {/* BADGES LOẠI HÌNH & KHẨN CẤP */}
        <div className="flex flex-wrap items-center gap-2">
          {formData.isEmergency ? (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-bold bg-red-50 text-red-700 border border-red-200">
              <AlertTriangle className="w-3.5 h-3.5 text-red-600" />
              CẤP CỨU / KHẨN CẤP
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">
              <ShieldCheck className="w-3.5 h-3.5 text-blue-600" />
              THEO LỊCH HẸN
            </span>
          )}

          {isPsychological ? (
            <span
              className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold border"
              style={psyBadgeStyle}
            >
              <Brain className="w-3.5 h-3.5" />
              Tư vấn tâm lý
            </span>
          ) : isPeriodic ? (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
              <ClipboardCheck className="w-3.5 h-3.5 text-emerald-600" />
              Khám định kỳ
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-200">
              <HeartPulse className="w-3.5 h-3.5 text-slate-500" />
              Khám Đa khoa
            </span>
          )}
        </div>
      </div>

      <div className="p-5 space-y-5">
        {/* CHẾ ĐỘ KHÁM SỨC KHỎE ĐỊNH KỲ (ẨN KHI TƯ VẤN TÂM LÝ) */}
        {!isPsychological && (
          <div className="p-4 rounded-xl border border-slate-200/90 bg-slate-50/40 space-y-3.5">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <label
                htmlFor="isPeriodicToggle"
                className={`inline-flex items-center gap-2.5 ${
                  isReadOnly ? "cursor-default" : "cursor-pointer"
                }`}
              >
                <input
                  id="isPeriodicToggle"
                  type="checkbox"
                  checked={isPeriodic}
                  onChange={(e) => handleChange("isPeriodic", e.target.checked)}
                  disabled={isReadOnly}
                  className="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500 disabled:opacity-60"
                />
                <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Phiếu Khám Sức khỏe Định kỳ
                </span>
              </label>
              <span className="text-[11px] text-slate-500">
                Bật nếu đây là đợt khám định kỳ cần kết luận Đạt / Không đạt
              </span>
            </div>

            {isPeriodic && (
              <div className="pt-2 border-t border-slate-200/70">
                <div className="flex items-center justify-between mb-2">
                  <span className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                    Kết quả Khám định kỳ{" "}
                    {!isReadOnly && <span className="text-red-500">*</span>}
                  </span>
                  {!isReadOnly && (
                    <span className="text-[11px] text-amber-600 font-medium">
                      * Bắt buộc khi Hoàn tất ca khám
                    </span>
                  )}
                </div>

                {isReadOnly ? (
                  <div>
                    {formData.periodicResult === PERIODIC_RESULTS.PASSED ? (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">
                        <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                        ĐẠT (PASSED)
                      </span>
                    ) : formData.periodicResult === PERIODIC_RESULTS.FAILED ? (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold bg-red-100 text-red-800 border border-red-300">
                        <XCircle className="w-4 h-4 text-red-600" />
                        KHÔNG ĐẠT (FAILED)
                      </span>
                    ) : (
                      <span className="text-xs text-slate-500 italic">
                        Chưa có kết quả định kỳ
                      </span>
                    )}
                  </div>
                ) : (
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <button
                      type="button"
                      onClick={() => handleChange("periodicResult", PERIODIC_RESULTS.PASSED)}
                      className={`flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border text-xs font-bold transition-all ${
                        formData.periodicResult === PERIODIC_RESULTS.PASSED
                          ? "bg-emerald-600 text-white border-emerald-600 shadow-xs ring-2 ring-emerald-500/20"
                          : "bg-white text-emerald-700 border-emerald-200 hover:bg-emerald-50"
                      }`}
                    >
                      <CheckCircle2 className="w-4 h-4" />
                      <span>ĐẠT (PASSED)</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => handleChange("periodicResult", PERIODIC_RESULTS.FAILED)}
                      className={`flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border text-xs font-bold transition-all ${
                        formData.periodicResult === PERIODIC_RESULTS.FAILED
                          ? "bg-red-600 text-white border-red-600 shadow-xs ring-2 ring-red-500/20"
                          : "bg-white text-red-700 border-red-200 hover:bg-red-50"
                      }`}
                    >
                      <XCircle className="w-4 h-4" />
                      <span>KHÔNG ĐẠT (FAILED)</span>
                    </button>
                  </div>
                )}

                {errors.periodicResult && (
                  <p className="mt-1.5 text-xs text-red-600 font-medium flex items-center gap-1">
                    <Activity className="w-3.5 h-3.5" />
                    {errors.periodicResult}
                  </p>
                )}
              </div>
            )}
          </div>
        )}

        {/* TRƯỜNG 1: NHÓM BỆNH DỊCH TỄ (ẨN KHI TƯ VẤN TÂM LÝ) */}
        {!isPsychological && (
          <div>
            <label
              htmlFor="diseaseGroupId"
              className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
            >
              Nhóm bệnh dịch tễ
            </label>
            {isReadOnly ? (
              <div className="w-full px-3 py-2 text-sm bg-slate-100 text-slate-800 rounded-lg border border-slate-200 font-medium">
                {displayDiseaseGroupName}
              </div>
            ) : (
              <select
                id="diseaseGroupId"
                name="diseaseGroupId"
                value={formData.diseaseGroupId ?? ""}
                onChange={handleDiseaseGroupSelect}
                disabled={isReadOnly}
                className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
                  errors.diseaseGroupId
                    ? "border-red-400 bg-red-50/30 text-red-900 focus:border-red-500"
                    : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-blue-500"
                }`}
              >
                <option value="">-- Chưa xác định / Chọn nhóm bệnh --</option>
                {diseaseGroups.map((group) => (
                  <option key={group.id} value={group.id}>
                    {group.code ? `[${group.code}] ` : ""}
                    {group.name}
                  </option>
                ))}
              </select>
            )}
            {errors.diseaseGroupId && (
              <p className="mt-1 text-xs text-red-600 font-medium">
                {errors.diseaseGroupId}
              </p>
            )}
            <p className="mt-1 text-[11px] text-slate-400">
              Phục vụ công tác thống kê dịch tễ học đường và báo cáo y tế định kỳ.
            </p>
          </div>
        )}

        {/* TRƯỜNG 2: TRIỆU CHỨNG LÂM SÀNG / BIỂU HIỆN TÂM LÝ */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label
              htmlFor="symptoms"
              className="block text-xs font-bold text-slate-700 uppercase tracking-wider"
            >
              {isPsychological ? "Biểu hiện" : "Triệu chứng lâm sàng"}{" "}
              {!isReadOnly && <span className="text-red-500">*</span>}
            </label>
            {!isReadOnly && (
              <span className="text-[11px] text-amber-600 font-medium">
                * Bắt buộc khi Hoàn tất ca khám
              </span>
            )}
          </div>
          <textarea
            id="symptoms"
            name="symptoms"
            rows={3}
            value={formData.symptoms || ""}
            onChange={(e) => handleChange("symptoms", e.target.value)}
            disabled={isReadOnly}
            placeholder={
              isReadOnly
                ? isPsychological
                  ? "Không ghi nhận biểu hiện tâm lý"
                  : "Không ghi nhận triệu chứng"
                : isPsychological
                ? "Ghi nhận trạng thái cảm xúc, hành vi, giấc ngủ, áp lực học tập/gia đình..."
                : "Mô tả lý do khám, triệu chứng khởi phát, thời gian xuất hiện, diễn biến..."
            }
            className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
              isReadOnly
                ? "bg-slate-100 text-slate-700 border-slate-200 cursor-not-allowed resize-none"
                : errors.symptoms
                ? "border-red-400 bg-red-50/30 text-red-900 focus:border-red-500"
                : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-blue-500"
            }`}
          />
          {errors.symptoms && (
            <p className="mt-1 text-xs text-red-600 font-medium flex items-center gap-1">
              <Activity className="w-3.5 h-3.5" />
              {errors.symptoms}
            </p>
          )}
        </div>

        {/* TRƯỜNG 3: BIỆN PHÁP SƠ CỨU / ĐỀ XUẤT GIẢI TỎA */}
        <div>
          <label
            htmlFor="treatmentSuggestions"
            className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
          >
            {isPsychological
              ? "Đề xuất giải tỏa"
              : "Biện pháp sơ cứu / Xử trí ban đầu"}
          </label>
          <textarea
            id="treatmentSuggestions"
            name="treatmentSuggestions"
            rows={2}
            value={formData.treatmentSuggestions || ""}
            onChange={(e) => handleChange("treatmentSuggestions", e.target.value)}
            disabled={isReadOnly}
            placeholder={
              isReadOnly
                ? isPsychological
                  ? "Không ghi nhận đề xuất giải tỏa"
                  : "Không ghi nhận xử trí ban đầu"
                : isPsychological
                ? "Các phương pháp thư giãn, bài tập hít thở, lịch sinh hoạt, hẹn tái tư vấn..."
                : "Các biện pháp sơ cứu tại chỗ, chườm ấm, hạ sốt, sát trùng, tư vấn nghỉ ngơi..."
            }
            className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
              isReadOnly
                ? "bg-slate-100 text-slate-700 border-slate-200 cursor-not-allowed resize-none"
                : errors.treatmentSuggestions
                ? "border-red-400 bg-red-50/30 text-red-900 focus:border-red-500"
                : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-blue-500"
            }`}
          />
          {errors.treatmentSuggestions && (
            <p className="mt-1 text-xs text-red-600 font-medium">
              {errors.treatmentSuggestions}
            </p>
          )}
        </div>

        {/* TRƯỜNG 4: KẾT LUẬN CHẨN ĐOÁN / TÓM TẮT BUỔI TƯ VẤN (BẮT BUỘC KHI HOÀN TẤT) */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label
              htmlFor="diagnosisNotes"
              className="block text-xs font-bold text-slate-700 uppercase tracking-wider"
            >
              {isPsychological ? "Tóm tắt buổi tư vấn" : "Kết luận chẩn đoán"}{" "}
              {!isReadOnly && <span className="text-red-500">*</span>}
            </label>
            {!isReadOnly && (
              <span className="text-[11px] text-amber-600 font-medium">
                * Bắt buộc khi Hoàn tất ca khám
              </span>
            )}
          </div>
          <textarea
            id="diagnosisNotes"
            name="diagnosisNotes"
            rows={3}
            value={formData.diagnosisNotes || ""}
            onChange={(e) => handleChange("diagnosisNotes", e.target.value)}
            disabled={isReadOnly}
            placeholder={
              isReadOnly
                ? isPsychological
                  ? "Không có tóm tắt buổi tư vấn"
                  : "Không có ghi chú chẩn đoán"
                : isPsychological
                ? "Tóm tắt nội dung trao đổi, đánh giá mức độ căng thẳng và nhận định chuyên môn..."
                : "Kết luận bệnh học chính thức của bác sĩ (ví dụ: Viêm mũi họng cấp / Rối loạn tiêu hóa / Bong gân độ 1)..."
            }
            className={`w-full px-3 py-2 text-sm rounded-lg border transition-colors focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 ${
              isReadOnly
                ? "bg-slate-100 text-slate-700 border-slate-200 cursor-not-allowed resize-none"
                : errors.diagnosisNotes
                ? "border-red-400 bg-red-50/30 text-red-900 focus:border-red-500"
                : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-blue-500"
            }`}
          />
          {errors.diagnosisNotes && (
            <p className="mt-1 text-xs text-red-600 font-medium flex items-center gap-1">
              <Activity className="w-3.5 h-3.5" />
              {errors.diagnosisNotes}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
