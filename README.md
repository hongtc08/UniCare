# UniCare - Hệ thống Quản lý Sức khỏe Sinh viên

> Đồ án môn học: Phân tích & Thiết kế Hướng đối tượng (OOAD) / Phát triển Ứng dụng Web  
> **Nhóm thực hiện:** Nhóm 2  
> **Khách hàng đối ứng:** Nhóm 13  

---

## 👥 Thành viên nhóm 2
- **24520579** - Tăng Chấn Hồng (*Leader / BA*)
- **24520437** - Nguyễn Hoàn Hải (*Backend*)
- **24521643** - Trần Thị Hồng Thanh (*Frontend*)
- **24521761** - Hà Thảo Tiên (*Database / Backend*)
- **24521984** - Trần Phương Ánh Vân (*QA / UI-UX*)

---

## 🏛️ Cấu trúc thư mục dự án & Kiến trúc Backend

Hệ thống áp dụng kiến trúc phân tầng định hướng **Clean Architecture** tinh gọn (Lightweight Clean Architecture tương tự *VTeaWebApp*):

```text
UniCare
├── docker-compose.yml                       # Điều phối môi trường Docker (Backend + Frontend)
├── .env.example                             # Biến môi trường mẫu (Chứa config MySQL Online)
├── .env                                     # Biến môi trường cục bộ (không commit lên Git)
├── README.md                                # Tài liệu dự án
│
└── backend                                  # Mã nguồn Spring Boot Backend
    ├── Dockerfile.dev                       # Image container chạy chế độ dev (Java 25)
    ├── .dockerignore                        # Danh sách file bỏ qua khi build Docker
    ├── pom.xml                              # Khai báo Maven dependencies (Web, JPA, Security, MySQL, JJWT)
    ├── mvnw / mvnw.cmd                      # Maven Wrapper độc lập không cần cài Maven ở máy host
    │
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── unicare
        │   │           ├── UnicareApplication.java  # Main Application Entrypoint
        │   │           ├── presentation             # Tầng Presentation (API / Web Controller)
        │   │           │   ├── controller           # Các REST Controllers tiếp nhận request HTTP
        │   │           │   └── advice               # Xử lý ngoại lệ tập trung (GlobalExceptionHandler)
        │   │           │
        │   │           ├── application              # Tầng Application (Nghiệp vụ & Điều phối luồng)
        │   │           │   ├── service              # Interface và Implementation xử lý nghiệp vụ
        │   │           │   └── dto                  # Data Transfer Objects (Request / Response)
        │   │           │
        │   │           ├── domain                   # Tầng Domain (Mô hình thực thể cốt lõi)
        │   │           │   ├── entity               # JPA Entities đại diện cho các bảng CSDL
        │   │           │   ├── repository           # Spring Data JPA Repository interfaces
        │   │           │   └── enums                # Các Enum định nghĩa trạng thái & quyền hạn
        │   │           │
        │   │           ├── infrastructure           # Tầng Infrastructure (Kỹ thuật nền tảng & 3rd party)
        │   │           │   ├── config               # Cấu hình Security, CORS, WebSocket, Swagger...
        │   │           │   ├── security             # JWT, CustomUserDetails, Auth Filters
        │   │           │   └── notification         # Tích hợp Firebase (FCM), Email service
        │   │           │
        │   │           └── common                   # Tiện ích và đối tượng dùng chung
        │   │               ├── response             # Chuẩn hóa định dạng JSON trả về (ApiResponse)
        │   │               └── util                 # Các hàm tiện ích (tính BMI, băm mật khẩu, format date...)
        │   │
        │   └── resources
        │       └── application.yml                  # Cấu hình ứng dụng và kết nối CSDL MySQL Online
        │
        └── test                                     # Kiểm thử tự động (Unit Test, Integration Test)
            └── java
                └── com
                    └── unicare
```

---

## ☁️ Cấu hình Database & Triển khai với Docker

> **Lưu ý:** Dự án sử dụng **MySQL Online / Cloud** (tương tự đồ án VTeaWebApp). Cả nhóm sẽ dùng chung một cơ sở dữ liệu trên cloud thông qua thông tin cấu hình trong file `.env`.

### 1. Cấu hình file `.env`
Sao chép `.env.example` thành `.env` và điền thông tin kết nối MySQL Online:
```env
DB_HOST=your-mysql-cloud-host.com
DB_PORT=3306
DB_NAME=unicare_db
DB_USER=your_db_username
DB_PASS=your_db_password
```

### 2. Hai cách chạy Backend linh hoạt

* **Cách 1: Chạy trực tiếp từ IDE (Khuyên dùng khi Dev hàng ngày):**  
  Mở project trên IntelliJ IDEA / VS Code và chạy `UnicareApplication.java`. Spring Boot sẽ tự nạp cấu hình từ `.env` và kết nối trực tiếp đến MySQL Online. Dễ dàng debug và hot reload.

* **Cách 2: Chạy qua Docker (Khi bàn giao cho QA / Test tích hợp):**  
  ```bash
  # Khởi động Backend container
  docker compose up -d --build

  # Xem logs thời gian thực
  docker compose logs -f backend

  # Dừng container
  docker compose down
  ```

---

## 📋 Mô tả phân tầng chức năng

| Tầng / Thư mục | Trách nhiệm chính |
| :--- | :--- |
| **`presentation/`** | Nhận request từ Client, kiểm tra tính hợp lệ cơ bản và chuyển tiếp xuống tầng Application; trả dữ liệu về theo chuẩn `ApiResponse`. |
| **`application/`** | Chứa logic nghiệp vụ của các Use Case (Đặt lịch, làm test DASS-21, lập phiếu khám, xuất báo cáo ẩn danh). Quản lý DTO Request/Response. |
| **`domain/`** | Trung tâm dữ liệu nghiệp vụ: Chứa các Entity (User, Appointment, HealthRecord, HospitalTransfer...) và các Repository giao tiếp DB. |
| **`infrastructure/`** | Cung cấp hạ tầng kỹ thuật: Cơ chế bảo mật JWT, phân quyền truy cập, đẩy thông báo thời gian thực (WebSocket/FCM). |
| **`common/`** | Các class tiện ích dùng chung xuyên suốt dự án để tránh lặp code. |