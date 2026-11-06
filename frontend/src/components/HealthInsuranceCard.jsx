import { useEffect, useState } from "react";
import { BadgeCheck, ChevronDown, TriangleAlert, UserRound } from "lucide-react";
import { formatInsuranceDate as date, getInsuranceStatus } from "../utils/insurance";
import "./HealthInsuranceCard.css";

// Nội dung quyền lợi cố định, không phụ thuộc dữ liệu cá nhân hoặc phản hồi API.
const INSURANCE_BENEFITS = "Được hưởng 80% chi phí khám bệnh, chữa bệnh trong phạm vi được hưởng BHYT (áp dụng tỷ lệ thanh toán một số thuốc, hóa chất, vật tư y tế và dịch vụ kỹ thuật theo quy định của Bộ trưởng Bộ Y tế).\n\nTrong trường hợp điều trị nội trú trái tuyến tại CSKCB tuyến TW sẽ được hưởng 32% (TH trên thẻ có mã nơi sinh sống là K1 hoặc K2 hoặc K3 sẽ được hưởng 80%). CSKCB tuyến tỉnh sẽ được hưởng 48% (TH trên thẻ có mã nơi sinh sống là K1 hoặc K2 hoặc K3 sẽ được hưởng 80%), từ ngày 01/01/2021 sẽ hưởng 80%, CSKCB là bệnh viện tuyến huyện sẽ hưởng 80%.";

export default function HealthInsuranceCard({ insurance }) {
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
              <p>{INSURANCE_BENEFITS}</p>
            </section>
        </div>

      </div>
    </section>
  );
}
