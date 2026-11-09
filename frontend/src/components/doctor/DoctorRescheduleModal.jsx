import { useState, useEffect } from "react";
import { Calendar, Clock, MapPin, RefreshCw, User, X, AlertCircle } from "lucide-react";
import SlotPicker from "../appointments/SlotPicker";
import { doctorAppointmentApi } from "../../api/doctorAppointmentApi";
import { formatDay, serviceLabel, timeLabel, todayInVietnam } from "../../utils/appointments";

export default function DoctorRescheduleModal({ isOpen, appointment, onClose, onSuccess }) {
  const [selectedDate, setSelectedDate] = useState(() => todayInVietnam());
  const [shifts, setShifts] = useState([]);
  const [loadingShifts, setLoadingShifts] = useState(true);
  const [selectedShiftId, setSelectedShiftId] = useState(null);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // Tải danh sách ca khám khả dụng theo ngày chọn
  useEffect(() => {
    let active = true;
    const serviceType = appointment?.shiftType || appointment?.shift_type;
    doctorAppointmentApi
      .getAvailableShifts({ date: selectedDate, serviceType })
      .then((available) => {
        if (!active) return;
        const normalized = available.map((s) => ({
          ...s,
          id: s.id,
          startTime: s.startTime || s.start_time,
          endTime: s.endTime || s.end_time,
          shiftDate: s.shiftDate || s.shift_date,
          room: s.room || "Trạm Y tế",
          availableSlots:
            s.availableSlots !== undefined
              ? s.availableSlots
              : Math.max(0, (s.maxSlots || s.max_slots || 0) - (s.bookedSlots || s.booked_slots || 0)),
          maxSlots: s.maxSlots || s.max_slots || 4,
          bookedSlots: s.bookedSlots || s.booked_slots || 0,
          status: s.status || "OPEN",
          doctorName: s.doctorName || s.doctor_name,
        }));
        setShifts(normalized);
      })
      .catch((cause) => {
        if (active) {
          setError(cause?.message || "Không thể tải danh sách ca khám khả dụng. Vui lòng thử lại.");
        }
      })
      .finally(() => {
        if (active) setLoadingShifts(false);
      });

    return () => {
      active = false;
    };
  }, [selectedDate, appointment]);

  const handleDateChange = (e) => {
    const newDate = e.target.value;
    setSelectedDate(newDate);
    setSelectedShiftId(null);
  };

  const handleReloadShifts = () => {
    setLoadingShifts(true);
    setError("");
    const serviceType = appointment?.shiftType || appointment?.shift_type;
    doctorAppointmentApi
      .getAvailableShifts({ date: selectedDate, serviceType })
      .then((available) => {
        const normalized = available.map((s) => ({
          ...s,
          id: s.id,
          startTime: s.startTime || s.start_time,
          endTime: s.endTime || s.end_time,
          shiftDate: s.shiftDate || s.shift_date,
          room: s.room || "Trạm Y tế",
          availableSlots:
            s.availableSlots !== undefined
              ? s.availableSlots
              : Math.max(0, (s.maxSlots || s.max_slots || 0) - (s.bookedSlots || s.booked_slots || 0)),
          maxSlots: s.maxSlots || s.max_slots || 4,
          bookedSlots: s.bookedSlots || s.booked_slots || 0,
          status: s.status || "OPEN",
          doctorName: s.doctorName || s.doctor_name,
        }));
        setShifts(normalized);
      })
      .catch((cause) => {
        setError(cause?.message || "Không thể tải danh sách ca khám khả dụng. Vui lòng thử lại.");
      })
      .finally(() => {
        setLoadingShifts(false);
      });
  };

  const handleClose = () => {
    if (submitting) return;
    setReason("");
    setError("");
    setSelectedShiftId(null);
    onClose();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedShiftId) {
      setError("Vui lòng chọn một ca khám mới từ danh sách khung giờ trống.");
      return;
    }
    const trimmed = reason.trim();
    if (!trimmed) {
      setError("Vui lòng nhập lý do dời lịch hẹn.");
      return;
    }
    if (trimmed.length > 500) {
      setError("Lý do dời lịch không được vượt quá 500 ký tự.");
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      const updated = await doctorAppointmentApi.rescheduleAppointment(
        appointment.id,
        selectedShiftId,
        trimmed
      );
      setReason("");
      setSelectedShiftId(null);
      onSuccess(updated);
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || "Không thể dời lịch hẹn. Vui lòng thử lại.");
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen || !appointment) return null;

  const studentName = appointment.studentName || appointment.full_name || "Sinh viên";
  const studentCode = appointment.studentCode || appointment.student_code || "";
  const appointmentCode = appointment.appointmentCode || appointment.appointment_code || `#${appointment.id}`;
  const currentShiftDate = appointment.shiftDate || appointment.shift_date;
  const currentStartTime = appointment.startTime || appointment.start_time;
  const currentEndTime = appointment.endTime || appointment.end_time;
  const currentRoom = appointment.room || "Trạm Y tế";
  const shiftType = appointment.shiftType || appointment.shift_type;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fadeIn"
      role="dialog"
      aria-modal="true"
      aria-labelledby="reschedule-modal-title"
    >
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-xl max-h-[92vh] flex flex-col overflow-hidden border border-slate-100 transition-all">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-blue-50/50">
          <div className="flex items-center gap-2.5 text-blue-800">
            <div className="p-2 bg-blue-100 text-blue-700 rounded-xl">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h2 id="reschedule-modal-title" className="text-lg font-bold text-slate-800">
                Dời Lịch Hẹn Ca Khám
              </h2>
              <p className="text-xs text-blue-600 font-medium">
                Chọn ca khám mới còn trống (status = OPEN & booked_slots &lt; max_slots)
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleClose}
            disabled={submitting}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-100 transition"
            aria-label="Đóng"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Body */}
        <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-5 flex-1">
          {/* Thông tin ca hiện tại */}
          <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/80 text-xs space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-slate-500 uppercase tracking-wider text-[11px]">
                Ca hiện tại cần dời:
              </span>
              <span className="font-mono font-bold text-blue-700">{appointmentCode}</span>
            </div>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-slate-700">
              <span className="flex items-center gap-1 font-medium">
                <User className="w-3.5 h-3.5 text-slate-400" />
                {studentName} {studentCode && `(${studentCode})`}
              </span>
              <span className="flex items-center gap-1 text-slate-600">
                <Clock className="w-3.5 h-3.5 text-slate-400" />
                {formatDay(currentShiftDate)} · {timeLabel(currentStartTime)} – {timeLabel(currentEndTime)}
              </span>
              <span className="flex items-center gap-1 text-slate-600">
                <MapPin className="w-3.5 h-3.5 text-slate-400" />
                {currentRoom}
              </span>
              <span className="px-2 py-0.5 bg-blue-100 text-blue-800 rounded font-medium text-[11px] ml-auto">
                {serviceLabel(shiftType)}
              </span>
            </div>
          </div>

          {/* Chọn ngày khám mới */}
          <div className="space-y-1.5">
            <label htmlFor="new-shift-date" className="text-sm font-semibold text-slate-700 block">
              1. Chọn ngày khám mới <span className="text-rose-500">*</span>
            </label>
            <div className="flex gap-2">
              <input
                id="new-shift-date"
                type="date"
                min={todayInVietnam()}
                value={selectedDate}
                onChange={handleDateChange}
                disabled={submitting}
                className="w-full px-3.5 py-2.5 text-sm rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
              <button
                type="button"
                onClick={handleReloadShifts}
                disabled={loadingShifts || submitting}
                className="px-3 py-2 text-xs font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-xl transition flex items-center gap-1.5"
                title="Tải lại danh sách ca"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loadingShifts ? "animate-spin" : ""}`} />
                Làm mới
              </button>
            </div>
          </div>

          {/* Danh sách khung giờ khám (SlotPicker) */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-sm font-semibold text-slate-700 block">
                2. Chọn khung giờ khám mới <span className="text-rose-500">*</span>
              </label>
              {selectedShiftId && (
                <span className="text-xs font-semibold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
                  Đã chọn 1 ca khám
                </span>
              )}
            </div>

            {loadingShifts ? (
              <div className="p-8 text-center text-slate-500 text-sm bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <RefreshCw className="w-5 h-5 mx-auto mb-2 animate-spin text-blue-600" />
                Đang tìm các ca khám khả dụng...
              </div>
            ) : shifts.length === 0 ? (
              <div className="p-6 text-center text-slate-500 text-sm bg-amber-50/60 rounded-xl border border-amber-200/60 space-y-1">
                <AlertCircle className="w-5 h-5 mx-auto text-amber-500" />
                <p className="font-semibold text-slate-700">Không có ca khám trống trong ngày này</p>
                <p className="text-xs text-slate-500">
                  Vui lòng chọn ngày khác hoặc mở thêm ca mới trong mục Quản lý ca trực.
                </p>
              </div>
            ) : (
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200">
                <SlotPicker
                  slots={shifts}
                  value={selectedShiftId}
                  onChange={(slot) => {
                    setSelectedShiftId(slot.id);
                    if (error) setError("");
                  }}
                  excludedAppointmentId={appointment.id}
                  excludedShiftId={appointment.shiftId || appointment.shift_id}
                  disabled={submitting}
                  showDoctor={true}
                  label={`Các ca khám còn trống ngày ${formatDay(selectedDate)}:`}
                />
              </div>
            )}
          </div>

          {/* Lý do dời lịch */}
          <div className="space-y-1.5">
            <div className="flex justify-between items-center">
              <label htmlFor="reschedule-reason" className="text-sm font-semibold text-slate-700">
                3. Lý do dời lịch hẹn <span className="text-rose-500">*</span>
              </label>
              <span className="text-xs text-slate-400">{reason.length}/500</span>
            </div>
            <textarea
              id="reschedule-reason"
              rows={3}
              value={reason}
              maxLength={500}
              disabled={submitting}
              onChange={(e) => {
                setReason(e.target.value);
                if (error) setError("");
              }}
              placeholder="Nhập lý do bác sĩ dời lịch (điều chỉnh ca trực, yêu cầu của sinh viên, ...)"
              className={`w-full p-3 text-sm rounded-xl border ${
                error ? "border-rose-300 focus:ring-rose-500" : "border-slate-300 focus:ring-blue-500"
              } focus:outline-none focus:ring-2 focus:border-transparent transition resize-none`}
            />
          </div>

          {error && (
            <div className="p-3 bg-rose-50 rounded-xl border border-rose-200 text-rose-700 text-xs flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Footer buttons */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={handleClose}
              disabled={submitting}
              className="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-xl transition disabled:opacity-50"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting || !selectedShiftId}
              className="px-5 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 rounded-xl transition shadow-sm disabled:opacity-50 flex items-center gap-1.5"
            >
              {submitting ? "Đang cập nhật..." : "Xác nhận dời lịch"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
