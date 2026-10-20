import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import ChangePasswordModal from '../ChangePasswordModal';
import { Bell, KeyRound, LogOut, ChevronDown } from 'lucide-react';

const Header = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Đóng dropdown khi click ra ngoài
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Tạo avatar từ tên đầy đủ (VD: "Tăng Chấn Hồng" -> "TH", "admin" -> "AD")
  const getInitials = () => {
    const name = user?.fullName || user?.name || user?.username || 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  };

  const roleDisplay = () => {
    switch (user?.role) {
      case 'SINH_VIEN':
        return 'Sinh viên';
      case 'NHAN_VIEN_Y_TE':
        return 'Cán bộ Y tế';
      case 'BAN_QUAN_LY':
        return 'Ban Quản lý';
      default:
        return 'Người dùng';
    }
  };

  return (
    <>
      <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between sticky top-0 z-20 shadow-sm">
        {/* 1. Bên trái: Để trống hoặc hiển thị breadcrumb/tiêu đề nhẹ nhàng */}
        <div className="flex items-center">
          <span className="text-sm font-medium text-slate-500">
            Hệ thống Quản lý Y tế Sinh viên
          </span>
        </div>

        {/* 2. Bên phải: Chuông thông báo & Thông tin User */}
        <div className="flex items-center space-x-4">
          {/* Nút Chuông thông báo */}
          <button
            type="button"
            className="relative p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-xl transition"
            title="Thông báo"
          >
            <Bell className="w-5 h-5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full ring-2 ring-white"></span>
          </button>

          {/* User Profile dropdown */}
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center space-x-3 p-1.5 rounded-xl hover:bg-slate-50 transition border border-transparent hover:border-slate-200"
            >
              {/* Avatar viết tắt */}
              <div className="w-9 h-9 rounded-xl bg-blue-100 text-blue-600 font-bold text-sm flex items-center justify-center shadow-xs">
                {getInitials()}
              </div>

              {/* Tên và chức danh */}
              <div className="text-left hidden md:block">
                <div className="text-sm font-semibold text-slate-800 leading-tight">
                  {user?.fullName || user?.name || user?.username || 'Người dùng'}
                </div>
                <div className="text-xs text-slate-400">
                  {roleDisplay()} · {user?.username || 'Chưa cập nhật'}
                </div>
              </div>

              <ChevronDown className="w-4 h-4 text-slate-400" />
            </button>

            {/* Dropdown Menu */}
            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-56 bg-white rounded-2xl shadow-xl border border-slate-100 py-2 z-50 animate-fade-in">
                <div className="px-4 py-2 border-b border-slate-100 md:hidden">
                  <div className="text-sm font-bold text-slate-800">
                    {user?.fullName || user?.name || user?.username}
                  </div>
                  <div className="text-xs text-slate-500">
                    {roleDisplay()} · {user?.username}
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => {
                    setDropdownOpen(false);
                    setChangePasswordOpen(true);
                  }}
                  className="w-full flex items-center space-x-3 px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50 transition"
                >
                  <KeyRound className="w-4 h-4 text-slate-400" />
                  <span>Đổi mật khẩu</span>
                </button>

                <div className="my-1 border-t border-slate-100"></div>

                <button
                  type="button"
                  onClick={handleLogout}
                  className="w-full flex items-center space-x-3 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition"
                >
                  <LogOut className="w-4 h-4 text-red-500" />
                  <span>Đăng xuất</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Modal Đổi Mật Khẩu */}
      <ChangePasswordModal
        isOpen={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
      />
    </>
  );
};

export default Header;
