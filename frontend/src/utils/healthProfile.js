export function fromHealthResponse(data) {
  return {
    height: data?.height == null ? "" : String(data.height),
    weight: data?.weight == null ? "" : String(data.weight),
    bloodGroup: data?.bloodType ?? "",
    allergies: data?.allergies ?? "",
    conditions: data?.underlyingDiseases ?? "",
    vaccinations: data?.vaccinationHistory ?? "",
  };
}

export function toHealthRequest(profile) {
  return {
    height: Number(profile.height), weight: Number(profile.weight),
    bloodType: profile.bloodGroup || null,
    allergies: profile.allergies.trim(),
    underlyingDiseases: profile.conditions.trim(),
    vaccinationHistory: profile.vaccinations.trim(),
  };
}
