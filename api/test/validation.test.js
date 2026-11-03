const test = require('node:test');
const assert = require('node:assert/strict');
const { isValidEmail, isValidPassword } = require('../validation');

test('valid email is accepted', () => {
  assert.equal(isValidEmail('runner@example.com'), true);
});

test('invalid email is rejected', () => {
  assert.equal(isValidEmail('runner@'), false);
});

test('strong password is accepted', () => {
  assert.equal(isValidPassword('Password@123'), true);
});

test('weak password is rejected', () => {
  assert.equal(isValidPassword('password'), false);
});
