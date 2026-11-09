import { useState, useEffect, useMemo, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Calendar as CalendarIcon,
  ChevronLeft,
  ChevronRight,
  Clock,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  FileText,
  ChevronRight as ChevronRightIcon,
  Play,
  RotateCcw,
  User,
  Activity,
  CheckCheck,
  Stethoscope,
  Info,
  ExternalLink,
  ShieldAlert,
  UserCheck,
  RefreshCw,
} from "lucide-react";
import { doctorAppointmentApi } from "../api/doctorAppointmentApi";
import { formatDay, serviceLabel, timeLabel, todayInVietnam } from "../utils/appointments";
import DoctorCancelModal from "../components/doctor/DoctorCancelModal";
import DoctorRescheduleModal from "../components/doctor/DoctorRescheduleModal";
import StudentFullProfileModal from "../components/doctor/StudentFullProfileModal";
import Toast from "../components/common/Toast";
import { useAuth } from "../hooks/useAuth";

export default function DoctorAppointmentList() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const doctorDisplayName = user?.fullName || "Bác Sĩ";

  // Bộ lọc
  const [filterDate, setFilterDate] = useState(() => todayInVietnam());
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [serviceFilter, setServiceFilter] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  // Dữ liệu danh sách ca khám
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [revision, setRevision] = useState(0);

  // Dữ liệu chi tiết hồ sơ sức khỏe & thông tin cá nhân bổ trợ cho SV đang chọn (Lazy loading)
  const [studentDetails, setStudentDetails] = useState({
    studentId: null,
    personalInfo: null,
    healthProfile: null,
    loadingExtra: false,
  });

  // Modals
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [rescheduleModalOpen, setRescheduleModalOpen] = useState(false);
  const [profileModalOpen, setProfileModalOpen] = useState(false);

  // Toast
  const [toast, setToast] = useState(null);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 4000);
  };

  const reloadAppointments = useCallback(() => {
    setLoading(true);
    setError(null);
    setRevision((prev) => prev + 1);
  }, []);

  // Điều hướng ngày
  const handlePrevDay = () => {
    const d = new Date(filterDate);
    d.setDate(d.getDate() - 1);
    setFilterDate(d.toISOString().slice(0, 10));
  };

  const handleNextDay = () => {
    const d = new Date(filterDate);
    d.setDate(d.getDate() + 1);
    setFilterDate(d.toISOString().slice(0, 10));
  };

  const handleToday = () => {
    setFilterDate(todayInVietnam());
  };

  const handleResetFilters = () => {
    setStatusFilter("ALL");
    setServiceFilter("ALL");
    setSearchQuery("");
  };

  // Tải danh sách lịch hẹn khi bộ lọc hoặc revision thay đổi
  useEffect(() => {
    let active = true;

    doctorAppointmentApi
      .getDoctorAppointments({
        date: filterDate,
        shiftType: serviceFilter !== "ALL" ? serviceFilter : undefined,
        status: statusFilter !== "ALL" ? statusFilter : undefined,
        keyword: searchQuery.trim() || undefined,
      })
      .then((data) => {
        if (!active) return;
        setAppointments(data);
        setError(null);
        setSelectedId((current) => {
          if (data.length > 0) {
            if (!current || !data.some((item) => item.id === current)) {
              return data[0].id;
            }
            return current;
          }
          return null;
        });
      })
      .catch((cause) => {
        if (active) {
          setError(
            cause?.response?.data?.message ||
              cause?.message ||
              "Không thể tải danh sách lịch khám. Vui lòng kiểm tra kết nối mạng."
          );
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [filterDate, statusFilter, serviceFilter, searchQuery, revision]);

  // Lịch hẹn đang chọn ở cột phải
  const selectedAppointment = useMemo(() => {
    const found = appointments.find((a) => a.id === selectedId);
    if (!found) return null;

    // Hợp nhất dữ liệu chi tiết nếu có
    const extraPersonal = studentDetails.personalInfo;
    const extraHealth = studentDetails.healthProfile;

    return {
      ...found,
      gender: extraPersonal?.gender || found.gender || "Chưa có",
      dateOfBirth: extraPersonal?.dateOfBirth || found.dateOfBirth || found.date_of_birth,
      studentPhone:
        extraPersonal?.phoneNumber || found.studentPhone || found.phone_number || "Chưa có",
      classCode: extraPersonal?.classCode || found.classCode || found.class_code || "Chưa có",
      facultyName:
        extraPersonal?.facultyName || found.facultyName || found.faculty_name || "Chưa có",
      health_records: {
        ...(found.health_records || {}),
        blood_type:
          extraHealth?.bloodType ||
          found.health_records?.blood_type ||
          found.health_records?.bloodType ||
          "Chưa cập nhật",
        height:
          extraHealth?.height ||
          found.health_records?.height ||
          "Chưa đo",
        weight:
          extraHealth?.weight ||
          found.health_records?.weight ||
          "Chưa đo",
        bmi:
          extraHealth?.bmi ||
          found.health_records?.bmi ||
          "--",
        bmi_classification:
          extraHealth?.bmiClassification ||
          found.health_records?.bmi_classification ||
          found.health_records?.bmiClassification ||
          "Chưa phân loại",
        allergies:
          extraHealth?.allergies ||
          found.health_records?.allergies ||
          "Không ghi nhận",
        underlying_diseases:
          extraHealth?.underlyingDiseases ||
          found.health_records?.underlying_diseases ||
          found.health_records?.underlyingDiseases ||
          "Không ghi nhận",
        updated_at:
          extraHealth?.updatedAt ||
          found.health_records?.updated_at ||
          "Mới nhất",
      },
    };
  }, [appointments, selectedId, studentDetails]);

  // Fetch thêm hồ sơ sức khỏe & thông tin cá nhân khi sinh viên thay đổi (Lazy Loading)
  useEffect(() => {
    let active = true;
    const currentAppt = appointments.find((a) => a.id === selectedId);
    const targetStudentId = currentAppt?.studentId || currentAppt?.student_id;

    if (!selectedId || !targetStudentId) {
      return () => {
        active = false;
      };
    }

    Promise.allSettled([
      doctorAppointmentApi.getStudentPersonalInfo(targetStudentId),
      doctorAppointmentApi.getStudentHealthProfile(targetStudentId),
    ]).then(([personalRes, healthRes]) => {
      if (!active) return;
      setStudentDetails({
        studentId: targetStudentId,
        personalInfo: personalRes.status === "fulfilled" ? personalRes.value : null,
        healthProfile: healthRes.status === "fulfilled" ? healthRes.value : null,
        loadingExtra: false,
      });
    });

    return () => {
      active = false;
    };
  }, [selectedId, appointments]);

  // Thống kê 4 thẻ trên cùng theo 4 trạng thái DB
  const counts = useMemo(() => {
    let total = appointments.length;
    let confirmed = 0;
    let completed = 0;
    let canceled = 0;
    let noShow = 0;

    appointments.forEach((item) => {
      const st = item.status;
      if (st === "CONFIRMED") confirmed++;
      else if (st === "COMPLETED") completed++;
      else if (st === "CANCELED") canceled++;
      else if (st === "NO_SHOW") noShow++;
    });

    return { total, confirmed, completed, canceled, noShow };
  }, [appointments]);

  // Xử lý hủy lịch thành công: Gọi lại API GET để cập nhật lại danh sách & thống kê
  const handleCancelSuccess = (updated) => {
    const code = updated?.appointmentCode || updated?.appointment_code || `#${selectedId}`;
    showToast(`Đã hủy lịch hẹn ca ${code} thành công.`, "success");
    reloadAppointments();
  };

  // Xử lý dời lịch thành công: Gọi lại API GET để cập nhật lại danh sách & thống kê
  const handleRescheduleSuccess = (updated) => {
    const code = updated?.appointmentCode || updated?.appointment_code || `#${selectedId}`;
    const newDate = updated?.shiftDate || updated?.shift_date;
    showToast(
      `Đã dời lịch ca ${code}${newDate ? ` sang ngày ${formatDay(newDate)}` : ""} thành công.`,
      "success"
    );
    reloadAppointments();
  };

  // Bắt đầu khám bệnh: Điều hướng sang /doctor/checkups/new?appointmentId={id}
  const handleStartExamination = () => {
    if (!selectedAppointment) return;
    navigate(`/doctor/checkups/new?appointmentId=${selectedAppointment.id}`);
  };

  // Helper render badge trạng thái chuẩn màu UniCare
  const renderStatusBadge = (status) => {
    switch (status) {
      case "CONFIRMED":
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-blue-50 text-blue-700 border border-blue-200">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-600 animate-pulse"></span>
            Đã xác nhận
          </span>
        );
      case "COMPLETED":
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCheck className="w-3 h-3" />
            Đã hoàn thành
          </span>
        );
      case "CANCELED":
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-rose-50 text-rose-700 border border-rose-200">
            <XCircle className="w-3 h-3" />
            Đã hủy
          </span>
        );
      case "NO_SHOW":
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-amber-50 text-amber-700 border border-amber-200">
            <AlertTriangle className="w-3 h-3" />
            Vắng mặt
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-medium bg-slate-100 text-slate-700">
            {status}
          </span>
        );
    }
  };

  // Initials generator
  const getInitials = (name) => {
    if (!name) return "SV";
    return name
      .split(" ")
      .filter(Boolean)
      .slice(-2)
      .map((p) => p[0])
      .join("")
      .toUpperCase();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 space-y-4">
      {/* Toast thông báo */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* 1. HEADER CHÍNH - Đồng bộ typography với View Lịch trực */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
            Lịch làm việc hôm nay
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            {formatDay(filterDate)} · Trạm Y tế Trường Đại học UniCare
          </p>
        </div>

        {/* Nút làm mới danh sách */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button
            type="button"
            onClick={reloadAppointments}
            disabled={loading}
            className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg border border-slate-200 transition-colors shrink-0 flex items-center gap-1.5 text-xs font-semibold disabled:opacity-50"
            title="Tải lại dữ liệu từ máy chủ"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin text-blue-600" : ""}`} />
            <span>Làm mới</span>
          </button>
        </div>
      </div>

      {/* 2. HÀNG 4 THẺ THỐNG KÊ TRẠNG THÁI (Đồng bộ chuẩn 4 status DB) */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {/* Thẻ 1: Tổng lịch hẹn */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 shadow-xs flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-blue-50 text-blue-600">
            <CalendarIcon className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              Tổng lịch hẹn
            </p>
            <p className="text-xl font-bold text-slate-800">{counts.total}</p>
            <p className="text-[11px] text-slate-500">
              {counts.confirmed} đã xác nhận · {counts.completed} đã khám
            </p>
          </div>
        </div>

        {/* Thẻ 2: Đã xác nhận (CONFIRMED) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 shadow-xs flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-blue-50 text-blue-600">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              Đã xác nhận (CONFIRMED)
            </p>
            <p className="text-xl font-bold text-blue-600">{counts.confirmed}</p>
            <p className="text-[11px] text-slate-500">Chờ tiếp nhận & khám bệnh</p>
          </div>
        </div>

        {/* Thẻ 3: Đã hoàn thành (COMPLETED) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 shadow-xs flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-emerald-50 text-emerald-600">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              Đã hoàn thành (COMPLETED)
            </p>
            <p className="text-xl font-bold text-emerald-600">{counts.completed}</p>
            <p className="text-[11px] text-slate-500">Đã hoàn tất ca khám</p>
          </div>
        </div>

        {/* Thẻ 4: Đã hủy / Vắng mặt (CANCELED / NO_SHOW) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 shadow-xs flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-rose-50 text-rose-600">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
              Đã hủy / Vắng mặt
            </p>
            <p className="text-xl font-bold text-rose-600">
              {counts.canceled + counts.noShow}
            </p>
            <p className="text-[11px] text-slate-500">
              {counts.canceled} đã hủy · {counts.noShow} vắng mặt
            </p>
          </div>
        </div>
      </div>

      {/* 3. THANH BỘ LỌC (Filter Bar) */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-3 shadow-xs space-y-2.5">
        <div className="flex flex-wrap items-center justify-between gap-2.5">
          {/* Cụm Điều hướng Ngày */}
          <div className="flex items-center gap-2">
            <div className="inline-flex items-center rounded-lg border border-slate-200 bg-slate-50 p-0.5">
              <button
                type="button"
                onClick={handlePrevDay}
                className="p-1.5 rounded-md text-slate-600 hover:text-slate-900 hover:bg-white transition-colors"
                title="Ngày trước"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={handleToday}
                className="px-2.5 py-1 text-xs font-semibold text-slate-700 hover:bg-white rounded-md transition-colors"
              >
                Hôm nay
              </button>
              <button
                type="button"
                onClick={handleNextDay}
                className="p-1.5 rounded-md text-slate-600 hover:text-slate-900 hover:bg-white transition-colors"
                title="Ngày sau"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>

            <div className="relative flex items-center">
              <CalendarIcon className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 pointer-events-none" />
              <input
                type="date"
                value={filterDate}
                onChange={(e) => setFilterDate(e.target.value)}
                className="pl-8 pr-2.5 py-1.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 font-medium"
                aria-label="Chọn ngày lọc lịch khám"
              />
            </div>
          </div>

          {/* Cụm Bộ lọc Phải */}
          <div className="flex flex-wrap items-center gap-2 flex-1 justify-end min-w-0">
            {/* Pill Bác sĩ */}
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-blue-50 border border-blue-200 text-blue-700 text-xs font-medium shrink-0">
              <UserCheck className="w-3.5 h-3.5 text-blue-600" />
              <span>
                BS: <strong>{doctorDisplayName}</strong>
              </span>
            </div>

            {/* Dropdown Dịch vụ */}
            <div className="relative min-w-[155px]">
              <Stethoscope className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <select
                value={serviceFilter}
                onChange={(e) => setServiceFilter(e.target.value)}
                className="w-full pl-8 pr-7 py-1.5 text-xs sm:text-sm bg-slate-50 hover:bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer font-medium"
              >
                <option value="ALL">Tất cả Dịch vụ</option>
                <option value="KHAM_DA_KHOA">Khám đa khoa</option>
                <option value="TU_VAN_TAM_LY">Tư vấn tâm lý</option>
              </select>
            </div>

            {/* Ô tìm kiếm nhanh */}
            <div className="relative min-w-[190px] flex-1 sm:max-w-xs">
              <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Tìm tên sinh viên, MSSV..."
                className="w-full pl-8 pr-7 py-1.5 text-xs sm:text-sm bg-slate-50 hover:bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => setSearchQuery("")}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  <XCircle className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            {/* Nút đặt lại bộ lọc */}
            <button
              type="button"
              onClick={handleResetFilters}
              className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg border border-slate-200 transition-colors shrink-0"
              title="Đặt lại bộ lọc"
            >
              <RotateCcw className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* Hàng 2: Tabs Trạng thái */}
        <div className="flex flex-wrap items-center justify-between gap-2 pt-2.5 border-t border-slate-100 text-xs">
          <div className="flex flex-wrap items-center gap-1.5">
            <span className="text-xs font-semibold text-slate-400 mr-1 flex items-center gap-1">
              <Filter className="w-3 h-3" /> Trạng thái:
            </span>
            {[
              { id: "ALL", label: "Tất cả" },
              { id: "CONFIRMED", label: "Đã xác nhận" },
              { id: "COMPLETED", label: "Đã hoàn thành" },
              { id: "CANCELED", label: "Đã hủy" },
              { id: "NO_SHOW", label: "Vắng mặt" },
            ].map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setStatusFilter(tab.id)}
                className={`px-2.5 py-1 text-xs rounded-lg font-medium transition-colors ${
                  statusFilter === tab.id
                    ? "bg-slate-900 text-white font-semibold"
                    : "bg-slate-100 hover:bg-slate-200 text-slate-600"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-1.5 text-[11px] text-slate-500">
            <CheckCircle2 className="w-3.5 h-3.5 text-blue-500" />
            <span>Đã kết nối API hệ thống ({appointments.length} ca khám)</span>
          </div>
        </div>
      </div>

      {/* 4. GIAO DIỆN 2 CỘT */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-start">
        {/* CỘT TRÁI (5 Cột trên Desktop): Danh sách ca khám */}
        <div className="lg:col-span-5 bg-white rounded-xl border border-slate-200/80 shadow-xs overflow-hidden flex flex-col">
          {/* Header cột trái */}
          <div className="px-4 py-3 border-b border-slate-200/80 flex items-center justify-between bg-slate-50/50">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-slate-800">Danh sách lịch khám</h2>
              <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-blue-700">
                {appointments.length} bệnh nhân
              </span>
            </div>
            <span className="text-xs text-slate-500 font-medium">{formatDay(filterDate)}</span>
          </div>

          {/* Danh sách items */}
          <div className="divide-y divide-slate-100 max-h-[640px] overflow-y-auto">
            {loading ? (
              /* SKELETON LOADING */
              <div className="p-4 space-y-3">
                {[1, 2, 3, 4].map((i) => (
                  <div key={i} className="animate-pulse flex items-center justify-between p-3 rounded-lg bg-slate-50">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-8 bg-slate-200 rounded"></div>
                      <div className="w-9 h-9 rounded-full bg-slate-200"></div>
                      <div className="space-y-1.5">
                        <div className="w-28 h-3.5 bg-slate-200 rounded"></div>
                        <div className="w-20 h-2.5 bg-slate-200 rounded"></div>
                      </div>
                    </div>
                    <div className="w-16 h-5 bg-slate-200 rounded-md"></div>
                  </div>
                ))}
              </div>
            ) : error ? (
              <div className="p-8 text-center text-rose-600 space-y-2">
                <AlertTriangle className="w-7 h-7 mx-auto text-rose-500" />
                <p className="text-xs font-semibold">{error}</p>
                <button
                  type="button"
                  onClick={reloadAppointments}
                  className="px-3 py-1 text-xs bg-slate-100 hover:bg-slate-200 rounded-lg text-slate-700 font-medium"
                >
                  Thử lại
                </button>
              </div>
            ) : appointments.length === 0 ? (
              <div className="p-10 text-center text-slate-400 space-y-2">
                <Stethoscope className="w-8 h-8 mx-auto text-slate-300" />
                <p className="text-sm font-semibold text-slate-700">Không có lịch khám nào</p>
                <p className="text-xs text-slate-400">
                  Không tìm thấy lịch hẹn phù hợp điều kiện lọc cho ngày này.
                </p>
              </div>
            ) : (
              appointments.map((item) => {
                const isSelected = item.id === selectedId;
                const studentName = item.studentName || item.full_name || "Chưa có tên";
                const studentCode = item.studentCode || item.student_code || "";
                const startTime = item.startTime || item.start_time;
                const endTime = item.endTime || item.end_time;
                const shiftType = item.shiftType || item.shift_type;
                const initials = getInitials(studentName);

                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => setSelectedId(item.id)}
                    className={`w-full p-3.5 text-left transition flex items-center justify-between gap-3 group ${
                      isSelected
                        ? "border-l-4 border-l-blue-600 bg-blue-50/40"
                        : "border-l-4 border-l-transparent hover:bg-slate-50/80"
                    }`}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      {/* Khung giờ & Ca */}
                      <div className="text-center shrink-0 w-16">
                        <span className="text-xs font-bold text-slate-800 block">
                          {timeLabel(startTime)}
                        </span>
                        <span className="text-[11px] text-slate-400 block">
                          {timeLabel(endTime)}
                        </span>
                        <span
                          className={`inline-block mt-0.5 px-1.5 py-0.2 rounded text-[10px] font-semibold ${
                            item.shift_session === "MORNING" || (startTime && startTime.startsWith("08"))
                              ? "bg-amber-50 text-amber-700 border border-amber-200/60"
                              : "bg-blue-50 text-blue-700 border border-blue-200/60"
                          }`}
                        >
                          {startTime && startTime.startsWith("08") ? "Ca Sáng" : "Ca Chiều"}
                        </span>
                      </div>

                      {/* Avatar viết tắt */}
                      <div
                        className={`w-9 h-9 rounded-full font-bold text-xs flex items-center justify-center shrink-0 ${
                          isSelected
                            ? "bg-blue-600 text-white"
                            : "bg-blue-100 text-blue-700 group-hover:bg-blue-200"
                        }`}
                      >
                        {initials}
                      </div>

                      {/* Họ tên, MSSV, Dịch vụ */}
                      <div className="min-w-0">
                        <h4 className="text-sm font-semibold text-slate-900 truncate">
                          {studentName}
                        </h4>
                        <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-0.5 truncate">
                          {studentCode && (
                            <span className="font-mono text-[11px] text-slate-500">
                              {studentCode}
                            </span>
                          )}
                          <span>·</span>
                          <span
                            className={`text-[11px] font-medium ${
                              shiftType === "TU_VAN_TAM_LY" ? "text-purple-600" : "text-blue-600"
                            }`}
                          >
                            {serviceLabel(shiftType)}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Badge trạng thái */}
                    <div className="flex items-center gap-1.5 shrink-0">
                      {renderStatusBadge(item.status)}
                      <ChevronRightIcon
                        className={`w-4 h-4 transition-transform ${
                          isSelected ? "text-blue-600 translate-x-0.5" : "text-slate-300"
                        }`}
                      />
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* CỘT PHẢI (7 Cột trên Desktop): Chi tiết hồ sơ bệnh nhân & lịch hẹn */}
        <div className="lg:col-span-7 bg-white rounded-xl border border-slate-200/80 shadow-xs overflow-hidden flex flex-col">
          {!selectedAppointment ? (
            <div className="p-16 text-center text-slate-400 space-y-3">
              <User className="w-12 h-12 mx-auto text-slate-300 stroke-1" />
              <h3 className="text-base font-semibold text-slate-700">Chưa chọn ca khám</h3>
              <p className="text-xs text-slate-400 max-w-sm mx-auto">
                Nhấp vào bất kỳ ca khám nào trong danh sách bên trái để xem đầy đủ hồ sơ sức khỏe và thông tin chi tiết.
              </p>
            </div>
          ) : (
            <div className="flex flex-col h-full">
              {/* HEADER SINH VIÊN */}
              <div className="p-5 border-b border-slate-100 bg-slate-50/50">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-center gap-3.5">
                    <div className="w-12 h-12 rounded-full bg-blue-600 text-white font-bold text-base flex items-center justify-center shadow-xs">
                      {getInitials(
                        selectedAppointment.studentName || selectedAppointment.full_name
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="text-base font-bold text-slate-900">
                          {selectedAppointment.studentName ||
                            selectedAppointment.full_name ||
                            "Sinh viên"}
                        </h3>
                        {renderStatusBadge(selectedAppointment.status)}
                      </div>
                      <p className="text-xs text-slate-500 font-mono mt-0.5">
                        MSSV:{" "}
                        <span className="font-semibold text-slate-700">
                          {selectedAppointment.studentCode ||
                            selectedAppointment.student_code ||
                            "Chưa có"}
                        </span>{" "}
                        ·{" "}
                        <span>
                          {selectedAppointment.facultyName ||
                            selectedAppointment.faculty_name ||
                            "Khoa CNTT"}
                        </span>
                      </p>
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={() => setProfileModalOpen(true)}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-blue-600 hover:text-blue-700 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors border border-blue-200/60"
                  >
                    <span>Hồ sơ đầy đủ</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </button>
                </div>

                {/* Dải thông tin học vụ & liên hệ */}
                {studentDetails.loadingExtra && studentDetails.studentId !== selectedAppointment.studentId ? (
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 mt-3.5 pt-3.5 border-t border-slate-100 animate-pulse">
                    {[1, 2, 3, 4].map((i) => (
                      <div key={i} className="space-y-1">
                        <div className="w-10 h-2 bg-slate-200 rounded"></div>
                        <div className="w-20 h-3.5 bg-slate-200 rounded"></div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 mt-3.5 pt-3.5 border-t border-slate-100 text-xs">
                    <div>
                      <span className="text-slate-400 block text-[11px]">Lớp:</span>
                      <span className="font-semibold text-slate-700">
                        {selectedAppointment.classCode ||
                          selectedAppointment.class_code ||
                          "Chưa có"}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[11px]">Giới tính:</span>
                      <span className="font-semibold text-slate-700">
                        {selectedAppointment.gender || "Chưa có"}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[11px]">Ngày sinh:</span>
                      <span className="font-semibold text-slate-700">
                        {selectedAppointment.dateOfBirth
                          ? formatDay(selectedAppointment.dateOfBirth)
                          : selectedAppointment.date_of_birth
                          ? formatDay(selectedAppointment.date_of_birth)
                          : "Chưa có"}
                      </span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[11px]">Số điện thoại:</span>
                      <span className="font-semibold text-slate-700">
                        {selectedAppointment.studentPhone ||
                          selectedAppointment.phone_number ||
                          "Chưa có"}
                      </span>
                    </div>
                  </div>
                )}
              </div>

              {/* Chi tiết cuộn */}
              <div className="p-5 space-y-5 flex-1 overflow-y-auto">
                {/* KHỐI HỒ SƠ SỨC KHỎE TÓM TẮT (health_records) */}
                <div className="space-y-2.5">
                  <div className="flex items-center justify-between">
                    <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                      <Activity className="w-3.5 h-3.5 text-blue-600" />
                      Hồ sơ sức khỏe
                    </h4>
                    <span className="text-[11px] text-slate-400">
                      Cập nhật:{" "}
                      {selectedAppointment.health_records?.updated_at || "Mới nhất"}
                    </span>
                  </div>

                  {studentDetails.loadingExtra && studentDetails.studentId !== selectedAppointment.studentId ? (
                    /* SKELETON LOADING CHO HỒ SƠ SỨC KHỎE */
                    <div className="space-y-2.5 animate-pulse">
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                        {[1, 2, 3, 4].map((i) => (
                          <div key={i} className="p-2.5 bg-slate-100 rounded-lg space-y-1.5">
                            <div className="w-12 h-2.5 bg-slate-200 rounded"></div>
                            <div className="w-16 h-3.5 bg-slate-200 rounded"></div>
                          </div>
                        ))}
                      </div>
                      <div className="p-3 bg-slate-100 rounded-lg h-12"></div>
                    </div>
                  ) : (
                    <>
                      {/* 4 Thông số cơ bản */}
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 text-xs">
                        <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100">
                          <span className="text-slate-400 block text-[11px] mb-0.5">Nhóm máu</span>
                          <span className="text-xs font-bold text-slate-800">
                            {selectedAppointment.health_records?.blood_type || "Chưa cập nhật"}
                          </span>
                        </div>

                        <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100">
                          <span className="text-slate-400 block text-[11px] mb-0.5">Chiều cao / Cân nặng</span>
                          <span className="text-xs font-bold text-slate-800">
                            {selectedAppointment.health_records?.height} cm ·{" "}
                            {selectedAppointment.health_records?.weight} kg
                          </span>
                        </div>

                        <div className="p-2.5 bg-blue-50/60 rounded-lg border border-blue-100">
                          <span className="text-blue-600 block text-[11px] mb-0.5">BMI</span>
                          <span className="text-xs font-bold text-blue-900">
                            {selectedAppointment.health_records?.bmi}{" "}
                            <span className="text-[10px] font-normal text-blue-700">
                              ({selectedAppointment.health_records?.bmi_classification})
                            </span>
                          </span>
                        </div>

                        <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100">
                          <span className="text-slate-400 block text-[11px] mb-0.5">Bệnh nền</span>
                          <span className="text-xs font-bold text-slate-800 truncate block">
                            {selectedAppointment.health_records?.underlying_diseases || "Không ghi nhận"}
                          </span>
                        </div>
                      </div>

                      {/* CẢNH BÁO DỊ ỨNG */}
                      {selectedAppointment.health_records?.allergies &&
                      selectedAppointment.health_records?.allergies !== "Không ghi nhận" ? (
                        <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg space-y-1 text-rose-900">
                          <div className="flex items-center gap-1.5">
                            <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
                            <h5 className="font-bold text-xs uppercase tracking-wider text-rose-700">
                              {selectedAppointment.health_records?.allergy_alert_title ||
                                `Dị ứng: ${selectedAppointment.health_records?.allergies}`}
                            </h5>
                            <span className="px-1.5 py-0.5 bg-rose-200/80 text-rose-800 rounded text-[10px] font-bold uppercase ml-auto">
                              Quan trọng
                            </span>
                          </div>
                          <p className="text-xs text-rose-800 font-medium pl-5">
                            {selectedAppointment.health_records?.allergy_alert_desc ||
                              selectedAppointment.health_records?.allergies}
                          </p>
                        </div>
                      ) : (
                        <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100 text-xs text-slate-500 flex items-center gap-2">
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                          <span>Không ghi nhận tiền sử dị ứng thuốc hay thực phẩm</span>
                        </div>
                      )}
                    </>
                  )}
                </div>

                {/* KHỐI CHI TIẾT LỊCH KHÁM HÔM NAY */}
                <div className="space-y-2.5 pt-3.5 border-t border-slate-100">
                  <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                    <FileText className="w-3.5 h-3.5 text-blue-600" />
                    Lịch khám hôm nay
                  </h4>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 text-xs">
                    <div className="p-2.5 bg-slate-50 rounded-lg">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Mã lịch hẹn</span>
                      <span className="font-mono font-bold text-blue-700">
                        {selectedAppointment.appointmentCode ||
                          selectedAppointment.appointment_code ||
                          `#${selectedAppointment.id}`}
                      </span>
                    </div>
                    <div className="p-2.5 bg-slate-50 rounded-lg">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Ca & Khung giờ</span>
                      <span className="font-semibold text-slate-800">
                        {(selectedAppointment.startTime || selectedAppointment.start_time || "").startsWith("08")
                          ? "Ca Sáng ("
                          : "Ca Chiều ("}
                        {timeLabel(
                          selectedAppointment.startTime || selectedAppointment.start_time
                        )}{" "}
                        –{" "}
                        {timeLabel(
                          selectedAppointment.endTime || selectedAppointment.end_time
                        )}
                        )
                      </span>
                    </div>
                    <div className="p-2.5 bg-slate-50 rounded-lg">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Loại hình khám</span>
                      <span className="font-semibold text-slate-800">
                        {serviceLabel(
                          selectedAppointment.shiftType || selectedAppointment.shift_type
                        )}
                      </span>
                    </div>
                    <div className="p-2.5 bg-slate-50 rounded-lg">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Phòng khám</span>
                      <span className="font-semibold text-slate-800 truncate block">
                        {selectedAppointment.room || "P.101 - Phòng y tế"}
                      </span>
                    </div>
                  </div>

                  {/* Triệu chứng ban đầu */}
                  <div className="p-3 bg-slate-50/80 rounded-lg border border-slate-200/80 text-xs space-y-1">
                    <span className="text-slate-500 font-semibold block text-[11px]">
                      Triệu chứng ban đầu (Sinh viên mô tả khi đặt lịch):
                    </span>
                    <p className="text-slate-700 leading-relaxed italic">
                      &ldquo;
                      {selectedAppointment.reasonSymptoms ||
                        selectedAppointment.reason_symptoms ||
                        "Sinh viên không ghi chú thêm triệu chứng ban đầu."}
                      &rdquo;
                    </p>
                  </div>

                  {/* Khối cảnh báo nếu ca khám bị CANCELED */}
                  {selectedAppointment.status === "CANCELED" && (
                    <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-xs space-y-1 text-rose-900">
                      <div className="flex items-center gap-1.5 font-bold">
                        <ShieldAlert className="w-4 h-4 text-rose-600" />
                        <span>
                          Thông tin hủy lịch (Hủy bởi:{" "}
                          <span className="uppercase text-rose-700 underline">
                            {selectedAppointment.canceledBy || selectedAppointment.canceled_by || "Bác sĩ"}
                          </span>
                          )
                        </span>
                      </div>
                      <p className="text-rose-800 pl-5">
                        <strong>Lý do: </strong>
                        {selectedAppointment.cancelRescheduleReason ||
                          selectedAppointment.cancel_reschedule_reason ||
                          "Không có lý do chi tiết."}
                      </p>
                    </div>
                  )}
                </div>
              </div>

              {/* FOOTER CỘT PHẢI - Cụm nút hành động */}
              <div className="p-4 border-t border-slate-200/80 bg-slate-50/60 flex items-center justify-between gap-2">
                {selectedAppointment.status === "CONFIRMED" ? (
                  <>
                    <span className="text-xs text-slate-500 hidden sm:inline">
                      Ca khám đang sẵn sàng tiếp nhận sinh viên.
                    </span>
                    <div className="flex items-center gap-2 ml-auto">
                      {/* Nút Hủy hẹn */}
                      <button
                        type="button"
                        onClick={() => setCancelModalOpen(true)}
                        className="px-3.5 py-1.5 text-xs font-semibold text-rose-600 hover:text-rose-700 bg-white hover:bg-rose-50 border border-rose-300 rounded-lg transition-colors shadow-xs"
                      >
                        Hủy hẹn
                      </button>

                      {/* Nút Dời lịch */}
                      <button
                        type="button"
                        onClick={() => setRescheduleModalOpen(true)}
                        className="px-3.5 py-1.5 text-xs font-semibold text-blue-600 hover:text-blue-700 bg-white hover:bg-blue-50 border border-blue-300 rounded-lg transition-colors shadow-xs"
                      >
                        Dời lịch
                      </button>

                      {/* Nút Bắt đầu khám */}
                      <button
                        type="button"
                        onClick={handleStartExamination}
                        className="inline-flex items-center gap-1.5 px-4 py-1.5 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 rounded-lg transition-colors shadow-xs"
                      >
                        <Play className="w-3 h-3 fill-current" />
                        <span>Bắt đầu khám</span>
                      </button>
                    </div>
                  </>
                ) : (
                  <div className="w-full flex items-center justify-between text-xs text-slate-500">
                    <span className="flex items-center gap-1.5">
                      <Info className="w-3.5 h-3.5 text-slate-400" />
                      {selectedAppointment.status === "COMPLETED"
                        ? "Ca khám này đã hoàn tất khám bệnh và lưu hồ sơ y tế."
                        : selectedAppointment.status === "CANCELED"
                        ? "Lịch hẹn này đã bị hủy. Không thể thực hiện các thao tác khám."
                        : "Sinh viên đã được ghi nhận vắng mặt (NO_SHOW)."}
                    </span>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        disabled
                        className="px-3 py-1.5 text-xs font-medium text-slate-400 bg-slate-100 rounded-lg cursor-not-allowed"
                      >
                        Hủy hẹn
                      </button>
                      <button
                        type="button"
                        disabled
                        className="px-3 py-1.5 text-xs font-medium text-slate-400 bg-slate-100 rounded-lg cursor-not-allowed"
                      >
                        Dời lịch
                      </button>
                      <button
                        type="button"
                        disabled
                        className="px-3.5 py-1.5 text-xs font-medium text-slate-400 bg-slate-200 rounded-lg cursor-not-allowed flex items-center gap-1"
                      >
                        <Play className="w-3 h-3 fill-current" />
                        Bắt đầu khám
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 5. CÁC MODAL NGHIỆP VỤ */}
      <DoctorCancelModal
        isOpen={cancelModalOpen}
        appointment={selectedAppointment}
        onClose={() => setCancelModalOpen(false)}
        onSuccess={handleCancelSuccess}
      />

      <DoctorRescheduleModal
        isOpen={rescheduleModalOpen}
        appointment={selectedAppointment}
        onClose={() => setRescheduleModalOpen(false)}
        onSuccess={handleRescheduleSuccess}
      />

      <StudentFullProfileModal
        isOpen={profileModalOpen}
        appointment={selectedAppointment}
        onClose={() => setProfileModalOpen(false)}
      />
    </div>
  );
}
