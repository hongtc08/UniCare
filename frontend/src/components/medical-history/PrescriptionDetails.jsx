import { Pill } from "lucide-react";
import "./PrescriptionDetails.css";

export default function PrescriptionDetails({ medicines = [] }) {
  const items = medicines ?? [];

  return (
    <section className="history-prescription">
      <div className="prescription-heading">
        <h3><Pill size={18} aria-hidden="true" /> Đơn thuốc điện tử</h3>
        {items.length > 0 && <span>{items.length} loại thuốc</span>}
      </div>
      {items.length > 0 ? (
        <div className="prescription-table-scroll" role="region" aria-label="Bảng đơn thuốc" tabIndex={0}>
          <table className="prescription-table">
            <colgroup><col className="prescription-name-column" /><col className="prescription-count-column" /><col /></colgroup>
            <thead><tr><th scope="col">Tên thuốc</th><th scope="col">Số lượng</th><th scope="col">Cách dùng</th></tr></thead>
            <tbody>
              {items.map((medicine) => (
                <tr key={medicine.id}>
                  <th scope="row">{medicine.medicineName || "Chưa ghi nhận tên thuốc"}</th>
                  <td>{medicine.quantity ?? "—"}</td>
                  <td>{medicine.dosageInstruction?.trim() || "Chưa ghi nhận hướng dẫn sử dụng."}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : <p className="prescription-empty">Phiếu khám này không có đơn thuốc.</p>}
    </section>
  );
}
