import { useState, useMemo } from "react";
import { Brain, AlertTriangle, RefreshCw, TrendingUp } from "lucide-react";

const SCALES = [
  {
    key: "depression",
    scoreField: "depressionScore",
    levelField: "depressionLevel",
    label: "Trầm cảm",
    color: "#7c3aed",
    dotClass: "bg-violet-600",
    borderClass: "border-violet-200/80 bg-violet-50/40",
    textClass: "text-violet-700",
  },
  {
    key: "anxiety",
    scoreField: "anxietyScore",
    levelField: "anxietyLevel",
    label: "Lo âu",
    color: "#2563eb",
    dotClass: "bg-blue-600",
    borderClass: "border-blue-200/80 bg-blue-50/40",
    textClass: "text-blue-700",
  },
  {
    key: "stress",
    scoreField: "stressScore",
    levelField: "stressLevel",
    label: "Căng thẳng",
    color: "#ea580c",
    dotClass: "bg-orange-600",
    borderClass: "border-orange-200/80 bg-orange-50/40",
    textClass: "text-orange-700",
  },
];

const SEVERITY_MAP = {
  NORMAL: {
    label: "Bình thường",
    badgeClass: "bg-emerald-50 text-emerald-700 border-emerald-200",
  },
  MILD: {
    label: "Nhẹ",
    badgeClass: "bg-sky-50 text-sky-700 border-sky-200",
  },
  MODERATE: {
    label: "Vừa",
    badgeClass: "bg-amber-50 text-amber-700 border-amber-200",
  },
  SEVERE: {
    label: "Nặng",
    badgeClass: "bg-orange-50 text-orange-700 border-orange-200",
  },
  EXTREMELY_SEVERE: {
    label: "Rất nặng",
    badgeClass: "bg-rose-50 text-rose-700 border-rose-200",
  },
};

function getSeverityInfo(rawLevel) {
  if (!rawLevel) {
    return {
      label: "Chưa phân loại",
      badgeClass: "bg-slate-100 text-slate-600 border-slate-200",
    };
  }
  const normalized = String(rawLevel).trim().toUpperCase();
  if (SEVERITY_MAP[normalized]) return SEVERITY_MAP[normalized];

  const lower = String(rawLevel).trim().toLowerCase();
  if (lower.includes("rất nặng")) return SEVERITY_MAP.EXTREMELY_SEVERE;
  if (lower.includes("nặng")) return SEVERITY_MAP.SEVERE;
  if (lower.includes("vừa")) return SEVERITY_MAP.MODERATE;
  if (lower.includes("nhẹ")) return SEVERITY_MAP.MILD;
  if (lower.includes("bình thường")) return SEVERITY_MAP.NORMAL;

  return {
    label: String(rawLevel),
    badgeClass: "bg-slate-100 text-slate-700 border-slate-200",
  };
}

function formatShortDate(dateValue) {
  if (!dateValue) return "—";
  const d = new Date(dateValue);
  if (Number.isNaN(d.getTime())) return "—";
  return `${String(d.getDate()).padStart(2, "0")}/${String(
    d.getMonth() + 1
  ).padStart(2, "0")}`;
}

