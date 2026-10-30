import React from "react";
import { 
  Clock, 
  MapPin, 
  User, 
  Users, 
  Coffee, 
  HeartPulse, 
  Brain,
  Plus
} from "lucide-react";

/**
 * Component ShiftCard:
 * - Giữ nguyên 100% nội dung và cấu trúc: Giờ, Tên bác sĩ, Phòng khám, Trạng thái slot, Progress Bar
 * - Text sử dụng break-words, whitespace-normal để tự xuống dòng đúng nghĩa từ ngữ, không bị cắt đôi từ
 */
function ShiftCard({ shift, onShiftClick }) {
  const isFull = shift.bookedCount >= shift.maxCapacity;
  const isPsychology = shift.serviceType === "TU_VAN_TAM_LY";
  const percentage = Math.min(
    Math.round((shift.bookedCount / shift.maxCapacity) * 100),
    100
  );

  return (
    <div
      onClick={() => onShiftClick && onShiftClick(shift)}
      className={`relative min-h-[130px] p-3.5 rounded-xl border flex flex-col justify-between transition-all duration-200 cursor-pointer shadow-xs hover:shadow-md hover:-translate-y-0.5 select-none ${
        isPsychology
          ? "bg-purple-50/70 border-purple-200/90 text-purple-950 hover:border-purple-300"
          : "bg-blue-50/70 border-blue-200/90 text-blue-950 hover:border-blue-300"
      }`}
    >
      {/* Top: Giờ ca + Badge dịch vụ */}
      <div>
        <div className="flex items-start justify-between gap-1.5 mb-2">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-700 whitespace-nowrap">
            <Clock className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span>{shift.startTime} - {shift.endTime}</span>
          </div>

          <span
            className={`inline-flex items-center gap-1 px-2 py-0.5 text-xs font-semibold rounded-md shrink-0 border whitespace-nowrap ${
              isPsychology
                ? "bg-purple-100/90 text-purple-700 border-purple-200"
                : "bg-blue-100/90 text-blue-700 border-blue-200"
            }`}
          >
            {isPsychology ? (
              <Brain className="w-3 h-3 shrink-0" />
            ) : (
              <HeartPulse className="w-3 h-3 shrink-0" />
            )}
            <span>{isPsychology ? "Tâm lý" : "Đa khoa"}</span>
          </span>
        </div>

        {/* Tên Bác sĩ: break-words, whitespace-normal để không bị cắt nửa từ */}
        <div className="flex items-start gap-1.5 mb-1.5">
          <User className="w-4 h-4 text-slate-500 shrink-0 mt-0.5" />
          <h4 className="text-sm font-semibold text-slate-800 leading-snug whitespace-normal break-words">
            {shift.doctorName}
          </h4>
        </div>

        {/* Phòng khám */}
        {shift.room && (
          <div className="flex items-start gap-1.5 text-[13px] text-slate-500 mb-3 whitespace-normal break-words">
            <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
            <span className="leading-snug">{shift.room}</span>
          </div>
        )}
      </div>

      {/* Bottom: Text Slot và Progress Bar */}
      <div className="pt-2 border-t border-slate-200/60 mt-auto">
        <div className="flex items-center justify-between text-xs font-medium text-slate-600 mb-1.5">
          <div className="flex items-center gap-1.5 whitespace-nowrap">
            <Users className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span>Đã đặt:</span>
            <span
              className={`font-bold px-1.5 py-0.2 rounded ${
                isFull
                  ? "bg-red-100 text-red-700"
                  : "bg-emerald-100 text-emerald-800"
              }`}
            >
              {shift.bookedCount}/{shift.maxCapacity}
            </span>
          </div>
        </div>

        <div className="w-full bg-slate-200/80 rounded-full h-1.5 overflow-hidden">
          <div
            className={`h-full transition-all duration-300 rounded-full ${
              isFull ? "bg-red-500" : "bg-emerald-500"
            }`}
            style={{ width: `${percentage}%` }}
          />
        </div>
      </div>
    </div>
  );
}

/**
 * Component ShiftCalendar:
 * - Bao bọc bằng container cuộn ngang: `w-full overflow-x-auto`
 * - Container lưới có `min-w-[1280px]` (Cột khung giờ 110px + 7 cột ngày mỗi cột min-w-[165px])
 * - Cột Khung Giờ bên trái làm DÍNH (Sticky): `sticky left-0 bg-white z-20` cùng shadow nhẹ `shadow-[4px_0_8px_-2px_rgba(0,0,0,0.06)]`
 */
