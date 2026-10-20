import React, { useState } from 'react';
import authApi from '../api/authApi';
import { X, Lock, AlertCircle, CheckCircle, Loader2 } from 'lucide-react';

const ChangePasswordModal = ({ isOpen, onClose }) => {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Reset lỗi khi người dùng gõ lại
    if (errorMessage) setErrorMessage('');
  };

  const handleClose = () => {
    setFormData({
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    });
    setErrorMessage('');
    setSuccessMessage('');
    onClose();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    const { currentPassword, newPassword, confirmPassword } = formData;

    // 1. Validate không được để trống
    if (!currentPassword.trim() || !newPassword.trim() || !confirmPassword.trim()) {
      setErrorMessage('Vui lòng điền đầy đủ tất cả các trường.');
      return;
    }

    // 2. Validate độ dài tối thiểu của mật khẩu mới
    if (newPassword.length < 6) {
      setErrorMessage('Mật khẩu mới phải có ít nhất 6 ký tự.');
      return;
    }

    // 3. Validate mật khẩu mới và xác nhận mật khẩu phải khớp nhau
    if (newPassword !== confirmPassword) {
      setErrorMessage('Mật khẩu mới và Xác nhận mật khẩu không khớp nhau.');
      return;
    }

    // 4. Validate mật khẩu mới không được trùng mật khẩu cũ
    if (currentPassword === newPassword) {
      setErrorMessage('Mật khẩu mới không được trùng với mật khẩu hiện tại.');
      return;
    }

    setLoading(true);
    try {
      const response = await authApi.changePassword({
        currentPassword,
        newPassword,
        confirmPassword,
      });

      const msg = response?.message || 'Đổi mật khẩu thành công!';
      setSuccessMessage(msg);

      // Chờ 1.5 giây để người dùng đọc thông báo thành công rồi tự động đóng modal
      setTimeout(() => {
        handleClose();
      }, 1500);
    } catch (error) {
      const serverMsg =
        error.response?.data?.message ||
        (error.response?.status === 400
          ? 'Mật khẩu hiện tại không đúng hoặc dữ liệu không hợp lệ.'
          : 'Không thể thực hiện đổi mật khẩu. Vui lòng thử lại sau.');
      setErrorMessage(serverMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 animate-fade-in">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden transform transition-all">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
          <div className="flex items-center space-x-2">
            <div className="p-2 bg-blue-100 text-blue-600 rounded-lg">
              <Lock className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-gray-800">Đổi mật khẩu cá nhân</h3>
          </div>
          <button
            onClick={handleClose}
            disabled={loading}
            className="text-gray-400 hover:text-gray-600 rounded-lg p-1 hover:bg-gray-100 transition disabled:opacity-50"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Thông báo Thành công */}
          {successMessage && (
            <div className="p-3 rounded-lg bg-green-50 border border-green-200 flex items-center space-x-2 text-green-700 text-sm">
              <CheckCircle className="w-5 h-5 flex-shrink-0 text-green-600" />
              <span>{successMessage}</span>
            </div>
          )}

          {/* Thông báo Lỗi */}
          {errorMessage && (
            <div className="p-3 rounded-lg bg-red-50 border border-red-200 flex items-start space-x-2 text-red-700 text-sm">
              <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-600" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Mật khẩu hiện tại */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Mật khẩu hiện tại <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
              name="currentPassword"
              value={formData.currentPassword}
              onChange={handleChange}
              disabled={loading || !!successMessage}
              placeholder="Nhập mật khẩu đang dùng"
              className="w-full px-3.5 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition disabled:bg-gray-100"
            />
          </div>

          {/* Mật khẩu mới */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Mật khẩu mới <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleChange}
              disabled={loading || !!successMessage}
              placeholder="Tối thiểu 6 ký tự"
              className="w-full px-3.5 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition disabled:bg-gray-100"
            />
          </div>

          {/* Xác nhận mật khẩu mới */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Xác nhận mật khẩu mới <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              disabled={loading || !!successMessage}
              placeholder="Nhập lại mật khẩu mới"
              className="w-full px-3.5 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition disabled:bg-gray-100"
            />
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-gray-100">
            <button
              type="button"
              onClick={handleClose}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 bg-gray-100 hover:bg-gray-200 rounded-lg transition disabled:opacity-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={loading || !!successMessage}
              className="px-5 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 active:bg-blue-800 rounded-lg transition shadow-sm flex items-center space-x-2 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Đang gửi...</span>
                </>
              ) : (
                <span>Đổi mật khẩu</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordModal;

