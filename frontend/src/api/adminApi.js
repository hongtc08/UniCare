import axiosClient from "./axiosClient";

function unwrap(response) {
  if (response?.success === false) throw new Error(response.message || "Thao tác không thành công.");
  return response?.data ?? response;
}

// Accounts follow BE3 AdminUserController; catalog endpoints await BE2.
function endpoint(resource) {
  return resource === "accounts" ? "/admin/users" : `/catalogs/${resource}`;
}

export const adminApi = {
  async list(resource) {
    const result = unwrap(await axiosClient.get(endpoint(resource)));
    if (!Array.isArray(result))
      throw new Error(
        "API cần trả về danh sách đầy đủ. Kiểm tra hợp đồng API trong ADMIN_UI.md.",
      );
    return result;
  },
  async save(resource, record) {
    const { id, ...fields } = record;
    // BE3 accepts only fullName/role on update; status uses its own endpoint.
    const payload = resource === "accounts"
      ? {
          ...(!id ? { username: fields.username.trim() } : {}),
          fullName: fields.fullName.trim(),
          role: fields.role,
        }
      : fields;
    return unwrap(
      await (id
        ? axiosClient.put(
            `${endpoint(resource)}/${encodeURIComponent(id)}`,
            payload,
          )
        : axiosClient.post(endpoint(resource), payload)),
    );
  },
  async setStatus(account, status) {
    return unwrap(
      await axiosClient.patch(
        `${endpoint("accounts")}/${encodeURIComponent(account.id)}/status`,
        { status },
      ),
    );
  },
  async resetPassword(id) {
    return unwrap(
      await axiosClient.post(
        `${endpoint("accounts")}/${encodeURIComponent(id)}/reset-password`,
      ),
    );
  },
  async remove(resource, id) {
    return axiosClient.delete(
      `${endpoint(resource)}/${encodeURIComponent(id)}`,
    );
  },
};

export function errorMessage(error) {
  if (error.response?.data?.message) return error.response.data.message;
  if (error.response?.status === 404)
    return "API chưa khả dụng. Vui lòng liên hệ nhóm backend hoặc thử lại sau.";
  if (!error.response && error.code === "ERR_NETWORK")
    return "Không thể kết nối máy chủ. Vui lòng kiểm tra backend và thử lại.";
  return (
    error.response?.data?.message ||
    error.message ||
    "Không thể thực hiện. Vui lòng thử lại."
  );
}
