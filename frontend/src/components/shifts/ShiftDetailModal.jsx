import React, { useState, useEffect } from "react";
import { 
  X, 
  Calendar, 
  Clock, 
  User, 
  Stethoscope, 
  Building2, 
  Users, 
  AlertTriangle, 
  Trash2, 
  Edit3,
  CheckCircle2
} from "lucide-react";
import { CLINIC_ROOMS, SHIFT_SLOTS, SERVICE_CONFIG } from "./mockShiftData";

/**
 * Modal Thêm/Sửa/Xem Chi tiết Ca khám:
 * 1. BỎ input chọn giờ tự do -> Dropdown Select "Ca trực":
 *    - "Ca Sáng (08:00 - 11:00)"
 *    - "Ca Chiều (13:00 - 17:00)"
 * 2. Phòng khám: Dropdown Select đúng 2 phòng (P.101 & P.102).
 *    Tự động gợi ý phòng theo Dịch vụ (Đa khoa -> P.101, Tâm lý -> P.102).
 * 3. Validation & Conflict Rules:
 *    - Chặn trùng lịch Bác sĩ (cùng ngày & cùng ca).
 *    - Chặn trùng Phòng khám (cùng ngày & cùng ca).
 *    - Rule Sửa: maxCapacity >= bookedCount.
 *    - Rule Xóa: bookedCount > 0 thì cảnh báo & chặn xóa.
 */
