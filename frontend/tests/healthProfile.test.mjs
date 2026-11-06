import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';
import { fromHealthResponse, toHealthRequest } from '../src/utils/healthProfile.js';
import { getHealthProfileCompletion } from '../src/utils/profileCompletion.js';
import { calculateBmi } from '../src/utils/bmi.js';
import { fromInsuranceResponse, toInsuranceRequest, validateInsuranceDraft, getInsuranceStatus } from '../src/utils/insurance.js';

test('new students receive an empty form, never another student fixture', () => {
  const form = fromHealthResponse(null);
  assert.ok(Object.values(form).every(value => value === ''));
  assert.equal(getHealthProfileCompletion(form).percent, 0);
  assert.equal(calculateBmi(form.height, form.weight), null);
});

test('maps API values and sends the exact update contract without student identity', () => {
  const form = fromHealthResponse({ height: 170, weight: 65, bloodType: 'O', allergies: ' Không có ', underlyingDiseases: 'Không', vaccinationHistory: 'Đủ mũi' });
  assert.equal(form.conditions, 'Không');
  assert.deepEqual(toHealthRequest(form), { height: 170, weight: 65, bloodType: 'O', allergies: 'Không có', underlyingDiseases: 'Không', vaccinationHistory: 'Đủ mũi' });
  assert.equal(getHealthProfileCompletion(form).percent, 100);
  assert.equal(toHealthRequest({ ...form, bloodGroup: '' }).bloodType, null);
});

test('BMI matches server rounding at classification boundaries', () => {
  assert.equal(calculateBmi(100, 24.96).value, 25);
  assert.equal(calculateBmi(100, 24.96).category.id, 'overweight');
  assert.equal(calculateBmi(100, 18.46).category.id, 'healthy');
  assert.equal(calculateBmi(100, 29.96).category.id, 'obesity');
});

test('validates and maps /api/health-insurances contract and calculates status', () => {
  const mapped = fromInsuranceResponse([
    {
      id: 9,
      studentId: 4,
      cardNumber: 'sv4790123456789',
      effectiveDate: '2026-01-01',
      expirationDate: '2026-12-31',
      initialHospital: 'Trung tâm Y tế Đại học',
      issueAt: 'TP. Hồ Chí Minh',
    },
  ], { issuedAt: 'TP. Hồ Chí Minh', benefits: 'Quyền lợi 80%' });

  assert.equal(mapped.id, 9);
  assert.equal(mapped.validFrom, '2026-01-01');
  assert.equal(mapped.validUntil, '2026-12-31');
  assert.equal(mapped.initialProvider, 'Trung tâm Y tế Đại học');
  assert.equal(mapped.issuedAt, 'TP. Hồ Chí Minh');
  assert.equal(validateInsuranceDraft(mapped), '');

  const req = toInsuranceRequest(mapped, 4);
  assert.deepEqual(req, {
    cardNumber: 'SV4790123456789',
    studentId: 4,
    effectiveDate: '2026-01-01',
    expirationDate: '2026-12-31',
    initialHospital: 'Trung tâm Y tế Đại học',
    issueAt: 'TP. Hồ Chí Minh',
  });

  assert.ok(validateInsuranceDraft({ ...mapped, cardNumber: '123' }).includes('10 chữ số hoặc 15 ký tự'));
  assert.ok(validateInsuranceDraft({ ...mapped, validFrom: '2026-12-31', validUntil: '2026-01-01' }).includes('Ngày bắt đầu hiệu lực'));
  assert.equal(getInsuranceStatus('2026-01-01', '2026-12-31', new Date('2026-06-01T00:00:00Z')).state, 'active');
  assert.throws(() => toInsuranceRequest(mapped, 0), /Không xác định được sinh viên/);
  assert.throws(() => toInsuranceRequest({}, undefined), /Không xác định được sinh viên/);
  assert.equal(fromInsuranceResponse(null).cardNumber, '');
  assert.equal(fromInsuranceResponse([]).validFrom, '');
});

test('API uses current-user endpoints, forwards abort signal and propagates failures', async () => {
  const calls = [];
  const failure = new Error('Network unavailable');
  const client = {
    get: async (...args) => { calls.push(['get', ...args]); return { data: null }; },
    post: async (...args) => { calls.push(['post', ...args]); return { data: { id: 1 } }; },
    put: async (...args) => { calls.push(['put', ...args]); throw failure; },
  };
  const context = vm.createContext({});
  const dependency = new vm.SyntheticModule(['default'], function () { this.setExport('default', client); }, { context });
  const source = await readFile(new URL('../src/api/healthProfileApi.js', import.meta.url), 'utf8');
  const module = new vm.SourceTextModule(source, { context });
  await module.link(() => dependency);
  await module.evaluate();
  const signal = new AbortController().signal;
  const api = module.namespace.healthProfileApi;
  assert.equal((await api.get(signal)).data, null);
  assert.equal(calls[0][1], '/health-profile');
  assert.equal(calls[0][2].signal, signal);
  assert.equal(calls[0][2].timeout, 15000);
  const payload = { height: 170, weight: 65 };
  await assert.rejects(api.save(payload), failure);
  assert.equal(calls[1][1], '/health-profile');
  assert.equal(calls[1][2], payload);
  assert.equal(calls[1][3].timeout, 15000);

  await api.getInsurance(signal);
  assert.equal(calls[2][1], '/health-insurances');
  await api.saveInsurance({ cardNumber: 'SV4790123456789' }, null);
  assert.equal(calls[3][0], 'post');
  assert.equal(calls[3][1], '/health-insurances');
  await assert.rejects(api.saveInsurance({ cardNumber: 'SV4790123456789' }, 9), failure);
  assert.equal(calls[4][0], 'put');
  assert.equal(calls[4][1], '/health-insurances/9');

  await api.getPersonalInfo(signal);
  assert.equal(calls[5][0], 'get');
  assert.equal(calls[5][1], '/health-profile/personal-info');
  assert.equal(calls[5][2].signal, signal);
  const personalInfo = { fullName: 'Sinh viên', dateOfBirth: '2005-02-14', phoneNumber: '0901234567', email: 'student@example.com', gender: 'Nữ' };
  await assert.rejects(api.savePersonalInfo(personalInfo), failure);
  assert.equal(calls[6][0], 'put');
  assert.equal(calls[6][1], '/health-profile/personal-info');
  assert.equal(calls[6][2], personalInfo);
  assert.equal(calls[6][3].timeout, 15000);
});
