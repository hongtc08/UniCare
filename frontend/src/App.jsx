import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { useAuth } from "./hooks/useAuth";
import ProtectedRoute from "./components/ProtectedRoute";
import MainLayout from "./components/layout/MainLayout";
import { ROLE_HOME } from "./config/roles";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import MedicalList from "./pages/MedicalList";
import Unauthorized from "./pages/Unauthorized";
import Accounts from "./pages/admin/Accounts";
import Catalogs from "./pages/admin/Catalogs";

function HomeRedirect() {
  const { user } = useAuth();
  return <Navigate to={ROLE_HOME[user?.role] || "/login"} replace />;
}

function UpcomingFeature() {
  return (
    <section className="data-panel upcoming-feature">
      <h1>Chức năng đang được phát triển</h1>
      <p>Phân hệ này sẽ được bổ sung trong sprint tiếp theo.</p>
    </section>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/unauthorized" element={<Unauthorized />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/profile" element={<Profile />} />
              <Route
                element={
                  <ProtectedRoute allowedRoles={["SINH_VIEN", "BAN_QUAN_LY"]} />
                }
              >
                <Route path="/dashboard" element={<Dashboard />} />
              </Route>
              <Route
                element={<ProtectedRoute allowedRoles={["NHAN_VIEN_Y_TE"]} />}
              >
                <Route path="/medical-list" element={<MedicalList />} />
                <Route path="/emergency" element={<UpcomingFeature />} />
              </Route>
              <Route
                element={
                  <ProtectedRoute
                    allowedRoles={["SINH_VIEN", "NHAN_VIEN_Y_TE"]}
                  />
                }
              >
                <Route path="/lich-kham" element={<UpcomingFeature />} />
              </Route>
              <Route element={<ProtectedRoute allowedRoles={["SINH_VIEN"]} />}>
                <Route path="/mental-health" element={<UpcomingFeature />} />
                <Route path="/health-knowledge" element={<UpcomingFeature />} />
              </Route>
              <Route
                element={<ProtectedRoute allowedRoles={["BAN_QUAN_LY"]} />}
              >
                <Route path="/admin/accounts" element={<Accounts />} />
                <Route
                  path="/admin/catalogs"
                  element={<Navigate to="/admin/catalogs/faculties" replace />}
                />
                <Route
                  path="/admin/catalogs/:resource"
                  element={<Catalogs />}
                />
              </Route>
            </Route>
          </Route>
          <Route path="*" element={<HomeRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
