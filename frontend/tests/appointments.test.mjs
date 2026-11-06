import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';
import { canChangeAppointment, filterBookingShifts, monthRange, moveMonth, normalizeShift, normalizeStatus, overlapsAppointment, requiredReason, slotUnavailableReason, todayInVietnam } from '../src/utils/appointments.js';

const now = new Date('2030-01-10T00:00:00Z');
const slot = normalizeShift({ id: 20, doctorId: 7, shiftDate: '2030-01-11', startTime: '09:00:00', endTime: '09:30:00', maxSlots: 10, bookedSlots: 2, status: 'OPEN' });
const appointment = { id: 4, shiftId: 18, shiftDate: slot.shiftDate, startTime: '09:15:00', endTime: '10:00:00', status: 'CONFIRMED' };

test('calendar respects Vietnam midnight, leap year and year rollover', () => {
  assert.equal(todayInVietnam(new Date('2026-11-05T18:00:00Z')), '2026-11-06');
  assert.deepEqual(monthRange('2028-02'), { fromDate: '2028-02-01', toDate: '2028-02-29' });
  assert.equal(moveMonth('2026-12', 1), '2027-01');
  assert.equal(moveMonth('2027-01', -1), '2026-12');
});

test('normalizes backend shift aliases and never invents availability', () => {
  assert.equal(slot.availableSlots, 8);
  const legacy = normalizeShift({ staffId: 3, shiftType: 'TU_VAN_TAM_LY', maxQuantity: 5, bookedQuantity: 5 });
  assert.equal(legacy.doctorId, 3);
  assert.equal(legacy.serviceType, 'TU_VAN_TAM_LY');
  assert.equal(legacy.availableSlots, 0);
  assert.equal(normalizeShift({}).availableSlots, 0);
});

test('full, closed and past shifts cannot be selected even when counts are stale', () => {
  assert.equal(slotUnavailableReason(slot, [], { now }), '');
  assert.equal(slotUnavailableReason({ ...slot, status: 'FULL' }, [], { now }), 'Đã kín lịch');
  assert.equal(slotUnavailableReason({ ...slot, bookedSlots: 10 }, [], { now }), 'Đã kín lịch');
  assert.equal(slotUnavailableReason({ ...slot, status: 'ENDED' }, [], { now }), 'Đã đóng');
  assert.equal(slotUnavailableReason(slot, [], { now: new Date('2030-01-11T02:00:00Z') }), 'Đã qua giờ');
});

test('overlap uses time intervals, ignores canceled appointments and allows adjacent slots', () => {
  assert.equal(overlapsAppointment(slot, [appointment]), true);
  assert.equal(overlapsAppointment(slot, [{ ...appointment, status: 'CANCELED' }]), false);
  assert.equal(overlapsAppointment(slot, [{ ...appointment, startTime: '09:30:00' }]), false);
  assert.equal(overlapsAppointment(slot, [{ ...appointment, shiftDate: '2030-01-12' }]), false);
  assert.equal(slotUnavailableReason(slot, [appointment], { now }), 'Trùng lịch');
});

test('rescheduling excludes itself but not another overlapping appointment', () => {
  assert.equal(slotUnavailableReason(slot, [appointment], { now, excludedAppointmentId: 4 }), '');
  assert.equal(slotUnavailableReason(slot, [appointment, { ...appointment, id: 5 }], { now, excludedAppointmentId: 4 }), 'Trùng lịch');
  assert.equal(slotUnavailableReason(slot, [], { now, excludedShiftId: 20 }), 'Ca hiện tại');
});

test('finder filters facility and availability without inventing facilities or hiding full slots when unchecked', () => {
  const open = { ...slot, facilityId: 1 };
  const full = { ...slot, id: 21, facilityId: 1, status: 'FULL' };
  const otherFacility = { ...slot, id: 22, facilityId: 2 };
  const rows = [open, full, otherFacility];
  assert.deepEqual(filterBookingShifts(rows, { facilityId: '1', onlyAvailable: true }, [], now), [open]);
  assert.deepEqual(filterBookingShifts(rows, { facilityId: '1' }, [], now), [open, full]);
  assert.deepEqual(filterBookingShifts(rows, { onlyAvailable: true }, [appointment], now), []);
  assert.deepEqual(filterBookingShifts(rows, { facilityId: '3' }, [], now), []);
});

