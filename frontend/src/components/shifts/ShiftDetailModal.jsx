import { useState } from "react";
import { 
  X, 
  Calendar, 
  Clock, 
  User, 
  Building2, 
  AlertTriangle, 
  Trash2, 
  Edit3, 
  Loader2 
} from "lucide-react";
import { CLINIC_ROOMS, SHIFT_SLOTS, SERVICE_CONFIG } from "./shiftConfig";

function formatDateDMY(dateString) {
  if (!dateString) return "";
  const parts = dateString.split("-");
  if (parts.length === 3) {
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }
  return dateString;
}

function normalizeTimeHHMM(timeStr, fallback = "08:00") {
  if (!timeStr) return fallback;
  return String(timeStr).slice(0, 5);
}

function getDefaultTimesForSlot(slotKey) {
  return SHIFT_SLOTS[slotKey] || SHIFT_SLOTS.MORNING;
}

function resolveShiftSlot(startTime, endTime) {
  const s = normalizeTimeHHMM(startTime, "");
  const e = normalizeTimeHHMM(endTime, "");
  if (s === SHIFT_SLOTS.MORNING.startTime && e === SHIFT_SLOTS.MORNING.endTime) {
    return "MORNING";
  }
  if (s === SHIFT_SLOTS.AFTERNOON.startTime && e === SHIFT_SLOTS.AFTERNOON.endTime) {
    return "AFTERNOON";
  }
  return "CUSTOM";
}

function toMinutes(timeStr) {
  if (!timeStr || !/^\d{2}:\d{2}$/.test(timeStr)) return Number.NaN;
  const [h, m] = timeStr.split(":").map(Number);
  return h * 60 + m;
}

function formatSpecialtyLabel(specialty) {
  if (!specialty) return "";
  const trimmed = String(specialty).trim();
  if (!trimmed) return "";
  const upper = trimmed.toUpperCase();
  if (upper === "DA_KHOA" || upper === "KHAM_DA_KHOA" || upper === "ĐA KHOA") {
    return "Đa khoa";
  }
  if (upper === "TAM_LY" || upper === "TU_VAN_TAM_LY" || upper === "TÂM LÝ") {
    return "Tâm lý";
  }
  return trimmed;
}

/**
 * Modal Thêm / Sửa / Chi tiết Ca khám tích hợp gọi API
 */
