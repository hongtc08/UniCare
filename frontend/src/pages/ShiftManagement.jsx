import React from "react";
import ShiftManagementView from "../components/shifts/ShiftManagementView";
import { useAuth } from "../hooks/useAuth";

/**
 * Trang Quản lý Lịch trực & Mở ca khám tuần
 * - Tích hợp trực tiếp với authentication state từ `useAuth()`.
 * - Tự động trích xuất `userRole` và `userId` (hoặc `doctorId`).
 * - Không còn bất kỳ UI Mock hay switcher nào.
 */
export default function ShiftManagement() {
  const { user } = useAuth();

  // Chuẩn hóa role sang 'ADMIN' hoặc 'DOCTOR'
  // Dự án hỗ trợ role 'BAN_QUAN_LY' (Admin) và 'NHAN_VIEN_Y_TE' (Bác sĩ/Y tế) hoặc 'ADMIN'/'DOCTOR'
  const userRole =
    user?.role === "ADMIN" || user?.role === "BAN_QUAN_LY"
      ? "ADMIN"
      : user?.role === "DOCTOR" || user?.role === "NHAN_VIEN_Y_TE"
      ? "DOCTOR"
      : "ADMIN";

  // Lấy userId/doctorId từ user profile đang đăng nhập
  const currentUserId = user?.id || user?.userId || user?.doctorId || "doc-1";
  const currentUserName = user?.fullName || user?.name || user?.username || "Bác sĩ";

  return (
    <div className="min-h-screen bg-slate-50/50 pb-12">
      <ShiftManagementView
        userRole={userRole}
        currentUserId={currentUserId}
        currentUserName={currentUserName}
        onShiftSelect={(shift) => {
          console.log("Ca khám được chọn:", shift);
        }}
      />
    </div>
  );
}
