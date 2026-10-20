import axiosClient from "./axiosClient";
import { demoData } from "../data/demoData";

export const isDemoMode = import.meta.env.VITE_ADMIN_DEMO === "true";
const STORAGE_KEY = "unicare.admin-demo.v1";

function readDemo() {
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored ? JSON.parse(stored) : structuredClone(demoData);
}

function writeDemo(data) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

function unwrap(response) {
  return response?.data ?? response;
}

// Proposed BE2/BE3 contract. Keep endpoint mapping here until their APIs are available.
function endpoint(resource) {
  return resource === "accounts" ? "/admin/users" : `/catalogs/${resource}`;
}

export const adminApi = {
  async list(resource) {
    if (isDemoMode) return readDemo()[resource];
    const result = unwrap(await axiosClient.get(endpoint(resource)));
    if (!Array.isArray(result))
      throw new Error(
        "API cần trả về danh sách đầy đủ. Kiểm tra hợp đồng API trong ADMIN_UI.md.",
      );
    return result;
  },

  async save(resource, record) {
    if (!isDemoMode) {
      const { id, ...payload } = record;
      return unwrap(
        await (id
          ? axiosClient.put(
              `${endpoint(resource)}/${encodeURIComponent(id)}`,
              payload,
            )
          : axiosClient.post(endpoint(resource), payload)),
      );
    }

    const data = readDemo();
    const key = resource === "accounts" ? "username" : "code";
    const normalize = (value) => value.trim().toLocaleLowerCase("vi");
    if (
      data[resource].some(
        (item) =>
          item.id !== record.id &&
          normalize(item[key]) === normalize(record[key]),
      )
    ) {
      throw new Error(
        `${resource === "accounts" ? "Tên đăng nhập" : "Mã danh mục"} đã tồn tại.`,
      );
    }
    if (
      resource === "faculties" &&
      data.faculties.some(
        (item) =>
          item.id !== record.id &&
          normalize(item.name) === normalize(record.name),
      )
    ) {
      throw new Error("Tên khoa đã tồn tại.");
    }
    if (
      resource === "classes" &&
      !data.faculties.some(
        (item) => String(item.id) === String(record.facultyId),
      )
    ) {
      throw new Error("Khoa đã bị xóa. Vui lòng tải lại và chọn khoa khác.");
    }
    const saved = { ...record, id: record.id || crypto.randomUUID() };
    data[resource] = record.id
      ? data[resource].map((item) => (item.id === record.id ? saved : item))
      : [saved, ...data[resource]];
    writeDemo(data);
    return saved;
  },

  async setStatus(account, status) {
    if (isDemoMode) return this.save("accounts", { ...account, status });
    return unwrap(
      await axiosClient.patch(
        `${endpoint("accounts")}/${encodeURIComponent(account.id)}/status`,
        { status },
      ),
    );
  },

  async resetPassword(id) {
    if (isDemoMode)
      return {
        message:
          "Đã mô phỏng cấp lại mật khẩu. Mật khẩu đăng nhập thực tế không thay đổi.",
      };
    return unwrap(
      await axiosClient.post(
        `${endpoint("accounts")}/${encodeURIComponent(id)}/reset-password`,
      ),
    );
  },

  async remove(resource, id) {
    if (!isDemoMode)
      return axiosClient.delete(
        `${endpoint(resource)}/${encodeURIComponent(id)}`,
      );
    const data = readDemo();
    if (
      resource === "faculties" &&
      data.classes.some((item) => String(item.facultyId) === String(id))
    ) {
      throw new Error(
        "Không thể xóa khoa đang có lớp trực thuộc. Hãy cập nhật các lớp trước.",
      );
    }
    data[resource] = data[resource].filter((item) => item.id !== id);
    writeDemo(data);
  },
};

export function errorMessage(error) {
  return (
    error.response?.data?.message ||
    error.message ||
    "Không thể thực hiện. Vui lòng thử lại."
  );
}
