# UniCare - Hệ thống Quản lý Sức khỏe Sinh viên

> **Đồ án môn học:** Phân tích & Thiết kế Hướng đối tượng (OOAD) / Phát triển Ứng dụng Web  
> **Nhóm thực hiện:** Nhóm 2  
> **Khách hàng đối ứng:** Nhóm 13  
> **Tài liệu đặc tả:** Mô hình Use-case v1.2 & Tài liệu nghiệp vụ v2.1

---

## 👥 Thành viên nhóm 2 & Phân vai
- **24520579** - Tăng Chấn Hồng (*Leader / BA / BE1*)
- **24520437** - Nguyễn Hoàn Hải (*Backend / BE2*)
- **24521643** - Trần Thị Hồng Thanh (*Frontend / FE1*)
- **24521761** - Hà Thảo Tiên (*Database / Backend / BE3*)
- **24521984** - Trần Phương Ánh Vân (*QA / UI-UX / FE2*)

---

## 📖 Quy Chuẩn Phát Triển & Phân Chia Ranh Giới Code
👉 Toàn bộ thành viên đọc kỹ hướng dẫn chạy dự án, quy ước đặt tên Tiếng Anh và ranh giới từng task tại:  
👉 **[`QUY_CHUAN_PHAT_TRIEN.md`](file:///c:/Users/HongLeo/Desktop/UniCare/QUY_CHUAN_PHAT_TRIEN.md)**

---

## 🏛️ Cấu trúc Thư mục Dự án

```text
UniCare
├── .env.example                             # Mẫu biến môi trường (PostgreSQL Online, JWT)
├── .env                                     # Biến môi trường cục bộ (không commit Git)
├── QUY_CHUAN_PHAT_TRIEN.md                  # Tài liệu quy chuẩn kỹ thuật cho 5 Dev
├── README.md                                # Tổng quan dự án
│
└── backend                                  # Mã nguồn Spring Boot Backend
    ├── pom.xml                              # Maven dependencies (Web, JPA, Security, PostgreSQL, JJWT)
    ├── mvnw / mvnw.cmd                      # Maven Wrapper độc lập
    │
    └── src
        ├── main
        │   ├── java/com/unicare
        │   │   ├── UnicareApplication.java  # Main Application Entrypoint
        │   │   ├── presentation             # REST Controllers & GlobalExceptionHandler
        │   │   ├── application              # DTO (Request/Response) & Services
        │   │   ├── domain                   # JPA Entities (User...), Enums & Repositories
        │   │   ├── infrastructure           # Security JWT, Config, Notification
        │   │   └── common                   # ApiResponse & Utilities
        │   └── resources
        │       └── application.yml          # Cấu hình Spring Boot & PostgreSQL Online
        └── test
            └── java/com/unicare             # Kiểm thử tự động
```

---

## ☁️ Khởi Chạy Dự Án (Dùng PostgreSQL Online — Không cần cài DB Offline hay Docker)

1. Nhận file `.env` từ Leader (hoặc sao chép từ `.env.example` và điền thông tin kết nối **PostgreSQL Online** dùng chung của nhóm).
2. Chạy ứng dụng (Spring Boot sẽ tự động nạp cấu hình từ file `.env`):
   - Mở thư mục `backend` bằng IntelliJ IDEA / VS Code và chạy [`UnicareApplication.java`](file:///c:/Users/HongLeo/Desktop/UniCare/backend/src/main/java/com/unicare/UnicareApplication.java).
   - Hoặc chạy từ Terminal:
     ```powershell
     cd backend
     .\mvnw.cmd spring-boot:run
     ```