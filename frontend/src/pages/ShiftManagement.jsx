import React, { useState } from "react";
import ShiftManagementView from "../components/shifts/ShiftManagementView";
import { useAuth } from "../hooks/useAuth";

/**
 * Trang ShiftManagement demo cả 2 chế độ (ADMIN / DOCTOR)
 */
export default function ShiftManagement() {
  const { user } = useAuth();

  // Cho phép chuyển đổi Role trên giao diện để tiện kiểm thử & demo
  const [demoRole, setDemoRole] = useState(
    user?.role === "BAN_QUAN_LY" ? "ADMIN" : user?.role === "NHAN_VIEN_Y_TE" ? "DOCTOR" : "ADMIN"
  );
  const [demoDoctorId, setDemoDoctorId] = useState("doc-1");

  return (
    <div className="min-h-screen bg-slate-50/50 pb-12">
      {/* Thanh công cụ Demo Role Switcher tiện lợi */}
      <div className="bg-slate-900 text-white px-4 py-2.5 text-xs flex flex-wrap items-center justify-between gap-3 shadow-sm rounded-lg mb-4">
        <div className="flex items-center gap-2">
          <span className="font-semibold text-amber-400">⚡ Chế độ thử nghiệm Role (Phân quyền):</span>
          <span className="text-slate-300">Chuyển đổi vai trò để xem phản hồi giao diện thực tế</span>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-slate-300">Vai trò:</label>
            <select
              value={demoRole}
              onChange={(e) => setDemoRole(e.target.value)}
              className="bg-slate-800 text-white border border-slate-700 rounded px-2.5 py-1 text-xs font-semibold focus:outline-none focus:ring-1 focus:ring-blue-400 cursor-pointer"
            >
              <option value="ADMIN">ADMIN (Xem tất cả ca & phân công)</option>
              <option value="DOCTOR">DOCTOR (Ẩn lọc BS, chỉ xem ca của mình)</option>
            </select>
          </div>

          {demoRole === "DOCTOR" && (
            <div className="flex items-center gap-2">
              <label className="text-slate-300">BS hiện tại:</label>
              <select
                value={demoDoctorId}
                onChange={(e) => setDemoDoctorId(e.target.value)}
                className="bg-slate-800 text-white border border-slate-700 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400 cursor-pointer"
              >
                <option value="doc-1">doc-1: BS.CKII Nguyễn Văn An</option>
                <option value="doc-2">doc-2: ThS.BS Trần Thị Mai</option>
                <option value="doc-3">doc-3: BS. Lê Hoàng Long</option>
                <option value="doc-4">doc-4: TS.BS Phạm Quỳnh Anh</option>
              </select>
            </div>
          )}
        </div>
      </div>

      {/* Render Component ShiftManagementView */}
      <ShiftManagementView
        userRole={demoRole}
        currentUserId={demoDoctorId}
        onShiftSelect={(shift) => {
          console.log("Đã chọn ca khám:", shift);
        }}
      />
    </div>
  );
}
