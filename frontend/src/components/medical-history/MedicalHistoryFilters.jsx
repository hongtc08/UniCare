import { useId } from "react";
import { checkupTypes } from "./medicalHistoryDisplay";

export default function MedicalHistoryFilters({ total, counts, value, onChange }) {
  const groupName = useId();
  const options = [
    { label: "Tất cả lần khám", count: total, value: "all" },
    ...checkupTypes.map((label) => ({ label, count: counts[label] ?? 0, value: label })),
  ];

  return (
    <fieldset className="history-filters">
      <legend>Loại hình khám</legend>
      <div className="history-filter-options">
        {options.map((option) => (
          <label key={option.value} className="history-filter-option">
            <input
              type="radio"
              name={groupName}
              checked={value === option.value}
              onChange={() => onChange(option.value)}
            />
            <span>{option.label}</span>
            <span className="history-filter-count" aria-label={`${option.count} lần khám`}>{option.count}</span>
          </label>
        ))}
      </div>
    </fieldset>
  );
}
