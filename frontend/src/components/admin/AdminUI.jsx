import { useState } from "react";
import {
  AlertCircle,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Database,
  Search,
} from "lucide-react";
import Modal from "../Modal";
import { isDemoMode, errorMessage } from "../../api/adminApi";

export function PageHeading({ title, description, action }) {
  return (
    <div className="page-heading">
      <div>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </div>
  );
}

export function DataNotice() {
  if (!isDemoMode) return null;
  return (
    <div className="demo-notice">
      <Database size={16} />
      <span>Chế độ dữ liệu mẫu · Thay đổi chỉ lưu trên trình duyệt này.</span>
    </div>
  );
}

export function Feedback({ message, error = false }) {
  if (!message) return null;
  return (
    <div
      className={`feedback ${error ? "feedback-error" : "feedback-success"}`}
      role={error ? "alert" : "status"}
    >
      {error ? <AlertCircle size={18} /> : <CheckCircle2 size={18} />}
      <span>{message}</span>
    </div>
  );
}

export function SearchField({ value, onChange, placeholder }) {
  return (
    <label className="search-field">
      <Search size={18} />
      <input
        type="search"
        aria-label={placeholder}
        placeholder={placeholder}
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

export function DataTable({
  columns,
  records,
  loading,
  error,
  onRetry,
  emptyText = "Thử thay đổi từ khóa hoặc bộ lọc.",
  page,
  onPageChange,
  pageSize = 6,
}) {
  const totalPages = Math.max(1, Math.ceil(records.length / pageSize));
  const currentPage = Math.min(page, totalPages);
  const start = (currentPage - 1) * pageSize;

  if (loading)
    return (
      <div className="table-state" role="status">
        Đang tải dữ liệu…
      </div>
    );
  if (error)
    return (
      <div className="table-state">
        <Feedback error message={error} />
        <button className="button secondary" onClick={onRetry}>
          Thử lại
        </button>
      </div>
    );

  return (
    <>
      <p className="table-scroll-hint">Cuộn ngang bảng để xem đầy đủ và thao tác.</p>
      <div
        className="table-scroll"
        role="region"
        aria-label="Bảng dữ liệu, cuộn ngang để xem thêm"
        tabIndex={0}
      >
        <table className="data-table">
          <thead>
            <tr>
              {columns.map((column) => (
                <th key={column.key} scope="col">
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {records.slice(start, start + pageSize).map((record) => (
              <tr key={record.id}>
                {columns.map((column) => (
                  <td key={column.key}>
                    {column.render
                      ? column.render(record)
                      : record[column.key] || "—"}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {records.length === 0 && (
        <div className="table-state">
          <Search size={28} />
          <h3>Không tìm thấy dữ liệu</h3>
          <p>{emptyText}</p>
        </div>
      )}
      <div className="table-footer">
        <span>
          {records.length
            ? `${start + 1}–${Math.min(start + pageSize, records.length)}`
            : "0"}{" "}
          trên {records.length} bản ghi
        </span>
        <div className="pagination">
          <button
            className="icon-button"
            aria-label="Trang trước"
            disabled={currentPage === 1}
            onClick={() => onPageChange(currentPage - 1)}
          >
            <ChevronLeft size={18} />
          </button>
          <span>
            Trang {currentPage} / {totalPages}
          </span>
          <button
            className="icon-button"
            aria-label="Trang sau"
            disabled={currentPage === totalPages}
            onClick={() => onPageChange(currentPage + 1)}
          >
            <ChevronRight size={18} />
          </button>
        </div>
      </div>
    </>
  );
}

export function RecordForm({
  title,
  fields,
  initialValues,
  onSave,
  onClose,
  submitLabel = "Lưu thay đổi",
  note,
}) {
  const [values, setValues] = useState(initialValues);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const cleaned = Object.fromEntries(
      Object.entries(values).map(([key, value]) => [
        key,
        typeof value === "string" ? value.trim() : value,
      ]),
    );
    if (
      fields.some(
        (field) => !field.optional && !String(cleaned[field.name] ?? "").trim(),
      )
    ) {
      setError("Vui lòng điền đầy đủ các trường bắt buộc.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      await onSave(cleaned);
      onClose();
    } catch (cause) {
      setError(errorMessage(cause));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal isOpen title={title} onClose={onClose} busy={busy}>
      <form onSubmit={submit} className="record-form">
        {note && <p className="form-note">{note}</p>}
        <Feedback error message={error} />
        {fields.map((field) => {
          const attributes = {
            name: field.name,
            value: values[field.name] ?? "",
            required: !field.optional,
            disabled: busy || field.disabled,
            maxLength: field.maxLength || 500,
            onChange: (event) =>
              setValues((current) => ({
                ...current,
                [field.name]: event.target.value,
              })),
          };
          return (
            <label className="form-field" key={field.name}>
              <span>
                {field.label}{" "}
                {!field.optional && <span className="required">*</span>}
              </span>
              {field.options ? (
                <select {...attributes}>
                  <option value="">Chọn {field.label.toLowerCase()}</option>
                  {field.options.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              ) : field.multiline ? (
                <textarea {...attributes} rows={3} />
              ) : (
                <input {...attributes} autoComplete="off" />
              )}
            </label>
          );
        })}
        <div className="dialog-actions">
          <button
            type="button"
            className="button secondary"
            disabled={busy}
            onClick={onClose}
          >
            Hủy
          </button>
          <button className="button primary" disabled={busy}>
            {busy ? "Đang lưu…" : submitLabel}
          </button>
        </div>
      </form>
    </Modal>
  );
}

export function ConfirmAction({
  title,
  description,
  confirmLabel,
  onConfirm,
  onClose,
  danger = false,
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function confirm() {
    setBusy(true);
    setError("");
    try {
      await onConfirm();
      onClose();
    } catch (cause) {
      setError(errorMessage(cause));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal isOpen title={title} onClose={onClose} busy={busy}>
      <p className="confirmation-copy">{description}</p>
      <Feedback error message={error} />
      <div className="dialog-actions">
        <button
          className="button secondary"
          onClick={onClose}
          disabled={busy}
          autoFocus
        >
          Hủy
        </button>
        <button
          className={`button ${danger ? "danger" : "primary"}`}
          onClick={confirm}
          disabled={busy}
        >
          {busy ? "Đang xử lý…" : confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
