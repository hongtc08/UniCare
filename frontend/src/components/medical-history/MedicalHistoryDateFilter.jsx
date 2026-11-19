import { useId } from "react";

export default function MedicalHistoryDateFilter({ from, to, onFromChange, onToChange }) {
  const id = useId();
  const invalid = from && to && from > to;

  return (
    <fieldset className="history-filters history-date-filter">
      <legend>Thời gian khám</legend>
      <label htmlFor={`${id}-from`}>Từ ngày</label>
      <input id={`${id}-from`} type="date" value={from} max={to || undefined}
        aria-invalid={Boolean(invalid)} aria-describedby={invalid ? `${id}-error` : undefined}
        onChange={(event) => onFromChange(event.target.value)} />
      <label htmlFor={`${id}-to`}>Đến ngày</label>
      <input id={`${id}-to`} type="date" value={to} min={from || undefined}
        aria-invalid={Boolean(invalid)} aria-describedby={invalid ? `${id}-error` : undefined}
        onChange={(event) => onToChange(event.target.value)} />
      {invalid && <p id={`${id}-error`} role="alert">Ngày kết thúc phải từ ngày bắt đầu trở đi.</p>}
      {(from || to) && <button type="button" className="history-action" onClick={() => {
        onFromChange("");
        onToChange("");
      }}>Xóa lọc thời gian</button>}
    </fieldset>
  );
}
