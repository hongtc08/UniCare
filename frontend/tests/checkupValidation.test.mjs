import test from "node:test";
import assert from "node:assert/strict";
import {
  validateCheckupForm,
  validateTransferForm,
  normalizeCheckupType,
} from "../src/utils/checkupValidation.js";

test("validateCheckupForm allows empty fields in DRAFT mode", () => {
  const emptyForm = {
    checkupType: "GENERAL",
    isPeriodic: false,
    periodicResult: null,
    symptoms: "",
    diagnosisNotes: "",
    treatmentSuggestions: "",
  };

  const result = validateCheckupForm(emptyForm, false);
  assert.equal(result.isValid, true);
  assert.equal(Object.keys(result.errors).length, 0);
});

test("validateCheckupForm requires symptoms and diagnosisNotes for GENERAL in COMPLETED mode", () => {
  const invalidForm = {
    checkupType: "GENERAL",
    symptoms: "   ",
    diagnosisNotes: "",
  };

  const result = validateCheckupForm(invalidForm, true);
  assert.equal(result.isValid, false);
  assert.ok(result.errors.symptoms);
  assert.ok(result.errors.diagnosisNotes);

  const validForm = {
    checkupType: "GENERAL",
    symptoms: "Sốt cao 39 độ, đau rát họng",
    diagnosisNotes: "Viêm họng cấp",
  };

  const validResult = validateCheckupForm(validForm, true);
  assert.equal(validResult.isValid, true);
  assert.equal(Object.keys(validResult.errors).length, 0);
});

test("validateCheckupForm requires symptoms and diagnosisNotes for PSYCHOLOGICAL in COMPLETED mode", () => {
  const invalidForm = {
    checkupType: "PSYCHOLOGICAL",
    symptoms: "",
    diagnosisNotes: "",
  };

  const result = validateCheckupForm(invalidForm, true);
  assert.equal(result.isValid, false);
  assert.ok(result.errors.symptoms);
  assert.ok(result.errors.diagnosisNotes);

  const validForm = {
    checkupType: "PSYCHOLOGICAL",
    symptoms: "Lo âu kéo dài, mất ngủ 2 tuần",
    diagnosisNotes: "Tư vấn stress áp lực mùa thi và phương pháp thư giãn",
  };

  const validResult = validateCheckupForm(validForm, true);
  assert.equal(validResult.isValid, true);
});

test("validateCheckupForm requires periodicResult when isPeriodic is true in COMPLETED mode", () => {
  const invalidPeriodic = {
    checkupType: "GENERAL",
    isPeriodic: true,
    periodicResult: null,
    symptoms: "Khám sức khỏe đầu năm, không triệu chứng bất thường",
    diagnosisNotes: "Sức khỏe bình thường",
  };

  const result = validateCheckupForm(invalidPeriodic, true);
  assert.equal(result.isValid, false);
  assert.ok(result.errors.periodicResult);

  const validPeriodic = {
    ...invalidPeriodic,
    periodicResult: "PASSED",
  };

  const validResult = validateCheckupForm(validPeriodic, true);
  assert.equal(validResult.isValid, true);
});

test("normalizeCheckupType maps legacy aliases to GENERAL or PSYCHOLOGICAL", () => {
  assert.equal(normalizeCheckupType("TU_VAN_TAM_LY"), "PSYCHOLOGICAL");
  assert.equal(normalizeCheckupType("PSYCHOLOGICAL"), "PSYCHOLOGICAL");
  assert.equal(normalizeCheckupType("KHAM_DA_KHOA"), "GENERAL");
  assert.equal(normalizeCheckupType("GENERAL"), "GENERAL");
});

test("validateTransferForm validates all required transfer fields", () => {
  const invalid = validateTransferForm({});
  assert.equal(invalid.isValid, false);
  assert.ok(invalid.errors.targetHospital);
  assert.ok(invalid.errors.reasonCategory);
  assert.ok(invalid.errors.transferReason);
  assert.ok(invalid.errors.currentCondition);
  assert.ok(invalid.errors.firstAidNotes);

  const valid = validateTransferForm({
    targetHospital: "Bệnh viện Bạch Mai",
    reasonCategory: "CAP_CUU_NANG",
    transferReason: "Chấn thương nặng vượt khả năng xử trí tại trạm",
    currentCondition: "Mạch nhanh, đau nhiều vùng cổ chân",
    firstAidNotes: "Cố định nẹp tạm thời, chườm lạnh",
    refusalNotes: "",
  });
  assert.equal(valid.isValid, true);
});
