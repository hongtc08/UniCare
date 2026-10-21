const MedicalList = () => {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">
          Danh sách phiếu khám & Lịch hẹn
        </h1>
      </div>
      <div
        className="bg-white rounded-xl shadow overflow-x-auto"
        role="region"
        aria-label="Danh sách khám, cuộn ngang để xem thêm"
        tabIndex={0}
      >
        <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
          <thead className="bg-gray-50 text-gray-600 font-semibold">
            <tr>
              <th className="px-6 py-3">Mã phiếu</th>
              <th className="px-6 py-3">Dịch vụ</th>
              <th className="px-6 py-3">Ngày hẹn</th>
              <th className="px-6 py-3">Trạng thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            <tr>
              <td colSpan={4} className="px-6 py-8 text-center text-slate-600">
                API danh sách khám chưa được tích hợp. Chưa có dữ liệu để hiển
                thị.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default MedicalList;
