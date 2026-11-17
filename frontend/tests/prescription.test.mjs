import test from "node:test";
import assert from "node:assert/strict";
import {
  createEmptyMedicine,
  isRowCompletelyEmpty,
  validateAndCleanMedicines,
} from "../src/utils/prescription.js";

test("createEmptyMedicine creates stable unique _localId and default values", () => {
  const row1 = createEmptyMedicine();
  const row2 = createEmptyMedicine();

  assert.ok(row1._localId);
  assert.ok(row2._localId);
  assert.notEqual(row1._localId, row2._localId);
  assert.equal(row1.quantity, 1);
  assert.equal(row1.medicineName, "");
  assert.equal(row1.dosageInstruction, "");

  const rowWithDbId = createEmptyMedicine({ id: 10, medicineName: "Paracetamol" });
  assert.equal(rowWithDbId._localId, "db_10");
  assert.equal(rowWithDbId.id, 10);
  assert.equal(rowWithDbId.medicineName, "Paracetamol");
});

test("isRowCompletelyEmpty accurately identifies untouched rows", () => {
  assert.equal(isRowCompletelyEmpty({ medicineName: "", dosageInstruction: "" }), true);
  assert.equal(isRowCompletelyEmpty({ medicineName: "   ", dosageInstruction: " " }), true);
  assert.equal(isRowCompletelyEmpty({ medicineName: "Paracetamol", dosageInstruction: "" }), false);
  assert.equal(isRowCompletelyEmpty({ medicineName: "", dosageInstruction: "Ngày 1 viên" }), false);
});

test("validateAndCleanMedicines filters out completely empty rows", () => {
  const input = [
    { _localId: "row_1", medicineName: "Paracetamol 500mg", quantity: 10, dosageInstruction: "Ngày 2 viên" },
    { _localId: "row_2", medicineName: "", quantity: 1, dosageInstruction: "" }, // row hoàn toàn trống
  ];

  const result = validateAndCleanMedicines(input);
  assert.equal(result.isValid, true);
  assert.equal(result.cleanedMedicines.length, 1);
  assert.equal(result.cleanedMedicines[0].medicineName, "Paracetamol 500mg");
  assert.equal(result.cleanedMedicines[0].quantity, 10);
  assert.equal(result.cleanedMedicines[0].dosageInstruction, "Ngày 2 viên");
});

test("validateAndCleanMedicines catches missing fields and invalid quantity", () => {
  const input = [
    { _localId: "row_1", medicineName: "", quantity: 10, dosageInstruction: "Ngày 2 viên" }, // thiếu tên
    { _localId: "row_2", medicineName: "Berberin", quantity: 0, dosageInstruction: "Ngày 4 viên" }, // số lượng 0
    { _localId: "row_3", medicineName: "Vitamin C", quantity: 5, dosageInstruction: "" }, // thiếu liều dùng
  ];

  const result = validateAndCleanMedicines(input);
  assert.equal(result.isValid, false);
  assert.ok(result.rowErrors["row_1"].medicineName);
  assert.ok(result.rowErrors["row_2"].quantity);
  assert.ok(result.rowErrors["row_3"].dosageInstruction);
});

test("validateAndCleanMedicines catches duplicate medicine names (case-insensitive)", () => {
  const input = [
    { _localId: "row_1", medicineName: "Paracetamol 500mg", quantity: 10, dosageInstruction: "Ngày 2 viên" },
    { _localId: "row_2", medicineName: "paracetamol 500mg", quantity: 5, dosageInstruction: "Sáng 1 viên" },
  ];

  const result = validateAndCleanMedicines(input);
  assert.equal(result.isValid, false);
  assert.ok(result.rowErrors["row_2"].medicineName);
  assert.match(result.rowErrors["row_2"].medicineName, /trùng lặp/);
});

test("validateAndCleanMedicines catches length overflow (>100 name, >200 dosage)", () => {
  const longName = "A".repeat(101);
  const longInstruction = "B".repeat(201);

  const input = [
    { _localId: "row_1", medicineName: longName, quantity: 1, dosageInstruction: "Ngày 1 viên" },
    { _localId: "row_2", medicineName: "Aspirin", quantity: 1, dosageInstruction: longInstruction },
  ];

  const result = validateAndCleanMedicines(input);
  assert.equal(result.isValid, false);
  assert.match(result.rowErrors["row_1"].medicineName, /100 ký tự/);
  assert.match(result.rowErrors["row_2"].dosageInstruction, /200 ký tự/);
});