export default function ShiftDetailModal({
  isOpen,
  onClose,
  shift,
  mode = "DETAIL", // 'DETAIL' | 'CREATE' | 'EDIT'
  doctors = [],
  onCreateShift,
  onUpdateShift,
  onDeleteShift,
  selectedDayForCreate,
  selectedSlotForCreate = "MORNING",
  userRole = "ADMIN",
  currentUserId,
  currentUserName = "Bác sĩ",
}) {
  const isAdmin = userRole === "ADMIN" || userRole === "BAN_QUAN_LY";
  const isDoctor = !isAdmin;
  const canManageShift =
    isAdmin ||
    (isDoctor &&
      shift != null &&
      Number(shift?.doctorId ?? shift?.staffId) === Number(currentUserId));

  // Lấy bác sĩ ban đầu:
  // Nếu là Doctor -> currentUserId
  // Nếu là Admin -> lấy bác sĩ đầu tiên có id hợp lệ (ưu tiên id dạng số từ BE)
  const getFallbackDoctorId = () => {
    if (isDoctor) return currentUserId || "";
    if (doctors.length > 0) {
      return doctors[0].id;
    }
    return currentUserId || "";
  };

  const initialDoctorId = getFallbackDoctorId();

  // Form State: khởi tạo trực tiếp từ props
  const [formData, setFormData] = useState(() => {
    if (mode === "CREATE" || !shift) {
      const defaultService = "KHAM_DA_KHOA";
      const slotPreset = getDefaultTimesForSlot(selectedSlotForCreate);
      return {
        date: selectedDayForCreate || new Date().toISOString().split("T")[0],
        shiftSlot: slotPreset.key,
        startTime: slotPreset.startTime,
        endTime: slotPreset.endTime,
        doctorId: initialDoctorId,
        serviceType: defaultService,
        room: SERVICE_CONFIG[defaultService].defaultRoom,
        maxCapacity: 10,
      };
    }
    const start = normalizeTimeHHMM(shift.startTime, "08:00");
    const end = normalizeTimeHHMM(shift.endTime, "11:00");
    return {
      date: shift.date,
      shiftSlot: resolveShiftSlot(start, end),
      startTime: start,
      endTime: end,
      doctorId: isDoctor ? currentUserId : shift.doctorId,
      serviceType: shift.serviceType,
      room: shift.room || "P.101 - Phòng y tế",
      maxCapacity: shift.maxCapacity,
    };
  });

  const [formError, setFormError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isEditing, setIsEditing] = useState(
    mode === "CREATE" || (mode === "EDIT" && canManageShift)
  );

  // Track sự thay đổi của modal hoặc shift qua key
  const [prevTrackKey, setPrevTrackKey] = useState(`${mode}-${shift?.id}-${isOpen}-${doctors.length}`);
  const currentTrackKey = `${mode}-${shift?.id}-${isOpen}-${doctors.length}`;

  if (prevTrackKey !== currentTrackKey) {
    setPrevTrackKey(currentTrackKey);
    setFormError("");
    setIsSubmitting(false);
    setIsDeleting(false);

    if (mode === "CREATE" || !shift) {
      setIsEditing(true);
      const defaultService = "KHAM_DA_KHOA";
      const slotPreset = getDefaultTimesForSlot(selectedSlotForCreate);
      setFormData({
        date: selectedDayForCreate || new Date().toISOString().split("T")[0],
        shiftSlot: slotPreset.key,
        startTime: slotPreset.startTime,
        endTime: slotPreset.endTime,
        doctorId: getFallbackDoctorId(),
        serviceType: defaultService,
        room: SERVICE_CONFIG[defaultService].defaultRoom,
        maxCapacity: 10,
      });
    } else {
      setIsEditing(canManageShift && mode === "EDIT");
      const start = normalizeTimeHHMM(shift.startTime, "08:00");
      const end = normalizeTimeHHMM(shift.endTime, "11:00");
      setFormData({
        date: shift.date,
        shiftSlot: resolveShiftSlot(start, end),
        startTime: start,
        endTime: end,
        doctorId: isDoctor ? currentUserId : shift.doctorId,
        serviceType: shift.serviceType,
        room: shift.room || "P.101 - Phòng y tế",
        maxCapacity: shift.maxCapacity,
      });
    }
  }

  if (!isOpen) return null;

  const handleServiceTypeChange = (newService) => {
    const recommendedRoom = SERVICE_CONFIG[newService]?.defaultRoom || CLINIC_ROOMS[0];
    setFormData((prev) => ({
      ...prev,
      serviceType: newService,
      room: recommendedRoom,
    }));
    setFormError("");
  };

  // Submit Handler
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");

    const startMinutes = toMinutes(formData.startTime);
    const endMinutes = toMinutes(formData.endTime);
    if (
      !formData.startTime ||
      !formData.endTime ||
      Number.isNaN(startMinutes) ||
      Number.isNaN(endMinutes) ||
      endMinutes <= startMinutes
    ) {
      setFormError("Giờ kết thúc phải lớn hơn giờ bắt đầu.");
      return;
    }

    if (formData.maxCapacity <= 0) {
      setFormError("Số lượng tối đa phải lớn hơn 0");
      return;
    }
    if (shift && formData.maxCapacity < shift.bookedCount) {
      setFormError(`Không thể giảm số lượng tối đa (${formData.maxCapacity}) thấp hơn số đã đặt (${shift.bookedCount})`);
      return;
    }

    // Kiểm tra Bác sĩ phụ trách:
    // Nếu ID bác sĩ là string mock dạng 'doc-1' (chưa phải số nguyên từ Database), chặn ngay ở FE để báo cho người dùng
    const numericDoctorId = Number(formData.doctorId);
    if (!numericDoctorId || isNaN(numericDoctorId)) {
      setFormError("Vui lòng chọn một Bác sĩ hợp lệ từ hệ thống (ID bác sĩ phải là tài khoản có trong Database).");
      return;
    }

    const doc = doctors.find((d) => String(d.id) === String(formData.doctorId));
    const resolvedDoctorName = isDoctor ? currentUserName : (doc ? doc.name : "Bác sĩ");
    const derivedSession =
      parseInt(formData.startTime.split(":")[0], 10) < 12 ? "MORNING" : "AFTERNOON";

    const payload = {
      staffId: numericDoctorId,
      doctorId: numericDoctorId,
      shiftDate: formData.date,
      startTime: formData.startTime,
      endTime: formData.endTime,
      shiftType: formData.serviceType,
      serviceType: formData.serviceType,
      room: formData.room,
      maxSlots: Number(formData.maxCapacity),
      maxQuantity: Number(formData.maxCapacity),
      status: shift?.status || "OPEN",
      doctorName: resolvedDoctorName,
      shiftSlot: derivedSession,
    };

    try {
      setIsSubmitting(true);
      if (mode === "CREATE" || !shift) {
        await onCreateShift(payload);
      } else {
        await onUpdateShift(shift.id, payload);
      }
      onClose();
    } catch (err) {
      const beMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Thao tác thất bại. Vui lòng kiểm tra lại.";
      setFormError(beMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Delete Handler
  const handleDelete = async () => {
    if (!shift) return;
    if (shift.bookedCount > 0) {
      setFormError(
        `Ca khám đã có ${shift.bookedCount} lịch hẹn. Vui lòng hủy hoặc dời lịch hẹn trước khi xóa.`
      );
      return;
    }

    if (window.confirm("Bạn có chắc chắn muốn xóa ca trực này khỏi hệ thống?")) {
      try {
        setIsDeleting(true);
        await onDeleteShift(shift.id);
        onClose();
      } catch (err) {
        const beMsg =
          err.response?.data?.message ||
          err.response?.data?.error ||
          err.message ||
          "Không thể xóa ca trực.";
        setFormError(beMsg);
      } finally {
        setIsDeleting(false);
      }
    }
  };

  const isFull = shift ? shift.bookedCount >= shift.maxCapacity : false;
  const shiftStartHHMM = shift ? normalizeTimeHHMM(shift.startTime, "08:00") : formData.startTime;
  const shiftEndHHMM = shift ? normalizeTimeHHMM(shift.endTime, "11:00") : formData.endTime;
  const shiftSessionLabel =
    parseInt(String(shiftStartHHMM).split(":")[0], 10) < 12 ? "Ca Sáng" : "Ca Chiều";
  const roomOptions = Array.from(
    new Set([
      ...CLINIC_ROOMS,
      ...(shift?.room ? [shift.room] : []),
      ...(formData.room ? [formData.room] : []),
    ])
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-in fade-in">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-lg overflow-hidden transition-all">
        
        {/* Header Modal */}
        <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div>
            <h3 className="text-base font-bold text-slate-900">
              {isEditing
                ? mode === "CREATE" || !shift
                  ? "Mở Ca Khám Mới Trong Tuần"
                  : "Chỉnh Sửa Thông Tin Ca Khám"
                : "Chi Tiết Ca Khám"}
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              {isEditing
                ? "Thiết lập phân công ca trực, khung giờ, bác sĩ và phòng khám"
                : `Mã ca trực: #${shift?.id}`}
            </p>
          </div>
          <button
            onClick={onClose}
            disabled={isSubmitting || isDeleting}
            className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 rounded-full transition-colors disabled:opacity-50"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Thông báo lỗi validation hoặc lỗi BE trả về */}
        {formError && (
          <div className="mx-6 mt-4 p-3 rounded-lg bg-red-50 border border-red-200 flex items-start gap-2.5 text-xs text-red-700 animate-in fade-in">
            <AlertTriangle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
            <span className="font-medium leading-relaxed">{formError}</span>
          </div>
        )}

        {/* VIEW CHI TIẾT (DETAIL) */}
        {!isEditing && shift ? (
          <div className="p-6 space-y-4">
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-100">
              <span className="text-xs text-slate-500 font-medium">Trạng thái chỗ</span>
              <span
                className={`px-2.5 py-1 text-xs font-bold rounded-full ${
                  isFull
                    ? "bg-red-100 text-red-700"
                    : "bg-emerald-100 text-emerald-700"
                }`}
              >
                {isFull ? "ĐÃ ĐẦY CHỖ (100%)" : "CÒN NHẬN BỆNH NHÂN"}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="flex items-start gap-2.5">
                <Calendar className="w-4 h-4 text-slate-400 mt-0.5" />
                <div>
                  <div className="text-xs text-slate-400">Ngày khám</div>
                  <div className="font-semibold text-slate-800">{formatDateDMY(shift.date)}</div>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <Clock className="w-4 h-4 text-slate-400 mt-0.5" />
                <div>
                  <div className="text-xs text-slate-400">Ca trực</div>
                  <div className="font-semibold text-slate-800">
                    {`${shiftSessionLabel} (${shiftStartHHMM} - ${shiftEndHHMM})`}
                  </div>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <User className="w-4 h-4 text-slate-400 mt-0.5" />
                <div>
                  <div className="text-xs text-slate-400">Bác sĩ phụ trách</div>
                  <div className="font-semibold text-slate-800">{shift.doctorName}</div>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <Building2 className="w-4 h-4 text-slate-400 mt-0.5" />
                <div>
                  <div className="text-xs text-slate-400">Phòng khám</div>
                  <div className="font-semibold text-slate-800">{shift.room}</div>
                </div>
              </div>
            </div>

            <div className="p-3.5 rounded-xl border border-slate-200/80 bg-slate-50/60">
              <div className="flex justify-between items-center mb-1.5 text-xs">
                <span className="font-medium text-slate-600">Lượng bệnh nhân đã đặt:</span>
                <span className="font-bold text-slate-900">
                  {shift.bookedCount} / {shift.maxCapacity} ({Math.round((shift.bookedCount / shift.maxCapacity) * 100)}%)
                </span>
              </div>
              <div className="w-full bg-slate-200 rounded-full h-2">
                <div
                  className={`h-2 rounded-full ${isFull ? "bg-red-500" : "bg-emerald-500"}`}
                  style={{ width: `${Math.min((shift.bookedCount / shift.maxCapacity) * 100, 100)}%` }}
                />
              </div>
            </div>

            {/* Actions cho Admin / Bác sĩ */}
            <div className="flex items-center justify-between pt-3 border-t border-slate-100">
              {canManageShift && (
                <button
                  type="button"
                  onClick={handleDelete}
                  disabled={isDeleting}
                  className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                >
                  {isDeleting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                  <span>Xóa ca</span>
                </button>
              )}

              <div className="flex items-center gap-2 ml-auto">
                {canManageShift && (
                  <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                  >
                    <Edit3 className="w-4 h-4" />
                    <span>Chỉnh sửa</span>
                  </button>
                )}
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 text-xs font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        ) : (
          /* FORM THÊM / SỬA CA KHÁM */
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            
            {/* Ngày làm việc & Ca trực */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Ngày làm việc <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  required
                  disabled={isSubmitting}
                  value={formData.date}
                  onChange={(e) => {
                    setFormData({ ...formData, date: e.target.value });
                    setFormError("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 disabled:bg-slate-50"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Mẫu ca trực <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.shiftSlot}
                  disabled={isSubmitting}
                  onChange={(e) => {
                    const nextSlot = e.target.value;
                    if (nextSlot === "CUSTOM") {
                      setFormData((prev) => ({ ...prev, shiftSlot: "CUSTOM" }));
                    } else {
                      const preset = getDefaultTimesForSlot(nextSlot);
                      setFormData((prev) => ({
                        ...prev,
                        shiftSlot: preset.key,
                        startTime: preset.startTime,
                        endTime: preset.endTime,
                      }));
                    }
                    setFormError("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white disabled:bg-slate-50"
                >
                  <option value="MORNING">Ca Sáng (08:00 - 11:00)</option>
                  <option value="AFTERNOON">Ca Chiều (13:00 - 17:00)</option>
                  <option value="CUSTOM">Tùy chỉnh</option>
                </select>
              </div>
            </div>

            {/* Giờ bắt đầu & Giờ kết thúc linh hoạt */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Giờ bắt đầu <span className="text-red-500">*</span>
                </label>
                <input
                  type="time"
                  required
                  disabled={isSubmitting}
                  value={formData.startTime}
                  onChange={(e) => {
                    const nextStart = e.target.value;
                    setFormData((prev) => ({
                      ...prev,
                      startTime: nextStart,
                      shiftSlot: resolveShiftSlot(nextStart, prev.endTime),
                    }));
                    setFormError("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 disabled:bg-slate-50"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Giờ kết thúc <span className="text-red-500">*</span>
                </label>
                <input
                  type="time"
                  required
                  disabled={isSubmitting}
                  value={formData.endTime}
                  onChange={(e) => {
                    const nextEnd = e.target.value;
                    setFormData((prev) => ({
                      ...prev,
                      endTime: nextEnd,
                      shiftSlot: resolveShiftSlot(prev.startTime, nextEnd),
                    }));
                    setFormError("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 disabled:bg-slate-50"
                />
              </div>
            </div>

            {/* Dịch vụ & Phòng khám */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Loại dịch vụ <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.serviceType}
                  disabled={isSubmitting}
                  onChange={(e) => handleServiceTypeChange(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white disabled:bg-slate-50"
                >
                  <option value="KHAM_DA_KHOA">Khám Đa khoa</option>
                  <option value="TU_VAN_TAM_LY">Tư vấn Tâm lý</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Phòng khám <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.room}
                  disabled={isSubmitting}
                  onChange={(e) => {
                    setFormData({ ...formData, room: e.target.value });
                    setFormError("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white disabled:bg-slate-50"
                >
                  {roomOptions.map((roomOption) => (
                    <option key={roomOption} value={roomOption}>
                      {roomOption}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Bác sĩ phụ trách */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Bác sĩ phụ trách <span className="text-red-500">*</span>
              </label>
              {isDoctor ? (
                <input
                  type="text"
                  readOnly
                  disabled
                  value={`${currentUserName} (Tài khoản của bạn)`}
                  className="w-full px-3 py-2 text-sm bg-slate-100 text-slate-700 border border-slate-200 rounded-lg font-medium cursor-not-allowed select-none"
                />
              ) : (
                <select
                  value={formData.doctorId}
                  disabled={isSubmitting}
                  onChange={(e) => {
                    setFormData({ ...formData, doctorId: e.target.value });
                    setFormError("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white disabled:bg-slate-50"
                >
                  {doctors.map((doc) => {
                    const specialtyLabel = formatSpecialtyLabel(doc.specialty);
                    return (
                      <option key={doc.id} value={doc.id}>
                        {doc.name || doc.fullName}
                        {specialtyLabel ? ` - ${specialtyLabel}` : ""}
                      </option>
                    );
                  })}
                </select>
              )}
            </div>

            {/* Số lượng tối đa */}
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="text-xs font-semibold text-slate-700">
                  Số lượng chỗ tối đa <span className="text-red-500">*</span>
                </label>
                {shift && shift.bookedCount > 0 && (
                  <span className="text-[11px] text-amber-600 font-medium">
                    (Đã có {shift.bookedCount} chỗ đặt trước)
                  </span>
                )}
              </div>
              <input
                type="number"
                min={shift ? shift.bookedCount || 1 : 1}
                max="50"
                disabled={isSubmitting}
                value={formData.maxCapacity}
                onChange={(e) => {
                  setFormData({
                    ...formData,
                    maxCapacity: parseInt(e.target.value, 10) || 1,
                  });
                  setFormError("");
                }}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 disabled:bg-slate-50"
              />
            </div>

            {/* Actions */}
            <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => {
                  if (mode === "DETAIL" && shift) {
                    setIsEditing(false);
                    setFormError("");
                  } else {
                    onClose();
                  }
                }}
                className="px-4 py-2 text-xs font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors disabled:opacity-50"
              >
                {mode === "DETAIL" && shift ? "Quay lại" : "Hủy"}
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex items-center gap-1.5 px-5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-xs transition-colors disabled:opacity-50"
              >
                {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                <span>{mode === "CREATE" || !shift ? "Mở Ca Khám" : "Lưu Thay Đổi"}</span>
              </button>
            </div>
          </form>
        )}

      </div>
    </div>
  );
}
