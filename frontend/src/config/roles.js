export const ROLE_LABELS = {
  SINH_VIEN: "Sinh viên",
  NHAN_VIEN_Y_TE: "Nhân viên y tế",
  BAN_QUAN_LY: "Quản lý",
};

export const ROLE_HOME = {
  SINH_VIEN: "/profile",
  NHAN_VIEN_Y_TE: "/medical-list",
  BAN_QUAN_LY: "/dashboard",
};

export const roleOptions = Object.entries(ROLE_LABELS).map(
  ([value, label]) => ({ value, label }),
);
