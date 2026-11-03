export const BMI_CATEGORIES = [
  { id: "underweight", label: "Thiếu cân", range: "< 18,5", upper: 18.5 },
  { id: "healthy", label: "Bình thường", range: "18,5 – < 25", upper: 25 },
  { id: "overweight", label: "Thừa cân", range: "25 – < 30", upper: 30 },
  { id: "obesity", label: "Béo phì", range: "≥ 30", upper: Infinity },
];

export function calculateBmi(heightCm, weightKg) {
  const height = Number(heightCm);
  const weight = Number(weightKg);
  if (!Number.isFinite(height) || !Number.isFinite(weight) || height <= 0 || weight <= 0) return null;
  const value = Math.round((weight / ((height / 100) ** 2)) * 10) / 10;
  if (!Number.isFinite(value)) return null;
  return {
    value,
    category: BMI_CATEGORIES.find(({ upper }) => value < upper),
    position: Math.max(0, Math.min(100, ((value - 12) / 28) * 100)),
  };
}
