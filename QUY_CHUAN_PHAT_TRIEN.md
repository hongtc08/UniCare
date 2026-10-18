# HƯỚNG DẪN PHÁT TRIỂN & QUY CHUẨN THỐNG NHẤT DỰ ÁN UNICARE (V2.1 / UC V1.2)

> **Dành cho toàn bộ thành viên nhóm:** BE1, BE2, BE3, FE1, FE2  
> **Mục tiêu:** Thống nhất cách chạy dự án với **PostgreSQL Online** (không cần cài DB offline hay Docker), quy ước đặt tên code bằng Tiếng Anh, phân ranh giới công việc rõ ràng để không bị conflict khi merge Git, và bám sát 4 nguyên tắc nghiệp vụ cốt lõi.

---

## 📌 1. Bốn Nguyên Tắc Nghiệp Vụ Cốt Lõi (Bắt buộc tuân thủ)

1. **Chỉ có 2 loại hình dịch vụ chính đặt lịch:**
   - `KHAM_DA_KHOA` (Khám bệnh đa khoa)
   - `TU_VAN_TAM_LY` (Tư vấn tâm lý)  
   *(Sơ cứu là luồng tiếp nhận cấp cứu trực tiếp; Khám định kỳ là mục tiêu khám có kết luận Đạt/Không đạt, không phải loại dịch vụ thứ 3).*
2. **Đặt lịch là Xác nhận Ngay (Auto-Confirm):**
   - Sinh viên đặt vào ca còn trống là thành công ngay (`DA_XAC_NHAN`), bỏ hoàn toàn trạng thái *Chờ duyệt*.
   - Ca khám đủ số lượng tối đa tự động khóa. Chặn đặt trùng khung giờ trong cùng ngày.
3. **Timer quét Vắng mặt (No-Show) tự động:**
   - Bộ định thời gian (Timer) nhắc hẹn trước 2 giờ và tự động chuyển lịch hẹn `DA_XAC_NHAN` chưa có phiếu khám sang `VANG_MAT` (No-Show) khi ca khám kết thúc.
4. **Phiếu khám Hoàn tất là Bất biến (Read-only):**
   - Khi bác sĩ bấm "Hoàn tất ca khám", phiếu khám, đơn thuốc và hồ sơ chuyển viện bị khóa cứng vĩnh viễn (Read-only).
5. **Quy tắc bảo mật & quyền riêng tư (NFR-01):**
   - Ban quản lý/Phòng CTSV bị chặn cứng quyền xem bệnh án chi tiết cá nhân; Dashboard 6 báo cáo thống kê và file xuất Excel/PDF ẩn danh 100%.

---

## ☁️ 2. Hướng Dẫn Chạy Dự Án (Dùng PostgreSQL Online — Không cần cài DB Offline hay Docker)

Cả nhóm sử dụng chung một cơ sở dữ liệu **PostgreSQL Online / Cloud** (Neon, Supabase, Aiven, Railway...). Các thành viên **không cần cài đặt PostgreSQL hay Docker trên máy cá nhân**.

### Bước 1: Tạo file `.env`
Sao chép file `.env.example` thành `.env` tại thư mục gốc dự án (hoặc nhận trực tiếp file `.env` từ Leader trong nhóm chat) với thông tin kết nối PostgreSQL Online:
```env
SERVER_PORT=8080

# Thông tin kết nối PostgreSQL Online dùng chung của nhóm
DB_HOST=your-postgres-cloud-host.com
DB_PORT=5432
DB_NAME=unicare_db
DB_USER=your_db_username
DB_PASS=your_db_password
DB_SSLMODE=require

JWT_SECRET=unicareSecretKeyForStudentHealthManagementSystem2026UniCareGroup2VerySecureKeyMustBe256Bits
JWT_EXPIRATION=86400000
```
*(Lưu ý: File [`application.yml`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/resources/application.yml) đã được cấu hình `spring.config.import` để tự động nạp file `.env` này mà không cần cấu hình biến môi trường thủ công trong IDE).*

