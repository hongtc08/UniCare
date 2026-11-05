import { useEffect, useState } from "react";
import { BadgeCheck, ChevronDown, Pencil, TriangleAlert, UserRound } from "lucide-react";
import { formatInsuranceDate as date, getInsuranceStatus } from "../utils/insurance";
import "./HealthInsuranceCard.css";

export default function HealthInsuranceCard({ insurance, onEdit, editing = false }) {
  const [now, setNow] = useState(() => new Date());
  const [expanded, setExpanded] = useState(true);
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 60000);
    return () => clearInterval(timer);
  }, []);
  const status = getInsuranceStatus(insurance.validFrom, insurance.validUntil, now);
  const rows = [
    ["Ngày sinh", date(insurance.dateOfBirth)],
    ["Giới tính", insurance.gender || "Chưa cập nhật"],
    ["Số thẻ BHYT", insurance.cardNumber || "Chưa cập nhật"],
    ["Nơi ĐK KCB BĐ", insurance.initialProvider || "Chưa cập nhật"],
    ["Nơi cấp, đổi thẻ BHYT", insurance.issuedAt || "Chưa cập nhật"],
  ];
  return (
    <section className="electronic-insurance insurance-toggle-item" aria-labelledby="electronic-insurance-heading">
        <h3 id="electronic-insurance-heading" className="insurance-toggle-heading">
          <button className="insurance-disclosure" aria-expanded={expanded} aria-controls="insurance-panel-body" onClick={() => setExpanded((value) => !value)}>
            <span className="insurance-disclosure-title">Thẻ BHYT bản điện tử</span>
        <span className={`insurance-status${status.state === "active" ? "" : " insurance-status-warning"}`}>
          {status.state === "active" ? <BadgeCheck size={16} aria-hidden="true" /> : <TriangleAlert size={16} aria-hidden="true" />}{status.label}
        </span>
            <ChevronDown size={18} className={expanded ? "is-expanded" : ""} aria-hidden="true" />
          </button>
        </h3>
      <div id="insurance-panel-body" className="electronic-insurance-body" hidden={!expanded}>
        {status.state !== "active" && <div className={`insurance-notice is-${status.state}`} role="status"><TriangleAlert size={20} aria-hidden="true" /><div><strong>{status.label}</strong><p>{status.state === "expiring" ? (status.daysRemaining === 0 ? "Thẻ có giá trị đến hết hôm nay." : `Thẻ còn ${status.daysRemaining} ngày đến hạn.`) : status.state === "expired" ? `Thẻ đã hết hạn ngày ${date(insurance.validUntil)}.` : status.state === "pending" ? `Thẻ có hiệu lực từ ${date(insurance.validFrom)}.` : "Cần bổ sung thời hạn sử dụng thẻ."}</p></div></div>}
        <div id="insurance-view-content" className="insurance-columns">
            <article className="insurance-digital-card" aria-label="Thông tin thẻ bảo hiểm y tế">
              <div className="insurance-card-person"><div className="insurance-card-avatar"><UserRound size={40} strokeWidth={1.5} aria-hidden="true" /></div><div><h3>{insurance.fullName}</h3><p>Thời hạn có giá trị</p><p>{date(insurance.validFrom)} đến {date(insurance.validUntil)}</p></div></div>
              <dl>{rows.map(([label, value]) => <div className="insurance-card-field" key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl>
            </article>
            <section className="insurance-benefits" aria-labelledby="insurance-benefits-heading">
              <h3 id="insurance-benefits-heading">Thông tin quyền lợi:</h3>
              <p>{insurance.benefits || "Chưa có thông tin quyền lợi từ cơ quan bảo hiểm. Vui lòng liên hệ bộ phận y tế để được hỗ trợ tra cứu."}</p>
            </section>
        </div>
        {onEdit && !editing && (
          <div className="insurance-bottom-actions">
            <button type="button" className="insurance-view-toggle" onClick={onEdit}>
              <Pencil size={16} aria-hidden="true" />{insurance.cardNumber ? "Chỉnh sửa thẻ BHYT" : "Thêm thẻ BHYT"}
            </button>
          </div>
        )}
      </div>
    </section>
  );
}
