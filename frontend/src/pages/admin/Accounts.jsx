import { normalizeSearch } from "../../utils/search";
import { useState } from "react";
import {
  KeyRound,
  LockKeyhole,
  LockKeyholeOpen,
  Pencil,
  Plus,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useResource } from "../../hooks/useResource";
import { adminApi } from "../../api/adminApi";
import { ROLE_LABELS, roleOptions } from "../../config/roles";
import {
  ConfirmAction,
  DataTable,
  Feedback,
  PageHeading,
  RecordForm,
  SearchField,
} from "../../components/admin/AdminUI";

export default function Accounts() {
  const { user } = useAuth();
  const { records, loading, error, reload } = useResource("accounts");
  const [query, setQuery] = useState("");
  const [role, setRole] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(1);
  const [editing, setEditing] = useState(null);
  const [action, setAction] = useState(null);
  const [message, setMessage] = useState("");

  const filtered = records.filter(
    (record) =>
      normalizeSearch(`${record.fullName} ${record.username}`).includes(
        normalizeSearch(query),
      ) &&
      (!role || record.role === role) &&
      (!status || record.status === status),
  );

  function changeFilter(setter, value) {
    setter(value);
    setPage(1);
  }

  async function save(values) {
    await adminApi.save("accounts", values);
    setMessage(editing.id ? "Đã cập nhật tài khoản." : "Đã thêm tài khoản.");
    reload();
  }

  async function confirmAction() {
    if (action.type === "reset") {
      const result = await adminApi.resetPassword(action.record.id);
      setMessage(
        result?.message ||
          "Đã cấp lại mật khẩu mặc định theo chính sách hệ thống.",
      );
    } else {
      const nextStatus =
        action.record.status === "ACTIVE" ? "LOCKED" : "ACTIVE";
      await adminApi.setStatus(action.record, nextStatus);
      setMessage(
        nextStatus === "LOCKED"
          ? "Đã khóa tài khoản."
          : "Đã mở khóa tài khoản.",
      );
      reload();
    }
  }

  const columns = [
    {
      key: "fullName",
      label: "Người dùng",
      render: (record) => (
        <div className="person-cell">
          <span className={`avatar avatar-${record.role.toLowerCase()}`}>
            {record.fullName
              .split(" ")
              .filter(Boolean)
              .slice(-2)
              .map((part) => part[0])
              .join("")}
          </span>
          <div>
            <strong>{record.fullName}</strong>
            <span>
              {record.username === user?.username
                ? "Tài khoản của bạn"
                : `Mã: ${record.username}`}
            </span>
          </div>
        </div>
      ),
    },
    {
      key: "username",
      label: "Tên đăng nhập",
      render: (record) => (
        <span className="record-code">{record.username}</span>
      ),
    },
    {
      key: "role",
      label: "Vai trò",
      render: (record) => (
        <span className={`role-tag role-${record.role.toLowerCase()}`}>
          {ROLE_LABELS[record.role]}
        </span>
      ),
    },
    {
      key: "status",
      label: "Trạng thái",
      render: (record) => (
        <span
          className={`status-tag ${record.status === "ACTIVE" ? "active" : "locked"}`}
        >
          <i />
          {record.status === "ACTIVE" ? "Hoạt động" : "Đã khóa"}
        </span>
      ),
    },
    {
      key: "actions",
      label: "Thao tác",
      render: (record) => {
        const self = record.username === user?.username;
        return (
          <div className="row-actions">
            <button
              className="icon-button"
              aria-label={`Sửa ${record.fullName}`}
              title="Sửa tài khoản"
              onClick={() => setEditing(record)}
            >
              <Pencil size={17} />
            </button>
            <button
              className="icon-button"
              aria-label={`Reset mật khẩu ${record.fullName}`}
              title={self ? "Đổi mật khẩu qua menu cá nhân" : "Reset mật khẩu"}
              disabled={self}
              onClick={() => setAction({ type: "reset", record })}
            >
              <KeyRound size={17} />
            </button>
            <button
              className={`icon-button ${record.status === "ACTIVE" ? "text-danger" : "text-success"}`}
              aria-label={`${record.status === "ACTIVE" ? "Khóa" : "Mở khóa"} ${record.fullName}`}
              title={
                self
                  ? "Không thể tự khóa tài khoản"
                  : record.status === "ACTIVE"
                    ? "Khóa tài khoản"
                    : "Mở khóa tài khoản"
              }
              disabled={self}
              onClick={() => setAction({ type: "status", record })}
            >
              {record.status === "ACTIVE" ? (
                <LockKeyhole size={17} />
              ) : (
                <LockKeyholeOpen size={17} />
              )}
            </button>
          </div>
        );
      },
    },
  ];

  const stats = [
    {
      label: "Tổng tài khoản",
      value: records.length,
      icon: Users,
      color: "blue",
    },
    {
      label: "Đang hoạt động",
      value: records.filter((record) => record.status === "ACTIVE").length,
      icon: ShieldCheck,
      color: "green",
    },
    {
      label: "Đã khóa",
      value: records.filter((record) => record.status === "LOCKED").length,
      icon: LockKeyhole,
      color: "red",
    },
  ];

  return (
    <div className="admin-page">
      <PageHeading
        title="Quản lý tài khoản"
        description="Quản lý người dùng và quyền truy cập hệ thống UniCare."
        action={
          <button
            className="button primary"
            onClick={() =>
              setEditing({
                username: "",
                fullName: "",
                role: "SINH_VIEN",
                status: "ACTIVE",
              })
            }
          >
            <Plus size={18} />
            Thêm tài khoản
          </button>
        }
      />
      <div className="stats-grid">
        {stats.map(({ label, value, icon: Icon, color }) => (
          <div className="stat-panel" key={label}>
            <span className={`stat-icon ${color}`}>
              <Icon size={22} />
            </span>
            <div>
              <p>{label}</p>
              <strong>
                {loading || error ? "—" : value.toLocaleString("vi-VN")}
              </strong>
            </div>
          </div>
        ))}
      </div>
      <Feedback message={message} />
      <section className="data-panel" aria-label="Danh sách tài khoản">
        <div className="panel-heading">
          <div>
            <h2>
              Danh sách tài khoản{" "}
              <span className="count-badge">{filtered.length}</span>
            </h2>
            <p>Tra cứu, cập nhật và quản lý trạng thái người dùng.</p>
          </div>
          <span className="panel-caption">3 vai trò hệ thống</span>
        </div>
        <div className="table-toolbar">
          <SearchField
            value={query}
            onChange={(value) => changeFilter(setQuery, value)}
            placeholder="Tìm theo họ tên, tên đăng nhập…"
          />
          <select
            aria-label="Lọc theo vai trò"
            value={role}
            onChange={(event) => changeFilter(setRole, event.target.value)}
          >
            <option value="">Tất cả vai trò</option>
            {roleOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <select
            aria-label="Lọc theo trạng thái"
            value={status}
            onChange={(event) => changeFilter(setStatus, event.target.value)}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Hoạt động</option>
            <option value="LOCKED">Đã khóa</option>
          </select>
        </div>
        <DataTable
          columns={columns}
          records={filtered}
          loading={loading}
          error={error}
          onRetry={reload}
          page={page}
          onPageChange={setPage}
        />
      </section>
      <p className="privacy-note">
        <ShieldCheck size={16} />
        Ban quản lý chỉ quản trị tài khoản, không truy cập bệnh án cá nhân.
      </p>
      {editing && (
        <RecordForm
          title={editing.id ? "Chỉnh sửa tài khoản" : "Thêm tài khoản"}
          initialValues={editing}
          fields={[
            { name: "fullName", label: "Họ và tên", maxLength: 100 },
            {
              name: "username",
              label: "Tên đăng nhập / MSSV / Mã nhân viên",
              maxLength: 50,
              disabled: Boolean(editing.id),
            },
            {
              name: "role",
              label: "Vai trò",
              options: roleOptions,
              disabled: editing.username === user?.username,
            },
          ]}
          note={
            editing.id
              ? "Tên đăng nhập không thay đổi. Khóa/mở khóa qua thao tác riêng trong bảng."
              : "Mật khẩu mặc định do máy chủ cấp theo chính sách của hệ thống."
          }
          onSave={save}
          onClose={() => setEditing(null)}
          submitLabel={editing.id ? "Lưu thay đổi" : "Thêm tài khoản"}
        />
      )}
      {action && (
        <ConfirmAction
          title={
            action.type === "reset"
              ? "Reset mật khẩu?"
              : action.record.status === "ACTIVE"
                ? "Khóa tài khoản?"
                : "Mở khóa tài khoản?"
          }
          description={
            action.type === "reset"
              ? `Cấp lại mật khẩu mặc định cho ${action.record.fullName} (${action.record.username}). Mật khẩu hiện tại sẽ không còn hiệu lực.`
              : `${action.record.fullName} (${action.record.username}) sẽ ${action.record.status === "ACTIVE" ? "không thể" : "có thể"} đăng nhập hệ thống.`
          }
          confirmLabel={
            action.type === "reset"
              ? "Reset mật khẩu"
              : action.record.status === "ACTIVE"
                ? "Khóa tài khoản"
                : "Mở khóa tài khoản"
          }
          danger={action.type === "status" && action.record.status === "ACTIVE"}
          onConfirm={confirmAction}
          onClose={() => setAction(null)}
        />
      )}
    </div>
  );
}
