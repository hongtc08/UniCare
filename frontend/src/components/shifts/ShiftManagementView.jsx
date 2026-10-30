import React, { useState, useMemo } from "react";
import { Plus } from "lucide-react";
import ShiftFilters from "./ShiftFilters";
import ShiftCalendar from "./ShiftCalendar";
import ShiftDetailModal from "./ShiftDetailModal";
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
 * ShiftManagementView - Quản lý Lịch trực & Mở Ca khám tuần
 * Tích hợp Authentication State thực tế:
 * - Admin: xem tất cả ca, bộ lọc Bác sĩ đầy đủ, phân công mọi bác sĩ.
 * - Doctor: chỉ xem lịch của chính mình, ẩn bộ lọc Bác sĩ, Modal cố định bác sĩ đang đăng nhập.
 */
export default function ShiftManagementView({
  userRole = "ADMIN",
  currentUserId = "doc-1",
  currentUserName = "Bác sĩ",
  onShiftSelect,
}) {
  const isAdmin = userRole === "ADMIN" || userRole === "BAN_QUAN_LY";
  const isDoctor = !isAdmin;

  // Bộ lọc
  const [selectedDate, setSelectedDate] = useState(() => formatDateToISO(new Date()));
  const [selectedDoctor, setSelectedDoctor] = useState("ALL");
  const [selectedService, setSelectedService] = useState("ALL");
  const [selectedRoom, setSelectedRoom] = useState("ALL");

  // State Ca khám
  const [shifts, setShifts] = useState(INITIAL_MOCK_SHIFTS);

  // State Modal
  const [modalState, setModalState] = useState({
    isOpen: false,
    mode: "DETAIL", // 'DETAIL' | 'CREATE' | 'EDIT'
    selectedShift: null,
    selectedDayForCreate: "",
    selectedSlotForCreate: "MORNING",
  });

  const currentDoctor = useMemo(() => {
    return (
      MOCK_DOCTORS.find((d) => d.id === currentUserId) || {
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

  const weekRangeLabel = useMemo(() => {
    if (weekDays.length < 7) return "";
    return `Tuần từ ${weekDays[0].formattedDate} đến ${weekDays[6].formattedDate}`;
  }, [weekDays]);

  // Phân quyền & Lọc đa tiêu chí
  const filteredShifts = useMemo(() => {
    return shifts.filter((shift) => {
      // 1. Phân quyền: DOCTOR chỉ thấy lịch của chính mình (doctorId === currentUserId)
      if (isDoctor) {
        if (shift.doctorId !== currentUserId) return false;
      }

      // 2. Lọc theo Bác sĩ (chỉ áp dụng khi ADMIN chọn lọc)
      if (isAdmin && selectedDoctor !== "ALL") {
        if (shift.doctorId !== selectedDoctor) return false;
      }

      // 3. Lọc theo Loại dịch vụ
      if (selectedService !== "ALL") {
        if (shift.serviceType !== selectedService) return false;
      }

      // 4. Lọc theo Phòng khám
      if (selectedRoom !== "ALL") {
        if (shift.room !== selectedRoom) return false;
      }

      return true;
    });
  }, [shifts, isAdmin, isDoctor, currentUserId, selectedDoctor, selectedService, selectedRoom]);

  // Handler điều hướng tuần
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

  // Handler Click Card xem chi tiết
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

  // Handler Mở ca khám mới trên Header
  const handleOpenCreateModal = () => {
    setModalState({
      isOpen: true,
      mode: "CREATE",
      selectedShift: null,
      selectedDayForCreate: selectedDate,
      selectedSlotForCreate: "MORNING",
    });
  };

  // Handler Mở ca nhanh tại ô trong grid
  const handleAddShiftAtCell = (dateStr, slotKey) => {
    setModalState({
      isOpen: true,
      mode: "CREATE",
      selectedShift: null,
      selectedDayForCreate: dateStr,
      selectedSlotForCreate: slotKey,
    });
  };

  // CRUD handlers
  const handleCreateShift = (newShiftData) => {
    setShifts((prev) => [newShiftData, ...prev]);
  };

  const handleUpdateShift = (updatedShift) => {
    setShifts((prev) =>
      prev.map((s) => (s.id === updatedShift.id ? updatedShift : s))
    );
  };

  const handleDeleteShift = (shiftId) => {
    setShifts((prev) => prev.filter((s) => s.id !== shiftId));
  };

  return (
    <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 py-4">
      {/* 1. Header: Tiêu đề chính + Action Button */}
      <div className="flex items-center justify-between gap-4 mb-5">
        <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
          Quản lý Lịch trực & Mở Ca khám tuần
        </h1>

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

      {/* 2. Bộ lọc ShiftFilters (Admin: đầy đủ BS; Doctor: ẩn dropdown BS) */}
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
        weekRangeLabel={weekRangeLabel}
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
        shifts={shifts}
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
