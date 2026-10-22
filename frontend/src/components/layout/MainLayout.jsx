import { useEffect, useRef, useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "./Sidebar";
import Header from "./Header";

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const mobileDialog = useRef(null);
  const location = useLocation();

  useEffect(() => {
    if (!mobileOpen) return;
    const dialog = mobileDialog.current;
    const previous = document.activeElement;
    const overflow = document.body.style.overflow;
    dialog.showModal();
    document.body.style.overflow = "hidden";
    return () => {
      dialog.close();
      document.body.style.overflow = overflow;
      previous?.focus();
    };
  }, [mobileOpen]);

  return (
    <div className="app-layout">
      <a className="skip-link" href="#main-content">
        Đến nội dung chính
      </a>
      <div className="desktop-sidebar">
        <Sidebar
          collapsed={collapsed}
          onToggle={() => setCollapsed(!collapsed)}
        />
      </div>
      <dialog
        ref={mobileDialog}
        className="mobile-sidebar-dialog"
        aria-label="Menu điều hướng"
        onCancel={() => setMobileOpen(false)}
        onClick={(event) => {
          if (event.target === event.currentTarget) setMobileOpen(false);
        }}
      >
        <Sidebar
          mobile
          onToggle={() => setMobileOpen(false)}
          onNavigate={() => setMobileOpen(false)}
        />
      </dialog>
      <div className="app-body">
        <Header onOpenSidebar={() => setMobileOpen(true)} />
        <main
          id="main-content"
          className="app-content"
          key={location.pathname}
          tabIndex={-1}
        >
          <Outlet />
        </main>
        <footer className="app-footer">
          <span>UniCare · Y tế học đường</span>
          <span>Hệ thống quản lý sức khỏe sinh viên</span>
        </footer>
      </div>
    </div>
  );
}