function formatFullDateTime(dateValue) {
  if (!dateValue) return "—";
  const d = new Date(dateValue);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

// Kích thước tọa độ cố định trong SVG viewBox
const SVG_W = 560;
const SVG_H = 220;
const PAD = { top: 18, right: 20, bottom: 32, left: 36 };
const PLOT_W = SVG_W - PAD.left - PAD.right;
const PLOT_H = SVG_H - PAD.top - PAD.bottom;
const Y_MAX = 42;
const Y_TICKS = [0, 10, 20, 30, 42];

function xForIndex(index, total) {
  if (total <= 1) return PAD.left + PLOT_W / 2;
  return PAD.left + (index / (total - 1)) * PLOT_W;
}

function yForScore(score) {
  const clamped = Math.max(0, Math.min(Y_MAX, Number(score) || 0));
  return PAD.top + PLOT_H - (clamped / Y_MAX) * PLOT_H;
}

/**
 * Biểu đồ đường diễn biến điểm 3 thang đo DASS-21 (Trầm cảm, Lo âu, Căng thẳng) phục vụ Tư vấn Tâm lý.
 */
export default function DassHistoryChart({
  scoreTrend = [],
  latestResult = null,
  loading = false,
  error = null,
  onRetry,
}) {
  const [selectedIndex, setSelectedIndex] = useState(null);

  const points = useMemo(() => {
    if (!Array.isArray(scoreTrend) || scoreTrend.length === 0) return [];
    return [...scoreTrend].sort((a, b) => {
      const ta = a?.testDate ? new Date(a.testDate).getTime() : 0;
      const tb = b?.testDate ? new Date(b.testDate).getTime() : 0;
      if (ta !== tb) return ta - tb;
      return (a?.id || 0) - (b?.id || 0);
    });
  }, [scoreTrend]);

  const effectiveIndex =
    points.length === 0
      ? -1
      : selectedIndex !== null && selectedIndex >= 0 && selectedIndex < points.length
      ? selectedIndex
      : points.length - 1;

  const activePoint = effectiveIndex >= 0 ? points[effectiveIndex] : null;
  const summaryResult = latestResult || (points.length > 0 ? points[points.length - 1] : null);

  return (
    <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs overflow-hidden">
      {/* HEADER */}
      <div className="px-4 py-3 border-b border-slate-100 bg-purple-50/40 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg bg-purple-100 text-purple-700">
            <Brain className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
              <span>Diễn biến điểm DASS-21</span>
              {!loading && !error && points.length > 0 && (
                <span className="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-purple-100 text-purple-800">
                  {points.length} lần test
                </span>
              )}
            </h3>
            <p className="text-[11px] text-slate-500">
              Thang điểm nhân hệ số 2 (0 – 42) · Chỉ đọc
            </p>
          </div>
        </div>

        {/* CHÚ GIẢI 3 THANG ĐO */}
        <div className="flex flex-wrap items-center gap-3 text-[11px] font-medium text-slate-600">
          {SCALES.map((scale) => (
            <span key={scale.key} className="inline-flex items-center gap-1.5">
              <span className={`w-2.5 h-2.5 rounded-full ${scale.dotClass}`} />
              <span>{scale.label}</span>
            </span>
          ))}
        </div>
      </div>

      {/* BODY */}
      <div className="p-4 space-y-3.5">
        {loading ? (
          <div className="space-y-3 animate-pulse">
            <div className="grid grid-cols-3 gap-2">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-14 rounded-lg bg-slate-100" />
              ))}
            </div>
            <div className="h-48 rounded-lg bg-slate-100" />
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
        ) : points.length === 0 ? (
          <div className="py-8 px-4 text-center text-slate-400 space-y-1.5 bg-slate-50/70 rounded-lg border border-dashed border-slate-200">
            <TrendingUp className="w-8 h-8 mx-auto text-slate-300" />
            <p className="text-xs font-semibold text-slate-600">
              Sinh viên chưa có kết quả đánh giá DASS-21
            </p>
            <p className="text-[11px] text-slate-400">
              Biểu đồ xu hướng 3 thang đo sẽ tự động hiển thị khi sinh viên hoàn thành bài test DASS-21.
            </p>
          </div>
        ) : (
          <>
            {/* TÓM TẮT KẾT QUẢ GẦN NHẤT */}
            {summaryResult && (
              <div className="grid grid-cols-3 gap-2">
                {SCALES.map((scale) => {
                  const score = summaryResult[scale.scoreField] ?? 0;
                  const sev = getSeverityInfo(summaryResult[scale.levelField]);
                  return (
                    <div
                      key={scale.key}
                      className={`p-2.5 rounded-lg border ${scale.borderClass} space-y-1`}
                    >
                      <div className="flex items-center justify-between gap-1">
                        <span className={`text-[11px] font-semibold ${scale.textClass}`}>
                          {scale.label}
                        </span>
                        <span className="text-sm font-bold text-slate-900">{score}</span>
                      </div>
                      <span
                        className={`inline-block px-1.5 py-0.5 rounded text-[10px] font-semibold border ${sev.badgeClass}`}
                      >
                        {sev.label}
                      </span>
                    </div>
                  );
                })}
              </div>
            )}

            {/* BIỂU ĐỒ ĐƯỜNG SVG */}
            <div className="rounded-lg border border-slate-100 bg-slate-50/40 p-2">
              <svg
                viewBox={`0 0 ${SVG_W} ${SVG_H}`}
                className="w-full h-auto overflow-visible select-none"
                role="img"
                aria-label="Biểu đồ diễn biến điểm DASS-21 theo thời gian"
              >
                {/* Lưới ngang & Nhãn trục Y */}
                {Y_TICKS.map((tick) => {
                  const y = yForScore(tick);
                  return (
                    <g key={tick}>
                      <line
                        x1={PAD.left}
                        y1={y}
                        x2={SVG_W - PAD.right}
                        y2={y}
                        stroke={tick === 0 ? "#cbd5e1" : "#e2e8f0"}
                        strokeDasharray={tick === 0 ? undefined : "3 3"}
                        strokeWidth="1"
                      />
                      <text
                        x={PAD.left - 8}
                        y={y + 3.5}
                        textAnchor="end"
                        className="fill-slate-400 text-[10px] font-medium"
                      >
                        {tick}
                      </text>
                    </g>
                  );
                })}

                {/* Đường gióng dọc tại điểm đang chọn */}
                {effectiveIndex >= 0 && (
                  <line
                    x1={xForIndex(effectiveIndex, points.length)}
                    y1={PAD.top}
                    x2={xForIndex(effectiveIndex, points.length)}
                    y2={PAD.top + PLOT_H}
                    stroke="#94a3b8"
                    strokeDasharray="3 3"
                    strokeWidth="1"
                  />
                )}

                {/* Đường nối 3 thang đo */}
                {points.length > 1 &&
                  SCALES.map((scale) => {
                    const polyPoints = points
                      .map(
                        (pt, idx) =>
                          `${xForIndex(idx, points.length)},${yForScore(
                            pt[scale.scoreField]
                          )}`
                      )
                      .join(" ");
                    return (
                      <polyline
                        key={scale.key}
                        fill="none"
                        stroke={scale.color}
                        strokeWidth="2.2"
                        strokeLinejoin="round"
                        strokeLinecap="round"
                        points={polyPoints}
                      />
                    );
                  })}

                {/* Các điểm dữ liệu & Vùng tương tác hover/click theo cột ngày */}
                {points.map((pt, idx) => {
                  const cx = xForIndex(idx, points.length);
                  const isActive = idx === effectiveIndex;
                  const showLabel =
                    points.length <= 7 ||
                    idx === 0 ||
                    idx === points.length - 1 ||
                    idx % Math.ceil(points.length / 6) === 0 ||
                    isActive;

                  return (
                    <g
                      key={pt.id ?? `${pt.testDate}-${idx}`}
                      className="cursor-pointer"
                      onMouseEnter={() => setSelectedIndex(idx)}
                      onClick={() => setSelectedIndex(idx)}
                    >
                      {/* Vùng bấm/hover rộng để dễ chọn điểm */}
                      <rect
                        x={cx - 16}
                        y={PAD.top}
                        width={32}
                        height={PLOT_H + 22}
                        fill="transparent"
                      />

                      {SCALES.map((scale) => (
                        <circle
                          key={scale.key}
                          cx={cx}
                          cy={yForScore(pt[scale.scoreField])}
                          r={isActive ? 5 : 3.5}
                          fill={isActive ? scale.color : "#ffffff"}
                          stroke={scale.color}
                          strokeWidth="2"
                        />
                      ))}

                      {showLabel && (
                        <text
                          x={cx}
                          y={SVG_H - 8}
                          textAnchor="middle"
                          className={`text-[10px] ${
                            isActive
                              ? "fill-slate-800 font-bold"
                              : "fill-slate-400 font-medium"
                          }`}
                        >
                          {formatShortDate(pt.testDate)}
                        </text>
                      )}
                    </g>
                  );
                })}
              </svg>
            </div>

            {/* TOOLTIP CHI TIẾT LẦN TEST ĐANG CHỌN */}
            {activePoint && (
              <div className="p-3 rounded-lg bg-slate-50 border border-slate-200/80 text-xs space-y-2">
                <div className="flex items-center justify-between gap-2 text-[11px]">
                  <span className="font-semibold text-slate-600">
                    Chi tiết lần test:{" "}
                    <strong className="text-slate-900">
                      {formatFullDateTime(activePoint.testDate)}
                    </strong>
                  </span>
                  {points.length > 1 && (
                    <span className="text-slate-400">
                      Lần {effectiveIndex + 1}/{points.length} (di chuột hoặc chạm điểm để đổi)
                    </span>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  {SCALES.map((scale) => {
                    const score = activePoint[scale.scoreField] ?? 0;
                    const sev = getSeverityInfo(activePoint[scale.levelField]);
                    return (
                      <div
                        key={scale.key}
                        className="flex items-center justify-between gap-2 px-2.5 py-1.5 rounded-md bg-white border border-slate-200/70"
                      >
                        <span className="inline-flex items-center gap-1.5 text-[11px] font-medium text-slate-700">
                          <span className={`w-2 h-2 rounded-full ${scale.dotClass}`} />
                          {scale.label}: <strong className="text-slate-900">{score}</strong>
                        </span>
                        <span
                          className={`px-1.5 py-0.2 rounded text-[10px] font-semibold border ${sev.badgeClass}`}
                        >
                          {sev.label}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
