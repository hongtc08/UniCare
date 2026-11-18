import { useEffect, useId, useRef, useState } from "react";
import { Check, Search } from "lucide-react";
import { emergencyIntakeApi } from "../../api/emergencyIntakeApi";

export default function StudentSearch({ selected, onSelect, disabled }) {
  const inputId = useId();
  const input = useRef(null);
  const request = useRef(null);
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => () => request.current?.abort(), []);

  async function search(event) {
    event.preventDefault();
    const query = keyword.trim();
    if (!query || loading || disabled) return;

    const controller = new AbortController();
    request.current = controller;
    onSelect(null);
    setResults(null);
    setError("");
    setLoading(true);
    try {
      const students = await emergencyIntakeApi.searchStudents(query, controller.signal);
      if (!controller.signal.aborted) setResults(students);
    } catch (error) {
      if (!controller.signal.aborted) {
        setError(error.response?.data?.message || "Không tra cứu được sinh viên. Vui lòng thử lại.");
      }
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }

  return (
    <>
      <form className="emergency-search" onSubmit={search} aria-label="Tra cứu sinh viên">
        <label htmlFor={inputId}>MSSV, họ tên hoặc lớp</label>
        <div className="emergency-search-controls">
          <input
            id={inputId}
            ref={input}
            type="search"
            autoFocus
            placeholder="Ví dụ 24520579 hoặc tên lớp"
            value={keyword}
            disabled={disabled}
            onChange={(event) => {
              request.current?.abort();
              setKeyword(event.target.value);
              setLoading(false);
              setResults(null);
              setError("");
              onSelect(null);
            }}
          />
          <button className="emergency-button" disabled={disabled || loading || !keyword.trim()}>
            <Search size={16} aria-hidden="true" /> Tìm sinh viên
          </button>
        </div>
      </form>
      {loading && <p className="emergency-muted" role="status">Đang tìm sinh viên…</p>}
      {error && <p className="emergency-error" role="alert">{error}</p>}
      {!loading && !error && results?.length === 0 && (
        <p className="emergency-muted" role="status">Không tìm thấy sinh viên phù hợp.</p>
      )}
      {!selected && results?.length > 0 && (
        <ul className="emergency-results" aria-label="Kết quả tra cứu">
          {results.map((student) => (
            <li key={student.studentId}>
              <button type="button" onClick={() => {
                onSelect(student);
                input.current?.focus();
              }}>
                <strong>{student.fullName}</strong>
                <span>{[student.studentCode, student.className || student.classCode, student.facultyName].filter(Boolean).join(" · ")}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
      {selected && (
        <div className="emergency-selected" role="status">
          <Check size={18} aria-hidden="true" />
          <div>
            <strong>{selected.fullName}</strong>
            <span>{[selected.studentCode, selected.className || selected.classCode, selected.facultyName].filter(Boolean).join(" · ")}</span>
          </div>
          <button type="button" className="emergency-button" disabled={disabled} onClick={() => {
            onSelect(null);
            input.current?.focus();
          }}>Đổi sinh viên</button>
        </div>
      )}
    </>
  );
}
