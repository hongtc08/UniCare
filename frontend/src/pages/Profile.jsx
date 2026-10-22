import { useState } from "react";
import { useAuth } from "../hooks/useAuth";
import ChangePasswordModal from "../components/ChangePasswordModal";
import { User, KeyRound, ShieldCheck } from "lucide-react";

const Profile = () => {
  const { user } = useAuth();
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <div className="flex flex-wrap gap-4 items-center justify-between pb-6 border-b border-gray-100">
          <div className="flex items-center space-x-4">
            <div className="p-4 bg-blue-50 text-blue-600 rounded-2xl">
              <User className="w-10 h-10" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-800">
                {user?.fullName ||
                  user?.name ||
                  user?.username ||
                  "Chưa cập nhật"}
              </h2>
              <p className="text-sm text-gray-500">
                Vai trò:{" "}
                <span className="font-semibold text-blue-600">
                  {user?.role || "SINH_VIEN"}
                </span>
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-1.5 px-3 py-1 bg-green-50 text-green-700 text-xs font-semibold rounded-full border border-green-200">
            <ShieldCheck className="w-4 h-4" />
            <span>Đã kích hoạt</span>
          </div>
        </div>

        <div className="pt-5 space-y-3.5">
          <div className="flex justify-between items-center text-sm py-1">
            <span className="text-gray-500 font-medium">
              Tên đăng nhập / MSSV / Mã NV:
            </span>
            <span className="font-semibold text-gray-800">
              {user?.username || "Chưa có"}
            </span>
          </div>
          <div className="flex justify-between items-center text-sm py-1">
            <span className="text-gray-500 font-medium">
              Quyền hạn hệ thống:
            </span>
            <span className="px-2.5 py-0.5 bg-gray-100 text-gray-700 rounded-md text-xs font-medium">
              {user?.role || "SINH_VIEN"}
            </span>
          </div>
        </div>

        {/* Nút mở Modal Đổi mật khẩu */}
        <div className="mt-8 pt-6 border-t border-gray-100 flex flex-wrap gap-4 items-center justify-between">
          <div>
            <h4 className="text-sm font-semibold text-gray-800">
              Bảo mật tài khoản
            </h4>
            <p className="text-xs text-gray-500 mt-0.5">
              Nên đổi mật khẩu định kỳ để bảo vệ dữ liệu y tế cá nhân
            </p>
          </div>
          <button
            type="button"
            onClick={() => setIsChangePasswordOpen(true)}
            className="flex items-center space-x-2 px-4 py-2 bg-blue-50 text-blue-600 hover:bg-blue-100 active:bg-blue-200 rounded-xl text-sm font-semibold transition"
          >
            <KeyRound className="w-4 h-4" />
            <span>Đổi mật khẩu</span>
          </button>
        </div>
      </div>

      {/* Modal Đổi Mật Khẩu */}
      <ChangePasswordModal
        isOpen={isChangePasswordOpen}
        onClose={() => setIsChangePasswordOpen(false)}
      />
    </div>
  );
};

export default Profile;
