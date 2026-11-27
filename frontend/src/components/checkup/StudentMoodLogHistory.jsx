import { SmilePlus, AlertTriangle, RefreshCw, Calendar, MessageSquareText } from "lucide-react";

const MOOD_LEVEL_CONFIG = {
  1: {
    label: "Rất tệ",
    badgeClass: "bg-rose-50 text-rose-700 border-rose-200",
    dotClass: "bg-rose-500",
    barClass: "bg-rose-500",
  },
  2: {
    label: "Tệ",
    badgeClass: "bg-orange-50 text-orange-700 border-orange-200",
    dotClass: "bg-orange-500",
    barClass: "bg-orange-500",
  },
  3: {
    label: "Bình thường",
    badgeClass: "bg-amber-50 text-amber-700 border-amber-200",
    dotClass: "bg-amber-500",
    barClass: "bg-amber-500",
  },
  4: {
    label: "Vui vẻ",
    badgeClass: "bg-sky-50 text-sky-700 border-sky-200",
    dotClass: "bg-sky-500",
    barClass: "bg-sky-500",
  },
  5: {
    label: "Tuyệt vời",
    badgeClass: "bg-emerald-50 text-emerald-700 border-emerald-200",
    dotClass: "bg-emerald-500",
    barClass: "bg-emerald-500",
  },
};

const PERIOD_OPTIONS = [
  { value: "WEEK", label: "7 ngày" },
  { value: "MONTH", label: "30 ngày" },
  { value: "ALL", label: "Tất cả" },
];

function formatMoodDate(logDate, logTime) {
  if (logTime) {
    const d = new Date(logTime);
    if (!Number.isNaN(d.getTime())) {
      return d.toLocaleString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    }
  }
  if (logDate) {
    const parts = String(logDate).slice(0, 10).split("-");
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return String(logDate);
  }
  return "—";
}

/**
 * Danh sách Nhật ký tâm trạng hằng ngày (Chỉ đọc) của Sinh viên phục vụ Bác sĩ / Chuyên gia tâm lý.
 */
export default function StudentMoodLogHistory({
  moodHistory = null,
  logs,
  loading = false,
  error = null,
  onRetry,
  period,
  onPeriodChange,
}) {
  const items = Array.isArray(logs)
    ? logs
    : Array.isArray(moodHistory?.logs)
    ? moodHistory.logs
    : [];
  const totalLogs = moodHistory?.totalLogs ?? items.length;
  const avgMood =
    moodHistory?.averageMoodLevel !== undefined && moodHistory?.averageMoodLevel !== null
      ? Number(moodHistory.averageMoodLevel)
      : null;

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs overflow-hidden">
      {/* HEADER */}
      <div className="px-4 py-3 border-b border-slate-100 bg-slate-50/70 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg bg-blue-50 text-blue-600">
            <SmilePlus className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
              <span>Nhật ký tâm trạng</span>
              <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold bg-slate-100 text-slate-600 border border-slate-200 normal-case">
                Chỉ đọc
              </span>
            </h3>
            <p className="text-[11px] text-slate-500">
              {!loading && !error
                ? `${totalLogs} ghi nhận${
                    avgMood !== null ? ` · Trung bình: ${avgMood.toFixed(1)}/5` : ""
                  }`
                : "Thang mức 1 (Rất tệ) – 5 (Tuyệt vời)"}
            </p>
          </div>
        </div>

        {/* Bộ lọc khoảng thời gian tùy chọn */}
        {onPeriodChange && (
          <div className="inline-flex rounded-lg border border-slate-200 bg-white p-0.5 gap-0.5">
            {PERIOD_OPTIONS.map((opt) => {
              const active = (period || moodHistory?.period || "MONTH") === opt.value;
              return (
                <button
                  key={opt.value}
                  type="button"
                  disabled={loading}
                  onClick={() => onPeriodChange(opt.value)}
                  className={`px-2 py-1 rounded-md text-[11px] font-semibold transition-colors disabled:opacity-50 ${
                    active
                      ? "bg-blue-600 text-white"
                      : "text-slate-600 hover:bg-slate-100"
                  }`}
                >
                  {opt.label}
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* BODY */}
      <div className="p-4">
        {loading ? (
          <div className="space-y-2.5 animate-pulse">
            {[1, 2, 3].map((i) => (
              <div key={i} className="p-3 rounded-lg bg-slate-100 h-16" />
            ))}
          </div>
        ) : error ? (
          <div className="p-5 rounded-lg bg-rose-50 border border-rose-200 text-center space-y-2">
            <AlertTriangle className="w-6 h-6 mx-auto text-rose-500" />
            <p className="text-xs font-semibold text-rose-700">{error}</p>
            {onRetry && (
              <button
                type="button"
                onClick={onRetry}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-white hover:bg-rose-100 text-rose-700 border border-rose-200 rounded-lg transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Thử lại</span>
              </button>
            )}
          </div>
        ) : items.length === 0 ? (
          <div className="py-7 px-4 text-center text-slate-400 space-y-1.5 bg-slate-50/70 rounded-lg border border-dashed border-slate-200">
            <SmilePlus className="w-7 h-7 mx-auto text-slate-300" />
            <p className="text-xs font-semibold text-slate-600">
              Sinh viên chưa có nhật ký tâm trạng
            </p>
            <p className="text-[11px] text-slate-400">
              Chưa có bản ghi cảm xúc hằng ngày nào trong khoảng thời gian đã chọn.
            </p>
          </div>
        ) : (
          <div className="space-y-2.5 max-h-72 overflow-y-auto pr-1">
            {items.map((item, index) => {
              const level = Math.max(1, Math.min(5, Number(item.moodLevel) || 3));
              const cfg = MOOD_LEVEL_CONFIG[level] || MOOD_LEVEL_CONFIG[3];
              const displayLabel = item.moodLabel || cfg.label;

              return (
                <div
                  key={item.id ?? `${item.logTime || item.logDate}-${index}`}
                  className="p-3 rounded-lg bg-slate-50/80 border border-slate-200/70 space-y-1.5"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="inline-flex items-center gap-1.5 text-[11px] font-medium text-slate-500">
                      <Calendar className="w-3.5 h-3.5 text-slate-400" />
                      {formatMoodDate(item.logDate, item.logTime)}
                    </span>

                    <div className="flex items-center gap-2">
                      {/* Thanh 5 nấc trực quan */}
                      <div className="flex items-center gap-0.5" aria-hidden="true">
                        {[1, 2, 3, 4, 5].map((step) => (
                          <span
                            key={step}
                            className={`w-2 h-1.5 rounded-xs ${
                              step <= level ? cfg.barClass : "bg-slate-200"
                            }`}
                          />
                        ))}
                      </div>

                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold border ${cfg.badgeClass}`}
                      >
                        <span className={`w-1.5 h-1.5 rounded-full ${cfg.dotClass}`} />
                        <span>
                          {level}/5 · {displayLabel}
                        </span>
                      </span>
                    </div>
                  </div>

                  <div className="flex items-start gap-1.5 text-xs text-slate-700">
                    <MessageSquareText className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
                    {item.notes && String(item.notes).trim() ? (
                      <p className="leading-relaxed whitespace-pre-line">{item.notes}</p>
                    ) : (
                      <p className="text-slate-400 italic">Không có ghi chú kèm theo</p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
