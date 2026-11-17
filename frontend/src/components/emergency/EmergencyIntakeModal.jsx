import { useId, useRef, useState } from "react";
import { TriangleAlert } from "lucide-react";
import Modal from "../Modal";
import StudentSearch from "./StudentSearch";
import { emergencyIntakeApi } from "../../api/emergencyIntakeApi";
import { useAuth } from "../../hooks/useAuth";

export default function EmergencyIntakeModal({ onClose, onCreated }) {
  const { user } = useAuth();
  const symptomsId = useId();
  const saving = useRef(false);
  const [student, setStudent] = useState(null);
  const [symptoms, setSymptoms] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event) {
    event.preventDefault();
    if (!student || saving.current) return;

    saving.current = true;
    setBusy(true);
    setError("");
    try {
      const checkup = await emergencyIntakeApi.create(student.studentId, symptoms);
      onCreated(checkup);
    } catch (error) {
      setError(error.response?.data?.message || "Chưa xác nhận được tiếp nhận. Vui lòng kiểm tra trước khi gửi lại.");
      saving.current = false;
      setBusy(false);
    }
  }

  return (
    <Modal isOpen onClose={onClose} title="Tiếp nhận ca khẩn cấp"
      description="Ghi nhận thông tin nhanh để bắt đầu xử trí" busy={busy} className="emergency-dialog">
      <StudentSearch selected={student} onSelect={(nextStudent) => {
        setStudent(nextStudent);
        setError("");
      }} disabled={busy} />
      <form className="emergency-intake-form" onSubmit={submit} aria-label="Thông tin tiếp nhận">
        <div className="emergency-form-grid">
          <label>Mã sinh viên<input value={student?.studentCode || ""} placeholder="Chọn sinh viên" readOnly /></label>
          <label>Họ và tên<input value={student?.fullName || ""} placeholder="Tên sinh viên" readOnly /></label>
          <label>Thời gian tiếp nhận<input value="Ghi tự động khi lưu" readOnly /></label>
          <label>Mức độ khẩn cấp<select disabled><option>Chưa có API lưu mức độ</option></select></label>
          <label className="emergency-form-wide">Loại cấp cứu<select disabled><option>Chưa có API lưu loại cấp cứu</option></select></label>
          <div className="emergency-form-wide emergency-field">
            <label htmlFor={symptomsId}>Mô tả tình trạng ban đầu</label>
            <textarea
              id={symptomsId}
              rows={3}
              placeholder="Mô tả triệu chứng và hoàn cảnh xảy ra…"
              value={symptoms}
              disabled={busy}
              onChange={(event) => setSymptoms(event.target.value)}
            />
          </div>
          <label>Nhân viên tiếp nhận<input value={user?.fullName || user?.username || "Tài khoản đang đăng nhập"} readOnly /></label>
          <label>Người báo cáo / liên hệ<input placeholder="Chưa có API lưu người liên hệ" disabled /></label>
        </div>
        {error && <p className="emergency-error" role="alert">{error}</p>}
        <div className="emergency-dialog-actions">
          <button type="button" className="emergency-button" onClick={onClose} disabled={busy}>Hủy</button>
          <button type="submit" className="emergency-button emergency-button-danger" disabled={busy || !student}>
            <TriangleAlert size={17} aria-hidden="true" />
            {busy ? "Đang tiếp nhận…" : "Tiếp nhận ca khẩn cấp"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
