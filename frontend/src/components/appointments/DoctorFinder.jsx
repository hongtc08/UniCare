import { Check } from "lucide-react";
import { SERVICES, serviceLabel } from "../../utils/appointments";
import "./DoctorFinder.css";

const SPECIALTIES = { KHAM_DA_KHOA: "Bác sĩ đa khoa", TU_VAN_TAM_LY: "Chuyên viên tâm lý" };

export default function DoctorFinder({ service, onServiceChange, facilityId, facilities, onFacilityChange, facilitiesLoading, facilitiesError, onFacilitiesRetry, onlyAvailable, onAvailabilityChange, doctors, doctorId, onDoctorChange, showDoctors = true, loading = false, error = false }) {
  return <aside className="booking-panel booking-filters doctor-finder">
    <h3>Tìm nhân viên y tế</h3>
    <div className="doctor-finder-filters">
      <label className="form-field"><span>Chuyên khoa</span><select value={service} onChange={(event) => onServiceChange(event.target.value)}>{SERVICES.map(({ value }) => <option key={value} value={value}>{SPECIALTIES[value]}</option>)}</select></label>
      <label className="form-field"><span>Cơ sở y tế</span><select value={facilityId} disabled={facilitiesLoading || !facilities.length} onChange={(event) => onFacilityChange(event.target.value)} aria-describedby="facility-list-note"><option value="">{facilitiesLoading ? "Đang tải cơ sở y tế…" : "Tất cả cơ sở y tế"}</option>{facilities.map((facility) => <option key={facility.id} value={facility.id}>{facility.name}</option>)}</select></label>
      {!facilitiesLoading && (facilitiesError || !facilities.length) && <div id="facility-list-note" className="facility-list-note"><p>{facilitiesError ? "Chưa tải được danh sách cơ sở y tế. Các ca khám bên cạnh vẫn được hiển thị." : "Chưa có cơ sở y tế trong danh mục."}</p>{facilitiesError && <button type="button" onClick={onFacilitiesRetry}>Tải lại danh sách</button>}</div>}
      <label className="available-only"><input type="checkbox" checked={onlyAvailable} onChange={(event) => onAvailabilityChange(event.target.checked)} /><span>Chỉ hiển thị lịch còn trống</span></label>
    </div>
    {showDoctors ? <div className="doctor-finder-results">
      <h4>{onlyAvailable ? "Nhân viên y tế còn lịch" : "Nhân viên y tế trong ngày"}</h4>
      {loading ? <p role="status" className="appointment-muted">Đang tìm nhân viên y tế…</p> : error ? <p className="appointment-muted">Chưa tải được danh sách. Hãy thử lại ở phần lịch khám.</p> : !doctors.length ? <p role="status" className="appointment-muted">Không có nhân viên y tế phù hợp. Hãy đổi ngày, cơ sở y tế hoặc bỏ lọc lịch còn trống.</p> : <div className="doctor-options" aria-label="Chọn nhân viên y tế">{doctors.map((doctor) => <button type="button" key={doctor.id} className={`doctor-option${doctorId === doctor.id ? " is-selected" : ""}`} aria-pressed={doctorId === doctor.id} onClick={() => onDoctorChange(doctor.id)}>
        <span className="doctor-avatar" aria-hidden="true">{doctor.name.replace(/^(BS\.|CV\.)\s*/i, "").split(/\s+/).filter(Boolean).slice(-2).map((part) => part[0]).join("")}</span>
        <span><strong>{doctor.name}</strong><small>{SPECIALTIES[service] || serviceLabel(service)}</small><small>{doctor.count ? `${doctor.count} ca còn chỗ` : "Không còn ca khả dụng"}</small></span>
        {doctorId === doctor.id && <Check className="doctor-selection-check" size={18} aria-hidden="true" />}
      </button>)}</div>}
    </div> : <p className="doctor-finder-hint">Chọn ngày trên lịch rồi bấm “Tiếp tục” để xem bác sĩ và khung giờ.</p>}
  </aside>;
}
