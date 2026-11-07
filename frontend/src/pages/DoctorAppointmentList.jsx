import { useState, useEffect, useMemo, useCallback } from "react";
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
  Loader2,
} from "lucide-react";
import { doctorAppointmentApi } from "../api/doctorAppointmentApi";
import { formatDay, serviceLabel, timeLabel, todayInVietnam } from "../utils/appointments";
import DoctorCancelModal from "../components/doctor/DoctorCancelModal";
import DoctorRescheduleModal from "../components/doctor/DoctorRescheduleModal";
import StudentFullProfileModal from "../components/doctor/StudentFullProfileModal";
import Toast from "../components/common/Toast";
import { useAuth } from "../hooks/useAuth";

export default function DoctorAppointmentList() {
  const { user } = useAuth();
  const doctorDisplayName = user?.fullName || "Bác Sĩ A";

  // Bộ lọc
  const [filterDate, setFilterDate] = useState(() => todayInVietnam());
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [serviceFilter, setServiceFilter] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  // Dữ liệu và trạng thái
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [revision, setRevision] = useState(0);

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

  // Điều hướng ngày (như View Quản lý ca trực)
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

  const handleRestoreMock = () => {
    doctorAppointmentApi.resetMockData(filterDate);
    reloadAppointments();
    showToast(`Đã khôi phục dữ liệu mẫu cho ngày ${formatDay(filterDate)}.`);
  };

  // Tải danh sách lịch hẹn khi bộ lọc hoặc revision thay đổi
  useEffect(() => {
    let active = true;
    setLoading(true);

    doctorAppointmentApi
      .getDoctorAppointments({
        date: filterDate,
        shiftType: serviceFilter !== "ALL" ? serviceFilter : undefined,
        status: statusFilter !== "ALL" ? statusFilter : undefined,
        search: searchQuery.trim() || undefined,
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
          setError(cause?.message || "Không thể tải danh sách lịch khám. Vui lòng thử lại.");
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
    return appointments.find((a) => a.id === selectedId) || null;
  }, [appointments, selectedId]);

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

    return {
      total,
      confirmed,
      completed,
      canceled,
      noShow,
      canceledOrNoShow: canceled + noShow,
    };
  }, [appointments]);

  // Xử lý hủy lịch thành công
  const handleCancelSuccess = (updated) => {
    setAppointments((prev) =>
      prev.map((item) => (item.id === updated.id ? { ...item, ...updated } : item))
    );
    showToast(`Đã hủy lịch hẹn ca ${updated.appointmentCode || updated.appointment_code} thành công.`);
  };

  // Xử lý dời lịch thành công
  const handleRescheduleSuccess = (updated) => {
    setAppointments((prev) =>
      prev.map((item) => (item.id === updated.id ? { ...item, ...updated } : item))
    );
    showToast(
      `Đã dời lịch ca ${updated.appointmentCode || updated.appointment_code} sang ngày ${formatDay(
        updated.shiftDate || updated.shift_date
      )}.`
    );
  };

  // Bắt đầu khám bệnh (dẫn sang Sprint 4)
  const handleStartExamination = () => {
    if (!selectedAppointment) return;
    const name = selectedAppointment.studentName || selectedAppointment.full_name;
    const code = selectedAppointment.appointmentCode || selectedAppointment.appointment_code;
    showToast(
      `Đang chuyển sang Màn hình Khám Bệnh (Sprint 4) cho sinh viên: ${name} (${code})...`,
      "info"
    );
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
      .join("");
  };

  return (
    <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 py-4 relative">
      {/* Toast popup */}
      <Toast toast={toast} onClose={() => setToast(null)} />

      {/* 1. Header: Đồng bộ 100% với Quản lý ca trực (View 2) */}
      <div className="flex items-center justify-between gap-4 mb-4">
        <div className="flex items-center gap-3">
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
            Lịch làm việc hôm nay
          </h1>
          {loading && (
            <div className="flex items-center gap-1.5 text-xs text-blue-600 bg-blue-50 px-2.5 py-1 rounded-full animate-pulse">
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
              <span>Đang tải...</span>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={reloadAppointments}
            disabled={loading}
            className="p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg border border-slate-200 transition-colors disabled:opacity-50"
            title="Làm mới danh sách"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />
          </button>

          <button
            type="button"
            onClick={handleRestoreMock}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 rounded-lg transition-colors shadow-xs"
            title="Khôi phục danh sách dữ liệu mẫu đầy đủ để kiểm thử"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Khôi phục mẫu</span>
          </button>
        </div>
      </div>

      {/* 2. 4 THẺ THỐNG KÊ ĐẾM THEO 4 TRẠNG THÁI DB (Kích thước gọn gàng, đồng bộ theme) */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-4">
        {/* Thẻ 1: Tổng lịch hẹn */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 sm:p-4 shadow-xs hover:border-slate-300 transition-colors">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 text-blue-600 rounded-lg shrink-0">
              <CalendarIcon className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 truncate">
                Tổng lịch hẹn
              </p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold text-slate-900">{counts.total}</span>
                <span className="text-[11px] text-slate-400 truncate">
                  {counts.confirmed} đã xác nhận
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Thẻ 2: Đã xác nhận (CONFIRMED) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 sm:p-4 shadow-xs hover:border-slate-300 transition-colors">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-blue-50 text-blue-700 rounded-lg shrink-0">
              <Clock className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 truncate">
                Đã xác nhận
              </p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold text-blue-700">{counts.confirmed}</span>
                <span className="text-[11px] text-slate-400 truncate">Chờ khám</span>
              </div>
            </div>
          </div>
        </div>

        {/* Thẻ 3: Đã hoàn thành (COMPLETED) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 sm:p-4 shadow-xs hover:border-slate-300 transition-colors">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-lg shrink-0">
              <CheckCheck className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 truncate">
                Đã hoàn thành
              </p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold text-emerald-600">{counts.completed}</span>
                <span className="text-[11px] text-slate-400 truncate">Đã xong ca</span>
              </div>
            </div>
          </div>
        </div>

        {/* Thẻ 4: Đã hủy / Vắng mặt (CANCELED / NO_SHOW) */}
        <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 sm:p-4 shadow-xs hover:border-slate-300 transition-colors">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-rose-50 text-rose-600 rounded-lg shrink-0">
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 truncate">
                Đã hủy / Vắng mặt
              </p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold text-rose-600">{counts.canceledOrNoShow}</span>
                <span className="text-[11px] text-slate-400 truncate">
                  {counts.canceled} hủy · {counts.noShow} vắng
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 3. BỘ LỌC ĐỒNG BỘ 100% STYLE VỚI SHIFTFILTERS (View 2) */}
      <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 sm:p-4 mb-4 shadow-xs space-y-3">
        {/* Hàng 1: Ngày, Pill Bác sĩ, Dịch vụ, Tìm kiếm, Nút Reset */}
        <div className="flex flex-wrap items-center justify-between gap-2.5">
          {/* Cụm Chọn ngày (Chevron + Hôm nay + DatePicker) */}
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
            {/* Pill Bác sĩ cố định (Đúng phong cách ShiftFilters) */}
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-blue-50 border border-blue-200 text-blue-700 text-xs font-medium shrink-0">
              <UserCheck className="w-3.5 h-3.5 text-blue-600" />
              <span>
                BS: <strong>{doctorDisplayName}</strong>
              </span>
            </div>

            {/* Dropdown Dịch vụ (Icon Stethoscope như View 2) */}
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

        {/* Hàng 2: Tabs Trạng thái & Thông báo trạng thái dữ liệu */}
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
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
            <span>Dữ liệu thử nghiệm đã sẵn sàng ({appointments.length} ca hẹn)</span>
          </div>
        </div>
      </div>

      {/* 4. GIAO DIỆN 2 CỘT (Cột trái: Danh sách lịch khám, Cột phải: Chi tiết hồ sơ) */}
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
              <div className="p-10 text-center text-slate-400 space-y-2">
                <Loader2 className="w-6 h-6 animate-spin mx-auto text-blue-600" />
                <p className="text-xs">Đang tải danh sách ca khám...</p>
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
                  Không tìm thấy lịch hẹn phù hợp điều kiện lọc.
                </p>
                <button
                  type="button"
                  onClick={handleRestoreMock}
                  className="mt-2 px-3 py-1.5 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-lg text-xs font-semibold transition"
                >
                  Tạo dữ liệu mẫu ngay
                </button>
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
                            (item.shift_session || (startTime && startTime.startsWith("08")))
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

        {/* CỘT PHẢI (7 Cột trên Desktop): Chi tiết ca hẹn & Hồ sơ sinh viên */}
        <div className="lg:col-span-7 bg-white rounded-xl border border-slate-200/80 shadow-xs overflow-hidden flex flex-col">
          {!selectedAppointment ? (
            <div className="p-14 text-center text-slate-400 space-y-3">
              <User className="w-10 h-10 mx-auto text-slate-300" />
              <h3 className="text-sm font-bold text-slate-700">Chưa chọn ca khám</h3>
              <p className="text-xs text-slate-400 max-w-xs mx-auto">
                Nhấp vào bất kỳ ca khám nào trong danh sách bên trái để xem đầy đủ hồ sơ và thao tác.
              </p>
            </div>
          ) : (
            <div className="flex flex-col h-full">
              {/* Header sinh viên */}
              <div className="p-5 border-b border-slate-200/80">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3.5">
                    <div className="w-12 h-12 rounded-full bg-blue-100 text-blue-700 font-bold flex items-center justify-center text-base shrink-0">
                      {getInitials(
                        selectedAppointment.studentName || selectedAppointment.full_name
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        {renderStatusBadge(selectedAppointment.status)}
                      </div>
                      <h3 className="text-lg font-bold text-slate-900 tracking-tight">
                        {selectedAppointment.studentName || selectedAppointment.full_name}
                      </h3>
                      <p className="text-xs text-slate-500 font-mono mt-0.5">
                        MSSV: {selectedAppointment.studentCode || selectedAppointment.student_code} ·{" "}
                        {selectedAppointment.facultyName ||
                          selectedAppointment.faculty_name ||
                          "Khoa CNTT"}
                      </p>
                    </div>
                  </div>

                  {/* Nút Hồ sơ đầy đủ */}
                  <button
                    type="button"
                    onClick={() => setProfileModalOpen(true)}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-lg transition-colors shrink-0"
                  >
                    <span>Hồ sơ đầy đủ</span>
                    <ExternalLink className="w-3 h-3" />
                  </button>
                </div>

                {/* Dải thông tin học vụ & liên hệ */}
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
                      {formatDay(
                        selectedAppointment.dateOfBirth || selectedAppointment.date_of_birth
                      )}
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
                      {selectedAppointment.health_records?.updated_at || "02/03/2026"}
                    </span>
                  </div>

                  {/* 4 Thông số cơ bản */}
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 text-xs">
                    <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Nhóm máu</span>
                      <span className="text-xs font-bold text-slate-800">
                        {selectedAppointment.health_records?.blood_type ||
                          selectedAppointment.health_records?.bloodType ||
                          "A dương"}
                      </span>
                    </div>

                    <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Chiều cao / Cân nặng</span>
                      <span className="text-xs font-bold text-slate-800">
                        {selectedAppointment.health_records?.height || "162"} cm ·{" "}
                        {selectedAppointment.health_records?.weight || "52"} kg
                      </span>
                    </div>

                    <div className="p-2.5 bg-blue-50/60 rounded-lg border border-blue-100">
                      <span className="text-blue-600 block text-[11px] mb-0.5">BMI</span>
                      <span className="text-xs font-bold text-blue-900">
                        {selectedAppointment.health_records?.bmi || "19.8"}{" "}
                        <span className="text-[10px] font-normal text-blue-700">
                          (
                          {selectedAppointment.health_records?.bmi_classification ||
                            selectedAppointment.health_records?.bmiClassification ||
                            "Bình thường"}
                          )
                        </span>
                      </span>
                    </div>

                    <div className="p-2.5 bg-slate-50 rounded-lg border border-slate-100">
                      <span className="text-slate-400 block text-[11px] mb-0.5">Bệnh nền</span>
                      <span className="text-xs font-bold text-slate-800 truncate block">
                        {selectedAppointment.health_records?.underlying_diseases ||
                          selectedAppointment.health_records?.underlyingDiseases ||
                          "Không ghi nhận"}
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
                        {selectedAppointment.room || "Phòng 102 - Khám Đa khoa"}
                      </span>
                    </div>
                  </div>

                  {/* Triệu chứng ban đầu */}
                  <div className="p-3 bg-slate-50 rounded-lg border border-slate-200/80 text-xs space-y-1">
                    <span className="font-semibold text-slate-600 block text-[11px]">
                      Triệu chứng ban đầu do sinh viên khai báo:
                    </span>
                    <p className="text-slate-800 italic">
                      &ldquo;
                      {selectedAppointment.reasonSymptoms ||
                        selectedAppointment.reason_symptoms ||
                        "Không có ghi chú triệu chứng"}
                      &rdquo;
                    </p>
                  </div>

                  {/* KHỐI CẢNH BÁO NẾU LỊCH HẸN BỊ HỦY (CANCELED) */}
                  {selectedAppointment.status === "CANCELED" && (
                    <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs space-y-1 text-amber-900">
                      <div className="flex items-center gap-1.5 font-bold text-amber-800">
                        <ShieldAlert className="w-4 h-4 text-amber-600" />
                        <span>Thông tin hủy lịch hẹn</span>
                      </div>
                      <div className="pl-5 space-y-0.5">
                        <p>
                          <strong>Người thực hiện:</strong>{" "}
                          <span className="px-1.5 py-0.5 bg-amber-100 rounded text-amber-900 font-semibold text-[11px]">
                            {selectedAppointment.canceledBy === "DOCTOR" ||
                            selectedAppointment.canceled_by === "DOCTOR"
                              ? "Bác sĩ phụ trách (DOCTOR)"
                              : "Sinh viên đặt lịch (STUDENT)"}
                          </span>
                        </p>
                        <p>
                          <strong>Lý do:</strong>{" "}
                          <span>
                            {selectedAppointment.cancelRescheduleReason ||
                              selectedAppointment.cancel_reschedule_reason ||
                              "Không có lý do chi tiết"}
                          </span>
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* FOOTER CỤM NÚT HÀNH ĐỘNG */}
              <div className="p-3.5 border-t border-slate-100 bg-slate-50/60 flex flex-wrap items-center justify-between gap-2.5">
                {selectedAppointment.status === "CONFIRMED" ? (
                  <>
                    <p className="text-xs text-slate-500 font-medium hidden sm:block">
                      Ca khám sẵn sàng tiếp nhận
                    </p>
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

      {/* 2 MODAL TƯƠNG TÁC */}
      {cancelModalOpen && (
        <DoctorCancelModal
          isOpen={cancelModalOpen}
          appointment={selectedAppointment}
          onClose={() => setCancelModalOpen(false)}
          onSuccess={handleCancelSuccess}
        />
      )}

      {rescheduleModalOpen && (
        <DoctorRescheduleModal
          isOpen={rescheduleModalOpen}
          appointment={selectedAppointment}
          onClose={() => setRescheduleModalOpen(false)}
          onSuccess={handleRescheduleSuccess}
        />
      )}

      {/* MODAL HỒ SƠ ĐẦY ĐỦ */}
      {profileModalOpen && (
        <StudentFullProfileModal
          isOpen={profileModalOpen}
          appointment={selectedAppointment}
          onClose={() => setProfileModalOpen(false)}
        />
      )}
    </div>
  );
}
