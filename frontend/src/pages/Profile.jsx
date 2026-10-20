import React from 'react';
import { useAuth } from '../context/AuthContext';
import { User } from 'lucide-react';

const Profile = () => {
  const { user } = useAuth();

  return (
    <div className="max-w-2xl bg-white p-6 rounded-xl shadow">
      <div className="flex items-center space-x-4 mb-6">
        <div className="p-4 bg-blue-100 text-blue-600 rounded-full">
          <User className="w-10 h-10" />
        </div>
        <div>
          <h2 className="text-xl font-bold text-gray-800">{user?.name || 'Chưa cập nhật'}</h2>
          <p className="text-sm text-gray-500">Vai trò: {user?.role || 'Sinh viên'}</p>
        </div>
      </div>
      <div className="border-t pt-4 space-y-3">
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">Email:</span>
          <span className="font-medium text-gray-800">sinhvien@unicare.edu.vn</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">Trạng thái tài khoản:</span>
          <span className="text-green-600 font-medium">Hoạt động</span>
        </div>
      </div>
    </div>
  );
};

export default Profile;
