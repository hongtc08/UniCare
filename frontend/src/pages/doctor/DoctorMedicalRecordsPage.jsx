import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  Search,
  User,
  ClipboardList,
  FileText,
  AlertTriangle,
  ShieldCheck,
  RefreshCw,
  GraduationCap,
  Heart,
  Stethoscope,
  Brain,
  ClipboardCheck,
  CheckCircle2,
  Clock,
  X,
  ExternalLink,
} from "lucide-react";
import { medicalRecordApi } from "../../api/medicalRecordApi";
import { isPsychologicalCheckup } from "../../utils/checkupValidation";

function getInitials(name) {
  if (!name) return "SV";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((p) => p[0])
    .join("")
    .toUpperCase();
}

function formatDateTime(value) {
  if (!value) return "—";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString("vi-VN");
}

function renderCheckupTypeBadge(item) {
  if (item.isEmergency) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-rose-50 text-rose-700 border border-rose-200">
        <AlertTriangle className="w-3 h-3" />
        Cấp cứu
      </span>
    );
  }
  if (isPsychologicalCheckup(item.checkupType)) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-purple-50 text-purple-700 border border-purple-200">
        <Brain className="w-3 h-3" />
        Tư vấn tâm lý
      </span>
    );
  }
  if (item.isPeriodic) {
    const resultUpper = String(item.periodicResult || "").toUpperCase();
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
        <ClipboardCheck className="w-3 h-3" />
        Khám định kỳ
        {resultUpper === "PASSED"
          ? " · Đạt"
          : resultUpper === "FAILED"
          ? " · Không đạt"
          : ""}
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-blue-50 text-blue-700 border border-blue-200">
      <Stethoscope className="w-3 h-3" />
      Khám đa khoa
    </span>
  );
}

function renderStatusBadge(status) {
  if (status === "COMPLETED") {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
        <CheckCircle2 className="w-3 h-3 text-emerald-600" />
        Đã hoàn tất
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-amber-100 text-amber-800 border border-amber-200">
      <Clock className="w-3 h-3 text-amber-600" />
      Bản nháp
    </span>
  );
}

/**
 * Trang Hồ sơ bệnh án dành cho Bác sĩ / Nhân viên y tế (/medical-records).
 * Cho phép tra cứu sinh viên, xem tóm tắt hồ sơ sức khỏe và mở lại từng phiếu khám / in giấy chuyển viện.
 */
