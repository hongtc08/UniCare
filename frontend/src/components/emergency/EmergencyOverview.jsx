import { Activity, Check, ChevronRight, Stethoscope, TriangleAlert } from "lucide-react";

export function EmergencyStats({ count }) {
  const metrics = [
    { label: "Ca đang xử lý", value: count || "—", note: "Ca tiếp nhận trong phiên này", Icon: TriangleAlert, tone: "red" },
    { label: "Đang sơ cứu", value: "—", note: "Chưa có dữ liệu sơ cứu", Icon: Stethoscope, tone: "amber" },
    { label: "Đang chuyển viện", value: "—", note: "Chưa có dữ liệu chuyển viện", Icon: ChevronRight, tone: "blue" },
    { label: "Đã kết thúc hôm nay", value: "—", note: "Chưa có dữ liệu kết thúc ca", Icon: Check, tone: "green" },
  ];
  return (
    <div className="emergency-stats" aria-label="Thống kê ca cấp cứu">
      {metrics.map(({ label, value, note, Icon, tone }) => (
        <div className="emergency-panel emergency-metric" key={label}>
          <span className={`emergency-metric-icon ${tone}`}><Icon size={20} aria-hidden="true" /></span>
          <div><div>{label}</div><strong>{value}</strong><small>{note}</small></div>
        </div>
      ))}
    </div>
  );
}

const steps = ["Tiếp nhận sinh viên", "Sơ cứu ban đầu", "Chuyển viện ngoài", "Kết thúc ca"];

export function EmergencyWorkflow() {
  return (
    <ol className="emergency-panel emergency-workflow" aria-label="Quy trình cấp cứu">
      {steps.map((step, index) => (
        <li key={step}><span>{index + 1}</span>{step}</li>
      ))}
    </ol>
  );
}

const time = new Intl.DateTimeFormat("vi-VN", { timeStyle: "short", timeZone: "Asia/Ho_Chi_Minh" });

export function EmergencyCaseTable({ cases }) {
  return (
    <section className="emergency-panel" aria-labelledby="emergency-cases">
      <header className="emergency-panel-heading emergency-table-heading">
        <div><h2 id="emergency-cases">Theo dõi ca cấp cứu</h2><p>Quy trình 4 bước: tiếp nhận, sơ cứu, chuyển viện và kết thúc</p></div>
        <div className="emergency-table-actions">
          <button className="emergency-button" disabled>Bộ lọc</button>
          <button className="emergency-text-button" disabled>Xem tất cả</button>
        </div>
      </header>
      <div className="emergency-table-scroll">
        <table className="emergency-table">
          <thead><tr>{["Mã ca", "Sinh viên", "Tiếp nhận", "Loại cấp cứu", "Bước hiện tại", "Mức độ"].map((label) => <th scope="col" key={label}>{label}</th>)}</tr></thead>
          <tbody>
            {cases.length === 0 && <tr><td colSpan={6}><div className="emergency-empty"><Activity size={20} aria-hidden="true" /> Chưa có ca nào được tiếp nhận trong phiên này.</div></td></tr>}
            {cases.map((item) => (
              <tr key={item.id}>
                <td>{item.checkupCode || item.id}</td>
                <td>{[item.studentName, item.studentCode].filter(Boolean).join(" · ") || "—"}</td>
                <td>{item.createdAt ? time.format(new Date(item.createdAt)) : "—"}</td>
                <td>—</td><td>Đã tiếp nhận</td><td>—</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <footer className="emergency-table-footer">
        <span>{cases.length} ca tiếp nhận trong phiên này</span>
        <div className="emergency-table-actions"><button className="emergency-button" disabled>Trước</button><button className="emergency-button" disabled>Sau</button></div>
      </footer>
    </section>
  );
}
