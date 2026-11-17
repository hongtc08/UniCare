import { useState, useEffect } from "react";
import {
  AlertTriangle,
  Heart,
  ShieldCheck,
  Clock,
  Activity,
  AlertCircle,
  Phone,
  Mail,
  GraduationCap,
} from "lucide-react";
import { medicalRecordApi } from "../../api/medicalRecordApi";

/**
 * Component hiển thị Hồ sơ Y tế Tổng hợp của Sinh viên cho Bác sĩ (FE2 / BE3).
 * Đóng vai trò wrapper độc lập, sẵn sàng tích hợp hoặc thay thế bởi PatientOverviewPanel từ FE1.
 *
 * @param {Object} props
 * @param {number|string} [props.studentId]
 * @param {Object} [props.fallbackData] - Thông tin dự phòng từ Checkup Detail hoặc Appointment
 */
export default function PatientOverviewWrapper({ studentId, fallbackData = {} }) {
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(() => Boolean(studentId));

  useEffect(() => {
    let active = true;

    if (!studentId) {
      return () => {
        active = false;
      };
    }

    medicalRecordApi
      .getStudentMedicalOverview(studentId)
      .then((data) => {
        if (!active) return;
        setOverview(data);
      })
      .catch((err) => {
        if (!active) return;
        console.warn("Không thể tải hồ sơ tổng hợp sinh viên:", err);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [studentId]);

  // Dữ liệu hợp nhất từ overview API và fallbackData
  const studentInfo = overview?.studentInfo || {};
  const healthProfile = overview?.healthProfile || {};
  const healthInsurance = overview?.healthInsurance || {};
  const previousCheckups = overview?.previousCheckups || overview?.checkupHistory || [];

  const displayName =
    studentInfo.fullName || fallbackData.studentName || "Sinh viên";
  const displayCode =
    studentInfo.studentCode || fallbackData.studentCode || "—";
  const displayClass =
    studentInfo.className || fallbackData.className || "—";
  const displayFaculty =
    studentInfo.facultyName || fallbackData.facultyName || "—";
  const displayPhone =
    studentInfo.phoneNumber || fallbackData.studentPhone || "—";
  const displayEmail =
    studentInfo.email || fallbackData.studentEmail || "";

  // Phân loại BMI
  const getBmiBadge = (bmi, classification) => {
    if (!bmi) return null;
    const num = Number(bmi);
    let colorClass = "bg-emerald-50 text-emerald-700 border-emerald-200";
    if (num < 18.5) {
      colorClass = "bg-amber-50 text-amber-700 border-amber-200";
    } else if (num >= 25) {
      colorClass = "bg-rose-50 text-rose-700 border-rose-200";
    }

    return (
      <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold border ${colorClass}`}>
        BMI {num.toFixed(1)} {classification ? `· ${classification}` : ""}
      </span>
    );
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs divide-y divide-slate-100 overflow-hidden">
      {/* 1. HEADER: THÔNG TIN CÁ NHÂN & HỌC VỤ */}
      <div className="p-4 bg-slate-50/60">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-full bg-blue-600 text-white font-bold text-sm flex items-center justify-center shrink-0 shadow-xs">
            {displayName
              .split(" ")
              .filter(Boolean)
              .slice(-2)
              .map((w) => w[0])
              .join("")
              .toUpperCase()}
          </div>
          <div className="min-w-0 flex-1">
            <h3 className="text-sm font-bold text-slate-800 truncate" title={displayName}>
              {displayName}
            </h3>
            <div className="flex items-center gap-2 text-xs text-slate-500 mt-0.5">
              <span className="font-semibold text-blue-700 bg-blue-50 px-1.5 py-0.2 rounded border border-blue-200/60">
                MSSV: {displayCode}
              </span>
              <span>·</span>
              <span className="truncate">{displayClass}</span>
            </div>
          </div>
        </div>

        <div className="mt-3 pt-3 border-t border-slate-200/60 grid grid-cols-2 gap-2 text-xs text-slate-600">
          <div className="flex items-center gap-1.5 truncate">
            <GraduationCap className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="truncate" title={displayFaculty}>
              {displayFaculty}
            </span>
          </div>
          <div className="flex items-center gap-1.5 truncate">
            <Phone className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="truncate">{displayPhone}</span>
          </div>
        </div>
        {displayEmail && (
          <div className="mt-1.5 flex items-center gap-1.5 text-xs text-slate-500 truncate">
            <Mail className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="truncate" title={displayEmail}>{displayEmail}</span>
          </div>
        )}
      </div>

      {loading ? (
        <div className="p-6 text-center text-xs text-slate-400 flex flex-col items-center justify-center gap-2">
          <div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
          <span>Đang nạp hồ sơ y tế sinh viên...</span>
        </div>
      ) : (
        <>
          {/* 2. CẢNH BÁO DỊ ỨNG & BỆNH NỀN (QUAN TRỌNG NHẤT CHO BÁC SĨ) */}
          <div className="p-4 space-y-2.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-700 uppercase tracking-wider">
              <AlertTriangle className="w-4 h-4 text-amber-500" />
              <span>Tiền sử Dị ứng & Bệnh nền</span>
            </div>

            {/* Dị ứng */}
            {healthProfile.allergies ? (
              <div className="p-2.5 rounded-lg bg-rose-50 border border-rose-200/80 text-xs">
                <span className="font-bold text-rose-800 block mb-0.5 flex items-center gap-1">
                  <AlertCircle className="w-3.5 h-3.5 text-rose-600" />
                  Dị ứng đã ghi nhận:
                </span>
                <p className="text-rose-700 font-medium">{healthProfile.allergies}</p>
              </div>
            ) : (
              <div className="p-2 rounded-lg bg-emerald-50/70 border border-emerald-200/60 text-[11px] text-emerald-800 flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                <span>Chưa ghi nhận dị ứng thuốc hoặc thực phẩm</span>
              </div>
            )}

            {/* Bệnh nền / Mãn tính */}
            {healthProfile.underlyingDiseases ? (
              <div className="p-2.5 rounded-lg bg-amber-50 border border-amber-200/80 text-xs">
                <span className="font-bold text-amber-800 block mb-0.5">
                  Bệnh nền / Mãn tính:
                </span>
                <p className="text-amber-700 font-medium">
                  {healthProfile.underlyingDiseases}
                </p>
              </div>
            ) : null}
          </div>

          {/* 3. THỂ TRẠNG & CHỈ SỐ BMI */}
          <div className="p-4 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                <Activity className="w-3.5 h-3.5 text-blue-600" />
                Thể trạng & BMI
              </span>
              {getBmiBadge(healthProfile.bmi, healthProfile.bmiClassification)}
            </div>

            <div className="grid grid-cols-3 gap-2 pt-1 text-center">
              <div className="p-2 bg-slate-50 rounded-lg border border-slate-100">
                <span className="text-[10px] text-slate-400 block">Chiều cao</span>
                <span className="text-xs font-bold text-slate-800">
                  {healthProfile.height ? `${healthProfile.height} cm` : "—"}
                </span>
              </div>
              <div className="p-2 bg-slate-50 rounded-lg border border-slate-100">
                <span className="text-[10px] text-slate-400 block">Cân nặng</span>
                <span className="text-xs font-bold text-slate-800">
                  {healthProfile.weight ? `${healthProfile.weight} kg` : "—"}
                </span>
              </div>
              <div className="p-2 bg-slate-50 rounded-lg border border-slate-100">
                <span className="text-[10px] text-slate-400 block">Nhóm máu</span>
                <span className="text-xs font-bold text-rose-600">
                  {healthProfile.bloodType || "—"}
                </span>
              </div>
            </div>
          </div>

          {/* 4. BẢO HIỂM Y TẾ (BHYT) */}
          <div className="p-4 space-y-1.5 text-xs">
            <div className="flex items-center justify-between">
              <span className="font-bold text-slate-700 uppercase tracking-wider text-[11px] flex items-center gap-1.5">
                <Heart className="w-3.5 h-3.5 text-red-500" />
                Thẻ BHYT
              </span>
              <span
                className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                  healthInsurance.status === "VALID" || !healthInsurance.status
                    ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                    : "bg-rose-50 text-rose-700 border border-rose-200"
                }`}
              >
                {healthInsurance.statusDescription ||
                  (healthInsurance.cardNumber ? "Hợp lệ" : "Chưa có")}
              </span>
            </div>
            <div className="text-slate-600 font-mono text-xs font-semibold">
              {healthInsurance.cardNumber || "Chưa cập nhật số thẻ"}
            </div>
            {healthInsurance.initialHospital && (
              <p className="text-[11px] text-slate-500 truncate" title={healthInsurance.initialHospital}>
                KCB: {healthInsurance.initialHospital}
              </p>
            )}
          </div>

          {/* 5. LỊCH SỬ CÁC LẦN KHÁM TRƯỚC */}
          <div className="p-4 space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-bold text-slate-700 uppercase tracking-wider text-[11px] flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-slate-500" />
                Lịch sử khám ({previousCheckups.length})
              </span>
            </div>

            {previousCheckups.length === 0 ? (
              <p className="text-[11px] text-slate-400 italic">
                Chưa có lịch sử khám bệnh trước đây
              </p>
            ) : (
              <div className="space-y-1.5 max-h-40 overflow-y-auto pr-1">
                {previousCheckups.slice(0, 3).map((item) => (
                  <div
                    key={item.id || item.checkupCode}
                    className="p-2 rounded-lg bg-slate-50 border border-slate-100 text-xs"
                  >
                    <div className="flex items-center justify-between text-[11px] text-slate-400">
                      <span>{item.checkupCode || "Phiếu khám"}</span>
                      <span>
                        {item.completedAt
                          ? new Date(item.completedAt).toLocaleDateString("vi-VN")
                          : "Đang xử lý"}
                      </span>
                    </div>
                    <p className="font-medium text-slate-700 truncate mt-0.5">
                      {item.diagnosisNotes || item.diagnosis || "Chưa có kết luận"}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}

      {/* FOOTER: GHI CHÚ TÍCH HỢP */}
      <div className="p-3 bg-slate-50/80 text-[10px] text-slate-400 text-center italic">
        UniCare · Hồ sơ Y tế Sinh viên tích hợp
      </div>
    </div>
  );
}
