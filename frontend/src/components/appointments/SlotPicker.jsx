import { LockKeyhole } from "lucide-react";
import { slotUnavailableReason, timeLabel } from "../../utils/appointments";
import "./SlotPicker.css";

/**
 * Shared by student booking, rescheduling and doctor screens.
 * slots: normalized ShiftResponse[]; value: shift ID; onChange: (shift) => void.
 * appointments are the target student's appointments, never the doctor's entire list.
 * Pass excludedAppointmentId/excludedShiftId when rescheduling.
 */
export default function SlotPicker({ slots, value, onChange, appointments = [], excludedAppointmentId, excludedShiftId, disabled = false, showDoctor = false, compact = false, now = new Date(), label = "Khung giờ khám" }) {
  return <fieldset className={`slot-picker${compact ? " is-compact" : ""}`} disabled={disabled}>
    <legend>{label}</legend>
    {!slots.length && <p className="slot-empty">Chưa có ca khám trong ngày này. Hãy chọn ngày hoặc bác sĩ khác.</p>}
    <div className="slot-grid">
      {slots.map((slot) => {
        const reason = slotUnavailableReason(slot, appointments, { excludedAppointmentId, excludedShiftId, now });
        const selected = String(value) === String(slot.id);
        return <button key={slot.id} type="button" className={`slot-choice${selected ? " is-selected" : ""}`} disabled={Boolean(reason)} aria-pressed={selected} onClick={() => onChange(slot)} title={`${timeLabel(slot.startTime)} – ${timeLabel(slot.endTime)} · ${slot.room || "Trạm y tế"} · ${reason || `Còn ${slot.availableSlots} chỗ`}`} aria-label={`${timeLabel(slot.startTime)} đến ${timeLabel(slot.endTime)}, ${slot.room || "chưa có phòng"}, ${reason || `còn ${slot.availableSlots} chỗ`}`}>
          <span>{timeLabel(slot.startTime)}{!compact && <> – {timeLabel(slot.endTime)}</>}</span>
          {!compact && <small>{reason ? <><LockKeyhole size={12} aria-hidden="true" />{reason}</> : `Còn ${slot.availableSlots}/${slot.maxSlots} chỗ`}</small>}
          {compact && reason && <LockKeyhole size={12} aria-hidden="true" />}
          {!compact && slot.room && <small>{slot.room}</small>}
          {showDoctor && slot.doctorName && <small>{slot.doctorName}</small>}
        </button>;
      })}
    </div>
  </fieldset>;
}
