import test from "node:test";
import assert from "node:assert/strict";
import { medicalRecordApi } from "../src/api/medicalRecordApi.js";
import axiosClient from "../src/api/axiosClient.js";

test("medicalRecordApi.createCheckup calls POST /medical-records with proper payload", async () => {
  const originalPost = axiosClient.post;
  let capturedUrl = "";
  let capturedBody = null;

  try {
    axiosClient.post = async (url, body) => {
      capturedUrl = url;
      capturedBody = body;
      return {
        success: true,
        data: {
          id: 101,
          checkupCode: "PK-20261117-001",
          status: "DRAFT",
          ...body,
        },
      };
    };

    const payload = {
      appointmentId: 12,
      studentId: 45,
      checkupType: "KHAM_DA_KHOA",
      symptoms: "Sốt nhẹ, ho khan",
      diagnosisNotes: "Viêm phế quản cấp",
      treatmentSuggestions: "Nghỉ ngơi, uống nhiều nước",
      diseaseGroupId: 1,
      status: "DRAFT",
    };

    const result = await medicalRecordApi.createCheckup(payload);

    assert.equal(capturedUrl, "/medical-records");
    assert.equal(capturedBody.appointmentId, 12);
    assert.equal(capturedBody.status, "DRAFT");
    assert.equal(result.id, 101);
    assert.equal(result.checkupCode, "PK-20261117-001");
  } finally {
    axiosClient.post = originalPost;
  }
});

test("medicalRecordApi.updateCheckup calls PUT /medical-records/{id}", async () => {
  const originalPut = axiosClient.put;
  let capturedUrl = "";
  let capturedBody = null;

  try {
    axiosClient.put = async (url, body) => {
      capturedUrl = url;
      capturedBody = body;
      return {
        success: true,
        data: {
          id: 101,
          ...body,
        },
      };
    };

    const payload = {
      symptoms: "Đau đầu dữ dội",
      diagnosisNotes: "Đau nửa đầu Migraine",
      status: "DRAFT",
    };

    const result = await medicalRecordApi.updateCheckup(101, payload);

    assert.equal(capturedUrl, "/medical-records/101");
    assert.equal(capturedBody.symptoms, "Đau đầu dữ dội");
    assert.equal(result.id, 101);
  } finally {
    axiosClient.put = originalPut;
  }
});

test("medicalRecordApi.updatePrescriptions formats medicines and calls PUT /medical-records/{id}/prescriptions", async () => {
  const originalPut = axiosClient.put;
  let capturedUrl = "";
  let capturedBody = null;

  try {
    axiosClient.put = async (url, body) => {
      capturedUrl = url;
      capturedBody = body;
      return {
        success: true,
        data: {
          id: 101,
          prescribedMedicines: body.medicines,
        },
      };
    };

    const medicines = [
      { medicineName: " Paracetamol 500mg ", quantity: "20", dosageInstruction: " Ngày uống 2 viên " },
      { medicineName: "Vitamin C 500mg", quantity: 10, dosageInstruction: "Sáng 1 viên sau ăn" },
    ];

    const result = await medicalRecordApi.updatePrescriptions(101, medicines);

    assert.equal(capturedUrl, "/medical-records/101/prescriptions");
    assert.equal(capturedBody.medicines.length, 2);
    assert.equal(capturedBody.medicines[0].medicineName, "Paracetamol 500mg");
    assert.equal(capturedBody.medicines[0].quantity, 20);
    assert.equal(capturedBody.medicines[0].dosageInstruction, "Ngày uống 2 viên");
    assert.equal(result.prescribedMedicines.length, 2);
  } finally {
    axiosClient.put = originalPut;
  }
});

test("medicalRecordApi.completeCheckup calls PUT /medical-records/{id}/complete", async () => {
  const originalPut = axiosClient.put;
  let capturedUrl = "";

  try {
    axiosClient.put = async (url) => {
      capturedUrl = url;
      return {
        success: true,
        data: {
          id: 101,
          status: "COMPLETED",
          completedAt: "2026-11-17T15:30:00Z",
        },
      };
    };

    const result = await medicalRecordApi.completeCheckup(101);

    assert.equal(capturedUrl, "/medical-records/101/complete");
    assert.equal(result.status, "COMPLETED");
  } finally {
    axiosClient.put = originalPut;
  }
});

test("medicalRecordApi.getCheckupByAppointment calls GET /medical-records/by-appointment/{appointmentId}", async () => {
  const originalGet = axiosClient.get;
  let capturedUrl = "";

  try {
    axiosClient.get = async (url) => {
      capturedUrl = url;
      return {
        success: true,
        data: {
          id: 202,
          appointmentId: 55,
          checkupCode: "PK-0055",
        },
      };
    };

    const result = await medicalRecordApi.getCheckupByAppointment(55);

    assert.equal(capturedUrl, "/medical-records/by-appointment/55");
    assert.equal(result.id, 202);
    assert.equal(result.appointmentId, 55);
  } finally {
    axiosClient.get = originalGet;
  }
});

test("medicalRecordApi.getDiseaseGroups forwards search query param", async () => {
  const originalGet = axiosClient.get;
  let capturedUrl = "";
  let capturedParams = null;

  try {
    axiosClient.get = async (url, options) => {
      capturedUrl = url;
      capturedParams = options?.params;
      return {
        success: true,
        data: [
          { id: 1, code: "HOHAP", name: "Bệnh đường hô hấp" },
          { id: 2, code: "TIEUHOA", name: "Bệnh đường tiêu hóa" },
        ],
      };
    };

    const result = await medicalRecordApi.getDiseaseGroups("hô hấp");

    assert.equal(capturedUrl, "/disease-groups");
    assert.equal(capturedParams.search, "hô hấp");
    assert.equal(result.length, 2);
  } finally {
    axiosClient.get = originalGet;
  }
});

test("medicalRecordApi rejects on backend failure envelope", async () => {
  const originalPost = axiosClient.post;

  try {
    axiosClient.post = async () => ({
      success: false,
      message: "Lịch hẹn đã được tiếp nhận trước đó",
      data: null,
    });

    await assert.rejects(
      async () => {
        await medicalRecordApi.createCheckup({ appointmentId: 99 });
      },
      {
        message: "Lịch hẹn đã được tiếp nhận trước đó",
      }
    );
  } finally {
    axiosClient.post = originalPost;
  }
});
