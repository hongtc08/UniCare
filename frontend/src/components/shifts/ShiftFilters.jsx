import React from "react";
import { 
  Calendar as CalendarIcon, 
  ChevronLeft, 
  ChevronRight, 
  UserRound, 
  Stethoscope, 
  Building2,
  RotateCcw,
  UserCheck
} from "lucide-react";
import { CLINIC_ROOMS } from "./mockShiftData";

/**
 * Component ShiftFilters - Bộ lọc chuẩn y tế gồm:
 * 1. Date Picker (chọn tuần / ngày)
 * 2. Dropdown Bác sĩ (chỉ hiển thị khi là ADMIN)
 * 3. Dropdown Loại dịch vụ (Khám Đa khoa / Tư vấn Tâm lý)
 * 4. Dropdown Phòng khám (chỉ gồm 2 phòng cố định P.101 & P.102)
 */
export default function ShiftFilters({
  selectedDate,
  onDateChange,
  selectedDoctor,
  onDoctorChange,
  selectedService,
  onServiceChange,
  selectedRoom,
  onRoomChange,
  doctors = [],
  userRole = "ADMIN",
  currentDoctorName = "",
  onResetFilters,
  onPrevWeek,
  onNextWeek,
  onToday,
  weekRangeLabel = "",
}) {
  const isAdmin = userRole === "ADMIN" || userRole === "BAN_QUAN_LY";

  return (
    <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 sm:p-4 mb-4 shadow-xs">
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
        
        {/* Tuần & Date Picker */}
        <div className="flex flex-wrap items-center gap-2">
          <div className="inline-flex items-center rounded-lg border border-slate-200 bg-slate-50 p-0.5">
            <button
              type="button"
              onClick={onPrevWeek}
              className="p-1.5 rounded-md text-slate-600 hover:text-slate-900 hover:bg-white transition-colors"
              title="Tuần trước"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              type="button"
              onClick={onToday}
              className="px-2.5 py-1 text-xs font-semibold text-slate-700 hover:bg-white rounded-md transition-colors"
            >
              Hôm nay
            </button>
            <button
              type="button"
              onClick={onNextWeek}
              className="p-1.5 rounded-md text-slate-600 hover:text-slate-900 hover:bg-white transition-colors"
              title="Tuần sau"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <div className="relative flex items-center">
            <CalendarIcon className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 pointer-events-none" />
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => onDateChange(e.target.value)}
              className="pl-8 pr-2.5 py-1.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 font-medium"
            />
          </div>

          {weekRangeLabel && (
            <span className="text-xs font-medium text-slate-500 px-2.5 py-1 rounded-md bg-slate-100 hidden sm:inline-block">
              {weekRangeLabel}
            </span>
          )}
        </div>

        {/* Cụm Dropdown Filters */}
        <div className="flex flex-wrap items-center gap-2.5">
          
          {/* 1. Dropdown Bác sĩ (CHỈ HIỆN KHI LÀ ADMIN) */}
          {isAdmin ? (
            <div className="relative min-w-[180px] flex-1 sm:flex-initial">
              <UserRound className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <select
                value={selectedDoctor}
                onChange={(e) => onDoctorChange(e.target.value)}
                className="w-full pl-8 pr-7 py-1.5 text-xs sm:text-sm bg-slate-50 hover:bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer"
              >
                <option value="ALL">Tất cả Bác sĩ</option>
                {doctors.map((doc) => (
                  <option key={doc.id} value={doc.id}>
                    {doc.name}
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-blue-50 border border-blue-200 text-blue-700 text-xs font-medium">
              <UserCheck className="w-3.5 h-3.5 text-blue-600" />
              <span>BS: <strong>{currentDoctorName || "Cá nhân"}</strong></span>
            </div>
          )}

          {/* 2. Dropdown Loại dịch vụ */}
          <div className="relative min-w-[165px] flex-1 sm:flex-initial">
            <Stethoscope className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            <select
              value={selectedService}
              onChange={(e) => onServiceChange(e.target.value)}
              className="w-full pl-8 pr-7 py-1.5 text-xs sm:text-sm bg-slate-50 hover:bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer"
            >
              <option value="ALL">Tất cả Dịch vụ</option>
              <option value="KHAM_DA_KHOA">Khám Đa khoa</option>
              <option value="TU_VAN_TAM_LY">Tư vấn Tâm lý</option>
            </select>
          </div>

          {/* 3. Dropdown Phòng khám (Đúng 2 phòng P.101 & P.102) */}
          <div className="relative min-w-[195px] flex-1 sm:flex-initial">
            <Building2 className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            <select
              value={selectedRoom}
              onChange={(e) => onRoomChange(e.target.value)}
              className="w-full pl-8 pr-7 py-1.5 text-xs sm:text-sm bg-slate-50 hover:bg-white border border-slate-200 rounded-lg text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer"
            >
              <option value="ALL">Tất cả Phòng khám</option>
              {CLINIC_ROOMS.map((room) => (
                <option key={room} value={room}>
                  {room}
                </option>
              ))}
            </select>
          </div>

          {/* Nút Reset */}
          <button
            type="button"
            onClick={onResetFilters}
            className="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors border border-transparent hover:border-slate-200"
            title="Đặt lại bộ lọc"
          >
            <RotateCcw className="w-4 h-4" />
          </button>
        </div>

      </div>
    </div>
  );
}
