import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

const MainLayout = () => {
  return (
    <div className="flex min-h-screen bg-slate-50 text-slate-800">
      {/* 1. Sidebar cố định bên trái (w-64) */}
      <Sidebar />

      {/* 2. Khu vực nội dung chính bên phải */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Topbar header màu trắng */}
        <Header />

        {/* Nội dung trang con (render qua Outlet) */}
        <main className="flex-1 p-6 md:p-8 max-w-7xl w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;

