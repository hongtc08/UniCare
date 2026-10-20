import React from 'react';
import { Calendar, UserCheck, Clock } from 'lucide-react';

const Dashboard = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Bảng điều khiển UniCare</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-xl shadow border border-gray-100 flex items-center space-x-4">
          <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-gray-500">Lịch khám đa khoa</p>
            <h3 className="text-xl font-bold text-gray-800">KHAM_DA_KHOA</h3>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl shadow border border-gray-100 flex items-center space-x-4">
          <div className="p-3 bg-green-100 text-green-600 rounded-lg">
            <UserCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-gray-500">Tư vấn tâm lý</p>
            <h3 className="text-xl font-bold text-gray-800">TU_VAN_TAM_LY</h3>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl shadow border border-gray-100 flex items-center space-x-4">
          <div className="p-3 bg-purple-100 text-purple-600 rounded-lg">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-gray-500">Trạng thái đặt lịch</p>
            <h3 className="text-xl font-bold text-gray-800">Tự động duyệt</h3>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
