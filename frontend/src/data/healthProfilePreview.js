// Temporary UI fixtures for fields not exposed by the current backend.
// Never send these values to the health-profile API or persist them in AuthContext.
export const studentPreview = {
  dateOfBirth: "2005-02-14",
  gender: "Nữ",
  email: "sinhvien@example.com",
  phone: "090 123 4567",
  identityNumber: "079205000001",
  address: "Ký túc xá Đại học Quốc gia, TP. Hồ Chí Minh",
  facultyName: "Khoa học Máy tính",
  className: "CS-24A",
};

export const healthRecordPreview = {
  height: 165,
  weight: 56,
  bloodType: "O",
  allergies: "Dị ứng hải sản (tôm, cua)",
  underlyingDiseases: "Không có",
  vaccinationHistory: "Đã tiêm 3 mũi COVID-19, Viêm gan B",
  updatedAt: "2026-10-25T09:30:00.000Z",
};

export const insurancePreview = {
  socialInsuranceNumber: "0123456789",
  cardNumber: "SV4790123456789",
  validFrom: "2026-01-01",
  validUntil: "2026-12-31",
  initialProvider: "Trung tâm Y tế Đại học",
  issuedAt: "TP. Hồ Chí Minh",
  benefits: "Được hưởng 80% chi phí khám bệnh, chữa bệnh trong phạm vi được hưởng BHYT (áp dụng tỷ lệ thanh toán một số thuốc, hóa chất, vật tư y tế và dịch vụ kỹ thuật theo quy định của Bộ trưởng Bộ Y tế).\n\nTrong trường hợp điều trị nội trú trái tuyến tại CSKCB tuyến TW sẽ được hưởng 32% (TH trên thẻ có mã nơi sinh sống là K1 hoặc K2 hoặc K3 sẽ được hưởng 80%). CSKCB tuyến tỉnh sẽ được hưởng 48% (TH trên thẻ có mã nơi sinh sống là K1 hoặc K2 hoặc K3 sẽ được hưởng 80%), từ ngày 01/01/2021 sẽ hưởng 80%, CSKCB là bệnh viện tuyến huyện sẽ hưởng 80%.",
};
