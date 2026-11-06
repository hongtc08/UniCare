import { useState } from "react";
import { Link } from "react-router-dom";
import { CalendarDays, CalendarPlus, Clock3, MapPin, RefreshCw } from "lucide-react";
import { appointmentApi } from "../../api/appointmentApi";
import { useAppointmentQuery } from "../../hooks/useAppointmentQuery";
import AppointmentActionModal from "../../components/appointments/AppointmentActionModal";
import { APPOINTMENT_STATUSES, canChangeAppointment, formatDay, normalizeStatus, serviceLabel, timeLabel } from "../../utils/appointments";
import "./StudentAppointments.css";

export default function MyAppointments() {
  const query = useAppointmentQuery(appointmentApi.mine);
  const [status, setStatus] = useState("ALL");
  const [action, setAction] = useState(null);
  const [message, setMessage] = useState("");
  const rows = query.data.filter((item) => status === "ALL" || normalizeStatus(item.status) === status)
    .sort((a, b) => `${b.shiftDate}T${b.startTime}`.localeCompare(`${a.shiftDate}T${a.startTime}`));
  const filters = [["ALL", "Tất cả"], ...Object.entries(APPOINTMENT_STATUSES)];
  function saved(text) { setAction(null); setMessage(text); query.reload(); }
  return <div className="appointments-page">
    <header className="appointment-heading"><div><h1>Lịch hẹn của tôi</h1><p>Theo dõi và quản lý lịch khám, tư vấn tại trạm y tế.</p></div><Link to="/lich-kham" className="button primary"><CalendarPlus size={17} />Đặt lịch mới</Link></header>
    {message && <p role="status" className="feedback feedback-success">{message}</p>}
    <section className="booking-panel appointments-list" aria-label="Danh sách lịch hẹn">
      <div className="appointments-toolbar"><div className="appointment-filters" role="group" aria-label="Lọc trạng thái lịch hẹn">{filters.map(([key, label]) => <button type="button" key={key} aria-pressed={status === key} className={status === key ? "is-active" : ""} onClick={() => setStatus(key)}>{label}<span>{query.loading || query.error ? "—" : query.data.filter((row) => key === "ALL" || normalizeStatus(row.status) === key).length}</span></button>)}</div><button type="button" className="icon-button" disabled={query.loading} onClick={query.reload} aria-label="Tải lại lịch hẹn"><RefreshCw size={17} /></button></div>
      {query.loading ? <div className="appointments-empty" role="status"><p>Đang tải lịch hẹn của bạn…</p></div> : query.error ? <div className="appointments-empty" role="alert"><h2>Chưa tải được lịch hẹn</h2><p>{query.error}</p><button type="button" className="button secondary" onClick={query.reload}>Thử lại</button></div> : !rows.length ? <div className="appointments-empty"><CalendarDays size={36} aria-hidden="true" /><h2>{status === "ALL" ? "Bạn chưa có lịch hẹn" : "Chưa có lịch hẹn ở trạng thái này"}</h2><p>{status === "ALL" ? "Chọn ngày, bác sĩ và khung giờ phù hợp để bắt đầu." : "Chọn trạng thái khác hoặc đặt một lịch hẹn mới."}</p><Link to="/lich-kham" className="button secondary">Đặt lịch khám</Link></div> : <ul className="appointment-records">{rows.map((appointment) => {
        const normalized = normalizeStatus(appointment.status);
        const changeable = canChangeAppointment(appointment);
        return <li key={appointment.id} className="appointment-record">
          <div className="appointment-date"><strong>{appointment.shiftDate ? Number(appointment.shiftDate.slice(-2)) : "—"}</strong><span>{appointment.shiftDate ? `Tháng ${Number(appointment.shiftDate.slice(5, 7))}, ${appointment.shiftDate.slice(0, 4)}` : "Chưa có ngày"}</span></div>
          <div className="appointment-main"><div className="appointment-record-heading"><h2>{serviceLabel(appointment.shiftType || appointment.serviceType)}</h2><span className={`appointment-status is-${normalized?.toLowerCase()}`}>{APPOINTMENT_STATUSES[normalized] || appointment.status}</span></div><p className="appointment-code">Mã lịch hẹn: <strong>{appointment.appointmentCode || `#${appointment.id}`}</strong></p><p>{appointment.doctorName || "Nhân viên y tế"}</p><div className="appointment-meta"><span><Clock3 size={15} aria-hidden="true" />{timeLabel(appointment.startTime)} – {timeLabel(appointment.endTime)}</span><span><MapPin size={15} aria-hidden="true" />{appointment.room || "Trạm y tế"}</span><time dateTime={appointment.shiftDate}>{formatDay(appointment.shiftDate, { weekday: "long" })}</time></div>{appointment.reasonSymptoms && <p className="appointment-note"><strong>Lý do khám:</strong> {appointment.reasonSymptoms}</p>}{appointment.cancelRescheduleReason && <p className="appointment-note"><strong>Lý do cập nhật:</strong> {appointment.cancelRescheduleReason}</p>}</div>
          <div className="appointment-row-actions">{changeable ? <><button type="button" className="button secondary" onClick={() => { setMessage(""); setAction({ type: "reschedule", appointment }); }}>Dời lịch</button><button type="button" className="appointment-cancel-link" onClick={() => { setMessage(""); setAction({ type: "cancel", appointment }); }}>Hủy hẹn</button></> : <span className="appointment-muted">{normalized === "CONFIRMED" ? "Đã đến giờ khám" : "Không còn thao tác"}</span>}</div>
        </li>;
      })}</ul>}
    </section>
    <p className="appointments-footnote">Bạn có thể hủy hoặc dời lịch đã xác nhận trước giờ khám. Khi dời lịch, dịch vụ được giữ nguyên.</p>
    {action && <AppointmentActionModal key={`${action.type}-${action.appointment.id}`} action={action.type} appointment={action.appointment} appointments={query.data} onClose={() => setAction(null)} onSaved={saved} />}
  </div>;
}
