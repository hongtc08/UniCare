import { useCallback, useRef, useState } from "react";
import Modal from "../Modal";
import SlotPicker from "./SlotPicker";
import { appointmentApi } from "../../api/appointmentApi";
import { useAppointmentQuery } from "../../hooks/useAppointmentQuery";
import { appointmentError, formatDay, requiredReason, serviceLabel, slotUnavailableReason, timeLabel, todayInVietnam } from "../../utils/appointments";

export default function AppointmentActionModal({ action, appointment, appointments, onClose, onSaved }) {
  const rescheduling = action === "reschedule";
  const [date, setDate] = useState(appointment.shiftDate >= todayInVietnam() ? appointment.shiftDate : todayInVietnam());
  const [doctorId, setDoctorId] = useState("");
  const [slotId, setSlotId] = useState(null);
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const submitting = useRef(false);
  const serviceType = appointment.shiftType || appointment.serviceType;
  const request = useCallback((signal) => rescheduling && date ? appointmentApi.shifts({ date, serviceType }, signal) : Promise.resolve([]), [date, rescheduling, serviceType]);
  const shifts = useAppointmentQuery(request);
  const doctors = [...new Map(shifts.data.map((slot) => [String(slot.doctorId), slot.doctorName || "Nhân viên y tế"])).entries()];
  const slots = shifts.data.filter((slot) => !doctorId || String(slot.doctorId) === doctorId).sort((a, b) => a.startTime.localeCompare(b.startTime));
  const selected = slots.find((slot) => String(slot.id) === String(slotId));

  async function submit(event) {
    event.preventDefault();
    if (submitting.current) return;
    const invalidReason = requiredReason(reason);
    if (invalidReason) { setError(invalidReason); return; }
    if (rescheduling && (!selected || slotUnavailableReason(selected, appointments, { excludedAppointmentId: appointment.id, excludedShiftId: appointment.shiftId }))) {
      setError("Vui lòng chọn ca khám mới còn chỗ và không trùng lịch."); return;
    }
    submitting.current = true; setBusy(true); setError("");
    try {
      if (rescheduling) await appointmentApi.reschedule(appointment.id, selected.id, reason);
      else await appointmentApi.cancel(appointment.id, reason);
      onSaved(rescheduling ? "Đã dời lịch hẹn. Vui lòng kiểm tra ngày và giờ mới." : "Đã hủy lịch hẹn.");
    } catch (cause) {
      setError(appointmentError(cause, "Không cập nhật được lịch hẹn. Vui lòng thử lại."));
      if (rescheduling) shifts.reload();
    } finally { submitting.current = false; setBusy(false); }
  }

  return <Modal isOpen onClose={onClose} busy={busy} title={rescheduling ? "Dời lịch hẹn" : "Hủy lịch hẹn"} className="appointment-action-modal">
    <form onSubmit={submit} className="appointment-action-form">
      <div className="appointment-current"><strong>{appointment.appointmentCode || `#${appointment.id}`} · {serviceLabel(serviceType)}</strong><p>{formatDay(appointment.shiftDate)} · {timeLabel(appointment.startTime)} – {timeLabel(appointment.endTime)}</p><p>{appointment.doctorName} · {appointment.room || "Trạm y tế"}</p></div>
      {error && <p className="feedback feedback-error" role="alert">{error}</p>}
      {rescheduling && <>
        <div className="reschedule-filters"><label className="form-field"><span>Ngày khám mới</span><input type="date" required min={todayInVietnam()} value={date} disabled={busy} onChange={(event) => { setDate(event.target.value); setSlotId(null); setDoctorId(""); }} /></label><label className="form-field"><span>Bác sĩ</span><select value={doctorId} disabled={busy || shifts.loading} onChange={(event) => { setDoctorId(event.target.value); setSlotId(null); }}><option value="">Tất cả bác sĩ</option>{doctors.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></label></div>
        {shifts.loading ? <p role="status">Đang tải ca khám mới…</p> : shifts.error ? <div className="appointment-feedback" role="alert"><p>{shifts.error}</p><button type="button" className="button secondary" onClick={shifts.reload}>Thử lại</button></div> : <SlotPicker slots={slots} value={slotId} onChange={(slot) => setSlotId(slot.id)} appointments={appointments} excludedAppointmentId={appointment.id} excludedShiftId={appointment.shiftId} disabled={busy} showDoctor={!doctorId} label="Chọn khung giờ mới" />}
      </>}
      <label className="form-field"><span>Lý do {rescheduling ? "dời" : "hủy"} lịch <span className="required">*</span></span><textarea rows={3} required maxLength={500} value={reason} disabled={busy} onChange={(event) => setReason(event.target.value)} placeholder={rescheduling ? "Vì sao bạn muốn đổi lịch hẹn?" : "Vì sao bạn cần hủy lịch hẹn?"} aria-describedby="appointment-reason-hint" /></label>
      <p id="appointment-reason-hint" className="appointment-muted">Lý do sẽ được gửi tới nhân viên y tế. Tối đa 500 ký tự.</p>
      <div className="dialog-actions"><button type="button" className="button secondary" disabled={busy} onClick={onClose}>Giữ lịch hiện tại</button><button type="submit" className={`button ${rescheduling ? "primary" : "appointment-danger"}`} disabled={busy || (rescheduling && (shifts.loading || Boolean(shifts.error) || !selected))}>{busy ? "Đang cập nhật…" : rescheduling ? "Xác nhận dời lịch" : "Xác nhận hủy lịch"}</button></div>
    </form>
  </Modal>;
}
