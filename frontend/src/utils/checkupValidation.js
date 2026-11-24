/**
 * Validation rules cho Phiếu khám bệnh (Đa khoa, Tư vấn tâm lý, Khám định kỳ) & Hồ sơ Chuyển viện (FE2).
 * Đồng bộ với MedicalCheckupServiceImpl của Backend.
 */

export const VALID_PERIODIC_RESULTS = ["PASSED", "FAILED"];
export const VALID_TRANSFER_REASONS = ["QUA_TAI", "THIEU_THIET_BI", "CAP_CUU_NANG"];

export function isPsychologicalCheckup(checkupType) {
  if (!checkupType) return false;
  const upper = String(checkupType).trim().toUpperCase();
  return upper === "PSYCHOLOGICAL" || upper === "TU_VAN_TAM_LY" || upper === "TAM_LY";
}

export function normalizeCheckupType(checkupType) {
  return isPsychologicalCheckup(checkupType) ? "PSYCHOLOGICAL" : "GENERAL";
}

export function validateCheckupForm(formData = {}, isCompleting = false) {
  const errors = {};

  if (isCompleting) {
    const isPsychological = isPsychologicalCheckup(formData.checkupType);

    if (isPsychological) {
      if (!formData.diagnosisNotes || !formData.diagnosisNotes.trim()) {
        errors.diagnosisNotes = "Vui lòng nhập tóm tắt buổi tư vấn trước khi hoàn tất";
      }
    } else {
      if (!formData.symptoms || !formData.symptoms.trim()) {
        errors.symptoms = "Vui lòng nhập triệu chứng lâm sàng trước khi hoàn tất ca khám";
      }
      if (!formData.diagnosisNotes || !formData.diagnosisNotes.trim()) {
        errors.diagnosisNotes = "Vui lòng nhập kết luận chẩn đoán trước khi hoàn tất ca khám";
      }

      if (formData.isPeriodic) {
        const result = formData.periodicResult ? String(formData.periodicResult).trim().toUpperCase() : "";
        if (!VALID_PERIODIC_RESULTS.includes(result)) {
          errors.periodicResult = "Vui lòng chọn kết quả khám sức khỏe định kỳ (Đạt / Không đạt) trước khi hoàn tất";
        }
      }
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

export function validateTransferForm(transferData = {}) {
  const errors = {};

  const targetHospital = transferData.targetHospital?.trim() || "";
  if (!targetHospital) {
    errors.targetHospital = "Vui lòng nhập tên bệnh viện tuyến trên tiếp nhận";
  } else if (targetHospital.length > 150) {
    errors.targetHospital = "Tên bệnh viện tuyến trên không được vượt quá 150 ký tự";
  }

  const reasonCategory = transferData.reasonCategory ? String(transferData.reasonCategory).trim().toUpperCase() : "";
  if (!VALID_TRANSFER_REASONS.includes(reasonCategory)) {
    errors.reasonCategory = "Vui lòng chọn phân loại lý do chuyển viện";
  }

  if (!transferData.transferReason || !transferData.transferReason.trim()) {
    errors.transferReason = "Vui lòng nhập lý do chuyển viện chi tiết";
  }

  if (!transferData.currentCondition || !transferData.currentCondition.trim()) {
    errors.currentCondition = "Vui lòng nhập tình trạng hiện tại của sinh viên";
  }

  if (!transferData.firstAidNotes || !transferData.firstAidNotes.trim()) {
    errors.firstAidNotes = "Vui lòng nhập biện pháp sơ cứu đã thực hiện";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}
