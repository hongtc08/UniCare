/**
 * Utility functions hỗ trợ chuẩn hóa, tạo mới và validate danh sách thuốc kê đơn động (FE2 / BE2).
 * Đảm bảo đồng bộ 100% với ràng buộc của UpdatePrescriptionRequest & MedicalCheckupServiceImpl:
 * - medicineName: NotBlank, max 100 chars, không trùng lặp tên
 * - quantity: NotNull, min 1
 * - dosageInstruction: NotBlank, max 200 chars
 */

let idCounter = 0;

/**
 * Tạo một dòng thuốc trống với _localId duy nhất
 */
export function createEmptyMedicine(initial = {}) {
  idCounter += 1;
  const uniqueKey = `med_${Date.now()}_${idCounter}_${Math.random().toString(36).substring(2, 6)}`;
  return {
    _localId: initial.id ? `db_${initial.id}` : initial._localId || uniqueKey,
    id: initial.id || null,
    medicineName: initial.medicineName || "",
    quantity: initial.quantity !== undefined && initial.quantity !== null ? Number(initial.quantity) : 1,
    dosageInstruction: initial.dosageInstruction || "",
  };
}

/**
 * Kiểm tra xem một dòng thuốc có hoàn toàn trống (chưa nhập thông tin gì) không
 */
export function isRowCompletelyEmpty(row) {
  const name = (row.medicineName || "").trim();
  const instruction = (row.dosageInstruction || "").trim();
  return name === "" && instruction === "";
}

/**
 * Validate và làm sạch danh sách thuốc trước khi gửi lên API
 * @param {Array} medicines
 * @returns {{
 *   isValid: boolean,
 *   rowErrors: Object.<string, {medicineName?: string, quantity?: string, dosageInstruction?: string}>,
 *   generalError: string|null,
 *   cleanedMedicines: Array<{medicineName: string, quantity: number, dosageInstruction: string}>
 * }}
 */
export function validateAndCleanMedicines(medicines = []) {
  const rowErrors = {};
  const cleanedMedicines = [];
  const seenNames = new Set();
  let hasError = false;

  for (const row of medicines) {
    const localId = row._localId || `row_${cleanedMedicines.length}`;

    // Nếu dòng hoàn toàn trống (bấm thêm dòng nhưng không nhập gì), bỏ qua
    if (isRowCompletelyEmpty(row)) {
      continue;
    }

    const rowErr = {};
    const name = (row.medicineName || "").trim();
    const instruction = (row.dosageInstruction || "").trim();
    const qty = Number(row.quantity);

    // 1. Kiểm tra Tên thuốc
    if (!name) {
      rowErr.medicineName = "Tên thuốc không được để trống";
    } else if (name.length > 100) {
      rowErr.medicineName = "Tên thuốc không được vượt quá 100 ký tự";
    }

    // 2. Kiểm tra Số lượng
    if (row.quantity === "" || row.quantity === null || row.quantity === undefined || isNaN(qty) || qty < 1) {
      rowErr.quantity = "Số lượng phải ≥ 1";
    }

    // 3. Kiểm tra Hướng dẫn liều dùng
    if (!instruction) {
      rowErr.dosageInstruction = "Hướng dẫn liều dùng không được để trống";
    } else if (instruction.length > 200) {
      rowErr.dosageInstruction = "Hướng dẫn không được vượt quá 200 ký tự";
    }

    // 4. Kiểm tra trùng lặp tên thuốc
    if (name) {
      const lower = name.toLowerCase();
      if (seenNames.has(lower)) {
        rowErr.medicineName = `Thuốc "${name}" bị trùng lặp trong đơn`;
      } else {
        seenNames.add(lower);
      }
    }

    if (Object.keys(rowErr).length > 0) {
      rowErrors[localId] = rowErr;
      hasError = true;
    } else {
      cleanedMedicines.push({
        medicineName: name,
        quantity: Math.floor(qty),
        dosageInstruction: instruction,
      });
    }
  }

  return {
    isValid: !hasError,
    rowErrors,
    generalError: hasError ? "Vui lòng kiểm tra lại thông tin các dòng thuốc bị lỗi trong đơn" : null,
    cleanedMedicines,
  };
}
