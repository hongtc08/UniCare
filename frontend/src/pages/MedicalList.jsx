const MedicalList = () => {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Danh sách phiếu khám & Lịch hẹn</h1>
      </div>
      <div className="bg-white rounded-xl shadow overflow-hidden">
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
              <td className="px-6 py-4 font-medium text-gray-900">PK-001</td>
              <td className="px-6 py-4">KHAM_DA_KHOA</td>
              <td className="px-6 py-4">2026-10-25 08:30</td>
              <td className="px-6 py-4"><span className="px-2 py-1 bg-green-100 text-green-700 rounded text-xs">DA_XAC_NHAN</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default MedicalList;
