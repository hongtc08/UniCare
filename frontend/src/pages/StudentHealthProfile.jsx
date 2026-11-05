import { useEffect, useRef, useState } from "react";
import { Check, ChevronRight, Download, Pencil } from "lucide-react";
import { Link } from "react-router-dom";
import { getHealthProfileCompletion } from "../utils/profileCompletion";
import "./StudentHealthProfile.css";
import BmiMeter from "../components/BmiMeter";
import HealthInsuranceCard from "../components/HealthInsuranceCard";
import Modal from "../components/Modal";
import { useAuth } from "../hooks/useAuth";
import { healthProfileApi } from "../api/healthProfileApi";
import { fromHealthResponse, toHealthRequest } from "../utils/healthProfile";
import {
  formatInsuranceDate,
  fromInsuranceResponse,
  toInsuranceRequest,
  validateInsuranceDraft,
} from "../utils/insurance";
import { readSession } from "../utils/session";

const initialProfile = fromHealthResponse(null);
const initialInsurance = fromInsuranceResponse(null);
const bloodGroups = ["A", "B", "AB", "O"];
const labels = { height: "Chiều cao", weight: "Cân nặng", bloodGroup: "Nhóm máu", allergies: "Dị ứng", conditions: "Bệnh nền", vaccinations: "Tiêm chủng" };

function Detail({ label, children }) {
  return <div className="health-detail"><dt>{label}</dt><dd>{children || "Chưa cập nhật"}</dd></div>;
}

function requestError(failure, fallback) {
  if (failure?.response?.data?.message) return failure.response.data.message;
  if (failure?.code === "ECONNABORTED") return "API phản hồi quá chậm. Vui lòng thử lại.";
  if (!failure?.response && failure?.request) return "Không kết nối được API. Kiểm tra backend ở cổng 8080 và địa chỉ frontend được phép truy cập.";
  if (failure?.response?.status === 404) return "Không tìm thấy API. Hãy khởi động lại backend để nạp mã nguồn mới.";
  return failure?.message || fallback;
}

