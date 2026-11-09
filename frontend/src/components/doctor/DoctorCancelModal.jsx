import { useState } from "react";
import { AlertTriangle, Clock, MapPin, User, X } from "lucide-react";
import { doctorAppointmentApi } from "../../api/doctorAppointmentApi";
import { formatDay, serviceLabel, timeLabel } from "../../utils/appointments";

export default function DoctorCancelModal({ isOpen, appointment, onClose, onSuccess }) {
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (!isOpen || !appointment) return null;

  const handleClose = () => {
    if (submitting) return;
    setReason("");
    setError("");
    onClose();
  };

  const handleConfirm = async (e) => {
    e.preventDefault();
    const trimmed = reason.trim();
    if (!trimmed) {
      setError("Vui lòng nhập lý do hủy lịch hẹn.");
      return;
    }
    if (trimmed.length > 500) {
      setError("Lý do hủy không được vượt quá 500 ký tự.");
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      const updated = await doctorAppointmentApi.cancelAppointment(appointment.id, trimmed);
      setReason("");
      onSuccess(updated);
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || "Không thể hủy lịch hẹn. Vui lòng thử lại.");
    } finally {
      setSubmitting(false);
    }
  };

  const studentName = appointment.studentName || appointment.full_name || "Sinh viên";
  const studentCode = appointment.studentCode || appointment.student_code || "";
  const appointmentCode = appointment.appointmentCode || appointment.appointment_code || `#${appointment.id}`;
  const shiftDate = appointment.shiftDate || appointment.shift_date;
  const startTime = appointment.startTime || appointment.start_time;
  const endTime = appointment.endTime || appointment.end_time;
  const room = appointment.room || "Trạm Y tế Cơ sở 1";
  const shiftType = appointment.shiftType || appointment.shift_type;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fadeIn"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cancel-modal-title"
    >
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-slate-100 transition-all">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-rose-50/50">
          <div className="flex items-center gap-2.5 text-rose-700">
            <div className="p-2 bg-rose-100 rounded-xl">
              <AlertTriangle className="w-5 h-5 text-rose-600" />
            </div>
            <div>
              <h2 id="cancel-modal-title" className="text-lg font-bold text-slate-800">
                Xác nhận Hủy Lịch Khám
              </h2>
              <p className="text-xs text-rose-600 font-medium">Thao tác này sẽ thông báo ngay đến sinh viên</p>
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

        {/* Content */}
        <form onSubmit={handleConfirm} className="p-6 space-y-4">
          {/* Thông tin tóm tắt ca hẹn */}
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200/80 space-y-2.5 text-sm">
            <div className="flex items-center justify-between pb-2 border-b border-slate-200/60">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">Mã ca khám</span>
              <span className="font-mono font-bold text-blue-700">{appointmentCode}</span>
            </div>

            <div className="flex items-center gap-2 text-slate-700">
              <User className="w-4 h-4 text-slate-400 flex-shrink-0" />
              <span className="font-semibold">{studentName}</span>
              {studentCode && <span className="text-slate-500 font-mono text-xs">({studentCode})</span>}
            </div>

            <div className="flex items-center gap-2 text-slate-600 text-xs">
              <Clock className="w-4 h-4 text-slate-400 flex-shrink-0" />
              <span>
                {formatDay(shiftDate)} · {timeLabel(startTime)} – {timeLabel(endTime)}
              </span>
            </div>

            <div className="flex items-center gap-2 text-slate-600 text-xs">
              <MapPin className="w-4 h-4 text-slate-400 flex-shrink-0" />
              <span>{room}</span>
              <span className="px-2 py-0.5 bg-blue-100 text-blue-800 rounded font-medium text-[11px] ml-auto">
                {serviceLabel(shiftType)}
              </span>
            </div>
          </div>

          {/* Ô nhập lý do hủy */}
          <div className="space-y-1.5">
            <div className="flex justify-between items-center">
              <label htmlFor="cancel-reason" className="text-sm font-semibold text-slate-700">
                Lý do hủy lịch khám <span className="text-rose-500">*</span>
              </label>
              <span className="text-xs text-slate-400">{reason.length}/500</span>
            </div>
            <textarea
              id="cancel-reason"
              rows={4}
              value={reason}
              maxLength={500}
              disabled={submitting}
              onChange={(e) => {
                setReason(e.target.value);
                if (error) setError("");
              }}
              placeholder="Vui lòng nêu rõ lý do hủy (bác sĩ bận việc khẩn cấp, ca cấp cứu, thiết bị y tế bảo trì, ...)"
              className={`w-full p-3 text-sm rounded-xl border ${
                error ? "border-rose-300 focus:ring-rose-500" : "border-slate-300 focus:ring-blue-500"
              } focus:outline-none focus:ring-2 focus:border-transparent transition resize-none`}
            />
            {error && <p className="text-xs text-rose-600 font-medium">{error}</p>}
          </div>

          <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-amber-800 text-xs flex items-start gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
            <span>
              Lịch khám sau khi hủy sẽ chuyển sang trạng thái <strong>CANCELED</strong> và ghi nhận người hủy là{" "}
              <strong>DOCTOR</strong>.
            </span>
          </div>

          {/* Buttons */}
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
              disabled={submitting}
              className="px-5 py-2 text-sm font-semibold text-white bg-rose-600 hover:bg-rose-700 active:bg-rose-800 rounded-xl transition shadow-sm disabled:opacity-50 flex items-center gap-1.5"
            >
              {submitting ? "Đang xử lý..." : "Xác nhận hủy lịch"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