test('cancel and reschedule require a reason; terminal and started appointments have no actions', () => {
  assert.ok(requiredReason('   '));
  assert.ok(requiredReason('a'.repeat(501)));
  assert.equal(requiredReason(' Bận lịch thi '), '');
  assert.equal(normalizeStatus('DA_XAC_NHAN'), 'CONFIRMED');
  assert.equal(canChangeAppointment(appointment, now), true);
  assert.equal(canChangeAppointment({ ...appointment, status: 'COMPLETED' }, now), false);
  assert.equal(canChangeAppointment(appointment, new Date('2030-01-12')), false);
});

test('appointment API matches backend contracts, uses JWT ownership and preserves failure', async () => {
  const calls = [];
  let response = { success: true, data: [] };
  let failure;
  const client = Object.fromEntries(['get', 'post', 'put'].map(method => [method, async (...args) => {
    calls.push([method, ...args]);
    if (failure) throw failure;
    return response;
  }]));
  const context = vm.createContext({});
  const axiosModule = new vm.SyntheticModule(['default'], function () { this.setExport('default', client); }, { context });
  const utilsModule = new vm.SourceTextModule(await readFile(new URL('../src/utils/appointments.js', import.meta.url), 'utf8'), { context });
  const module = new vm.SourceTextModule(await readFile(new URL('../src/api/appointmentApi.js', import.meta.url), 'utf8'), { context });
  await utilsModule.link(() => { throw new Error('Unexpected import'); });
  await module.link(name => name.includes('axiosClient') ? axiosModule : utilsModule);
  await module.evaluate();
  const api = module.namespace.appointmentApi;
  const signal = new AbortController().signal;
  await api.mine(signal);
  assert.equal(calls[0][1], '/appointments/my-appointments');
  assert.equal(calls[0][2].signal, signal);
  await api.shifts({ date: '2030-01-11', serviceType: 'KHAM_DA_KHOA' }, signal);
  assert.equal(calls[1][1], '/shifts');
  assert.equal(calls[1][2].params.date, '2030-01-11');
  response = { success: true, data: { id: 4, appointmentCode: 'LH-4' } };
  await api.create(20, '  Đau đầu  ');
  assert.equal(calls[2][1], '/appointments');
  assert.equal(JSON.stringify(calls[2][2]), JSON.stringify({ shiftId: 20, reasonSymptoms: 'Đau đầu' }));
  await api.cancel(4, '  Bận thi  ');
  assert.equal(calls[3][1], '/appointments/4/cancel');
  assert.equal(calls[3][2].reason, 'Bận thi');
  await api.reschedule(4, 21, ' Bận học ');
  assert.equal(calls[4][1], '/appointments/4/reschedule');
  assert.equal(calls[4][2].newShiftId, 21);
  await assert.rejects(api.cancel(4, ' '), /Vui lòng nhập lý do/);
  assert.equal(calls.length, 5);
  response = { success: false, message: 'Ca khám đã đầy' };
  await assert.rejects(api.create(20, 'Đau đầu'), /Ca khám đã đầy/);
  response = { success: true, data: null };
  await assert.rejects(api.create(20, 'Đau đầu'), /chưa trả thông tin lịch hẹn đã lưu/);
  response = { success: true, data: {} };
  await assert.rejects(api.mine(signal), /danh sách không đúng định dạng/);
  failure = { response: { status: 405 } };
  await assert.rejects(api.create(20, 'Đau đầu'), /chưa sẵn sàng/);
  failure = new Error('Network unavailable');
  await assert.rejects(api.reschedule(4, 21, 'Bận học'), failure);
  failure = undefined;
  response = { success: true, data: [{ id: 1, name: 'Cơ sở kiểm thử' }] };
  const facilities = await api.facilities(signal);
  assert.equal(facilities[0].id, 1);
  assert.equal(calls.at(-1)[1], '/facilities');
  assert.equal(calls.at(-1)[2].signal, signal);
});
