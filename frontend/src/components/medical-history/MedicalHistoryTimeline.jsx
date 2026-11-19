import { ArrowUpRight, CalendarDays, UserRound } from "lucide-react";
import { checkupDate, checkupType, periodicResult } from "./medicalHistoryDisplay";

export default function MedicalHistoryTimeline({ records, onSelect }) {
  return (
    <ol className="history-timeline" aria-label="Các lần khám đã hoàn tất">
      {records.map((record) => {
        const type = checkupType(record);
        const result = periodicResult(record);
        return (
          <li key={record.id} className="history-entry">
            <div className="history-entry-date"><CalendarDays size={16} aria-hidden="true" /> {checkupDate(record)}</div>
            <article className="history-card">
              <div className="history-card-heading">
                <div>
                  <h2>{type}</h2>
                  {record.checkupCode && <p className="history-card-code">{record.checkupCode}</p>}
                </div>
                {type === "Khám định kỳ" && (
                  <span className="history-result" data-result={result} aria-label={`Kết quả định kỳ: ${result}`}>{result}</span>
                )}
              </div>
              <div className="history-card-summary"><strong>Kết luận chẩn đoán</strong><p>{record.diagnosisNotes?.trim() || "Chưa ghi nhận"}</p></div>
              <footer className="history-card-footer">
                <div className="history-card-doctor">
                  <UserRound size={17} aria-hidden="true" />
                  <span>{record.doctorName || "Chưa ghi nhận bác sĩ"}</span>
                </div>
                <button type="button" className="history-card-open" onClick={() => onSelect(record)}
                  aria-label={`Xem phiếu ${record.checkupCode || type}, ngày ${checkupDate(record)}`}>
                  Xem chi tiết <ArrowUpRight size={16} aria-hidden="true" />
                </button>
              </footer>
            </article>
          </li>
        );
      })}
    </ol>
  );
}