export default function ShiftCalendar({
  weekDays = [],
  shifts = [],
  onShiftClick,
  onAddShiftClick,
  isAdmin = false,
}) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
      {/* 2. Container cuộn ngang (Horizontal Scroll Wrapper) */}
      <div className="w-full overflow-x-auto">
        {/* Lưới có min-w-[1280px] đảm bảo không gian thoáng cho tất cả các cột */}
        <div className="min-w-[1280px] grid grid-cols-[110px_repeat(7,minmax(165px,1fr))]">
          
          {/* ============================================================== */}
          {/* 1. HÀNG HEADER NGÀY                                           */}
          {/* ============================================================== */}
          {/* Cột Khung Giờ Sticky ở Header */}
          <div className="sticky left-0 z-30 bg-slate-100 border-r border-b border-slate-200 p-3 text-center flex flex-col items-center justify-center shadow-[4px_0_8px_-2px_rgba(0,0,0,0.06)]">
            <span className="text-xs uppercase tracking-wider font-bold text-slate-500 whitespace-nowrap">
              Khung giờ
            </span>
          </div>

          {/* 7 Cột Ngày (Thứ 2 -> CN) */}
          {weekDays.map((day) => (
            <div
              key={day.date}
              className={`p-3 text-center border-r border-b border-slate-200 last:border-r-0 transition-colors bg-slate-50 ${
                day.isToday ? "bg-blue-50/80" : ""
              }`}
            >
              <div className="text-sm font-medium text-slate-600 uppercase tracking-tight">
                {day.dayName}
              </div>

              <div
                className={`inline-flex items-center justify-center w-8 h-8 rounded-full text-lg font-bold mt-1 ${
                  day.isToday
                    ? "bg-blue-600 text-white shadow-xs"
                    : "text-slate-800"
                }`}
              >
                {day.dayNumber}
              </div>

              <div className="text-xs text-slate-400 mt-0.5 font-medium">
                {day.formattedDate}
              </div>
            </div>
          ))}

          {/* ============================================================== */}
          {/* 2. KHỐI 1: CA SÁNG (08:00 - 11:00)                             */}
          {/* ============================================================== */}
          {/* Cột Khung Giờ Sáng Sticky */}
          <div className="sticky left-0 z-20 bg-slate-50/95 border-r border-b border-slate-200 p-3.5 flex flex-col items-center justify-center text-center shadow-[4px_0_8px_-2px_rgba(0,0,0,0.06)]">
            <span className="text-sm font-semibold text-slate-800">08:00</span>
            <span className="text-xs text-slate-400 my-0.5">đến</span>
            <span className="text-sm font-semibold text-slate-800">11:00</span>
            <span className="mt-2 text-xs font-semibold text-blue-700 bg-blue-100/90 px-2 py-0.5 rounded-md whitespace-nowrap">
              Ca Sáng
            </span>
          </div>

          {/* 7 Cột Ngày - Ca Sáng */}
          {weekDays.map((day) => {
            const morningShifts = shifts.filter(
              (s) =>
                s.date === day.date &&
                (s.shiftSlot === "MORNING" || parseInt(s.startTime?.split(":")[0], 10) < 12)
            );

            return (
              <div
                key={`morning-${day.date}`}
                className={`p-2.5 border-r border-b border-slate-200 last:border-r-0 flex flex-col gap-2 min-h-[190px] ${
                  day.isToday ? "bg-blue-50/20" : "bg-white"
                }`}
              >
                {/* Danh sách ca trực xếp dọc */}
                {morningShifts.map((shift) => (
                  <ShiftCard
                    key={shift.id}
                    shift={shift}
                    onShiftClick={onShiftClick}
                  />
                ))}

                {/* Nút thêm ca mờ khi còn phòng trống */}
                {isAdmin && morningShifts.length < 2 && (
                  <button
                    type="button"
                    onClick={() =>
                      onAddShiftClick && onAddShiftClick(day.date, "MORNING")
                    }
                    className="w-full py-2 px-2 text-xs font-medium text-slate-400 hover:text-blue-600 hover:bg-blue-50/80 border border-dashed border-slate-200 hover:border-blue-300 rounded-lg transition-all flex items-center justify-center gap-1 group mt-auto"
                    title="Mở thêm ca khám cho ca sáng"
                  >
                    <Plus className="w-3.5 h-3.5 transition-transform group-hover:scale-110 shrink-0" />
                    <span className="whitespace-nowrap">
                      {morningShifts.length === 0 ? "Mở ca sáng" : "+ Thêm ca"}
                    </span>
                  </button>
                )}

                {!isAdmin && morningShifts.length === 0 && (
                  <div className="flex-1 flex items-center justify-center text-xs text-slate-300">
                    --
                  </div>
                )}
              </div>
            );
          })}

          {/* ============================================================== */}
          {/* 3. KHỐI 2: KHOẢNG NGHỈ TRƯA (11:00 - 13:00)                    */}
          {/* ============================================================== */}
          {/* Cột Khung Giờ Trưa Sticky */}
          <div className="sticky left-0 z-20 bg-slate-100 border-r border-b border-dashed border-slate-200 p-2.5 flex flex-col items-center justify-center text-center shadow-[4px_0_8px_-2px_rgba(0,0,0,0.06)]">
            <span className="text-xs font-semibold text-slate-600 whitespace-nowrap">
              11:00 - 13:00
            </span>
          </div>

          {/* 7 Cột Nghỉ Trưa làm mờ */}
          <div className="col-span-7 p-2.5 bg-slate-100/70 border-b border-dashed border-slate-200 flex items-center justify-center gap-2 text-xs font-medium text-slate-500 italic">
            <Coffee className="w-4 h-4 text-slate-400 shrink-0" />
            <span>Khoảng nghỉ trưa (Không tiếp nhận lịch khám)</span>
          </div>

          {/* ============================================================== */}
          {/* 4. KHỐI 3: CA CHIỀU (13:00 - 17:00)                            */}
          {/* ============================================================== */}
          {/* Cột Khung Giờ Chiều Sticky */}
          <div className="sticky left-0 z-20 bg-slate-50/95 border-r border-slate-200 p-3.5 flex flex-col items-center justify-center text-center shadow-[4px_0_8px_-2px_rgba(0,0,0,0.06)]">
            <span className="text-sm font-semibold text-slate-800">13:00</span>
            <span className="text-xs text-slate-400 my-0.5">đến</span>
            <span className="text-sm font-semibold text-slate-800">17:00</span>
            <span className="mt-2 text-xs font-semibold text-amber-700 bg-amber-100/90 px-2 py-0.5 rounded-md whitespace-nowrap">
              Ca Chiều
            </span>
          </div>

          {/* 7 Cột Ngày - Ca Chiều */}
          {weekDays.map((day) => {
            const afternoonShifts = shifts.filter(
              (s) =>
                s.date === day.date &&
                (s.shiftSlot === "AFTERNOON" || parseInt(s.startTime?.split(":")[0], 10) >= 12)
            );

            return (
              <div
                key={`afternoon-${day.date}`}
                className={`p-2.5 border-r border-slate-200 last:border-r-0 flex flex-col gap-2 min-h-[190px] ${
                  day.isToday ? "bg-blue-50/20" : "bg-white"
                }`}
              >
                {/* Danh sách ca trực xếp dọc */}
                {afternoonShifts.map((shift) => (
                  <ShiftCard
                    key={shift.id}
                    shift={shift}
                    onShiftClick={onShiftClick}
                  />
                ))}

                {/* Nút thêm ca mờ khi còn phòng trống */}
                {isAdmin && afternoonShifts.length < 2 && (
                  <button
                    type="button"
                    onClick={() =>
                      onAddShiftClick && onAddShiftClick(day.date, "AFTERNOON")
                    }
                    className="w-full py-2 px-2 text-xs font-medium text-slate-400 hover:text-amber-600 hover:bg-amber-50/80 border border-dashed border-slate-200 hover:border-amber-300 rounded-lg transition-all flex items-center justify-center gap-1 group mt-auto"
                    title="Mở thêm ca khám cho ca chiều"
                  >
                    <Plus className="w-3.5 h-3.5 transition-transform group-hover:scale-110 shrink-0" />
                    <span className="whitespace-nowrap">
                      {afternoonShifts.length === 0 ? "Mở ca chiều" : "+ Thêm ca"}
                    </span>
                  </button>
                )}

                {!isAdmin && afternoonShifts.length === 0 && (
                  <div className="flex-1 flex items-center justify-center text-xs text-slate-300">
                    --
                  </div>
                )}
              </div>
            );
          })}

        </div>
      </div>
    </div>
  );
}
