import { ChevronLeft, ChevronRight } from "lucide-react";
import { formatDay, monthRange, moveMonth, slotUnavailableReason, todayInVietnam } from "../../utils/appointments";

export default function BookingCalendar({ month, onMonthChange, value, onChange, shifts = [], appointments = [], disabled = false }) {
  const today = todayInVietnam();
  const [year, number] = month.split("-").map(Number);
  const offset = (new Date(year, number - 1, 1).getDay() + 6) % 7;
  const count = Number(monthRange(month).toDate.slice(-2));
  const cells = Array.from({ length: Math.ceil((offset + count) / 7) * 7 }, (_, index) => {
    const day = index - offset + 1;
    return day >= 1 && day <= count ? `${month}-${String(day).padStart(2, "0")}` : null;
  });
  return <section className="booking-calendar" aria-label="Chọn ngày khám">
    <div className="calendar-month">
      <button type="button" className="icon-button" aria-label="Tháng trước" disabled={disabled || month <= today.slice(0, 7)} onClick={() => onMonthChange(moveMonth(month, -1))}><ChevronLeft size={20} /></button>
      <h2>Tháng {number} năm {year}</h2>
      <button type="button" className="icon-button" aria-label="Tháng sau" disabled={disabled} onClick={() => onMonthChange(moveMonth(month, 1))}><ChevronRight size={20} /></button>
    </div>
    <div className="calendar-weekdays" aria-hidden="true">{["T2", "T3", "T4", "T5", "T6", "T7", "CN"].map((day) => <span key={day}>{day}</span>)}</div>
    <div className="calendar-days">
      {cells.map((date, index) => {
        if (!date) return <span key={`blank-${index}`} className="calendar-blank" />;
        const dayShifts = shifts.filter((slot) => slot.shiftDate === date);
        const reasons = dayShifts.map((slot) => slotUnavailableReason(slot, appointments));
        const available = reasons.some((reason) => !reason);
        const conflict = !available && reasons.includes("Trùng lịch");
        const state = available ? "available" : conflict ? "conflict" : "full";
        return <button type="button" key={date} className={`calendar-day${date === value ? " is-selected" : ""}${index % 7 > 4 ? " is-weekend" : ""}`} disabled={disabled || date < today} aria-pressed={date === value} aria-current={date === today ? "date" : undefined} aria-label={`${formatDay(date, { weekday: "long" })}${dayShifts.length ? `, ${available ? "còn chỗ" : conflict ? "trùng lịch" : "đã kín hoặc đóng ca"}` : ", chưa có ca khám"}`} onClick={() => onChange(date)}>
          <span>{Number(date.slice(-2))}</span>{dayShifts.length > 0 && <span className={`calendar-dot is-${state}`} aria-hidden="true" />}
        </button>;
      })}
    </div>
    <div className="calendar-legend"><span><i className="calendar-dot is-available" />Còn trống</span><span><i className="calendar-dot is-full" />Đã kín / đóng ca</span><span><i className="calendar-dot is-conflict" />Trùng lịch</span></div>
  </section>;
}
