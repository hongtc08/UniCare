import { NavLink } from "react-router-dom";
import {
  Activity,
  BookOpen,
  Calendar,
  Clock,
  FolderTree,
  Heart,
  HeartPulse,
  LayoutDashboard,
  Menu,
  ShieldCheck,
  Users,
  X,
} from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { ROLE_LABELS } from "../../config/roles";

const navigation = {
  SINH_VIEN: [
    { to: "/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
    { to: "/profile", label: "Trang cá nhân", icon: Heart },
    { to: "/mental-health", label: "Sức khỏe tinh thần", icon: Activity },
    { to: "/lich-kham", label: "Lịch khám", icon: Calendar },
    { to: "/health-knowledge", label: "Kiến thức sức khỏe", icon: BookOpen },
  ],
  NHAN_VIEN_Y_TE: [
    { to: "/medical-list", label: "Danh sách khám", icon: Clock },
    { to: "/profile", label: "Trang cá nhân", icon: Heart },
  ],
  BAN_QUAN_LY: [
    { to: "/dashboard", label: "Bảng điều hành", icon: LayoutDashboard },
    { to: "/admin/accounts", label: "Quản lý tài khoản", icon: Users },
    { to: "/admin/catalogs", label: "Danh mục dùng chung", icon: FolderTree },
  ],
};

export default function Sidebar({
  collapsed,
  mobile = false,
  onToggle,
  onNavigate,
}) {
  const { user } = useAuth();
  return (
    <aside
      className={`app-sidebar ${collapsed ? "is-collapsed" : ""} ${mobile ? "is-mobile" : ""}`}
    >
      <div className="sidebar-brand">
        <span className="brand-icon">
          <HeartPulse size={23} />
        </span>
        <div className="sidebar-label">
          <strong>UniCare</strong>
          <small>Y TẾ HỌC ĐƯỜNG</small>
        </div>
        <button
          className="icon-button sidebar-toggle"
          aria-label={
            mobile
              ? "Đóng menu điều hướng"
              : collapsed
                ? "Mở rộng menu"
                : "Thu gọn menu"
          }
          onClick={onToggle}
        >
          {mobile ? <X size={19} /> : <Menu size={19} />}
        </button>
      </div>
      <nav className="sidebar-nav" aria-label="Điều hướng chính">
        <p className="nav-label sidebar-label">{ROLE_LABELS[user?.role]}</p>
        {(navigation[user?.role] || []).map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            title={collapsed ? label : undefined}
            onClick={onNavigate}
            className={({ isActive }) =>
              `sidebar-link ${isActive ? "selected" : ""}`
            }
          >
            <Icon size={19} />
            <span className="sidebar-label">{label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-footer">
        <ShieldCheck size={21} />
        <div className="sidebar-label">
          <strong>Y tế học đường</strong>
          <p>Chăm sóc sức khỏe sinh viên</p>
        </div>
      </div>
    </aside>
  );
}
