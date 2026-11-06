import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, CalendarDays, Check, CheckCircle2, ShieldCheck } from "lucide-react";
import { appointmentApi } from "../../api/appointmentApi";
import { useAppointmentQuery } from "../../hooks/useAppointmentQuery";
import BookingCalendar from "../../components/appointments/BookingCalendar";
import BookingSummary from "../../components/appointments/BookingSummary";
import DoctorFinder from "../../components/appointments/DoctorFinder";
import { appointmentError, filterBookingShifts, formatDay, monthRange, SERVICES, serviceLabel, slotUnavailableReason, timeLabel, todayInVietnam } from "../../utils/appointments";
import "./StudentAppointments.css";

const STEPS = ["Ngày & dịch vụ", "Bác sĩ & khung giờ", "Xác nhận lịch hẹn"];

export default function StudentBooking() {
  const [step, setStep] = useState(0);
  const [date, setDate] = useState(todayInVietnam);
  const [month, setMonth] = useState(() => todayInVietnam().slice(0, 7));
  const [service, setService] = useState(SERVICES[0].value);
  const [doctorId, setDoctorId] = useState("");
  const [facilityId, setFacilityId] = useState("");
  const [onlyAvailable, setOnlyAvailable] = useState(true);
  const [slotId, setSlotId] = useState(null);
  const [symptoms, setSymptoms] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [created, setCreated] = useState(null);
  const [now, setNow] = useState(() => new Date());
  const title = useRef(null);
  const submitting = useRef(false);
  const loadShifts = useCallback((signal) => appointmentApi.shifts({ ...monthRange(month), serviceType: service, ...(facilityId ? { facilityId } : {}) }, signal), [month, service, facilityId]);
  const shifts = useAppointmentQuery(loadShifts);
  const appointments = useAppointmentQuery(appointmentApi.mine);
  const facilities = useAppointmentQuery(appointmentApi.facilities);
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 30000);
    return () => clearInterval(timer);
  }, []);
  const filteredShifts = filterBookingShifts(shifts.data, { facilityId, onlyAvailable }, appointments.data, now);
  const dayShifts = filteredShifts.filter((slot) => slot.shiftDate === date);
  const doctors = [...new Map(dayShifts.map((slot) => [String(slot.doctorId), { id: String(slot.doctorId), name: slot.doctorName || "Nhân viên y tế", count: dayShifts.filter((item) => String(item.doctorId) === String(slot.doctorId) && !slotUnavailableReason(item, appointments.data, { now })).length }])).values()];
  const doctorSlots = dayShifts.filter((slot) => String(slot.doctorId) === doctorId).sort((a, b) => a.startTime.localeCompare(b.startTime));
  const selected = doctorSlots.find((slot) => String(slot.id) === String(slotId));
  const unavailable = selected ? slotUnavailableReason(selected, appointments.data, { now }) : "Chưa chọn khung giờ";
  const dataReady = !shifts.loading && !shifts.error && !appointments.loading && !appointments.error;

  function changeDate(value) { setDate(value); setDoctorId(""); setSlotId(null); setError(""); }
  function changeService(value) { setStep(0); setService(value); setDoctorId(""); setSlotId(null); setError(""); }
  function goToStep(value) { setStep(value); setError(""); requestAnimationFrame(() => title.current?.focus()); }

  async function confirm(event) {
    event.preventDefault();
    if (submitting.current) return;
    if (!selected || unavailable || !dataReady) { setError("Khung giờ không còn khả dụng. Hãy quay lại bước 2 để chọn lại."); return; }
    submitting.current = true;
    setBusy(true); setError("");
    try {
      const result = await appointmentApi.create(selected.id, symptoms);
      setCreated(result);
    } catch (cause) {
      setError(appointmentError(cause, "Không đặt được lịch hẹn. Vui lòng thử lại."));
      shifts.reload(); appointments.reload();
    } finally { submitting.current = false; setBusy(false); }
  }

  if (created) return <div className="appointments-page"><section className="booking-success" role="status">
    <CheckCircle2 size={48} aria-hidden="true" /><h1>Đã đặt lịch hẹn</h1><p>Lịch khám của bạn đã được xác nhận.</p>
    <strong className="confirmation-code">{created.appointmentCode || `#${created.id}`}</strong>
    <p>{formatDay(created.shiftDate || date)} · {timeLabel(created.startTime || selected?.startTime)} · {created.doctorName || selected?.doctorName}</p>
    <Link className="button primary" to="/lich-hen-cua-toi">Xem lịch hẹn của tôi <ArrowRight size={16} /></Link>
  </section></div>;

  return <div className="appointments-page">
    <header className="appointment-heading"><div><h1>Đặt lịch khám</h1><p>Chọn lịch phù hợp để được chăm sóc tại trạm y tế.</p></div><Link className="button secondary" to="/lich-hen-cua-toi"><CalendarDays size={16} />Lịch hẹn của tôi</Link></header>
    <ol className="booking-steps" aria-label="Tiến trình đặt lịch">
      {STEPS.map((label, index) => <li key={label} className={index === step ? "is-current" : index < step ? "is-complete" : ""} aria-current={step === index ? "step" : undefined}>
        <span className="step-number">{index < step ? <Check size={16} aria-hidden="true" /> : index + 1}</span><span>{label}</span>
      </li>)}
    </ol>
    <h2 className="booking-step-title" ref={title} tabIndex={-1}>{STEPS[step]}</h2>
    {error && <p role="alert" className="feedback feedback-error">{error}</p>}
    {appointments.error && <div role="alert" className="appointment-feedback"><p>{appointments.error}</p><button className="button secondary" onClick={appointments.reload}>Tải lại lịch hẹn</button></div>}
    <div className={`booking-layout${step === 2 ? " is-review" : ""}`}>
      {step < 2 && <DoctorFinder
        service={service} onServiceChange={changeService} facilityId={facilityId} facilities={facilities.data}
        onFacilityChange={(value) => { setFacilityId(value); setDoctorId(""); setSlotId(null); setError(""); }}
        facilitiesLoading={facilities.loading} facilitiesError={facilities.error} onFacilitiesRetry={facilities.reload}
        onlyAvailable={onlyAvailable} onAvailabilityChange={(value) => { setOnlyAvailable(value); setDoctorId(""); setSlotId(null); setError(""); }}
        doctors={doctors} doctorId={doctorId} onDoctorChange={(value) => { setDoctorId(value); setSlotId(null); setError(""); }}
        loading={shifts.loading || appointments.loading} error={Boolean(shifts.error || appointments.error)}
      />}
      {step < 2 ? <div className="booking-panel calendar-panel">
        {shifts.loading && <p role="status" className="calendar-loading">Đang tải ca khám…</p>}
        {shifts.error && <div role="alert" className="appointment-feedback"><p>{shifts.error}</p><button className="button secondary" onClick={shifts.reload}>Thử lại</button></div>}
        <BookingCalendar month={month} onMonthChange={(value) => { setMonth(value); changeDate(""); }} value={date} onChange={changeDate} shifts={filteredShifts} appointments={appointments.data} />
        {!shifts.loading && !shifts.error && date && !dayShifts.length && <p className="calendar-empty" role="status">Không có ca {serviceLabel(service).toLowerCase()} phù hợp ngày {formatDay(date)}. Hãy đổi ngày hoặc bộ lọc.</p>}
      </div> : <form className="booking-panel booking-review" id="confirm-booking" onSubmit={confirm}>
        <h3>Thông tin trước buổi khám</h3><p className="appointment-muted">Mô tả ngắn để nhân viên y tế chuẩn bị cho buổi khám hoặc tư vấn của bạn.</p>
        <label className="form-field"><span>Triệu chứng / lý do khám (không bắt buộc)</span><textarea rows={6} maxLength={1000} disabled={busy} value={symptoms} onChange={(event) => setSymptoms(event.target.value)} placeholder="Bạn gặp vấn đề gì và bắt đầu từ khi nào?" aria-describedby="symptom-hint" /></label>
        <p id="symptom-hint" className="appointment-muted">{symptoms.length}/1000 ký tự. Nếu cần cấp cứu, hãy liên hệ trực tiếp trạm y tế.</p>
        <div className="booking-help"><ShieldCheck size={20} aria-hidden="true" /><p>Thông tin này được gửi cho nhân viên y tế phục vụ lịch hẹn của bạn.</p></div>
      </form>}
      <BookingSummary
        step={step} date={date} service={service} onServiceChange={changeService}
        doctor={doctors.find((doctor) => doctor.id === doctorId)} selected={selected}
        slots={doctorSlots} slotId={slotId} onSlotChange={(slot) => { setSlotId(slot.id); setError(""); }}
        appointments={appointments.data} ready={dataReady} now={now} busy={busy} unavailable={unavailable}
        symptoms={symptoms} onSymptomsChange={setSymptoms}
        canContinue={dataReady && (step === 0 ? Boolean(date && dayShifts.length) : Boolean(selected && !unavailable))}
        onNext={() => goToStep(step + 1)} onBack={() => goToStep(step - 1)}
      />
    </div>
  </div>;
}
