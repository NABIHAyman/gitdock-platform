// src/types/auth.js

/**
 * @typedef {Object} User
 * @property {number} id
 * @property {string} email
 * @property {string} firstName
 * @property {string} lastName
 * @property {string} [username]
 * @property {string} [avatarUrl]
 * @property {string} [role]
 */

/**
 * @typedef {Object} LoginCredentials
 * @property {string} email
 * @property {string} password
 */

/**
 * @typedef {Object} RegisterData
 * @property {string} firstName
 * @property {string} lastName
 * @property {string} email
 * @property {string} [username]
 */

/**
 * @typedef {Object} LoginResponse
 * @property {string} token
 * @property {User} user
 */

/**
 * @typedef {Object} ActivationData
 * @property {string} token
 * @property {string} password
 * @property {string} confirmPassword
 */

/**
 * @typedef {Object} ResetPasswordData
 * @property {string} token
 * @property {string} password
 * @property {string} confirmPassword
 */