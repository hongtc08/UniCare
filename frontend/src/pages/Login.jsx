import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import authApi from "../api/authApi";

import {
  Activity,
  Lock,
  User,
  AlertCircle,
  Loader2,
  Eye,
  EyeOff,
} from "lucide-react";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleNavigateByRole = (role) => {
    switch (role) {
      case "SINH_VIEN":
        navigate("/profile");
        break;
      case "NHAN_VIEN_Y_TE":
        // Tab đầu tiên của Nhân viên y tế: Lịch hôm nay (/lich-kham)
        navigate("/medical-list");
        break;
      case "BAN_QUAN_LY":
        // Tab đầu tiên của Ban quản lý: Bảng điều hành (/dashboard)
        navigate("/dashboard");
        break;
      default:
        navigate("/dashboard");
        break;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setLoading(true);

    try {
      // response.data đã được unwrap từ axiosClient
      const response = await authApi.login({
        username: username.trim(),
        password: password,
      });

      const { token, role, fullName } = response?.data ?? response;

      // Lưu trữ state xác thực
      login({ username, fullName, role }, token);

      // Chuyển hướng theo role
      handleNavigateByRole(role);
    } catch (error) {
      // Bắt lỗi từ BE trả về (vd: 401 BadCredentialsException hoặc 403 LockedException)
      const message =
        error.response?.data?.message ||
        (error.response?.status === 401
          ? "Tên đăng nhập hoặc mật khẩu không chính xác."
          : error.response?.status === 403
            ? "Tài khoản của bạn đã bị khóa hoặc không có quyền truy cập."
            : "Không thể kết nối đến máy chủ. Vui lòng thử lại sau.");
      setErrorMessage(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="bg-white p-8 rounded-2xl shadow-xl max-w-md w-full border border-gray-100">
        <div className="flex justify-center mb-4 text-blue-600">
          <div className="p-3 bg-blue-50 rounded-2xl">
            <Activity className="w-10 h-10 text-blue-600" />
          </div>
        </div>

        <h2 className="text-2xl font-bold text-center text-gray-800">
          Hệ thống Y tế UniCare
        </h2>
        <p className="text-sm text-center text-gray-500 mt-1 mb-6">
          Đăng nhập hệ thống chăm sóc sức khỏe sinh viên
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Tên đăng nhập (MSSV / Mã NV)
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <User className="w-5 h-5" />
              </span>
              <input
                type="text"
                required
                disabled={loading}
                className="w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition disabled:bg-gray-100"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="VD: 24520579 hoặc NV001"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Mật khẩu
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <Lock className="w-5 h-5" />
              </span>
              <input
                type={showPassword ? "text" : "password"}
                required
                disabled={loading}
                className="w-full pl-10 pr-10 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition disabled:bg-gray-100"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                tabIndex={-1}
                className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600 transition focus:outline-none"
              >
                {showPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>

          {/* Thông báo lỗi nổi bật */}
          {errorMessage && (
            <div className="p-3 rounded-lg bg-red-50 border border-red-200 flex items-start space-x-2 text-red-700 text-sm animate-shake">
              <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-600" />
              <span>{errorMessage}</span>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white font-semibold py-2.5 rounded-lg shadow-sm transition flex items-center justify-center space-x-2 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                <span>Đang xử lý...</span>
              </>
            ) : (
              <span>Đăng nhập</span>
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
