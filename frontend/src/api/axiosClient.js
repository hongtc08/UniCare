import axios from 'axios';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Tự động đính kèm Token nếu có
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Trả về response.data, xử lý 401, 403 và reject lỗi
axiosClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // Xóa token và role trong localStorage, ép chuyển hướng về trang /login
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (status === 403) {
      // Chuyển hướng sang trang /unauthorized khi không có quyền truy cập
      window.location.href = '/unauthorized';
    }

    // Luôn reject để component gọi API vẫn có thể catch được lỗi
    return Promise.reject(error);
  }
);

export default axiosClient;
