import axiosClient from './axiosClient';

export const authApi = {
  /**
   * Gọi API Đăng nhập
   * POST /api/auth/login
   * @param {{ username: string, password: string }} credentials
   * @returns {Promise<{ token: string, tokenType: string, username: string, fullName: string, role: 'SINH_VIEN' | 'NHAN_VIEN_Y_TE' | 'BAN_QUAN_LY' }>}
   */
  login: (credentials) => axiosClient.post('/auth/login', credentials),

  /**
   * Đổi mật khẩu cá nhân
   * PUT /api/auth/change-password
   */
  changePassword: (data) => axiosClient.put('/auth/change-password', data),
};

export default authApi;