export default function StudentHealthProfile() {
  const { user, updateIdentity } = useAuth();
  const [personalInfo, setPersonalInfo] = useState(null);
  const [personalInfoError, setPersonalInfoError] = useState("");
  const [savingHolder, setSavingHolder] = useState(false);
  const baseStudent = personalInfo ?? { fullName: user?.fullName, username: user?.username };
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [reload] = useState(0);
  const [profile, setProfile] = useState(initialProfile);
  const [draft, setDraft] = useState(initialProfile);
  const [editing, setEditing] = useState(false);
  const [updatedAt, setUpdatedAt] = useState(null);
  const [message, setMessage] = useState("");
  const editButton = useRef(null);

  // Thông tin cá nhân được tải và lưu qua /health-profile/personal-info.
  const [holderInfo, setHolderInfo] = useState(() => ({
    fullName: baseStudent?.fullName || user?.fullName || user?.username || "",
    socialInsuranceNumber: "",
    dateOfBirth: baseStudent.dateOfBirth || "",
    identityNumber: baseStudent.identityNumber || "",
    phone: baseStudent.phone || "",
    gender: baseStudent.gender || "",
    email: baseStudent.email || "",
    address: baseStudent.address || "",
  }));
  const [holderDraft, setHolderDraft] = useState(holderInfo);
  const [editingHolder, setEditingHolder] = useState(false);
  const editHolderButton = useRef(null);

  // State cho Popup chỉnh sửa/thêm Thẻ BHYT điện tử (/api/health-insurances)
  const [insuranceData, setInsuranceData] = useState(initialInsurance);
  const [insuranceDraft, setInsuranceDraft] = useState(initialInsurance);
  const [editingInsurance, setEditingInsurance] = useState(false);
  const [savingInsurance, setSavingInsurance] = useState(false);
  const [insuranceError, setInsuranceError] = useState("");
  const [insuranceLoadError, setInsuranceLoadError] = useState("");

  const student = {
    ...baseStudent,
    fullName: holderInfo.fullName || baseStudent?.fullName || user?.fullName || user?.username,
    dateOfBirth: holderInfo.dateOfBirth,
    identityNumber: holderInfo.identityNumber,
    phone: holderInfo.phone,
    address: holderInfo.address,
  };
  const completion = getHealthProfileCompletion(profile);
  const name = student?.fullName || "Chưa cập nhật";
  const initials = name.split(/\s+/).filter(Boolean).slice(-2).map((part) => part[0]).join("");
  const insurance = {
    ...insuranceData,
    socialInsuranceNumber: insuranceData.socialInsuranceNumber,
    fullName: name,
    phone: student.phone,
    dateOfBirth: student.dateOfBirth,
    gender: student.gender,
  };

  useEffect(() => {
    const controller = new AbortController();
    Promise.allSettled([
      healthProfileApi.get(controller.signal),
      healthProfileApi.getInsurance(controller.signal),
      healthProfileApi.getPersonalInfo(controller.signal),
    ])
      .then(([healthResult, insuranceResult, personalResult]) => {
        if (controller.signal.aborted) return;
        if (personalResult.status === "fulfilled" && personalResult.value?.data && personalResult.value.success !== false) {
          const info = personalResult.value.data;
          setPersonalInfo(info);
          setHolderInfo((prev) => ({
            ...prev,
            fullName: info.fullName ?? "",
            dateOfBirth: info.dateOfBirth ?? "",
            phone: info.phoneNumber ?? info.phone ?? "",
            gender: info.gender ?? "",
            email: info.email ?? "",
          }));
          setPersonalInfoError("");
          updateIdentity(info);
          setInsuranceData((prev) => ({ ...prev, studentId: info.studentId }));
        } else {
          setPersonalInfoError(requestError(personalResult.reason, "Không tải được thông tin cá nhân. Bạn vẫn có thể nhập và thử lưu."));
        }
        if (healthResult.status === "fulfilled" && healthResult.value?.success !== false) {
          const health = healthResult.value;
          const source = health.data;
          setProfile(fromHealthResponse(source));
          setUpdatedAt(source?.updatedAt ?? null);
          if (health.data?.studentId) {
            setInsuranceData((prev) => ({ ...prev, studentId: health.data.studentId }));
          }
        } else {
          setProfile(fromHealthResponse(null));
          setUpdatedAt(null);
          setError("Không tải được hồ sơ sức khỏe. Vui lòng tải lại trang để thử lại.");
        }

        if (insuranceResult.status === "fulfilled" && insuranceResult.value?.success !== false) {
          const raw = insuranceResult.value?.data;
          const record = Array.isArray(raw) ? raw[0] : raw;
          if (record) {
            setInsuranceData(fromInsuranceResponse(record));
          }
        } else {
          setInsuranceLoadError(requestError(insuranceResult.reason, "Không tải được thẻ BHYT. Bạn vẫn có thể nhập thông tin và thử lưu thẻ."));
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      })
      .catch((failure) => {
        if (!controller.signal.aborted) {
          setError(requestError(failure, "Không xử lý được dữ liệu hồ sơ. Vui lòng thử lại."));
        }
      });
    return () => controller.abort();
  }, [reload, updateIdentity]);

  function edit() {
    setDraft({ ...profile });
    setMessage("");
    setEditing(true);
  }
  function finish() {
    setEditing(false);
    requestAnimationFrame(() => editButton.current?.focus());
  }
  async function save(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    const payload = toHealthRequest(draft);
    try {
      const response = await healthProfileApi.save(payload);
      if (!response?.data || response.success === false) throw new Error("Không nhận được hồ sơ sức khỏe đã lưu.");
      setProfile(fromHealthResponse(response.data));
      setUpdatedAt(response.data?.updatedAt ?? new Date().toISOString());
      setMessage("Đã lưu hồ sơ sức khỏe.");
      finish();
    } catch (failure) {
      setError(failure.response?.data?.message || failure.message || "Không lưu được hồ sơ sức khỏe. Vui lòng thử lại.");
    } finally {
      setSaving(false);
    }
  }

  function editHolder() {
    setHolderDraft({ ...holderInfo, fullName: name });
    setMessage("");
    setEditingHolder(true);
  }
  function finishHolder() {
    setEditingHolder(false);
    requestAnimationFrame(() => editHolderButton.current?.focus());
  }
  async function saveHolder(event) {
    event.preventDefault();
    const payload = {
      fullName: holderDraft.fullName.trim(),
      dateOfBirth: holderDraft.dateOfBirth || null,
      phoneNumber: holderDraft.phone.trim(),
      gender: holderDraft.gender.trim(),
      email: holderDraft.email.trim(),
    };
    setSavingHolder(true);
    setPersonalInfoError("");
    try {
      const response = await healthProfileApi.savePersonalInfo(payload);
      if (!response?.data || response.success === false) throw new Error("Không nhận được thông tin cá nhân đã lưu.");
      const info = response.data;
      setPersonalInfo(info);
      setInsuranceData((prev) => ({ ...prev, studentId: info.studentId ?? prev.studentId }));
      setHolderInfo((prev) => ({ ...prev, fullName: info.fullName ?? "", dateOfBirth: info.dateOfBirth ?? "", phone: info.phoneNumber ?? info.phone ?? "", gender: info.gender ?? "", email: info.email ?? "" }));
      updateIdentity(info);
      setMessage("Đã lưu thông tin cá nhân.");
      finishHolder();
    } catch (failure) {
      setPersonalInfoError(requestError(failure, "Không lưu được thông tin cá nhân. Vui lòng thử lại."));
    } finally {
      setSavingHolder(false);
    }
  }

  function openInsuranceModal() {
    editHolder();
    setInsuranceDraft({
      ...insuranceData,
      socialInsuranceNumber: insuranceData.socialInsuranceNumber,
    });
    setInsuranceError("");
    setMessage("");
    setEditingInsurance(true);
  }
  function closeInsuranceModal() {
    if (savingInsurance || savingHolder) return;
    finishHolder();
    setEditingInsurance(false);
    setInsuranceError("");
  }
  async function saveInsurance(event) {
    event.preventDefault();
    const validationMessage = validateInsuranceDraft(insuranceDraft);
    if (validationMessage) {
      setInsuranceError(validationMessage);
      return;
    }
    setSavingInsurance(true);
    setInsuranceError("");
    setMessage("");

    const resolvedStudentId =
      personalInfo?.studentId ??
      insuranceData.studentId ??
      user?.userId ??
      user?.id ??
      readSession()?.userId;

    try {
      const payload = toInsuranceRequest(insuranceDraft, resolvedStudentId);
      const response = await healthProfileApi.saveInsurance(payload, insuranceData.id);
      if (!response?.data || response.success === false) throw new Error("Không nhận được thông tin thẻ BHYT đã lưu.");
      setInsuranceData(fromInsuranceResponse(response.data));
      setInsuranceLoadError("");
      setMessage("Đã lưu thông tin thẻ BHYT.");
      setEditingInsurance(false);
    } catch (failure) {
      setInsuranceError(requestError(failure, "Không lưu được thẻ BHYT. Vui lòng thử lại."));
    } finally {
      setSavingInsurance(false);
    }
  }

  function download() {
    const content = [
      "HỒ SƠ SỨC KHỎE",
      `${name} · ${student?.studentCode || user?.username}`,
      ...Object.entries(profile).map(([key, value]) => `${labels[key]}: ${value || "Chưa cập nhật"}${key === "height" ? " cm" : key === "weight" ? " kg" : ""}`),
      "",
      "THÔNG TIN BẢO HIỂM Y TẾ",
      `Số thẻ BHYT: ${insurance.cardNumber || "Chưa cập nhật"}`,
      `Mã BHXH: ${insurance.socialInsuranceNumber || "Chưa cập nhật"}`,
      `Giá trị sử dụng: ${formatInsuranceDate(insurance.validFrom)} - ${formatInsuranceDate(insurance.validUntil)}`,
      `Nơi ĐK KCB ban đầu: ${insurance.initialProvider || "Chưa cập nhật"}`,
      `Nơi cấp, đổi thẻ BHYT: ${insurance.issuedAt || "Chưa cập nhật"}`,
    ].join("\n");
    const url = URL.createObjectURL(new Blob(["\ufeff", content], { type: "text/plain;charset=utf-8" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = "ho-so-suc-khoe.txt";
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  return (
    <div className="health-page">
      <nav className="health-breadcrumb" aria-label="Đường dẫn">
        <Link to="/dashboard">UniHealth</Link><ChevronRight size={13} aria-hidden="true" /><span aria-current="page">Hồ sơ sức khỏe</span>
      </nav>
      <header className="page-heading">
        <div><h1>Hồ sơ sức khỏe điện tử</h1><p>Kiểm tra thông tin cá nhân, sinh học và bảo hiểm.</p></div>
        <div className="health-actions">
          <button className="button secondary" onClick={download} disabled={editing || editingHolder || editingInsurance || loading || Boolean(error) || saving || savingInsurance}><Download size={16} aria-hidden="true" />Tải hồ sơ</button>
        </div>
      </header>
      {loading && <p role="status" className="form-note">Đang tải hồ sơ sức khỏe…</p>}
      {error && <div role="alert" className="feedback feedback-error">{error}</div>}
      {message && <p role="status" className="feedback feedback-success"><Check size={17} aria-hidden="true" />{message}</p>}
      <div className="health-layout">
        <aside className="data-panel health-identity" aria-label="Thông tin sinh viên">
          <div className="health-identity-heading">
            <div className="avatar health-avatar" aria-hidden="true">{initials}</div>
            <h2>{name}</h2>
            <p>{[student?.facultyName, student?.className].filter(Boolean).join(" · ") || "Chưa cập nhật khoa / lớp"}</p>
            <div className="health-completion" aria-live="polite">
              <span className="health-sample-badge">Hồ sơ hoàn thiện {completion.percent}%</span>
              <progress value={completion.completed} max={completion.total} aria-label="Mức hoàn thiện thông tin sức khỏe" />
              <small>{completion.completed}/{completion.total} mục sức khỏe đã cung cấp</small>
            </div>
          </div>
          <dl>
            <Detail label="Mã sinh viên">{student?.studentCode || user?.username}</Detail>
            <Detail label="Ngày sinh">{student?.dateOfBirth ? formatInsuranceDate(student.dateOfBirth) : null}</Detail>
            <Detail label="Giới tính">{student?.gender}</Detail>
            <Detail label="Email">{student?.email}</Detail>
            <Detail label="Số điện thoại">{student?.phone}</Detail>
          </dl>
        </aside>
        <div className="health-sections">
          <section className="data-panel health-biological" aria-labelledby="biology-heading">
            <div className="panel-heading">
              <div><h2 id="biology-heading">{editing ? "Chỉnh sửa thông tin sinh học" : "Thông tin sinh học"}</h2><p>{editing ? "Nhập thông tin sức khỏe của bạn. Các trường có * là bắt buộc." : (updatedAt ? `Cập nhật lần cuối ${new Date(updatedAt).toLocaleDateString("vi-VN")}` : "Chưa cập nhật hồ sơ sức khỏe")}</p></div>
              {!editing && <button ref={editButton} className="health-edit-link" disabled={loading || saving} onClick={edit}><Pencil size={14} aria-hidden="true" />Chỉnh sửa</button>}
            </div>
            {editing ? (
              <form className="health-form" onSubmit={save}>
                <fieldset className="health-form-grid" disabled={saving}>
                  {[{ key: "height", unit: "cm", min: 0.1, max: 300 }, { key: "weight", unit: "kg", min: 0.1, max: 500 }].map(({ key, unit, min, max }) => (
                    <label className="form-field" key={key}>
                      <span>{labels[key]} ({unit}) <span className="required">*</span></span>
                      <input type="number" required min={min} max={max} step="0.1" value={draft[key]} onChange={(e) => setDraft({ ...draft, [key]: e.target.value })} aria-describedby={`${key}-hint`} />
                      <small id={`${key}-hint`}>Từ {min} đến {max} {unit}.</small>
                    </label>
                  ))}
                  <label className="form-field"><span>Nhóm máu</span><select value={draft.bloodGroup} onChange={(e) => setDraft({ ...draft, bloodGroup: e.target.value })}><option value="">Chưa xác định</option>{bloodGroups.map((group) => <option key={group}>{group}</option>)}</select></label>
                  {["allergies", "conditions", "vaccinations"].map((key) => <label className="form-field" key={key}><span>{labels[key]}</span><textarea rows={3} maxLength={2000} value={draft[key]} onChange={(e) => setDraft({ ...draft, [key]: e.target.value })} placeholder={key === "vaccinations" ? "Tên vắc-xin, số mũi và ngày tiêm (nếu biết)" : "Ghi rõ thông tin hoặc nhập ‘Không có’"} /></label>)}
                </fieldset>
                <p className="form-note">Thông tin được lưu vào hồ sơ sức khỏe của tài khoản đang đăng nhập.</p>
                <div className="dialog-actions"><button type="button" className="button secondary" disabled={saving} onClick={finish}>Hủy thay đổi</button><button type="submit" disabled={saving} className="button primary">{saving ? "Đang lưu…" : "Lưu hồ sơ"}</button></div>
              </form>
            ) : (
              <dl className="health-details-grid">
                <Detail label="Nhóm máu">{profile.bloodGroup || "Chưa xác định"}</Detail>
                <Detail label="Chiều cao">{profile.height ? `${profile.height} cm` : null}</Detail>
                <Detail label="Cân nặng">{profile.weight ? `${Number(profile.weight).toLocaleString("vi-VN")} kg` : null}</Detail>
                <Detail label="Dị ứng">{profile.allergies}</Detail>
                <Detail label="Bệnh nền">{profile.conditions}</Detail>
                <Detail label="Tiêm chủng">{profile.vaccinations}</Detail>
              </dl>
            )}
            <BmiMeter height={profile.height} weight={profile.weight} saved={Boolean(message)} />
          </section>

          <section className="data-panel health-insurance" aria-labelledby="insurance-heading">
            <div className="panel-heading">
              <div>
                <h2 id="insurance-heading">Thông tin cá nhân và bảo hiểm</h2>
              </div>
              <button ref={editHolderButton} type="button" className="health-edit-link" disabled={loading || savingHolder || savingInsurance} onClick={openInsuranceModal}>
                <Pencil size={14} aria-hidden="true" />Cập nhật
              </button>
            </div>
            {personalInfoError && <p role="alert" className="feedback feedback-error">{personalInfoError}</p>}
              <dl className="health-details-grid">
                <Detail label="Họ tên">{name}</Detail>
                <Detail label="Mã BHXH">{insurance?.socialInsuranceNumber}</Detail>
                <Detail label="Ngày sinh">{student?.dateOfBirth ? formatInsuranceDate(student.dateOfBirth) : null}</Detail>
                <Detail label="ĐDCN/CCCD/Hộ chiếu">{student?.identityNumber}</Detail>
                <Detail label="Số điện thoại">{student?.phone}</Detail>
                <Detail label="Địa chỉ">{student?.address}</Detail>
              </dl>

            {insuranceLoadError && <p role="alert" className="feedback feedback-error">{insuranceLoadError}</p>}
            <div className="insurance-toggle-list">
              <HealthInsuranceCard insurance={insurance} />
            </div>
          </section>
        </div>
      </div>

      <Modal
        isOpen={editingInsurance}
        onClose={closeInsuranceModal}
        busy={savingInsurance || savingHolder}
        title="Cập nhật thông tin cá nhân và bảo hiểm"
        className="insurance-edit-modal"
      >
        <h3>Thông tin cá nhân</h3>
        {personalInfoError && <p role="alert" className="feedback feedback-error">{personalInfoError}</p>}
              <form className="health-form" onSubmit={saveHolder}>
                <fieldset className="health-form-grid" disabled={savingHolder || savingInsurance}>
                  <label className="form-field">
                    <span>Họ tên <span className="required">*</span></span>
                    <input
                      type="text"
                      required
                      maxLength={100}
                      value={holderDraft.fullName}
                      onChange={(e) => setHolderDraft({ ...holderDraft, fullName: e.target.value })}
                    />
                  </label>
                  <label className="form-field">
                    <span>Ngày sinh <span className="required">*</span></span>
                    <input
                      type="date"
                      required
                      value={holderDraft.dateOfBirth}
                      onChange={(e) => setHolderDraft({ ...holderDraft, dateOfBirth: e.target.value })}
                    />
                  </label>
                  <label className="form-field">
                    <span>Số điện thoại <span className="required">*</span></span>
                    <input
                      type="tel"
                      required
                      maxLength={15}
                      value={holderDraft.phone}
                      onChange={(e) => setHolderDraft({ ...holderDraft, phone: e.target.value })}
                      placeholder="VD: 090 123 4567"
                    />
                  </label>
                  <label className="form-field"><span>Email</span><input type="email" maxLength={100} value={holderDraft.email} onChange={(e) => setHolderDraft({ ...holderDraft, email: e.target.value })} /></label>
                  <label className="form-field"><span>Giới tính</span><select value={holderDraft.gender} onChange={(e) => setHolderDraft({ ...holderDraft, gender: e.target.value })}><option value="">Chưa cập nhật</option><option value="Nam">Nam</option><option value="Nữ">Nữ</option><option value="Khác">Khác</option></select></label>
                </fieldset>
                <div className="dialog-actions">
                  <button type="button" className="button secondary" disabled={savingHolder || savingInsurance} onClick={closeInsuranceModal}>Hủy thay đổi</button>
                  <button type="submit" disabled={savingHolder || savingInsurance} className="button primary">{savingHolder ? "Đang lưu…" : "Lưu thông tin"}</button>
                </div>
              </form>
        <h3>Thẻ BHYT</h3>
        <form className="record-form" onSubmit={saveInsurance}>
          {insuranceError && <div role="alert" className="feedback feedback-error">{insuranceError}</div>}
          <fieldset className="insurance-modal-grid" disabled={savingInsurance}>
            <label className="form-field">
              <span>Số thẻ BHYT <span className="required">*</span></span>
              <input
                type="text"
                required
                maxLength={15}
                value={insuranceDraft.cardNumber}
                onChange={(e) => setInsuranceDraft({ ...insuranceDraft, cardNumber: e.target.value.toUpperCase() })}
                placeholder="VD: SV4790123456789"
                aria-describedby="bhyt-card-hint"
              />
              <small id="bhyt-card-hint">10 chữ số hoặc 15 ký tự (2 chữ cái đầu + 13 chữ số).</small>
            </label>
            <label className="form-field">
              <span>Nơi cấp, đổi thẻ BHYT</span>
              <input
                type="text"
                maxLength={120}
                value={insuranceDraft.issuedAt}
                onChange={(e) => setInsuranceDraft({ ...insuranceDraft, issuedAt: e.target.value })}
                placeholder="VD: TP. Hồ Chí Minh"
              />
            </label>
            <label className="form-field">
              <span>Ngày có hiệu lực <span className="required">*</span></span>
              <input
                type="date"
                required
                value={insuranceDraft.validFrom}
                onChange={(e) => setInsuranceDraft({ ...insuranceDraft, validFrom: e.target.value })}
              />
            </label>
            <label className="form-field">
              <span>Ngày hết hạn <span className="required">*</span></span>
              <input
                type="date"
                required
                min={insuranceDraft.validFrom || undefined}
                value={insuranceDraft.validUntil}
                onChange={(e) => setInsuranceDraft({ ...insuranceDraft, validUntil: e.target.value })}
              />
            </label>
            <label className="form-field">
              <span>Nơi ĐK KCB ban đầu <span className="required">*</span></span>
              <input
                type="text"
                required
                maxLength={150}
                value={insuranceDraft.initialProvider}
                onChange={(e) => setInsuranceDraft({ ...insuranceDraft, initialProvider: e.target.value })}
                placeholder="VD: Trung tâm Y tế Đại học"
              />
            </label>
          </fieldset>
          <p className="form-note">Trạng thái hiệu lực của thẻ BHYT sẽ tự động tính theo ngày có hiệu lực và ngày hết hạn.</p>
          <div className="dialog-actions">
            <button type="button" className="button secondary" disabled={savingInsurance} onClick={closeInsuranceModal}>Hủy</button>
            <button type="submit" disabled={savingInsurance} className="button primary">{savingInsurance ? "Đang lưu…" : "Lưu thẻ BHYT"}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
