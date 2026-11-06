import { ArrowLeft, ArrowRight, CalendarDays, ShieldCheck } from "lucide-react";
import SlotPicker from "./SlotPicker";
import { formatDay, SERVICES, serviceLabel, slotUnavailableReason, timeLabel } from "../../utils/appointments";
import "./BookingSummary.css";

export default function BookingSummary({ step, date, service, onServiceChange, doctor, slots, slotId, onSlotChange, selected, appointments, ready, now, busy, unavailable, symptoms, onSymptomsChange, canContinue, onNext, onBack }) {
  const name = selected?.doctorName || doctor?.name;
  const initials = name?.replace(/^(BS\.|CV\.)\s*/i, "").split(/\s+/).filter(Boolean).slice(-2).map((part) => part[0]).join("");
  const availableCount = slots.filter((slot) => !slotUnavailableReason(slot, appointments, { now })).length;
  return <aside className="booking-panel booking-summary">
    <header className="booking-summary-heading"><h3>Chi tiết lịch khám</h3><p>{formatDay(date, { weekday: "long" })}</p></header>
    {name ? <div className="summary-doctor"><span className="summary-doctor-avatar" aria-hidden="true">{initials}</span><div><strong>{name}</strong><small>{service === "TU_VAN_TAM_LY" ? "Chuyên viên tâm lý" : "Bác sĩ đa khoa"}</small></div></div> : <p className="summary-doctor-placeholder">Chọn nhân viên y tế để xem chi tiết lịch khám.</p>}
    <label className="form-field summary-service"><span>Loại lịch khám</span><select value={service} disabled={busy} onChange={(event) => onServiceChange(event.target.value)}>{SERVICES.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
    {step === 1 && <div className="summary-slots"><div className="summary-slots-heading"><span>Khung giờ còn trống</span>{name && ready && <strong>Còn {availableCount} khung giờ</strong>}</div>{name ? <SlotPicker slots={slots} value={slotId} onChange={onSlotChange} appointments={appointments} disabled={!ready || busy} now={now} compact label="Chọn khung giờ khám" /> : <p className="appointment-muted">Chọn bác sĩ để xem các khung giờ trong ngày.</p>}</div>}
    {selected && <p className="summary-selected-slot" role="status">{timeLabel(selected.startTime)} – {timeLabel(selected.endTime)} · {selected.room || "Trạm y tế"}</p>}
    {step < 2 && <label className="form-field summary-notes"><span>Ghi chú cho nhân viên y tế</span><textarea rows={4} maxLength={1000} value={symptoms} onChange={(event) => onSymptomsChange(event.target.value)} placeholder="Mô tả ngắn gọn vấn đề của bạn (không bắt buộc)" /></label>}
    {step === 2 && <div className="summary-final-details"><span>Dịch vụ: {serviceLabel(service)}</span>{unavailable && <p role="alert" className="feedback feedback-error">{unavailable}. Hãy quay lại để chọn ca khám.</p>}</div>}
    <div className="booking-summary-actions">
      {step < 2 ? <button key="next-step" type="button" className="button primary" disabled={!canContinue} onClick={(event) => { event.preventDefault(); onNext(); }}>Tiếp tục <ArrowRight size={16} /></button> : <button key="confirm-booking" type="submit" form="confirm-booking" className="button primary" disabled={busy || !ready || Boolean(unavailable)}><CalendarDays size={18} />{busy ? "Đang đặt lịch…" : "Xác nhận lịch khám"}</button>}
      {step > 0 && <button type="button" className="button secondary" disabled={busy} onClick={onBack}><ArrowLeft size={15} />Quay lại</button>}
    </div>
    <p className="booking-privacy"><ShieldCheck size={17} aria-hidden="true" />Thông tin sức khỏe của bạn được bảo mật an toàn.</p>
  </aside>;
}
