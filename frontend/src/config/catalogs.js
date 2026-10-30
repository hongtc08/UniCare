export const catalogConfig = {
  faculties: {
    label: "Khoa",
    singular: "khoa",
    description: "Quản lý các khoa đào tạo trong trường.",
    fields: [
      { name: "code", label: "Mã khoa", maxLength: 10 },
      { name: "name", label: "Tên khoa", maxLength: 100 },
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
      { name: "cohort", label: "Khóa tuyển sinh", maxLength: 10 },
    ],
  },
  "disease-groups": {
    label: "Nhóm bệnh",
    singular: "nhóm bệnh",
    description: "Chuẩn hóa nhóm bệnh phục vụ thống kê dịch tễ học đường.",
    fields: [
      { name: "code", label: "Mã nhóm bệnh", maxLength: 10 },
      { name: "name", label: "Tên nhóm bệnh", maxLength: 60 },
    ],
  },
};
