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

## 🏛️ Kiến trúc Backend (Lightweight Clean Architecture)

Hệ thống Backend được xây dựng trên nền tảng **Spring Boot**, áp dụng kiến trúc phân tầng định hướng **Clean Architecture** (tương tự đồ án *VTeaWebApp* nhưng được chuẩn hóa vị trí DTO và bổ sung các module thông báo/tiện ích).

```text
backend
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── unicare
    │   │           ├── presentation         # Tầng Presentation (API / Web Controller)
    │   │           │   ├── controller       # Các REST Controllers tiếp nhận request HTTP
    │   │           │   └── advice           # Xử lý ngoại lệ tập trung (GlobalExceptionHandler)
    │   │           │
    │   │           ├── application          # Tầng Application (Nghiệp vụ & Điều phối luồng)
    │   │           │   ├── service          # Interface và Implementation xử lý nghiệp vụ
    │   │           │   └── dto              # Data Transfer Objects (Request / Response)
    │   │           │
    │   │           ├── domain               # Tầng Domain (Mô hình thực thể cốt lõi)
    │   │           │   ├── entity           # JPA Entities đại diện cho các bảng CSDL
    │   │           │   ├── repository       # Spring Data JPA Repository interfaces
    │   │           │   └── enums            # Các Enum định nghĩa trạng thái & quyền hạn
    │   │           │
    │   │           ├── infrastructure       # Tầng Infrastructure (Kỹ thuật nền tảng & 3rd party)
    │   │           │   ├── config           # Cấu hình Security, CORS, WebSocket, Swagger...
    │   │           │   ├── security         # JWT, CustomUserDetails, Auth Filters
    │   │           │   └── notification     # Tích hợp Firebase (FCM), Email service
    │   │           │
    │   │           └── common               # Tiện ích và đối tượng dùng chung
    │   │               ├── response         # Chuẩn hóa định dạng JSON trả về (ApiResponse)
    │   │               └── util             # Các hàm tiện ích (tính BMI, băm mật khẩu, format date...)
    │   │
    │   └── resources                        # Tài nguyên cấu hình (application.yml, migrations...)
    │
    └── test                                 # Kiểm thử tự động (Unit Test, Integration Test)
        └── java
            └── com
                └── unicare
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