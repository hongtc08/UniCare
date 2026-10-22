export const catalogConfig = {
  faculties: {
    label: "Khoa",
    singular: "khoa",
    description: "Quản lý các khoa đào tạo trong trường.",
    fields: [
      { name: "code", label: "Mã khoa", maxLength: 10 },
      { name: "name", label: "Tên khoa", maxLength: 100 },
      { name: "description", label: "Mô tả", optional: true, multiline: true },
    ],
  },
  classes: {
    label: "Lớp",
    singular: "lớp",
    description: "Quản lý lớp học, khoa trực thuộc và khóa tuyển sinh.",
    fields: [
      { name: "code", label: "Mã lớp", maxLength: 10 },
      { name: "name", label: "Tên lớp", maxLength: 50 },
      { name: "facultyId", label: "Khoa trực thuộc", source: "faculties" },
      { name: "cohort", label: "Khóa tuyển sinh", maxLength: 20 },
    ],
  },
  "disease-groups": {
    label: "Nhóm bệnh",
    singular: "nhóm bệnh",
    description: "Chuẩn hóa nhóm bệnh phục vụ thống kê dịch tễ học đường.",
    fields: [
      { name: "code", label: "Mã nhóm bệnh", maxLength: 20 },
      { name: "name", label: "Tên nhóm bệnh", maxLength: 100 },
      { name: "description", label: "Mô tả", optional: true, multiline: true },
    ],
  },
  medicines: {
    label: "Thuốc",
    singular: "thuốc",
    description: "Quản lý danh mục thuốc cơ bản phục vụ kê đơn tại phòng y tế.",
    fields: [
      { name: "code", label: "Mã thuốc", maxLength: 20 },
      { name: "name", label: "Tên thuốc", maxLength: 100 },
      { name: "unit", label: "Đơn vị tính", maxLength: 30 },
      { name: "strength", label: "Hàm lượng", maxLength: 50 },
      {
        name: "description",
        label: "Ghi chú",
        optional: true,
        multiline: true,
      },
    ],
  },
};