export default function ShiftDetailModal({
  isOpen,
  onClose,
  shift,
  mode = "DETAIL", // 'DETAIL' | 'CREATE' | 'EDIT'
  doctors = [],
  shifts = [],
  onCreateShift,
  onUpdateShift,
  onDeleteShift,
  selectedDayForCreate,
  selectedSlotForCreate = "MORNING",
  userRole = "ADMIN",
}) {
  const isAdmin = userRole === "ADMIN" || userRole === "BAN_QUAN_LY";

  // Form State
  const [formData, setFormData] = useState({
    date: selectedDayForCreate || new Date().toISOString().split("T")[0],
    shiftSlot: selectedSlotForCreate || "MORNING",
    doctorId: doctors[0]?.id || "",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    maxCapacity: 10,
  });

  const [errorMessage, setErrorMessage] = useState("");
  const [isEditing, setIsEditing] = useState(mode === "CREATE" || mode === "EDIT");

  // Đồng bộ form khi mở modal hoặc đổi shift
  useEffect(() => {
    if (!isOpen) {
      setErrorMessage("");
      return;
    }

    if (mode === "CREATE") {
      setIsEditing(true);
      const defaultService = "KHAM_DA_KHOA";
      setFormData({
        date: selectedDayForCreate || new Date().toISOString().split("T")[0],
        shiftSlot: selectedSlotForCreate || "MORNING",
        doctorId: doctors[0]?.id || "",
        serviceType: defaultService,
        room: SERVICE_CONFIG[defaultService].defaultRoom,
        maxCapacity: 10,
      });
    } else if (shift) {
      setIsEditing(mode === "EDIT");
      setFormData({
        date: shift.date,
        shiftSlot: shift.shiftSlot || (parseInt(shift.startTime?.split(":")[0], 10) < 12 ? "MORNING" : "AFTERNOON"),
        doctorId: shift.doctorId,
        serviceType: shift.serviceType,
        room: shift.room || "P.101 - Phòng y tế",
        maxCapacity: shift.maxCapacity,
      });
    }
  }, [isOpen, mode, shift, selectedDayForCreate, selectedSlotForCreate, doctors]);

  if (!isOpen) return null;

  // Tự động gợi ý phòng theo Dịch vụ khi người dùng thay đổi loại dịch vụ
  const handleServiceTypeChange = (newService) => {
    const recommendedRoom = SERVICE_CONFIG[newService]?.defaultRoom || CLINIC_ROOMS[0];
    setFormData((prev) => ({
      ...prev,
      serviceType: newService,
      room: recommendedRoom,
    }));
    setErrorMessage("");
  };

  /**
   * Kiểm tra xung đột & Validation Rules
   */
  const validateShift = (dataToValidate, currentShiftId = null) => {
    const targetSlot = SHIFT_SLOTS[dataToValidate.shiftSlot];

    // Lọc danh sách các ca khác trong cùng Ngày và cùng Ca trực
    const otherShiftsInSlot = shifts.filter(
      (s) =>
        s.id !== currentShiftId &&
        s.date === dataToValidate.date &&
        (s.shiftSlot === dataToValidate.shiftSlot ||
          (dataToValidate.shiftSlot === "MORNING" && parseInt(s.startTime?.split(":")[0], 10) < 12) ||
          (dataToValidate.shiftSlot === "AFTERNOON" && parseInt(s.startTime?.split(":")[0], 10) >= 12))
    );

    // Rule 1: Chặn trùng Bác sĩ
    const doctorConflict = otherShiftsInSlot.find(
      (s) => s.doctorId === dataToValidate.doctorId
    );
    if (doctorConflict) {
      const doc = doctors.find((d) => d.id === dataToValidate.doctorId);
      return `Bác sĩ ${doc?.name || ""} đã có ca trực trong ${targetSlot.label} ngày ${dataToValidate.date}! Không thể xếp thêm ca.`;
    }

    // Rule 2: Chặn trùng Phòng khám
    const roomConflict = otherShiftsInSlot.find(
      (s) => s.room === dataToValidate.room
    );
    if (roomConflict) {
      return `${dataToValidate.room} đã được phân cho ca khác trong ${targetSlot.label} ngày ${dataToValidate.date}! Vui lòng chọn phòng khám còn lại.`;
    }

    // Rule 3: Khi sửa, số lượng tối đa không được nhỏ hơn số slot đã đặt
    if (currentShiftId && shift) {
      if (dataToValidate.maxCapacity < shift.bookedCount) {
        return `Số lượng tối đa (${dataToValidate.maxCapacity}) không thể nhỏ hơn số người đã đặt (${shift.bookedCount})!`;
      }
    }

    if (dataToValidate.maxCapacity <= 0) {
      return "Số lượng chỗ tối đa phải lớn hơn 0!";
    }

    return null;
  };

  // Submit Handler
  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage("");

    const currentShiftId = shift?.id || null;
    const validationError = validateShift(formData, currentShiftId);
    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    const doc = doctors.find((d) => d.id === formData.doctorId);
    const slotInfo = SHIFT_SLOTS[formData.shiftSlot];

    const payload = {
      ...formData,
      startTime: slotInfo.startTime,
      endTime: slotInfo.endTime,
      doctorName: doc ? doc.name : "Bác sĩ",
    };

    if (mode === "CREATE" || !shift) {
      onCreateShift({
        ...payload,
        id: `shift-${Date.now()}`,
        bookedCount: 0,
      });
    } else {
      onUpdateShift({
        ...shift,
        ...payload,
      });
    }
    onClose();
  };

  // Delete Handler
  const handleDelete = () => {
    if (!shift) return;
    if (shift.bookedCount > 0) {
      setErrorMessage(
        `Không thể xóa ca trực này vì đã có ${shift.bookedCount} sinh viên/bệnh nhân đặt chỗ! Hãy hủy lịch hẹn của bệnh nhân trước.`
      );
      return;
    }

    if (window.confirm("Bạn có chắc chắn muốn xóa ca trực này khỏi lịch?")) {
      onDeleteShift(shift.id);
      onClose();
    }
  };

  const isFull = shift ? shift.bookedCount >= shift.maxCapacity : false;
  const slotDetails = shift
    ? SHIFT_SLOTS[shift.shiftSlot] || (parseInt(shift.startTime?.split(":")[0], 10) < 12 ? SHIFT_SLOTS.MORNING : SHIFT_SLOTS.AFTERNOON)
    : SHIFT_SLOTS[formData.shiftSlot];

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
                ? "Thiết lập phân công ca trực, bác sĩ và phòng khám cố định"
                : `Mã ca trực: #${shift?.id}`}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Thông báo lỗi validation nếu có */}
        {errorMessage && (
          <div className="mx-6 mt-4 p-3 rounded-lg bg-red-50 border border-red-200 flex items-start gap-2.5 text-xs text-red-700 animate-in fade-in">
            <AlertTriangle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
            <span className="font-medium">{errorMessage}</span>
          </div>
        )}

        {/* 1. VIEW CHI TIẾT (DETAIL) */}
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
                  <div className="font-semibold text-slate-800">{shift.date}</div>
                </div>
              </div>

              <div className="flex items-start gap-2.5">
                <Clock className="w-4 h-4 text-slate-400 mt-0.5" />
                <div>
                  <div className="text-xs text-slate-400">Ca trực</div>
                  <div className="font-semibold text-slate-800">
                    {slotDetails?.label || `${shift.startTime} - ${shift.endTime}`}
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
                  {shift.bookedCount} / {shift.maxCapacity} chỗ ({Math.round((shift.bookedCount / shift.maxCapacity) * 100)}%)
                </span>
              </div>
              <div className="w-full bg-slate-200 rounded-full h-2">
                <div
                  className={`h-2 rounded-full ${isFull ? "bg-red-500" : "bg-emerald-500"}`}
                  style={{ width: `${Math.min((shift.bookedCount / shift.maxCapacity) * 100, 100)}%` }}
                />
              </div>
            </div>

            {/* Actions cho Admin */}
            <div className="flex items-center justify-between pt-3 border-t border-slate-100">
              {isAdmin && (
                <button
                  type="button"
                  onClick={handleDelete}
                  className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>Xóa ca</span>
                </button>
              )}

              <div className="flex items-center gap-2 ml-auto">
                {isAdmin && (
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
          /* 2. FORM THÊM / SỬA CA KHÁM */
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            
            {/* Hàng 1: Ngày làm việc & Dropdown Ca trực */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Ngày làm việc <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  required
                  value={formData.date}
                  onChange={(e) => {
                    setFormData({ ...formData, date: e.target.value });
                    setErrorMessage("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                />
              </div>

              {/* DROPDOWN SELECT CA TRỰC (Thay thế hoàn toàn 2 input giờ tự do) */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Ca trực <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.shiftSlot}
                  onChange={(e) => {
                    setFormData({ ...formData, shiftSlot: e.target.value });
                    setErrorMessage("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white"
                >
                  <option value="MORNING">Ca Sáng (08:00 - 11:00)</option>
                  <option value="AFTERNOON">Ca Chiều (13:00 - 17:00)</option>
                </select>
              </div>
            </div>

            {/* Hàng 2: Loại dịch vụ & Phòng khám */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Loại dịch vụ <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.serviceType}
                  onChange={(e) => handleServiceTypeChange(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white"
                >
                  <option value="KHAM_DA_KHOA">Khám Đa khoa</option>
                  <option value="TU_VAN_TAM_LY">Tư vấn Tâm lý</option>
                </select>
              </div>

              {/* DROPDOWN SELECT PHÒNG KHÁM (Chỉ đúng 2 phòng P.101 & P.102) */}
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Phòng khám <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.room}
                  onChange={(e) => {
                    setFormData({ ...formData, room: e.target.value });
                    setErrorMessage("");
                  }}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white"
                >
                  {CLINIC_ROOMS.map((room) => (
                    <option key={room} value={room}>
                      {room}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Hàng 3: Bác sĩ phụ trách */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Bác sĩ phụ trách <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.doctorId}
                onChange={(e) => {
                  setFormData({ ...formData, doctorId: e.target.value });
                  setErrorMessage("");
                }}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer bg-white"
              >
                {doctors.map((doc) => (
                  <option key={doc.id} value={doc.id}>
                    {doc.name} - {doc.specialty === "KHAM_DA_KHOA" ? "Khoa Đa Khoa" : "Tâm Lý"}
                  </option>
                ))}
              </select>
            </div>

            {/* Hàng 4: Số lượng tối đa */}
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
                value={formData.maxCapacity}
                onChange={(e) => {
                  setFormData({
                    ...formData,
                    maxCapacity: parseInt(e.target.value, 10) || 1,
                  });
                  setErrorMessage("");
                }}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              />
            </div>

            {/* Modal Actions */}
            <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => {
                  if (mode === "DETAIL" && shift) {
                    setIsEditing(false);
                    setErrorMessage("");
                  } else {
                    onClose();
                  }
                }}
                className="px-4 py-2 text-xs font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
              >
                {mode === "DETAIL" && shift ? "Quay lại" : "Hủy"}
              </button>
              <button
                type="submit"
                className="px-5 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-xs transition-colors"
              >
                {mode === "CREATE" || !shift ? "Mở Ca Khám" : "Lưu Thay Đổi"}
              </button>
            </div>
          </form>
        )}

      </div>
    </div>
  );
}
