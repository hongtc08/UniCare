export const MOCK_DOCTORS = [
  { id: "doc-1", name: "BS.CKII Nguyễn Văn An", specialty: "KHAM_DA_KHOA" },
  { id: "doc-2", name: "ThS.BS Trần Thị Mai", specialty: "TU_VAN_TAM_LY" },
  { id: "doc-3", name: "BS. Lê Hoàng Long", specialty: "KHAM_DA_KHOA" },
  { id: "doc-4", name: "TS.BS Phạm Quỳnh Anh", specialty: "TU_VAN_TAM_LY" },
];

// Chỉ gồm đúng 2 phòng cố định
export const CLINIC_ROOMS = [
  "P.101 - Phòng y tế",
  "P.102 - Phòng tư vấn tâm lý",
];

// 2 Ca trực chuẩn cố định
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

// Mock data mẫu: Thể hiện rõ việc 1 ô có thể có đồng thời CẢ 2 ca (Đa khoa & Tâm lý)
export const INITIAL_MOCK_SHIFTS = [
  // Thứ 2: Sáng có CẢ 2 ca (Đa khoa tại P.101 và Tâm lý tại P.102)
  {
    id: "shift-1",
    date: "2026-10-26",
    shiftSlot: "MORNING",
    startTime: "08:00",
    endTime: "11:00",
    doctorId: "doc-1",
    doctorName: "BS.CKII Nguyễn Văn An",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    bookedCount: 4,
    maxCapacity: 10,
  },
  {
    id: "shift-2",
    date: "2026-10-26",
    shiftSlot: "MORNING",
    startTime: "08:00",
    endTime: "11:00",
    doctorId: "doc-2",
    doctorName: "ThS.BS Trần Thị Mai",
    serviceType: "TU_VAN_TAM_LY",
    room: "P.102 - Phòng tư vấn tâm lý",
    bookedCount: 8,
    maxCapacity: 8, // Full chỗ
  },
  // Thứ 2 chiều
  {
    id: "shift-3",
    date: "2026-10-26",
    shiftSlot: "AFTERNOON",
    startTime: "13:00",
    endTime: "17:00",
    doctorId: "doc-4",
    doctorName: "TS.BS Phạm Quỳnh Anh",
    serviceType: "TU_VAN_TAM_LY",
    room: "P.102 - Phòng tư vấn tâm lý",
    bookedCount: 3,
    maxCapacity: 6,
  },

  // Thứ 3: Sáng & Chiều
  {
    id: "shift-4",
    date: "2026-10-27",
    shiftSlot: "MORNING",
    startTime: "08:00",
    endTime: "11:00",
    doctorId: "doc-3",
    doctorName: "BS. Lê Hoàng Long",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    bookedCount: 10,
    maxCapacity: 10,
  },
  {
    id: "shift-5",
    date: "2026-10-27",
    shiftSlot: "AFTERNOON",
    startTime: "13:00",
    endTime: "17:00",
    doctorId: "doc-1",
    doctorName: "BS.CKII Nguyễn Văn An",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    bookedCount: 3,
    maxCapacity: 12,
  },

  // Thứ 4: Chiều có CẢ 2 ca
  {
    id: "shift-6",
    date: "2026-10-28",
    shiftSlot: "AFTERNOON",
    startTime: "13:00",
    endTime: "17:00",
    doctorId: "doc-3",
    doctorName: "BS. Lê Hoàng Long",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    bookedCount: 5,
    maxCapacity: 8,
  },
  {
    id: "shift-7",
    date: "2026-10-28",
    shiftSlot: "AFTERNOON",
    startTime: "13:00",
    endTime: "17:00",
    doctorId: "doc-4",
    doctorName: "TS.BS Phạm Quỳnh Anh",
    serviceType: "TU_VAN_TAM_LY",
    room: "P.102 - Phòng tư vấn tâm lý",
    bookedCount: 5,
    maxCapacity: 6,
  },

  // Thứ 5
  {
    id: "shift-8",
    date: "2026-10-29",
    shiftSlot: "MORNING",
    startTime: "08:00",
    endTime: "11:00",
    doctorId: "doc-1",
    doctorName: "BS.CKII Nguyễn Văn An",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    bookedCount: 12,
    maxCapacity: 12,
  },
  {
    id: "shift-9",
    date: "2026-10-29",
    shiftSlot: "AFTERNOON",
    startTime: "13:00",
    endTime: "17:00",
    doctorId: "doc-2",
    doctorName: "ThS.BS Trần Thị Mai",
    serviceType: "TU_VAN_TAM_LY",
    room: "P.102 - Phòng tư vấn tâm lý",
    bookedCount: 2,
    maxCapacity: 8,
  },

  // Thứ 6
  {
    id: "shift-10",
    date: "2026-10-30",
    shiftSlot: "MORNING",
    startTime: "08:00",
    endTime: "11:00",
    doctorId: "doc-3",
    doctorName: "BS. Lê Hoàng Long",
    serviceType: "KHAM_DA_KHOA",
    room: "P.101 - Phòng y tế",
    bookedCount: 6,
    maxCapacity: 10,
  },
  {
    id: "shift-11",
    date: "2026-10-30",
    shiftSlot: "AFTERNOON",
    startTime: "13:00",
    endTime: "17:00",
    doctorId: "doc-4",
    doctorName: "TS.BS Phạm Quỳnh Anh",
    serviceType: "TU_VAN_TAM_LY",
    room: "P.102 - Phòng tư vấn tâm lý",
    bookedCount: 4,
    maxCapacity: 6,
  },
];
