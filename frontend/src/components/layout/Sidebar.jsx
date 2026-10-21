import { NavLink } from "react-router-dom";
import { Heart, Menu, MessageSquare, X } from "lucide-react";
import { navigation } from "../../config/navigation";
import { useAuth } from "../../hooks/useAuth";
import { ROLE_LABELS } from "../../config/roles";


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
        {collapsed && !mobile ? (
          <button className="brand-icon" onClick={onToggle} aria-label="Mở rộng menu" title="Mở rộng menu" aria-expanded={false}>
            <Heart size={23} />
          </button>
        ) : (
          <span className="brand-icon"><Heart size={23} /></span>
        )}
        <div className="sidebar-label">
          <strong>UniCare</strong>
          <small>Y TẾ HỌC ĐƯỜNG</small>
        </div>
        {(!collapsed || mobile) && <button
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
        </button>}
      </div>
      <nav className="sidebar-nav" aria-label="Điều hướng chính">
        <p className="nav-label sidebar-label">{ROLE_LABELS[user?.role]}</p>
        {(navigation[user?.role] || []).map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            aria-label={label}
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
        <MessageSquare size={21} />
        <div className="sidebar-label">
          <strong>Cần hỗ trợ?</strong>
          <p>Đường dây khẩn cấp</p>
          <p>+1 (555) 010-CARE</p>
        </div>
      </div>
    </aside>
  );
}