export default function DoctorMedicalRecordsPage() {
  const navigate = useNavigate();

  // Tìm kiếm sinh viên (Cột trái)
  const [keyword, setKeyword] = useState("");
  const [lastSearchedKeyword, setLastSearchedKeyword] = useState("");
  const [students, setStudents] = useState([]);
  const [studentsLoading, setStudentsLoading] = useState(true);
  const [studentsError, setStudentsError] = useState(null);

  // Sinh viên đang chọn & Hồ sơ tổng hợp (Cột phải)
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [overview, setOverview] = useState(null);
  const [overviewLoading, setOverviewLoading] = useState(false);
  const [overviewError, setOverviewError] = useState(null);

  const searchRequestRef = useRef(0);
  const overviewRequestRef = useRef(0);

  // Tải danh sách sinh viên ban đầu khi mở trang
  useEffect(() => {
    let active = true;
    const reqId = ++searchRequestRef.current;

    medicalRecordApi
      .searchStudents("")
      .then((list) => {
        if (!active || reqId !== searchRequestRef.current) return;
        setStudents(Array.isArray(list) ? list : []);
        setStudentsError(null);
      })
      .catch((err) => {
        if (!active || reqId !== searchRequestRef.current) return;
        setStudentsError(
          err?.response?.data?.message ||
            err?.message ||
            "Không thể tải danh sách sinh viên."
        );
      })
      .finally(() => {
        if (active && reqId === searchRequestRef.current) {
          setStudentsLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const executeStudentSearch = (searchTerm) => {
    const reqId = ++searchRequestRef.current;
    const trimmed = searchTerm.trim();
    setLastSearchedKeyword(trimmed);
    setStudentsLoading(true);
    setStudentsError(null);

    medicalRecordApi
      .searchStudents(trimmed)
      .then((list) => {
        if (reqId !== searchRequestRef.current) return;
        setStudents(Array.isArray(list) ? list : []);
      })
      .catch((err) => {
        if (reqId !== searchRequestRef.current) return;
        setStudentsError(
          err?.response?.data?.message ||
            err?.message ||
            "Không thể tra cứu danh sách sinh viên."
        );
      })
      .finally(() => {
        if (reqId === searchRequestRef.current) {
          setStudentsLoading(false);
        }
      });
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    executeStudentSearch(keyword);
  };

  const handleClearSearch = () => {
    setKeyword("");
    executeStudentSearch("");
  };

  const fetchStudentOverview = (student) => {
    if (!student?.studentId) return;
    const reqId = ++overviewRequestRef.current;
    setSelectedStudent(student);
    setOverviewLoading(true);
    setOverviewError(null);

    medicalRecordApi
      .getStudentMedicalOverview(student.studentId)
      .then((data) => {
        if (reqId !== overviewRequestRef.current) return;
        setOverview(data || null);
      })
      .catch((err) => {
        if (reqId !== overviewRequestRef.current) return;
        setOverview(null);
        setOverviewError(
          err?.response?.data?.message ||
            err?.message ||
            "Không thể tải hồ sơ bệnh án của sinh viên này."
        );
      })
      .finally(() => {
        if (reqId === overviewRequestRef.current) {
          setOverviewLoading(false);
        }
      });
  };

  const studentInfo = overview?.studentInfo || selectedStudent || {};
  const healthProfile = overview?.healthProfile || {};
  const healthInsurance = overview?.healthInsurance || {};
  const previousCheckups =
    overview?.previousCheckups || overview?.checkupHistory || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 space-y-4">
      {/* HEADER */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
            Hồ sơ bệnh án Sinh viên
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Tra cứu lịch sử khám bệnh, tư vấn tâm lý, cấp cứu và in lại giấy giới thiệu chuyển viện.
          </p>
        </div>

        <button
          type="button"
          onClick={() => executeStudentSearch(lastSearchedKeyword)}
          disabled={studentsLoading}
          className="self-start sm:self-auto inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg border border-slate-200 transition-colors disabled:opacity-50"
          title="Làm mới danh sách sinh viên"
        >
          <RefreshCw
            className={`w-3.5 h-3.5 ${studentsLoading ? "animate-spin text-blue-600" : ""}`}
          />
          <span>Làm mới</span>
        </button>
      </div>

      {/* BỐ CỤC 2 CỘT */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-start">
        {/* CỘT TRÁI (4 Cột): TÌM KIẾM & DANH SÁCH SINH VIÊN */}
        <div className="lg:col-span-4 bg-white rounded-xl border border-slate-200/80 shadow-xs overflow-hidden">
          <div className="p-3.5 border-b border-slate-100 bg-slate-50/60 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                <User className="w-3.5 h-3.5 text-blue-600" />
                Tra cứu sinh viên
              </span>
              {!studentsLoading && !studentsError && (
                <span className="text-[11px] font-medium text-slate-500">
                  {students.length} kết quả
                </span>
              )}
            </div>

            <form onSubmit={handleSearchSubmit} className="flex items-center gap-2">
              <div className="relative flex-1">
                <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                <input
                  type="text"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="Nhập MSSV, họ tên hoặc lớp..."
                  className="w-full pl-8 pr-7 py-1.5 text-xs bg-white border border-slate-200 rounded-lg text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                />
                {keyword && (
                  <button
                    type="button"
                    onClick={handleClearSearch}
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    title="Xóa từ khóa"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
              <button
                type="submit"
                disabled={studentsLoading}
                className="px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50 shrink-0"
              >
                Tìm
              </button>
            </form>
          </div>

          <div className="divide-y divide-slate-100 max-h-[640px] overflow-y-auto">
            {studentsLoading ? (
              <div className="p-4 space-y-3">
                {[1, 2, 3, 4].map((i) => (
                  <div key={i} className="flex items-center gap-3 animate-pulse">
                    <div className="w-9 h-9 rounded-full bg-slate-200 shrink-0" />
                    <div className="flex-1 space-y-1.5">
                      <div className="w-32 h-3 bg-slate-200 rounded" />
                      <div className="w-24 h-2.5 bg-slate-100 rounded" />
                    </div>
                  </div>
                ))}
              </div>
            ) : studentsError ? (
              <div className="p-6 text-center space-y-2">
                <AlertTriangle className="w-7 h-7 mx-auto text-rose-500" />
                <p className="text-xs font-semibold text-rose-600">{studentsError}</p>
                <button
                  type="button"
                  onClick={() => executeStudentSearch(lastSearchedKeyword)}
                  className="px-3 py-1.5 text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors"
                >
                  Thử lại
                </button>
              </div>
            ) : students.length === 0 ? (
              <div className="p-8 text-center text-slate-400 space-y-1.5">
                <User className="w-8 h-8 mx-auto text-slate-300" />
                <p className="text-xs font-semibold text-slate-600">
                  Không tìm thấy sinh viên phù hợp
                </p>
                <p className="text-[11px] text-slate-400">
                  Hãy thử tìm theo MSSV, họ tên đầy đủ hoặc mã lớp.
                </p>
              </div>
            ) : (
              students.map((st) => {
                const isSelected =
                  Number(selectedStudent?.studentId) === Number(st.studentId);
                return (
                  <button
                    key={st.studentId}
                    type="button"
                    onClick={() => fetchStudentOverview(st)}
                    className={`w-full p-3.5 text-left transition flex items-center gap-3 ${
                      isSelected
                        ? "border-l-4 border-l-blue-600 bg-blue-50/50"
                        : "border-l-4 border-l-transparent hover:bg-slate-50"
                    }`}
                  >
                    <div
                      className={`w-9 h-9 rounded-full font-bold text-xs flex items-center justify-center shrink-0 ${
                        isSelected
                          ? "bg-blue-600 text-white"
                          : "bg-blue-100 text-blue-700"
                      }`}
                    >
                      {getInitials(st.fullName)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <h3 className="text-xs sm:text-sm font-semibold text-slate-900 truncate">
                        {st.fullName || "Chưa cập nhật tên"}
                      </h3>
                      <div className="flex items-center gap-1.5 text-[11px] text-slate-500 mt-0.5 truncate">
                        <span className="font-mono font-semibold text-blue-700">
                          {st.studentCode || st.username || `#${st.studentId}`}
                        </span>
                        {(st.classCode || st.className) && (
                          <>
                            <span>·</span>
                            <span className="truncate">
                              {st.classCode || st.className}
                            </span>
                          </>
                        )}
                      </div>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* CỘT PHẢI (8 Cột): CHI TIẾT HỒ SƠ & DANH SÁCH PHIẾU KHÁM */}
        <div className="lg:col-span-8 space-y-4">
          {!selectedStudent ? (
            <div className="bg-white rounded-xl border border-slate-200/80 shadow-xs p-14 text-center text-slate-400 space-y-3">
              <ClipboardList className="w-12 h-12 mx-auto text-slate-300 stroke-1" />
              <h2 className="text-base font-semibold text-slate-700">
                Chưa chọn sinh viên
              </h2>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                Tìm kiếm và chọn một sinh viên ở danh sách bên trái để xem toàn bộ lịch sử phiếu khám bệnh, phiếu tư vấn tâm lý và hồ sơ chuyển viện.
              </p>
            </div>
          ) : overviewLoading ? (
            <div className="bg-white rounded-xl border border-slate-200/80 shadow-xs p-14 text-center text-slate-500 text-xs flex flex-col items-center justify-center gap-3">
              <div className="w-7 h-7 border-3 border-blue-600 border-t-transparent rounded-full animate-spin" />
              <span>Đang tải hồ sơ bệnh án sinh viên...</span>
            </div>
          ) : overviewError ? (
            <div className="bg-white rounded-xl border border-rose-200 shadow-xs p-10 text-center space-y-3">
              <AlertTriangle className="w-9 h-9 mx-auto text-rose-500" />
              <p className="text-sm font-semibold text-rose-700">{overviewError}</p>
              <button
                type="button"
                onClick={() => fetchStudentOverview(selectedStudent)}
                className="px-4 py-2 text-xs font-semibold bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 rounded-lg transition-colors"
              >
                Thử lại
              </button>
            </div>
          ) : (
            <>
              {/* TÓM TẮT SINH VIÊN + CẢNH BÁO DỊ ỨNG + BHYT */}
              <div className="bg-white rounded-xl border border-slate-200/80 shadow-xs p-5 space-y-4">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="flex items-center gap-3.5">
                    <div className="w-12 h-12 rounded-full bg-blue-600 text-white font-bold text-base flex items-center justify-center shrink-0 shadow-xs">
                      {getInitials(studentInfo.fullName)}
                    </div>
                    <div>
                      <h2 className="text-base font-bold text-slate-900">
                        {studentInfo.fullName || "Sinh viên"}
                      </h2>
                      <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500 mt-0.5">
                        <span className="font-mono font-semibold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-200/60">
                          MSSV: {studentInfo.studentCode || "—"}
                        </span>
                        {(studentInfo.className || studentInfo.classCode) && (
                          <span className="inline-flex items-center gap-1">
                            <GraduationCap className="w-3.5 h-3.5 text-slate-400" />
                            {studentInfo.className || studentInfo.classCode}
                            {studentInfo.facultyName
                              ? ` · ${studentInfo.facultyName}`
                              : ""}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Trạng thái BHYT */}
                  <div className="flex items-center gap-2 text-xs bg-slate-50 px-3 py-2 rounded-lg border border-slate-200/80">
                    <Heart className="w-4 h-4 text-rose-500 shrink-0" />
                    <div>
                      <span className="text-[10px] text-slate-400 block uppercase font-semibold">
                        Thẻ BHYT
                      </span>
                      <span className="font-semibold text-slate-800 font-mono">
                        {healthInsurance.cardNumber || "Chưa cập nhật"}
                      </span>
                    </div>
                    {healthInsurance.cardNumber && (
                      <span
                        className={`ml-1 px-2 py-0.5 rounded text-[10px] font-bold border ${
                          healthInsurance.status === "VALID" || !healthInsurance.status
                            ? "bg-emerald-50 text-emerald-700 border-emerald-200"
                            : "bg-rose-50 text-rose-700 border-rose-200"
                        }`}
                      >
                        {healthInsurance.statusDescription || "Hợp lệ"}
                      </span>
                    )}
                  </div>
                </div>

                {/* Dải thông tin dị ứng */}
                {healthProfile.allergies ? (
                  <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-xs flex items-start gap-2 text-rose-800">
                    <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
                    <div>
                      <span className="font-bold">Cảnh báo dị ứng: </span>
                      <span>{healthProfile.allergies}</span>
                    </div>
                  </div>
                ) : (
                  <div className="p-2.5 rounded-lg bg-emerald-50/70 border border-emerald-200/60 text-xs text-emerald-800 flex items-center gap-2">
                    <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Chưa ghi nhận tiền sử dị ứng thuốc hoặc thực phẩm.</span>
                  </div>
                )}
              </div>

              {/* DANH SÁCH PHIẾU KHÁM BỆNH */}
              <div className="bg-white rounded-xl border border-slate-200/80 shadow-xs overflow-hidden">
                <div className="px-5 py-3.5 border-b border-slate-100 bg-slate-50/60 flex items-center justify-between">
                  <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                    <FileText className="w-4 h-4 text-blue-600" />
                    Danh sách phiếu khám ({previousCheckups.length})
                  </h3>
                </div>

                {previousCheckups.length === 0 ? (
                  <div className="p-10 text-center text-slate-400 space-y-2">
                    <FileText className="w-9 h-9 mx-auto text-slate-300" />
                    <p className="text-xs font-semibold text-slate-600">
                      Sinh viên chưa có phiếu khám nào
                    </p>
                    <p className="text-[11px] text-slate-400">
                      Các phiếu khám đa khoa, định kỳ, tâm lý hoặc cấp cứu đã lập sẽ hiển thị tại đây.
                    </p>
                  </div>
                ) : (
                  <div className="divide-y divide-slate-100">
                    {previousCheckups.map((item) => {
                      const isPsy = isPsychologicalCheckup(item.checkupType);
                      const summaryText =
                        item.diagnosisNotes ||
                        item.symptoms ||
                        "Chưa có kết luận chẩn đoán";

                      return (
                        <div
                          key={item.id}
                          className="p-4 sm:p-5 hover:bg-slate-50/70 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                        >
                          <div className="space-y-1.5 min-w-0 flex-1">
                            <div className="flex flex-wrap items-center gap-2">
                              <span className="font-mono text-xs font-bold text-blue-700">
                                {item.checkupCode || `#${item.id}`}
                              </span>
                              {renderCheckupTypeBadge(item)}
                              {renderStatusBadge(item.status)}
                            </div>

                            <p className="text-sm font-semibold text-slate-800 line-clamp-2">
                              <span className="text-slate-500 font-normal">
                                {isPsy ? "Tóm tắt tư vấn: " : "Chẩn đoán: "}
                              </span>
                              {summaryText}
                            </p>

                            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-[11px] text-slate-500">
                              <span>
                                Thời gian:{" "}
                                <strong className="font-medium text-slate-700">
                                  {formatDateTime(item.completedAt || item.createdAt)}
                                </strong>
                              </span>
                              {item.doctorName && (
                                <span>
                                  · Bác sĩ:{" "}
                                  <strong className="font-medium text-slate-700">
                                    {item.doctorName}
                                  </strong>
                                </span>
                              )}
                              {item.diseaseGroupName && (
                                <span>· Nhóm bệnh: {item.diseaseGroupName}</span>
                              )}
                            </div>
                          </div>

                          <div className="shrink-0">
                            <button
                              type="button"
                              onClick={() => navigate(`/doctor/checkups/${item.id}`)}
                              className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-blue-700 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-lg transition-colors"
                            >
                              <span>Xem chi tiết / In chuyển viện</span>
                              <ExternalLink className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
