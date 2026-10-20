import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, ChevronDown, KeyRound, LogOut, Menu, User } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { ROLE_LABELS } from "../../config/roles";
import ChangePasswordModal from "../ChangePasswordModal";

export default function Header({ onOpenSidebar }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [panel, setPanel] = useState(null);
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);
  const region = useRef(null);
  const trigger = useRef(null);
  const name = user?.fullName || user?.name || user?.username || "Người dùng";
  const initials = name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((part) => part[0])
    .join("");

  useEffect(() => {
    function outside(event) {
      if (!region.current?.contains(event.target)) setPanel(null);
    }
    function escape(event) {
      if (event.key === "Escape") {
        setPanel(null);
        trigger.current?.focus();
      }
    }
    document.addEventListener("pointerdown", outside);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("pointerdown", outside);
      document.removeEventListener("keydown", escape);
    };
  }, []);

  function toggle(event, next) {
    trigger.current = event.currentTarget;
    setPanel((current) => (current === next ? null : next));
  }

  return (
    <>
      <header className="app-header">
        <div className="header-start">
          <button
            className="icon-button mobile-menu"
            aria-label="Mở menu điều hướng"
            onClick={onOpenSidebar}
          >
            <Menu size={21} />
          </button>
          <span className="header-caption">
            Hệ thống Quản lý Y tế Sinh viên
          </span>
          <span className="mobile-brand">UniCare</span>
        </div>
        <div className="header-actions" ref={region}>
          <button
            className="icon-button"
            aria-label="Thông báo"
            aria-expanded={panel === "notifications"}
            onClick={(event) => toggle(event, "notifications")}
          >
            <Bell size={20} />
          </button>
          <span className="header-divider" />
          <button
            className="profile-trigger"
            aria-label="Menu cá nhân"
            aria-expanded={panel === "profile"}
            onClick={(event) => toggle(event, "profile")}
          >
            <span className="avatar">{initials}</span>
            <span className="profile-copy">
              <strong>{name}</strong>
              <small>
                {ROLE_LABELS[user?.role]} · {user?.username}
              </small>
            </span>
            <ChevronDown size={15} />
          </button>
          {panel === "notifications" && (
            <section
              className="header-popover notification-popover"
              aria-label="Thông báo"
            >
              <h2>Thông báo</h2>
              <Bell size={25} />
              <p>Chưa có thông báo để hiển thị.</p>
              <small>Thông báo sẽ xuất hiện khi dịch vụ được kết nối.</small>
            </section>
          )}
          {panel === "profile" && (
            <div className="header-popover profile-popover">
              <div className="popover-identity">
                <strong>{name}</strong>
                <small>{ROLE_LABELS[user?.role]}</small>
              </div>
              <button
                onClick={() => {
                  setPanel(null);
                  navigate("/profile");
                }}
              >
                <User size={17} />
                Trang cá nhân
              </button>
              <button
                onClick={() => {
                  setPanel(null);
                  setChangePasswordOpen(true);
                }}
              >
                <KeyRound size={17} />
                Đổi mật khẩu
              </button>
              <button
                className="text-danger"
                onClick={() => {
                  logout();
                  navigate("/login");
                }}
              >
                <LogOut size={17} />
                Đăng xuất
              </button>
            </div>
          )}
        </div>
      </header>
      <ChangePasswordModal
        isOpen={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
      />
    </>
  );
}
