import test from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import vm from "node:vm";

async function adapter(response) {
  const calls = [];
  const client = Object.fromEntries(["get", "post", "put", "patch", "delete"].map(method => [method, async (...args) => {
    calls.push({ method, args });
    return response;
  }]));
  const dependency = new vm.SyntheticModule(["default"], function () { this.setExport("default", client); });
  const source = await readFile(new URL("../src/api/adminApi.js", import.meta.url), "utf8");
  const module = new vm.SourceTextModule(source);
  await module.link(() => dependency);
  await module.evaluate();
  return { ...module.namespace, calls };
}

test("BE3 list envelope returns the complete account array", async () => {
  const records = [{ id: 1, username: "SV001", fullName: "Sinh viên", role: "SINH_VIEN", status: "ACTIVE" }];
  const { adminApi, calls } = await adapter({ success: true, data: records });
  assert.deepEqual(await adminApi.list("accounts"), records);
  assert.deepEqual(calls[0], { method: "get", args: ["/admin/users"] });
});

test("create and update send only BE3 DTO fields", async () => {
  const { adminApi, calls } = await adapter({ success: true, data: { id: 7 } });
  const fields = { username: "  SV001  ", fullName: "  Sinh viên  ", role: "SINH_VIEN", status: "LOCKED", password: "must-not-be-sent" };
  await adminApi.save("accounts", fields);
  await adminApi.save("accounts", { ...fields, id: 7 });
  assert.deepEqual(calls, [
    { method: "post", args: ["/admin/users", { username: "SV001", fullName: "Sinh viên", role: "SINH_VIEN" }] },
    { method: "put", args: ["/admin/users/7", { fullName: "Sinh viên", role: "SINH_VIEN" }] },
  ]);
});

test("lock/unlock and reset use separate endpoints; reset preserves message without data", async () => {
  const { adminApi, calls } = await adapter({ success: true, message: "Đã cấp lại mật khẩu" });
  await adminApi.setStatus({ id: 7 }, "LOCKED");
  await adminApi.setStatus({ id: 7 }, "ACTIVE");
  const result = await adminApi.resetPassword(7);
  assert.equal(result.message, "Đã cấp lại mật khẩu");
  assert.deepEqual(calls, [
    { method: "patch", args: ["/admin/users/7/status", { status: "LOCKED" }] },
    { method: "patch", args: ["/admin/users/7/status", { status: "ACTIVE" }] },
    { method: "post", args: ["/admin/users/7/reset-password"] },
  ]);
});

test("preserve backend validation, conflict and missing-account errors", async () => {
  const { errorMessage } = await adapter();
  for (const status of [400, 404, 409]) {
    assert.equal(errorMessage({ response: { status, data: { message: "Chi tiết từ BE3" } } }), "Chi tiết từ BE3");
  }
  assert.match(errorMessage({ code: "ERR_NETWORK" }), /Không thể kết nối/);
});

test("reject failure envelopes and unexpected paginated responses", async () => {
  const failure = await adapter({ success: false, message: "Không có quyền" });
  await assert.rejects(failure.adminApi.list("accounts"), /Không có quyền/);
  const paginated = await adapter({ success: true, data: { content: [] } });
  await assert.rejects(paginated.adminApi.list("accounts"), /chưa đúng định dạng/);
});

test("catalog payload and endpoint remain compatible", async () => {
  const { adminApi, calls } = await adapter({ data: { id: 2 } });
  await adminApi.save("faculties", { id: 2, code: "CNTT", name: "Công nghệ thông tin" });
  assert.deepEqual(calls[0], { method: "put", args: ["/catalogs/faculties/2", { code: "CNTT", name: "Công nghệ thông tin" }] });
});

for (const resource of ["faculties", "classes", "disease-groups", "medicines"]) {
  test(`catalog CRUD contract: ${resource}`, async () => {
    const { adminApi, calls } = await adapter({ success: true, data: [] });
    await adminApi.list(resource);
    await adminApi.save(resource, { code: "TEST", name: "Kiểm thử" });
    await adminApi.save(resource, { id: 10, code: "TEST", name: "Đã sửa" });
    await adminApi.remove(resource, 10);
    assert.deepEqual(calls.map(({ method, args }) => [method, args[0]]), [
      ["get", `/catalogs/${resource}`], ["post", `/catalogs/${resource}`],
      ["put", `/catalogs/${resource}/10`], ["delete", `/catalogs/${resource}/10`],
    ]);
  });
}

test("delete handles empty 204 response and rejects failure envelope", async () => {
  const empty = await adapter(undefined);
  assert.equal(await empty.adminApi.remove("faculties", 2), undefined);
  const rejected = await adapter({ success: false, message: "Khoa đang được sử dụng" });
  await assert.rejects(rejected.adminApi.remove("faculties", 2), /đang được sử dụng/);
});
