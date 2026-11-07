import { AlertTriangle, CheckCircle, Heart, Mail, Phone, ShieldCheck, User, X } from "lucide-react";
import { formatDay } from "../../utils/appointments";

export default function StudentFullProfileModal({ isOpen, appointment, onClose }) {
  if (!isOpen || !appointment) return null;

  const studentName = appointment.studentName || appointment.full_name || "Sinh viên";
  const studentCode = appointment.studentCode || appointment.student_code || "";
  const studentEmail = appointment.studentEmail || appointment.student_email || `${studentCode}@student.edu.vn`;
  const studentPhone = appointment.studentPhone || appointment.phone_number || "Chưa cập nhật";
  const gender = appointment.gender || "Chưa cập nhật";
  const dateOfBirth = appointment.dateOfBirth || appointment.date_of_birth;
  const classCode = appointment.classCode || appointment.class_code || "Chưa cập nhật";
  const className = appointment.className || appointment.class_name || "";
  const facultyName = appointment.facultyName || appointment.faculty_name || "Chưa cập nhật";
  const health = appointment.health_records || {};

  const initials = studentName
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((p) => p[0])
    .join("");

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fadeIn"
      role="dialog"
      aria-modal="true"
      aria-labelledby="student-profile-modal-title"
    >
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[92vh] flex flex-col overflow-hidden border border-slate-100">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-full bg-blue-100 text-blue-700 font-bold flex items-center justify-center text-base">
              {initials || "SV"}
            </div>
            <div>
              <h2 id="student-profile-modal-title" className="text-lg font-bold text-slate-800">
                {studentName}
              </h2>
              <p className="text-xs text-slate-500 font-mono">
                MSSV: {studentCode} · {facultyName}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-200/60 transition"
            aria-label="Đóng"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable body */}
        <div className="p-6 overflow-y-auto space-y-6">
          {/* Thông tin cá nhân & học vụ */}
          <div>
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5" /> Thông tin cá nhân & học vụ
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-sm">
              <div className="p-3 bg-slate-50 rounded-xl">
                <span className="text-xs text-slate-400 block mb-0.5">Giới tính</span>
                <span className="font-semibold text-slate-800">{gender}</span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl">
                <span className="text-xs text-slate-400 block mb-0.5">Ngày sinh</span>
                <span className="font-semibold text-slate-800">{formatDay(dateOfBirth)}</span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl">
                <span className="text-xs text-slate-400 block mb-0.5">Số điện thoại</span>
                <span className="font-semibold text-slate-800 flex items-center gap-1">
                  <Phone className="w-3 h-3 text-slate-400" />
                  {studentPhone}
                </span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl">
                <span className="text-xs text-slate-400 block mb-0.5">Lớp sinh hoạt</span>
                <span className="font-semibold text-slate-800">{classCode}</span>
                {className && <small className="block text-[11px] text-slate-500">{className}</small>}
              </div>
              <div className="p-3 bg-slate-50 rounded-xl col-span-2">
                <span className="text-xs text-slate-400 block mb-0.5">Email trường</span>
                <span className="font-semibold text-slate-800 flex items-center gap-1">
                  <Mail className="w-3 h-3 text-slate-400" />
                  {studentEmail}
                </span>
              </div>
            </div>
          </div>

          {/* Hồ sơ sức khỏe thể chất */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                <Heart className="w-3.5 h-3.5 text-rose-500" /> Chỉ số sinh học & thể lực
              </h3>
              <span className="text-xs text-slate-400">Cập nhật: {health.updated_at || "02/03/2026"}</span>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-xs text-slate-500 block mb-0.5">Chiều cao</span>
                <span className="text-base font-bold text-slate-800">{health.height ? `${health.height} cm` : "—"}</span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-xs text-slate-500 block mb-0.5">Cân nặng</span>
                <span className="text-base font-bold text-slate-800">{health.weight ? `${health.weight} kg` : "—"}</span>
              </div>
              <div className="p-3 bg-blue-50/70 rounded-xl border border-blue-100">
                <span className="text-xs text-blue-600 block mb-0.5">Chỉ số BMI</span>
                <span className="text-base font-bold text-blue-900">
                  {health.bmi || "—"}{" "}
                  <span className="text-xs font-normal text-blue-700">({health.bmi_classification || health.bmiClassification || "Bình thường"})</span>
                </span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-xs text-slate-500 block mb-0.5">Nhóm máu</span>
                <span className="text-base font-bold text-rose-600">{health.blood_type || health.bloodType || "—"}</span>
              </div>
            </div>
          </div>

          {/* Cảnh báo dị ứng (nếu có) */}
          {health.allergies && health.allergies !== "Không ghi nhận" ? (
            <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl space-y-1 text-rose-900">
              <div className="flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0" />
                <h4 className="font-bold text-sm">
                  {health.allergy_alert_title || `Dị ứng: ${health.allergies}`}
                </h4>
              </div>
              <p className="text-xs text-rose-700 pl-7">
                {health.allergy_alert_desc || health.allergies}
              </p>
            </div>
          ) : (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-800 text-xs flex items-center gap-2">
              <CheckCircle className="w-4 h-4 text-emerald-600 flex-shrink-0" />
              <span>Không ghi nhận tiền sử dị ứng thuốc hay thực phẩm.</span>
            </div>
          )}

          {/* Bệnh lý nền & Tiền sử */}
          <div className="space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-blue-600" /> Bệnh nền & Lịch sử y khoa
            </h3>
            <div className="space-y-2 text-sm">
              <div className="p-3 bg-slate-50 rounded-xl flex items-start justify-between">
                <div>
                  <span className="text-xs font-semibold text-slate-500 block">Bệnh nền mạn tính:</span>
                  <span className="text-slate-800">{health.underlying_diseases || health.underlyingDiseases || "Không ghi nhận"}</span>
                </div>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl flex items-start justify-between">
                <div>
                  <span className="text-xs font-semibold text-slate-500 block">Tiền sử tiêm chủng:</span>
                  <span className="text-slate-800">{health.vaccination_history || "Đã tiêm đủ các mũi cơ bản"}</span>
                </div>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl flex items-start justify-between">
                <div>
                  <span className="text-xs font-semibold text-slate-500 block">Tiền sử bệnh án / phẫu thuật:</span>
                  <span className="text-slate-800">{health.medical_history || "Không có"}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end px-6 py-3.5 border-t border-slate-100 bg-slate-50">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 text-sm font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-100 rounded-xl transition"
          >
            Đóng hồ sơ
          </button>
        </div>
      </div>
    </div>
  );
}
