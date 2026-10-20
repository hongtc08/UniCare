import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  HeartPulse,
  LayoutDashboard,
  Heart,
  Activity,
  Calendar,
  BookOpen,
  Clock,
  FileText,
  AlertTriangle,
  BarChart2,
  Headphones,
  Menu,
} from 'lucide-react';

const Sidebar = () => {
  const { user } = useAuth();
  const role = user?.role || 'SINH_VIEN';

  // Định nghĩa menu điều hướng theo Role
  const navItems = {
    SINH_VIEN: [
      { to: '/dashboard', label: 'Bảng điều khiển', icon: LayoutDashboard },
      { to: '/profile', label: 'Hồ sơ sức khỏe', icon: Heart },
      { to: '/mental-health', label: 'Sức khỏe tinh thần', icon: Activity },
      { to: '/lich-kham', label: 'Lịch khám', icon: Calendar },
      { to: '/health-knowledge', label: 'Kiến thức sức khỏe', icon: BookOpen },
    ],
    NHAN_VIEN_Y_TE: [
      { to: '/lich-kham', label: 'Lịch hôm nay', icon: Clock },
      { to: '/medical-list', label: 'Hồ sơ bệnh án', icon: FileText },
      { to: '/emergency', label: 'Cấp cứu & chuyển viện', icon: AlertTriangle },
    ],
    BAN_QUAN_LY: [
      { to: '/dashboard', label: 'Bảng điều hành', icon: BarChart2 },
      { to: '/medical-list', label: 'Thống kê khám bệnh', icon: FileText },
    ],
  };

  const currentNav = navItems[role] || navItems.SINH_VIEN;

  return (
    <aside className="w-64 bg-[#0f1d36] text-slate-300 flex flex-col h-screen sticky top-0 border-r border-slate-800/80 select-none z-30">
      {/* 1. Header Sidebar */}
      <div className="h-16 px-5 flex items-center justify-between border-b border-slate-800/80">
        <div className="flex items-center space-x-3">
          <div className="bg-blue-600 p-2 rounded-xl text-white shadow-md shadow-blue-500/20">
            <HeartPulse className="w-5 h-5" />
          </div>
          <div>
            <div className="text-white font-bold text-lg leading-tight tracking-tight">UniCare</div>
            <div className="text-[10px] text-slate-400 font-semibold tracking-wider uppercase">
              Y TẾ HỌC ĐƯỜNG
            </div>
          </div>
        </div>
        <button
          type="button"
          className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800/60 transition"
          title="Thu gọn menu"
        >
          <Menu className="w-5 h-5" />
        </button>
      </div>

      {/* 2. Menu Navigation theo Role */}
      <nav className="flex-1 px-3 py-5 space-y-1.5 overflow-y-auto">
        <div className="px-3 pb-2 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
          {role === 'SINH_VIEN'
            ? 'SINH VIÊN'
            : role === 'NHAN_VIEN_Y_TE'
            ? 'NHÂN VIÊN Y TẾ'
            : 'QUẢN LÝ'}
        </div>

        {currentNav.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30 font-semibold'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`
              }
            >
              <Icon className="w-4 h-4 flex-shrink-0" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* 3. Footer Sidebar: Hộp hỗ trợ khẩn cấp */}
      <div className="p-3 border-t border-slate-800/80">
        <div className="bg-slate-800/60 border border-slate-700/40 p-3 rounded-xl flex items-center space-x-3">
          <div className="p-2 bg-blue-600/20 text-blue-400 rounded-lg flex-shrink-0">
            <Headphones className="w-4 h-4" />
          </div>
          <div className="min-w-0">
            <p className="text-[11px] font-semibold text-slate-200">Cần hỗ trợ y tế?</p>
            <p className="text-[10px] text-slate-400">Đường dây khẩn cấp:</p>
            <p className="text-[11px] font-bold text-blue-400 truncate">+1 (555) 010-CARE</p>
          </div>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;

