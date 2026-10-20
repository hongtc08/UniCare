import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Activity, LogOut, User, Calendar, FileText } from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-blue-600 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/dashboard" className="flex items-center space-x-2 font-bold text-xl">
          <Activity className="w-6 h-6" />
          <span>UniCare</span>
        </Link>
        <div className="flex items-center space-x-6">
          <Link to="/dashboard" className="flex items-center space-x-1 hover:text-blue-200">
            <Calendar className="w-4 h-4" />
            <span>Dashboard</span>
          </Link>
          <Link to="/medical-list" className="flex items-center space-x-1 hover:text-blue-200">
            <FileText className="w-4 h-4" />
            <span>Khám bệnh</span>
          </Link>
          <Link to="/profile" className="flex items-center space-x-1 hover:text-blue-200">
            <User className="w-4 h-4" />
            <span>{user?.name || 'Hồ sơ'}</span>
          </Link>
          <button
            onClick={handleLogout}
            className="flex items-center space-x-1 bg-red-500 hover:bg-red-600 px-3 py-1.5 rounded text-sm transition"
          >
            <LogOut className="w-4 h-4" />
            <span>Đăng xuất</span>
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
