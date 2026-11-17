import test from "node:test";
import assert from "node:assert/strict";
import { validateCheckupForm } from "../src/utils/checkupValidation.js";

test("validateCheckupForm allows empty fields in DRAFT mode", () => {
  const emptyForm = {
    checkupType: "KHAM_DA_KHOA",
    symptoms: "",
    diagnosisNotes: "",
    treatmentSuggestions: "",
  };

  const result = validateCheckupForm(emptyForm, false);
  assert.equal(result.isValid, true);
  assert.equal(Object.keys(result.errors).length, 0);
});

test("validateCheckupForm requires symptoms and diagnosisNotes for KHAM_DA_KHOA in COMPLETED mode", () => {
  const invalidForm = {
    checkupType: "KHAM_DA_KHOA",
    symptoms: "   ",
    diagnosisNotes: "",
  };

  const result = validateCheckupForm(invalidForm, true);
  assert.equal(result.isValid, false);
  assert.ok(result.errors.symptoms);
  assert.ok(result.errors.diagnosisNotes);

  const validForm = {
    checkupType: "KHAM_DA_KHOA",
    symptoms: "Sốt cao 39 độ, đau rát họng",
    diagnosisNotes: "Viêm họng cấp",
  };

  const validResult = validateCheckupForm(validForm, true);
  assert.equal(validResult.isValid, true);
  assert.equal(Object.keys(validResult.errors).length, 0);
});

test("validateCheckupForm requires diagnosisNotes for TU_VAN_TAM_LY in COMPLETED mode", () => {
  const invalidForm = {
    checkupType: "TU_VAN_TAM_LY",
    symptoms: "",
    diagnosisNotes: "",
  };

  const result = validateCheckupForm(invalidForm, true);
  assert.equal(result.isValid, false);
  assert.ok(result.errors.diagnosisNotes);

  const validForm = {
    checkupType: "TU_VAN_TAM_LY",
    symptoms: "",
    diagnosisNotes: "Tư vấn stress áp lực mùa thi và phương pháp thư giãn",
  };

  const validResult = validateCheckupForm(validForm, true);
  assert.equal(validResult.isValid, true);
});
