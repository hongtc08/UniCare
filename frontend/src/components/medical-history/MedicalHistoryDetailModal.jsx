import { LockKeyhole } from "lucide-react";
import Modal from "../Modal";
import PrescriptionDetails from "./PrescriptionDetails";
import { checkupDate, checkupType, medicalDate, periodicResult } from "./medicalHistoryDisplay";

function RecordFields({ fields, stacked = false }) {
  return (
    <dl className={`history-fields${stacked ? " history-fields-stacked" : ""}`}>
      {fields.map(([label, value]) => (
        <div key={label}><dt>{label}</dt><dd>{value || "Chưa ghi nhận"}</dd></div>
      ))}
    </dl>
  );
}

function TransferDetails({ transfer }) {
  return (
    <section>
      <h3>Giấy chuyển viện</h3>
      {transfer ? (
        <RecordFields stacked fields={[
          ["Mã giấy chuyển viện", transfer.recordCode],
          ["Nơi chuyển đến", transfer.targetHospital],
          ["Thời gian chuyển viện", medicalDate(transfer.transferredAt, true)],
          ["Nhóm lý do", transfer.reasonCategory],
          ["Lý do chuyển viện", transfer.transferReason],
          ["Tình trạng lúc chuyển", transfer.currentCondition],
          ["Biện pháp sơ cứu", transfer.firstAidNotes],
          ...(transfer.refusalNotes ? [["Ghi chú từ chối chuyển viện", transfer.refusalNotes]] : []),
        ]} />
      ) : <p className="history-muted">Phiếu khám này không có giấy chuyển viện.</p>}
    </section>
  );
}

export default function MedicalHistoryDetailModal({ record, onClose }) {
  const type = checkupType(record);

  return (
    <Modal isOpen closeOnBackdrop title="Chi tiết phiếu khám" onClose={onClose} className="history-dialog">
      <div className="history-detail">
        <div className="history-detail-meta">
          <div>
            <p className="history-muted">{record.checkupCode || "Phiếu khám đã hoàn tất"}</p>
            <h3>{type}</h3>
          </div>
          <span><LockKeyhole size={14} aria-hidden="true" /> Chỉ đọc</span>
        </div>
        <section>
          <h3>Thông tin lần khám</h3>
          <RecordFields fields={[
            ["Ngày khám", checkupDate(record)],
            ["Bác sĩ", record.doctorName],
            ...(type === "Khám định kỳ" ? [["Kết quả định kỳ", periodicResult(record)]] : []),
          ]} />
        </section>
        <section className="history-clinical">
          <h3>Kết quả khám và điều trị</h3>
          <RecordFields stacked fields={[
            ["Triệu chứng", record.symptoms],
            ["Kết luận chẩn đoán", record.diagnosisNotes],
            ["Xử trí / đề xuất điều trị", record.treatmentSuggestions],
          ]} />
        </section>
        <PrescriptionDetails medicines={record.prescribedMedicines} />
        <TransferDetails transfer={record.transferRecord} />
      </div>
    </Modal>
  );
}
