import { normalizeSearch } from "../../utils/search";
import { useState } from "react";
import { NavLink, Navigate, useParams } from "react-router-dom";
import {
  Building2,
  GraduationCap,
  HeartPulse,
  Pencil,
  Pill,
  Plus,
  Trash2,
} from "lucide-react";
import { catalogConfig } from "../../config/catalogs";
import { adminApi } from "../../api/adminApi";
import { useResource } from "../../hooks/useResource";
import {
  ConfirmAction,
  DataTable,
  Feedback,
  PageHeading,
  RecordForm,
  SearchField,
} from "../../components/admin/AdminUI";

const catalogIcons = {
  faculties: Building2,
  classes: GraduationCap,
  "disease-groups": HeartPulse,
  medicines: Pill,
};

function CatalogContent({ resource }) {
  const config = catalogConfig[resource];
  const { records, loading, error, reload } = useResource(resource);
  const faculties = useResource("faculties");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [message, setMessage] = useState("");

  const facultyName = (id) =>
    faculties.records.find((faculty) => String(faculty.id) === String(id))
      ?.name || "Chưa tải được khoa";
  const filtered = records.filter((record) =>
    normalizeSearch(
      `${record.code} ${record.name} ${record.cohort || ""}`,
    ).includes(normalizeSearch(query)),
  );
  const fields = config.fields.map((field) =>
    field.source
      ? {
          ...field,
          options: faculties.records.map((faculty) => ({
            value: faculty.id,
            label: faculty.name,
          })),
        }
      : { ...field, disabled: field.name === "code" && Boolean(editing?.id) },
  );

  const columns = [
    ...config.fields
      .filter((field) => !field.multiline || resource !== "medicines")
      .map((field) => ({
        key: field.name,
        label: field.label,
        render: (record) =>
          field.name === "facultyId" ? (
            facultyName(record.facultyId)
          ) : field.name === "code" ? (
            <span className="record-code">{record.code}</span>
          ) : field.name === "name" ? (
            <strong>{record.name}</strong>
          ) : (
            <span className={field.multiline ? "description-cell" : ""}>
              {record[field.name] || "—"}
            </span>
          ),
      })),
    {
      key: "actions",
      label: "Thao tác",
      render: (record) => (
        <div className="row-actions">
          <button
            className="icon-button"
            aria-label={`Sửa ${record.name}`}
            title="Chỉnh sửa"
            onClick={() => setEditing(record)}
          >
            <Pencil size={17} />
          </button>
          <button
            className="icon-button text-danger"
            aria-label={`Xóa ${record.name}`}
            title="Xóa"
            onClick={() => setDeleting(record)}
          >
            <Trash2 size={17} />
          </button>
        </div>
      ),
    },
  ];

  async function save(values) {
    await adminApi.save(resource, values);
    setMessage(`Đã ${editing.id ? "cập nhật" : "thêm"} ${config.singular}.`);
    reload();
  }

  return (
    <>
      <section className="data-panel">
        <div className="panel-heading">
          <div>
            <h2>
              Danh sách {config.singular}{" "}
              <span className="count-badge">{records.length}</span>
            </h2>
            <p>{config.description}</p>
          </div>
          <button
            className="button primary"
            onClick={() =>
              setEditing(
                Object.fromEntries(
                  config.fields.map((field) => [field.name, ""]),
                ),
              )
            }
          >
            <Plus size={18} />
            Thêm {config.singular}
          </button>
        </div>
        <div className="table-toolbar">
          <SearchField
            value={query}
            onChange={(value) => {
              setQuery(value);
              setPage(1);
            }}
            placeholder={`Tìm mã hoặc tên ${config.singular}…`}
          />
          <span className="panel-caption">{filtered.length} kết quả</span>
        </div>
        {resource === "classes" && faculties.error && (
          <div className="inline-feedback">
            <Feedback error message={faculties.error} />
            <button className="button secondary" onClick={faculties.reload}>
              Tải lại khoa
            </button>
          </div>
        )}
        <DataTable
          columns={columns}
          records={filtered}
          loading={loading}
          error={error}
          onRetry={reload}
          page={page}
          onPageChange={setPage}
          emptyText={`Thay đổi từ khóa hoặc thêm ${config.singular} đầu tiên.`}
        />
      </section>
      <Feedback message={message} />
      <p className="privacy-note">
        Danh mục được dùng chung trong hệ thống. Không thể xóa dữ liệu đang được
        tham chiếu.
      </p>
      {editing && (
        <RecordForm
          title={`${editing.id ? "Chỉnh sửa" : "Thêm"} ${config.singular}`}
          fields={fields}
          initialValues={editing}
          onSave={save}
          onClose={() => setEditing(null)}
          note={
            resource === "classes" && !faculties.records.length
              ? "Cần có khoa trước khi tạo lớp. Hãy thêm khoa hoặc kiểm tra kết nối dữ liệu."
              : "Các trường có dấu * là bắt buộc. Mã danh mục không thay đổi sau khi tạo."
          }
        />
      )}
      {deleting && (
        <ConfirmAction
          title={`Xóa ${config.singular}?`}
          description={`Bạn sắp xóa “${deleting.name}” (${deleting.code}). Thao tác này không thể hoàn tác. Dữ liệu đang được sử dụng sẽ không được phép xóa.`}
          confirmLabel={`Xóa ${config.singular}`}
          danger
          onClose={() => setDeleting(null)}
          onConfirm={async () => {
            await adminApi.remove(resource, deleting.id);
            setMessage(`Đã xóa ${config.singular}.`);
            reload();
          }}
        />
      )}
    </>
  );
}

export default function Catalogs() {
  const { resource = "faculties" } = useParams();
  if (!catalogConfig[resource])
    return <Navigate to="/admin/catalogs/faculties" replace />;

  return (
    <div className="admin-page">
      <PageHeading
        title="Danh mục dùng chung"
        description="Chuẩn hóa dữ liệu đào tạo và y tế trong toàn hệ thống."
      />
      <nav className="catalog-tabs" aria-label="Loại danh mục">
        {Object.entries(catalogConfig).map(([key, config]) => {
          const Icon = catalogIcons[key];
          return (
            <NavLink
              key={key}
              to={`/admin/catalogs/${key}`}
              className={({ isActive }) =>
                isActive ? "catalog-tab selected" : "catalog-tab"
              }
            >
              <Icon size={19} />
              {config.label}
            </NavLink>
          );
        })}
      </nav>
      <CatalogContent key={resource} resource={resource} />
    </div>
  );
}
