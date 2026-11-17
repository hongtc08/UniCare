import { Stethoscope, AlertTriangle, Activity, ShieldCheck, HeartPulse } from "lucide-react";

/**
 * Form Phiếu khám Đa khoa: Nhập các trường lâm sàng của Phiếu khám (FE2).
 * Tương thích với CreateMedicalCheckupRequest & UpdateMedicalCheckupRequest của Spring Boot.
 *
 * @param {Object} props
 * @param {Object} props.formData - { checkupType, isEmergency, diseaseGroupId, symptoms, treatmentSuggestions, diagnosisNotes }
 * @param {Function} props.onChange - (fieldName, value) => void
 * @param {Array} props.diseaseGroups - Danh sách nhóm bệnh [{ id, code, name }]
 * @param {boolean} [props.isReadOnly=false] - Trạng thái khóa cứng khi phiếu đã COMPLETED
 * @param {Object} [props.errors={}] - Đối tượng chứa lỗi validation { diagnosisNotes: "..." }
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
  const handleChange = (field, value) => {
    if (isReadOnly || !onChange) return;
    onChange(field, value);
  };

  const handleDiseaseGroupSelect = (e) => {
    if (isReadOnly || !onChange) return;
    const rawVal = e.target.value;
    // Chuẩn hóa sang Number hoặc null để tránh gửi chuỗi rỗng "" sang backend
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

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs overflow-hidden">
      {/* HEADER PHẦN KHÁM LÂM SÀNG */}
      <div className="px-5 py-4 border-b border-slate-100 bg-slate-50/50 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
            <Stethoscope className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800">
              Nội dung Khám lâm sàng & Chẩn đoán
            </h2>
            <p className="text-[11px] text-slate-500">
              Ghi nhận triệu chứng, xử trí sơ cứu ban đầu và kết luận của bác sĩ
            </p>
          </div>
        </div>

        {/* BADGES LOẠI HÌNH & KHẨN CẤP */}
        <div className="flex items-center gap-2">
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

          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-700 border border-slate-200">
            <HeartPulse className="w-3.5 h-3.5 text-slate-500" />
            {formData.checkupType === "TU_VAN_TAM_LY" ? "Tư vấn tâm lý" : "Khám Đa khoa"}
          </span>
        </div>
      </div>

      <div className="p-5 space-y-5">
        {/* TRƯỜNG 1: NHÓM BỆNH DỊCH TỄ */}
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

        {/* TRƯỜNG 2: TRIỆU CHỨNG LÂM SÀNG */}
        <div>
          <label
            htmlFor="symptoms"
            className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
          >
            Triệu chứng lâm sàng
          </label>
          <textarea
            id="symptoms"
            name="symptoms"
            rows={3}
            value={formData.symptoms || ""}
            onChange={(e) => handleChange("symptoms", e.target.value)}
            disabled={isReadOnly}
            placeholder={
              isReadOnly
                ? "Không ghi nhận triệu chứng"
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
            <p className="mt-1 text-xs text-red-600 font-medium">
              {errors.symptoms}
            </p>
          )}
        </div>

        {/* TRƯỜNG 3: BIỆN PHÁP SƠ CỨU / XỬ TRÍ BAN ĐẦU */}
        <div>
          <label
            htmlFor="treatmentSuggestions"
            className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5"
          >
            Biện pháp sơ cứu / Xử trí ban đầu
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
                ? "Không ghi nhận xử trí ban đầu"
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

        {/* TRƯỜNG 4: KẾT LUẬN CHẨN ĐOÁN (BẮT BUỘC KHI HOÀN TẤT) */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label
              htmlFor="diagnosisNotes"
              className="block text-xs font-bold text-slate-700 uppercase tracking-wider"
            >
              Kết luận chẩn đoán {!isReadOnly && <span className="text-red-500">*</span>}
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
                ? "Không có ghi chú chẩn đoán"
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
