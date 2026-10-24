import { Activity, BookOpen, Calendar, Clock, Heart, LayoutDashboard, ClipboardList, TriangleAlert, ChartColumn, FileChartColumn, Bell, Settings, Users } from "lucide-react";

export const navigation = {
  SINH_VIEN: [
    { to: "/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
    { to: "/profile", label: "Hồ sơ sức khỏe", icon: Heart },
    { to: "/mental-health", label: "Sức khỏe tinh thần", icon: Activity },
    { to: "/lich-kham", label: "Lịch khám", icon: Calendar },
    { to: "/health-knowledge", label: "Kiến thức sức khỏe", icon: BookOpen },
  ],
  NHAN_VIEN_Y_TE: [
    { to: "/medical-list", label: "Lịch hôm nay", icon: Clock },
    { to: "/medical-records", label: "Hồ sơ bệnh án", icon: ClipboardList },
    { to: "/emergency", label: "Cấp cứu & chuyển viện", icon: TriangleAlert },
  ],
  BAN_QUAN_LY: [
    { to: "/dashboard", label: "Bảng điều hành", icon: ChartColumn },
    { to: "/admin/accounts", label: "Quản lý tài khoản", icon: Users },
    { to: "/admin/reports", label: "Thống kê & báo cáo", icon: FileChartColumn },
    { to: "/admin/notifications", label: "Trung tâm thông báo", icon: Bell },
    { to: "/admin/settings", label: "Cài đặt hệ thống", icon: Settings },
  ],
};

