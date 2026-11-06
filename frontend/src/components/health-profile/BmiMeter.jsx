import { Activity, Check, Info } from "lucide-react";
import { BMI_CATEGORIES, calculateBmi } from "../../utils/health-profile/bmi";
import "./BmiMeter.css";

export default function BmiMeter({ height, weight, saved = false }) {
  const result = calculateBmi(height, weight);
  if (!result) return <section className="bmi-meter"><h3>Chỉ số khối cơ thể (BMI)</h3><p>Nhập chiều cao và cân nặng, sau đó lưu hồ sơ để xem BMI.</p></section>;
  const { value, category, position } = result;
  const formatted = value.toLocaleString("vi-VN", { minimumFractionDigits: 1, maximumFractionDigits: 1 });

  return (
    <section className={`bmi-meter bmi-${category.id}`} aria-label="Chỉ số khối cơ thể BMI">
      <div className="bmi-heading">
        <h3><Activity size={18} aria-hidden="true" />Chỉ số khối cơ thể <span>(BMI)</span></h3>
        <span className="bmi-save-state">{saved ? <><Check size={14} aria-hidden="true" />Vừa cập nhật</> : "Theo thông tin đã lưu"}</span>
      </div>
      <div className="bmi-summary" role="status" aria-live="polite" aria-atomic="true">
        <div className="bmi-value"><strong>{formatted}</strong><span>kg/m²</span></div>
        <div className="bmi-result"><span className="bmi-category"><i aria-hidden="true" />{category.label}</span><p>Từ {Number(height).toLocaleString("vi-VN")} cm · {Number(weight).toLocaleString("vi-VN")} kg</p></div>
      </div>
      <div className="bmi-scale" aria-hidden="true">
        <div className="bmi-track">
          {BMI_CATEGORIES.map((item) => <span key={item.id} className={`bmi-band bmi-${item.id}`} />)}
        </div>
        <div className="bmi-pointer" style={{ left: `${position}%` }}><span /></div>
        <div className="bmi-ticks"><span style={{ left: "0%" }}>12</span><span style={{ left: `${6.5 / 28 * 100}%` }}>18,5</span><span style={{ left: `${13 / 28 * 100}%` }}>25</span><span style={{ left: `${18 / 28 * 100}%` }}>30</span><span style={{ left: "100%" }}>40+</span></div>
      </div>
      <ul className="bmi-legend" aria-label="Các khoảng BMI">
        {BMI_CATEGORIES.map((item) => <li key={item.id} className={`bmi-${item.id}${item.id === category.id ? " is-current" : ""}`}><span className="bmi-legend-label"><i aria-hidden="true" />{item.label}{item.id === category.id && <Check size={13} aria-label="Mức hiện tại" />}</span><span className="bmi-range">{item.range}</span></li>)}
      </ul>
      <p className="bmi-note"><Info size={14} aria-hidden="true" /><span>Thang tham khảo cho người từ 20 tuổi. BMI là chỉ số sàng lọc, không thay thế đánh giá y tế. <a href="https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html" target="_blank" rel="noreferrer">Theo CDC</a>.</span></p>
    </section>
  );
}
