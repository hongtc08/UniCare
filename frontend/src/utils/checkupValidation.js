/**
 * Validation rules cho Phiếu khám bệnh Đa khoa (FE2).
 * Đồng bộ với MedicalCheckupServiceImpl.validateRequiredFieldsForCompletion của Backend.
 */

export function validateCheckupForm(formData = {}, isCompleting = false) {
  const errors = {};

  if (isCompleting) {
    const isPsychological = formData.checkupType === "TU_VAN_TAM_LY";

    if (!isPsychological) {
      // Khám đa khoa yêu cầu cả triệu chứng và kết luận chẩn đoán
      if (!formData.symptoms || !formData.symptoms.trim()) {
        errors.symptoms = "Vui lòng nhập triệu chứng lâm sàng trước khi hoàn tất ca khám";
      }

      if (!formData.diagnosisNotes || !formData.diagnosisNotes.trim()) {
        errors.diagnosisNotes = "Vui lòng nhập kết luận chẩn đoán trước khi hoàn tất ca khám";
      }
    } else {
      // Tư vấn tâm lý yêu cầu tóm tắt buổi tư vấn
      if (!formData.diagnosisNotes || !formData.diagnosisNotes.trim()) {
        errors.diagnosisNotes = "Vui lòng nhập tóm tắt buổi tư vấn trước khi hoàn tất";
      }
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}
