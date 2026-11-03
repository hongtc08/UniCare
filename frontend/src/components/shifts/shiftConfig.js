/**
 * Cấu hình hệ thống lịch trực & ca khám bệnh tại trạm y tế
 */

// 2 Phòng khám cố định của trạm y tế
export const CLINIC_ROOMS = [
  "P.101 - Phòng y tế",
  "P.102 - Phòng tư vấn tâm lý",
];

// 2 Khung giờ ca trực chuẩn cố định trong ngày
export const SHIFT_SLOTS = {
  MORNING: {
    key: "MORNING",
    label: "Ca Sáng (08:00 - 11:00)",
    startTime: "08:00",
    endTime: "11:00",
  },
  AFTERNOON: {
    key: "AFTERNOON",
    label: "Ca Chiều (13:00 - 17:00)",
    startTime: "13:00",
    endTime: "17:00",
  },
};

// Cấu hình hiển thị theo loại hình dịch vụ
export const SERVICE_CONFIG = {
  KHAM_DA_KHOA: {
    label: "Khám Đa khoa",
    badge: "Đa khoa",
    color: "bg-blue-50 text-blue-700 border-blue-200/80 hover:border-blue-300",
    defaultRoom: "P.101 - Phòng y tế",
  },
  TU_VAN_TAM_LY: {
    label: "Tư vấn Tâm lý",
    badge: "Tâm lý",
    color: "bg-purple-50 text-purple-700 border-purple-200/80 hover:border-purple-300",
    defaultRoom: "P.102 - Phòng tư vấn tâm lý",
  },
};

export const SERVICE_TYPES = SERVICE_CONFIG;
