import React, { useState, useMemo, useEffect, useCallback } from "react";
import { Plus, RefreshCw, Loader2 } from "lucide-react";
import ShiftFilters from "./ShiftFilters";
import ShiftCalendar from "./ShiftCalendar";
import ShiftDetailModal from "./ShiftDetailModal";
import Toast from "../common/Toast";
import shiftService from "../../services/shiftService";
import { MOCK_DOCTORS, INITIAL_MOCK_SHIFTS } from "./mockShiftData";

function getMonday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  return new Date(d.setDate(diff));
}

function formatDateToISO(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * Hàm map Shift từ API response của Backend về cấu trúc UI Component
 */
function mapBackendShiftToUI(shift) {
  const startTime = shift.startTime ? shift.startTime.substring(0, 5) : "08:00";
  const endTime = shift.endTime ? shift.endTime.substring(0, 5) : "11:00";
  const startHour = parseInt(startTime.split(":")[0], 10);
  const shiftSlot = startHour < 12 ? "MORNING" : "AFTERNOON";

  return {
    id: shift.id,
    date: shift.shiftDate || shift.date,
    startTime: startTime,
    endTime: endTime,
    shiftSlot: shiftSlot,
    doctorId: shift.doctorId,
    doctorName: shift.doctorName || "Bác sĩ",
    serviceType: shift.serviceType,
    room: shift.room || "P.101 - Phòng y tế",
    bookedCount: shift.bookedQuantity ?? shift.bookedCount ?? 0,
    maxCapacity: shift.maxQuantity ?? shift.maxCapacity ?? 10,
    status: shift.status || "DANG_MO",
  };
}

/**
 * ShiftManagementView - Quản lý Lịch trực & Mở Ca khám tuần
 * Tích hợp toàn diện API GET / POST / PUT / DELETE shifts
 */
export default function ShiftManagementView({
  userRole = "ADMIN",
  currentUserId,
  currentUserName = "Bác sĩ",
  onShiftSelect,
}) {
  const isAdmin = userRole === "ADMIN" || userRole === "BAN_QUAN_LY";
  const isDoctor = !isAdmin;

  // State bộ lọc
  const [selectedDate, setSelectedDate] = useState(() => formatDateToISO(new Date()));
  const [selectedDoctor, setSelectedDoctor] = useState("ALL");
  const [selectedService, setSelectedService] = useState("ALL");
  const [selectedRoom, setSelectedRoom] = useState("ALL");

  // State Danh sách ca khám & Trạng thái tải
  const [shifts, setShifts] = useState(INITIAL_MOCK_SHIFTS);
  const [isLoading, setIsLoading] = useState(false);
  const [toast, setToast] = useState(null);

  // State Modal
  const [modalState, setModalState] = useState({
    isOpen: false,
    mode: "DETAIL", // 'DETAIL' | 'CREATE' | 'EDIT'
    selectedShift: null,
    selectedDayForCreate: "",
    selectedSlotForCreate: "MORNING",
  });

  // Tự động tắt Toast sau 4 giây
  const showToast = useCallback((message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => {
      setToast((prev) => (prev?.message === message ? null : prev));
    }, 4000);
  }, []);

  const currentDoctor = useMemo(() => {
    return (
      MOCK_DOCTORS.find((d) => String(d.id) === String(currentUserId)) || {
        id: currentUserId,
        name: currentUserName || "Bác sĩ cá nhân",
      }
    );
  }, [currentUserId, currentUserName]);

  // Tính 7 ngày trong tuần
  const weekDays = useMemo(() => {
    const monday = getMonday(new Date(selectedDate));
    const dayNames = ["Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"];
    const todayStr = formatDateToISO(new Date());

    return Array.from({ length: 7 }).map((_, index) => {
      const current = new Date(monday);
      current.setDate(monday.getDate() + index);
      const dateStr = formatDateToISO(current);

      return {
        date: dateStr,
        dayName: dayNames[index],
        dayNumber: current.getDate(),
        formattedDate: `${current.getDate()}/${current.getMonth() + 1}`,
        isToday: dateStr === todayStr,
      };
    });
  }, [selectedDate]);

  const weekRange = useMemo(() => {
    if (weekDays.length < 7) return { fromDate: null, toDate: null, label: "" };
    return {
      fromDate: weekDays[0].date,
      toDate: weekDays[6].date,
      label: `Tuần từ ${weekDays[0].formattedDate} đến ${weekDays[6].formattedDate}`,
    };
  }, [weekDays]);

  /**
   * 1. READ: Gọi API lấy danh sách ca khám tuần
   */
  const fetchShifts = useCallback(async () => {
    if (!weekRange.fromDate || !weekRange.toDate) return;

    try {
      setIsLoading(true);

      const params = {
        fromDate: weekRange.fromDate,
        toDate: weekRange.toDate,
      };

      // Phân quyền: DOCTOR chỉ lấy lịch của mình
      if (isDoctor && currentUserId) {
        params.doctorId = currentUserId;
      } else if (isAdmin && selectedDoctor !== "ALL") {
        params.doctorId = selectedDoctor;
      }

      if (selectedService !== "ALL") {
        params.serviceType = selectedService;
      }

      // Gọi service
      const res = await shiftService.getShifts(params);
      const rawList = res?.data || res || [];

      if (Array.isArray(rawList)) {
        const mapped = rawList.map(mapBackendShiftToUI);
        setShifts(mapped);
      }
    } catch (err) {
      console.warn("Backend API chưa khả dụng hoặc trả về lỗi, duy trì dữ liệu fallback:", err);
      // Nếu BE offline trong lúc dev, giữ dữ liệu hiện tại để không gián đoạn thao tác
    } finally {
      setIsLoading(false);
    }
  }, [weekRange.fromDate, weekRange.toDate, isDoctor, currentUserId, isAdmin, selectedDoctor, selectedService]);

  useEffect(() => {
    fetchShifts();
  }, [fetchShifts]);

  // Bộ lọc phụ ở Client (như lọc theo Phòng khám)
  const filteredShifts = useMemo(() => {
    return shifts.filter((shift) => {
      // Phân quyền: Bác sĩ chỉ xem lịch của mình
      if (isDoctor) {
        if (String(shift.doctorId) !== String(currentUserId)) return false;
      }
      // Lọc theo Phòng khám
      if (selectedRoom !== "ALL") {
        if (shift.room !== selectedRoom) return false;
      }
      return true;
    });
  }, [shifts, isDoctor, currentUserId, selectedRoom]);

  // Điều hướng Tuần
  const handlePrevWeek = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() - 7);
    setSelectedDate(formatDateToISO(d));
  };

  const handleNextWeek = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + 7);
    setSelectedDate(formatDateToISO(d));
  };

  const handleToday = () => {
    setSelectedDate(formatDateToISO(new Date()));
  };

  const handleResetFilters = () => {
    setSelectedDoctor("ALL");
    setSelectedService("ALL");
    setSelectedRoom("ALL");
  };

  // Mở modal
  const handleShiftClick = (shift) => {
    setModalState({
      isOpen: true,
      mode: "DETAIL",
      selectedShift: shift,
      selectedDayForCreate: "",
      selectedSlotForCreate: "MORNING",
    });
    if (onShiftSelect) onShiftSelect(shift);
  };

  const handleOpenCreateModal = () => {
    setModalState({
      isOpen: true,
      mode: "CREATE",
      selectedShift: null,
      selectedDayForCreate: selectedDate,
      selectedSlotForCreate: "MORNING",
    });
  };

  const handleAddShiftAtCell = (dateStr, slotKey) => {
    setModalState({
      isOpen: true,
      mode: "CREATE",
      selectedShift: null,
      selectedDayForCreate: dateStr,
      selectedSlotForCreate: slotKey,
    });
  };

  /**
   * 2. CREATE: Gọi API tạo mới ca khám
   */
  const handleCreateShift = async (payload) => {
    try {
      const res = await shiftService.createShift(payload);
      const createdData = res?.data || res;
      showToast("Mở ca khám tuần thành công!", "success");
      await fetchShifts();
      return createdData;
    } catch (err) {
      const beMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Mở ca khám thất bại.";
      showToast(beMsg, "error");
      throw err;
    }
  };

  /**
   * 3. UPDATE: Gọi API cập nhật ca khám
   */
  const handleUpdateShift = async (id, payload) => {
    try {
      const res = await shiftService.updateShift(id, payload);
      const updatedData = res?.data || res;
      showToast("Cập nhật thông tin ca khám thành công!", "success");
      await fetchShifts();
      return updatedData;
    } catch (err) {
      const beMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Cập nhật ca khám thất bại.";
      showToast(beMsg, "error");
      throw err;
    }
  };

  /**
   * 4. DELETE: Gọi API xóa ca khám
   */
  const handleDeleteShift = async (id) => {
    try {
      await shiftService.deleteShift(id);
      showToast("Đã xóa ca khám thành công!", "success");
      await fetchShifts();
    } catch (err) {
      const beMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Xóa ca khám thất bại.";
      showToast(beMsg, "error");
      throw err;
    }
  };

  return (
    <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 py-4 relative">
      {/* Toast Notification */}
      <Toast toast={toast} onClose={() => setToast(null)} />

      {/* 1. Header: Tiêu đề chính + Action Button */}
      <div className="flex items-center justify-between gap-4 mb-5">
        <div className="flex items-center gap-3">
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
            Quản lý Lịch trực & Mở Ca khám tuần
          </h1>
          {isLoading && (
            <div className="flex items-center gap-1.5 text-xs text-blue-600 bg-blue-50 px-2.5 py-1 rounded-full animate-pulse">
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
              <span>Đang đồng bộ...</span>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={fetchShifts}
            disabled={isLoading}
            className="p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg border border-slate-200 transition-colors disabled:opacity-50"
            title="Làm mới lịch trực"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? "animate-spin" : ""}`} />
          </button>

          {isAdmin && (
            <button
              type="button"
              onClick={handleOpenCreateModal}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 rounded-lg shadow-xs transition-colors shrink-0"
            >
              <Plus className="w-4 h-4" />
              <span>Mở Ca Khám</span>
            </button>
          )}
        </div>
      </div>

      {/* 2. Bộ lọc ShiftFilters */}
      <ShiftFilters
        selectedDate={selectedDate}
        onDateChange={setSelectedDate}
        selectedDoctor={selectedDoctor}
        onDoctorChange={setSelectedDoctor}
        selectedService={selectedService}
        onServiceChange={setSelectedService}
        selectedRoom={selectedRoom}
        onRoomChange={setSelectedRoom}
        doctors={MOCK_DOCTORS}
        userRole={userRole}
        currentDoctorName={currentDoctor.name}
        onResetFilters={handleResetFilters}
        onPrevWeek={handlePrevWeek}
        onNextWeek={handleNextWeek}
        onToday={handleToday}
        weekRangeLabel={weekRange.label}
      />

      {/* 3. Lịch Time-Grid View */}
      <ShiftCalendar
        weekDays={weekDays}
        shifts={filteredShifts}
        onShiftClick={handleShiftClick}
        onAddShiftClick={handleAddShiftAtCell}
        isAdmin={isAdmin}
      />

      {/* 4. Modal Thêm / Sửa / Xem chi tiết */}
      <ShiftDetailModal
        isOpen={modalState.isOpen}
        onClose={() => setModalState({ ...modalState, isOpen: false })}
        shift={modalState.selectedShift}
        mode={modalState.mode}
        doctors={MOCK_DOCTORS}
        onCreateShift={handleCreateShift}
        onUpdateShift={handleUpdateShift}
        onDeleteShift={handleDeleteShift}
        selectedDayForCreate={modalState.selectedDayForCreate}
        selectedSlotForCreate={modalState.selectedSlotForCreate}
        userRole={userRole}
        currentUserId={currentUserId}
        currentUserName={currentUserName}
      />
    </div>
  );
}