### Bước 2: Chạy Backend
- **Cách 1 (Từ IDE):** Mở thư mục `backend` bằng **IntelliJ IDEA / VS Code** và bấm Run [`UnicareApplication.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/UnicareApplication.java).
- **Cách 2 (Từ Terminal tại thư mục `backend`):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

---

## 🏗️ 3. Quy Chuẩn Đặt Tên & Cấu Trúc Phân Tầng (Clean Architecture)

### Quy ước đặt tên (Naming Convention)
- **Tên Class, Interface, Method, Biến, Bảng:** Thống nhất đặt tên bằng **Tiếng Anh** (ví dụ: `User`, `UserRepository`, `Student`, `Appointment`, `MedicalRecord`, `Shift`...), còn các hằng số **Enum giá trị nghiệp vụ** giữ đúng mã quy định trong bảng phân công (ví dụ: `Role.SINH_VIEN`, `Role.NHAN_VIEN_Y_TE`, `Role.BAN_QUAN_LY`, `UserStatus.ACTIVE`, `UserStatus.LOCKED`).

### Cấu trúc Package `com.unicare`
```text
com.unicare
├── common                   # Dùng chung toàn hệ thống
│   ├── response             # ApiResponse<T> (Do BE3 phụ trách ở SP1-03)
│   └── util                 # Các hàm tiện ích dùng chung
│
├── presentation             # Tầng Giao tiếp HTTP
│   ├── controller           # Các REST Controllers
│   └── advice               # GlobalExceptionHandler (Do BE3 phụ trách ở SP1-03)
│
├── application              # Tầng Nghiệp vụ
│   ├── service              # Interfaces & impl/
│   └── dto                  # request/ và response/
│
├── domain                   # Tầng Thực thể & CSDL
│   ├── entity               # JPA Entities (User, Student, Doctor, HealthProfile...)
│   ├── enums                # Các Enum trạng thái & phân quyền
│   └── repository           # Spring Data JPA Repositories (UserRepository...)
│
└── infrastructure           # Tầng Hạ tầng & Bảo mật
    ├── config               # Cấu hình hệ thống & Seed Data (BE2 phụ trách ở SP1-02)
    ├── security             # Cấu hình Spring Security + JWT (BE1 phụ trách ở SP1-01)
    └── notification         # Tích hợp thông báo & Email
```

---

## ✅ 4. Công Việc BE1 Đã Hoàn Thành Ở Sprint 1 (SP1-01)

**BE1** chỉ thực hiện đúng phạm vi của task **SP1-01** để không giẫm chân hay gây conflict với BE2 và BE3:

1. **Khởi tạo Spring Boot, Security + JWT Filter (`infrastructure/security`):**
   - [`SecurityConfig.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/infrastructure/security/SecurityConfig.java): Cấu hình `SecurityFilterChain` stateless, `BCryptPasswordEncoder`, CORS.
   - [`JwtTokenProvider.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/infrastructure/security/JwtTokenProvider.java) & [`JwtAuthenticationFilter.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/infrastructure/security/JwtAuthenticationFilter.java): Tạo và kiểm tra Bearer JWT Token.
   - [`CustomUserDetails.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/infrastructure/security/CustomUserDetails.java) & [`CustomUserDetailsService.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/infrastructure/security/CustomUserDetailsService.java): Tải thông tin tài khoản từ [`UserRepository.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/domain/repository/UserRepository.java).
2. **Entity & Enum nền tảng xác thực (`domain`):**
   - [`User.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/domain/entity/User.java), [`UserRepository.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/domain/repository/UserRepository.java), [`Role.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/domain/enums/Role.java), [`UserStatus.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/domain/enums/UserStatus.java).
3. **2 API Xác thực cốt lõi (`presentation/controller` & `application`):**
   - `POST /api/auth/login`: Kiểm tra `username` (MSSV/Mã NV) & `password` băm BCrypt, trả về JWT kèm Role (`SINH_VIEN`, `NHAN_VIEN_Y_TE`, `BAN_QUAN_LY`). Không tự khóa tài khoản khi nhập sai nhiều lần.
   - `PUT /api/auth/change-password`: Kiểm tra mật khẩu hiện tại, khớp 2 lần mật khẩu mới và cập nhật mật khẩu băm mới.

---

## 📋 5. Ranh Giới Công Việc Sprint 1 Cho Các Thành Viên Còn Lại

| Thành viên | Mã Task | Công việc cụ thể (Không lấn sân sang nhau) |
| :--- | :--- | :--- |
| **BE2** | **SP1-02** | 1. Mở rộng [`User.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/domain/entity/User.java) (nếu cần) và viết tiếp các JPA Entities còn lại trong `domain/entity` (Student, MedicalStaff, HealthProfile, HealthInsurance, MentalHealthTest, MoodDiary, Shift, Appointment, MedicalRecord, HospitalTransfer, Notification, AuditLog).<br>2. Viết CRUD API cho các danh mục dùng chung (Khoa, Lớp, Khóa, Nhóm bệnh dịch tễ, Thuốc cơ bản).<br>3. Viết Seed Data tạo dữ liệu mẫu cho 3 Role để cả nhóm test. |
| **BE3** | **SP1-03** | 1. Xây dựng cấu trúc chuẩn `ApiResponse<T>` trong `common/response` và `GlobalExceptionHandler` trong `presentation/advice` (sau khi tạo xong có thể bọc chuẩn cho toàn bộ Controller).<br>2. Cấu hình `@EnableMethodSecurity` phân quyền theo 3 Role.<br>3. Viết API cho Ban quản lý: Thêm/Sửa/Tìm kiếm tài khoản, Khóa/Mở khóa thủ công (`ACTIVE`/`LOCKED`), Cấp lại mật khẩu mặc định. |
| **FE1** | **SP1-04** | 1. Khởi tạo thư mục `frontend` (React + Vite + TailwindCSS/Ant Design), cấu hình Axios Interceptor tự đính kèm Bearer Token.<br>2. Thiết lập `ProtectedRoute` điều hướng theo 3 Role.<br>3. Làm giao diện Đăng nhập và Đổi mật khẩu kết nối với `POST /api/auth/login` và `PUT /api/auth/change-password`. |
| **FE2** | **SP1-05** | 1. Dựng bộ khung App Layout Responsive (Sidebar thu gọn trên Mobile 360px, Header có chuông thông báo) cho 3 Role.<br>2. Xây dựng màn hình Quản lý Tài khoản (kết nối API của BE3) và CRUD Danh mục dùng chung (kết nối API của BE2). |

---

## 🌿 6. Quy Tắc Nhánh Git & Commit

- **Nhánh làm việc:** `feature/<mã-task>-<tên-ngắn>` (Ví dụ: `feature/sp1-01-auth`, `feature/sp1-02-database`)
- **Commit message:** `[<Mã Task>] [<Vai trò>] <Nội dung>` (Ví dụ: `[SP1-01] [BE1] Khoi tao Security JWT va API Login, ChangePassword`)
- Chỉ chạm vào đúng các package/file thuộc phạm vi task của mình để hạn chế tối đa xung đột (merge conflict) khi tạo Pull Request.
