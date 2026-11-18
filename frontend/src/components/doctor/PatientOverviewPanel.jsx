import { useCallback, useId } from "react";
import { Activity, ShieldCheck, TriangleAlert } from "lucide-react";
import { getPatientOverview } from "../../api/patientOverviewApi";
import { useAppointmentQuery } from "../../hooks/useAppointmentQuery";
import { formatDay } from "../../utils/appointments";
import "./PatientOverviewPanel.css";

const missing = "Chưa cập nhật";
const number = new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 2 });

function measure(value, unit = "") {
  return value == null ? missing : [number.format(value), unit].filter(Boolean).join(" ");
}

function Fields({ items }) {
  return (
    <dl className="patient-overview-fields">
      {items.map(([label, value]) => (
        <div key={label}><dt>{label}</dt><dd>{value || missing}</dd></div>
      ))}
    </dl>
  );
}

function OverviewDetails({ studentInfo, healthProfile, healthInsurance }) {
  const health = healthProfile;
  const insurance = healthInsurance;

  return (
    <div className="patient-overview-content">
      <div className="patient-overview-identity">
        <strong>{studentInfo.fullName || "Sinh viên"}</strong>
        <p>{[studentInfo.studentCode, studentInfo.className || studentInfo.classCode,
          studentInfo.facultyName || studentInfo.facultyCode].filter(Boolean).join(" · ")}</p>
      </div>
      <div className={`patient-overview-allergy${health?.allergies?.trim() ? " has-allergy" : ""}`}>
        <TriangleAlert size={18} aria-hidden="true" />
        <div><strong>Dị ứng</strong><p>{health?.allergies?.trim() || "Chưa có thông tin dị ứng."}</p></div>
      </div>
      <section className="patient-overview-group">
        <h3><Activity size={17} aria-hidden="true" /> Hồ sơ sức khỏe</h3>
        {health ? (
          <Fields items={[
            ["Chiều cao", measure(health.height, "cm")],
            ["Cân nặng", measure(health.weight, "kg")],
            ["BMI", measure(health.bmi)],
            ["Nhóm máu", health.bloodType],
            ["Bệnh nền", health.underlyingDiseases],
            ["Tiền sử bệnh", health.medicalHistory],
          ]} />
        ) : <p>Chưa có hồ sơ sức khỏe.</p>}
      </section>
      <section className="patient-overview-group">
        <h3><ShieldCheck size={17} aria-hidden="true" /> Bảo hiểm y tế</h3>
        {insurance ? (
          <Fields items={[
            ["Số thẻ", insurance.cardNumber],
            ["Trạng thái", insurance.statusDescription],
            ["Hiệu lực từ", insurance.effectiveDate && formatDay(insurance.effectiveDate)],
            ["Hết hạn", insurance.expirationDate && formatDay(insurance.expirationDate)],
            ["Nơi đăng ký khám ban đầu", insurance.initialHospital],
          ]} />
        ) : <p>Chưa có thông tin bảo hiểm y tế.</p>}
      </section>
    </div>
  );
}

export default function PatientOverviewPanel({ studentId }) {
  const titleId = useId();
  const load = useCallback((signal) => studentId ? getPatientOverview(studentId, signal) : Promise.resolve(null), [studentId]);
  const { data, error, loading, reload } = useAppointmentQuery(load);

  let content = <p className="patient-overview-message">Chọn sinh viên để xem hồ sơ.</p>;
  if (studentId && loading) {
    content = <p className="patient-overview-message" role="status">Đang tải hồ sơ sinh viên…</p>;
  } else if (studentId && error) {
    content = (
      <div className="patient-overview-message">
        <p role="alert">{error}</p>
        <button type="button" onClick={reload}>Thử lại</button>
      </div>
    );
  } else if (studentId && data) {
    content = <OverviewDetails {...data} />;
  }

  return (
    <section className="patient-overview" aria-labelledby={titleId}>
      <h2 id={titleId}>Hồ sơ y tế sinh viên</h2>
      {content}
    </section>
  );
}
