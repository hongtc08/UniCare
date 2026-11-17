import { Plus, Trash2, Pill, AlertCircle, FileX } from "lucide-react";
import { createEmptyMedicine } from "../../utils/prescription";

/**
 * Bảng Kê đơn thuốc điện tử động (FE2 / BE2).
 * Cho phép bác sĩ thêm, sửa, xóa các dòng thuốc linh hoạt và khóa Read-only khi ca khám hoàn tất.
 *
 * @param {Object} props
 * @param {Array} props.medicines - Danh sách thuốc [{ _localId, id, medicineName, quantity, dosageInstruction }]
 * @param {Function} props.onChange - (updatedMedicines) => void
 * @param {boolean} [props.isReadOnly=false] - Trạng thái khóa cứng chỉ đọc
 * @param {Object} [props.rowErrors={}] - Map lỗi từng dòng { [_localId]: { medicineName, quantity, dosageInstruction } }
 */
export default function PrescriptionTable({
  medicines = [],
  onChange,
  isReadOnly = false,
  rowErrors = {},
}) {
  // Thêm một dòng thuốc mới
  const handleAddRow = () => {
    if (isReadOnly || !onChange) return;
    const newRow = createEmptyMedicine();
    onChange([...medicines, newRow]);
  };

  // Cập nhật trường của một dòng thuốc cụ thể
  const handleRowChange = (localId, field, value) => {
    if (isReadOnly || !onChange) return;
    const nextMedicines = medicines.map((row) => {
      if (row._localId !== localId) return row;

      let val = value;
      if (field === "quantity") {
        // Đảm bảo số lượng là số nguyên dương nếu có giá trị
        if (value === "") {
          val = "";
        } else {
          const num = parseInt(value, 10);
          val = isNaN(num) ? "" : Math.max(1, num);
        }
      }

      return {
        ...row,
        [field]: val,
      };
    });

    onChange(nextMedicines);
  };

  // Xóa một dòng thuốc
  const handleRemoveRow = (localId) => {
    if (isReadOnly || !onChange) return;
    const nextMedicines = medicines.filter((row) => row._localId !== localId);
    onChange(nextMedicines);
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs overflow-hidden">
      {/* HEADER BẢNG ĐƠN THUỐC */}
      <div className="px-5 py-4 border-b border-slate-100 bg-slate-50/50 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
            <Pill className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-800">
              Kê đơn thuốc điện tử
            </h2>
            <p className="text-[11px] text-slate-500">
              Chỉ định thuốc, hàm lượng, số lượng và hướng dẫn sử dụng chi tiết
            </p>
          </div>
        </div>

        {/* NÚT THÊM DÒNG THUỐC (ẨN HOÀN TOÀN KHI READ-ONLY) */}
        {!isReadOnly && (
          <button
            type="button"
            onClick={handleAddRow}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 active:bg-emerald-200 border border-emerald-200 rounded-lg transition-colors shadow-2xs"
          >
            <Plus className="w-4 h-4" />
            Thêm thuốc
          </button>
        )}
      </div>

      {/* NỘI DUNG BẢNG */}
      <div className="p-5">
        {/* Trường hợp trống ở chế độ Read-only */}
        {isReadOnly && medicines.length === 0 ? (
          <div className="py-10 text-center text-slate-400 text-xs flex flex-col items-center justify-center gap-2">
            <FileX className="w-8 h-8 text-slate-300" />
            <span className="font-medium text-slate-500">
              Không có đơn thuốc đi kèm trong ca khám này
            </span>
            <span className="text-[11px]">
              (Bác sĩ không kê thuốc hoặc chỉ tư vấn / xử trí sơ cứu tại chỗ)
            </span>
          </div>
        ) : medicines.length === 0 ? (
          /* Trường hợp chưa thêm thuốc ở chế độ chỉnh sửa */
          <div className="py-8 text-center border-2 border-dashed border-slate-200 rounded-xl">
            <Pill className="w-8 h-8 text-slate-300 mx-auto mb-2" />
            <p className="text-xs font-semibold text-slate-600">
              Chưa có thuốc nào trong đơn
            </p>
            <p className="text-[11px] text-slate-400 mt-0.5 mb-3">
              Nếu ca khám cần kê thuốc, hãy nhấn "Thêm thuốc" bên dưới
            </p>
            <button
              type="button"
              onClick={handleAddRow}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-lg transition-colors"
            >
              <Plus className="w-4 h-4" />
              Thêm dòng thuốc đầu tiên
            </button>
          </div>
        ) : (
          /* BẢNG DANH SÁCH THUỐC */
          <div className="overflow-x-auto rounded-lg border border-slate-200">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                  <th className="py-2.5 px-3 text-center w-12">STT</th>
                  <th className="py-2.5 px-3 min-w-[200px]">
                    Tên thuốc & Hàm lượng {!isReadOnly && <span className="text-red-500">*</span>}
                  </th>
                  <th className="py-2.5 px-3 w-28 text-center">
                    Số lượng {!isReadOnly && <span className="text-red-500">*</span>}
                  </th>
                  <th className="py-2.5 px-3 min-w-[260px]">
                    Hướng dẫn liều dùng {!isReadOnly && <span className="text-red-500">*</span>}
                  </th>
                  {!isReadOnly && (
                    <th className="py-2.5 px-3 text-center w-16">Thao tác</th>
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {medicines.map((row, index) => {
                  const err = rowErrors[row._localId] || {};
                  return (
                    <tr
                      key={row._localId}
                      className={`hover:bg-slate-50/50 transition-colors ${
                        Object.keys(err).length > 0 ? "bg-red-50/30" : ""
                      }`}
                    >
                      {/* STT */}
                      <td className="py-2.5 px-3 text-center font-bold text-slate-400 align-top pt-3.5">
                        {index + 1}
                      </td>

                      {/* TÊN THUỐC */}
                      <td className="py-2.5 px-3 align-top">
                        <input
                          type="text"
                          maxLength={100}
                          value={row.medicineName || ""}
                          onChange={(e) =>
                            handleRowChange(row._localId, "medicineName", e.target.value)
                          }
                          disabled={isReadOnly}
                          placeholder="VD: Paracetamol 500mg, Amoxicillin 500mg..."
                          className={`w-full px-2.5 py-1.5 text-xs rounded-md border transition-colors focus:outline-hidden focus:ring-1 focus:ring-emerald-500 ${
                            isReadOnly
                              ? "bg-slate-100 text-slate-800 border-slate-200 cursor-not-allowed font-medium"
                              : err.medicineName
                              ? "border-red-400 bg-red-50/40 text-red-900 focus:border-red-500"
                              : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-emerald-500"
                          }`}
                        />
                        {err.medicineName && (
                          <p className="mt-1 text-[11px] text-red-600 font-medium flex items-center gap-1">
                            <AlertCircle className="w-3 h-3 shrink-0" />
                            {err.medicineName}
                          </p>
                        )}
                      </td>

                      {/* SỐ LƯỢNG */}
                      <td className="py-2.5 px-3 align-top">
                        <input
                          type="number"
                          min={1}
                          max={999}
                          value={row.quantity ?? ""}
                          onChange={(e) =>
                            handleRowChange(row._localId, "quantity", e.target.value)
                          }
                          disabled={isReadOnly}
                          placeholder="1"
                          className={`w-full px-2.5 py-1.5 text-xs rounded-md border text-center transition-colors focus:outline-hidden focus:ring-1 focus:ring-emerald-500 ${
                            isReadOnly
                              ? "bg-slate-100 text-slate-800 border-slate-200 cursor-not-allowed font-bold"
                              : err.quantity
                              ? "border-red-400 bg-red-50/40 text-red-900 focus:border-red-500"
                              : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-emerald-500 font-semibold"
                          }`}
                        />
                        {err.quantity && (
                          <p className="mt-1 text-[11px] text-red-600 font-medium text-center">
                            {err.quantity}
                          </p>
                        )}
                      </td>

                      {/* HƯỚNG DẪN LIỀU DÙNG */}
                      <td className="py-2.5 px-3 align-top">
                        <input
                          type="text"
                          maxLength={200}
                          value={row.dosageInstruction || ""}
                          onChange={(e) =>
                            handleRowChange(row._localId, "dosageInstruction", e.target.value)
                          }
                          disabled={isReadOnly}
                          placeholder="VD: Ngày uống 2 lần, mỗi lần 1 viên sau khi ăn no..."
                          className={`w-full px-2.5 py-1.5 text-xs rounded-md border transition-colors focus:outline-hidden focus:ring-1 focus:ring-emerald-500 ${
                            isReadOnly
                              ? "bg-slate-100 text-slate-800 border-slate-200 cursor-not-allowed"
                              : err.dosageInstruction
                              ? "border-red-400 bg-red-50/40 text-red-900 focus:border-red-500"
                              : "border-slate-300 bg-white text-slate-800 hover:border-slate-400 focus:border-emerald-500"
                          }`}
                        />
                        {err.dosageInstruction && (
                          <p className="mt-1 text-[11px] text-red-600 font-medium flex items-center gap-1">
                            <AlertCircle className="w-3 h-3 shrink-0" />
                            {err.dosageInstruction}
                          </p>
                        )}
                      </td>

                      {/* NÚT XÓA DÒNG (ẨN HOÀN TOÀN KHI READ-ONLY) */}
                      {!isReadOnly && (
                        <td className="py-2.5 px-3 text-center align-top pt-3">
                          <button
                            type="button"
                            onClick={() => handleRemoveRow(row._localId)}
                            className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
                            title="Xóa dòng thuốc này"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* FOOTER BẢNG ĐƠN THUỐC (NÚT THÊM DÒNG DƯỚI BẢNG) */}
        {!isReadOnly && medicines.length > 0 && (
          <div className="mt-3 flex items-center justify-between">
            <span className="text-[11px] text-slate-400">
              * Tổng cộng: {medicines.length} loại thuốc được kê trong đơn.
            </span>
            <button
              type="button"
              onClick={handleAddRow}
              className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-semibold text-emerald-700 hover:text-emerald-800 hover:bg-emerald-50 rounded-md transition-colors"
            >
              <Plus className="w-3.5 h-3.5" />
              Thêm thuốc khác
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
