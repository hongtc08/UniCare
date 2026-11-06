// Completion covers the six health fields the student can supply in this form.
// Explicit answers such as "Không có" count; missing or whitespace-only values do not.
export function getHealthProfileCompletion(profile = {}) {
  const filled = (value) => typeof value === "string" && value.trim().length > 0;
  const checks = [
    Number(profile.height) > 0 && Number(profile.height) <= 300,
    Number(profile.weight) > 0 && Number(profile.weight) <= 500,
    ["A", "B", "AB", "O"].includes(profile.bloodGroup),
    filled(profile.allergies), filled(profile.conditions), filled(profile.vaccinations),
  ];
  const completed = checks.filter(Boolean).length;
  return { completed, total: checks.length, percent: Math.round(completed / checks.length * 100) };
}
