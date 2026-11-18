import { useState } from "react";
import { ClipboardList } from "lucide-react";
import { getStudentMedicalHistory } from "../../api/studentMedicalHistoryApi";
import MedicalHistoryDetailModal from "../../components/medical-history/MedicalHistoryDetailModal";
import MedicalHistoryFilters from "../../components/medical-history/MedicalHistoryFilters";
import MedicalHistoryDateFilter from "../../components/medical-history/MedicalHistoryDateFilter";
import MedicalHistoryTimeline from "../../components/medical-history/MedicalHistoryTimeline";
import { checkupType, checkupDateKey } from "../../components/medical-history/medicalHistoryDisplay";
import { useAppointmentQuery } from "../../hooks/useAppointmentQuery";
import "./StudentMedicalHistory.css";

export default function StudentMedicalHistory() {
  const { data, error, loading, reload } = useAppointmentQuery(getStudentMedicalHistory);
  const [selected, setSelected] = useState(null);
  const [type, setType] = useState("all");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");

  let content;
  if (loading) {
    content = <p className="history-state" role="status">Đang tải lịch sử khám…</p>;
  } else if (error) {
    content = (
      <div className="history-state">
        <p role="alert">{error}</p>
        <button type="button" className="history-action" onClick={reload}>Thử lại</button>
      </div>
    );
  } else if (data.length === 0) {
    content = <div className="history-state"><ClipboardList size={21} aria-hidden="true" /><p>Bạn chưa có lần khám đã hoàn tất.</p></div>;
  } else {
    const counts = {};
    const datedRecords = data.filter((record) => {
      if (!from && !to) return true;
      const date = checkupDateKey(record);
      return date && (!from || date >= from) && (!to || date <= to);
    });
    const visibleRecords = datedRecords.filter((record) => {
      const label = checkupType(record);
      counts[label] = (counts[label] ?? 0) + 1;
      return type === "all" || type === label;
    });
    content = (
      <div className="history-layout">
        <aside className="history-sidebar" aria-label="Bộ lọc lịch sử khám">
          <MedicalHistoryFilters total={datedRecords.length} counts={counts} value={type} onChange={setType} />
          <MedicalHistoryDateFilter from={from} to={to} onFromChange={setFrom} onToChange={setTo} />
        </aside>
        <div className="history-results" aria-live="polite">
          {visibleRecords.length ? (
            <MedicalHistoryTimeline records={visibleRecords} onSelect={setSelected} />
          ) : (
            <div className="history-state">
              <p>Không có lần khám phù hợp với bộ lọc.</p>
              <button type="button" className="history-action" onClick={() => {
                setType("all");
                setFrom("");
                setTo("");
              }}>Xem tất cả lần khám</button>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="history-page">
      <header className="history-heading">
        <h1>Lịch sử khám bệnh</h1>
        <p>Tra cứu các lần khám đã hoàn tất, đơn thuốc và giấy chuyển viện.</p>
      </header>
      {content}
      {selected && <MedicalHistoryDetailModal record={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
