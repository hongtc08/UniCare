import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';

const Unauthorized = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <ShieldAlert className="w-16 h-16 text-red-500 mb-4" />
      <h1 className="text-3xl font-bold text-gray-800 mb-2">403 - Không có quyền truy cập</h1>
      <p className="text-gray-600 mb-6">Bạn không có quyền xem thông tin hoặc chức năng này.</p>
      <Link to="/dashboard" className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition">
        Về Trang chủ
      </Link>
    </div>
  );
};

export default Unauthorized;
