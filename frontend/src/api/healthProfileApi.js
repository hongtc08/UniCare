import axiosClient from "./axiosClient";

export const healthProfileApi = {
  get: (signal) => axiosClient.get("/health-profile", { signal, timeout: 15000 }),
  save: (data) => axiosClient.put("/health-profile", data, { timeout: 15000 }),
  getPersonalInfo: (signal) => axiosClient.get("/health-profile/personal-info", { signal, timeout: 15000 }),
  savePersonalInfo: (data) => axiosClient.put("/health-profile/personal-info", data, { timeout: 15000 }),
  getInsurance: (signal) => axiosClient.get("/health-insurances", { signal, timeout: 15000 }),
  createInsurance: (data) => axiosClient.post("/health-insurances", data, { timeout: 15000 }),
  updateInsurance: (id, data) => axiosClient.put(`/health-insurances/${id}`, data, { timeout: 15000 }),
  saveInsurance: (data, id) =>
    id != null
      ? axiosClient.put(`/health-insurances/${id}`, data, { timeout: 15000 })
      : axiosClient.post("/health-insurances", data, { timeout: 15000 }),
};
