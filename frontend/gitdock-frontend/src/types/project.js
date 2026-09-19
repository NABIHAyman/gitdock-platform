// src/types/project.js

// L'équivalent de l'enum TypeScript en JavaScript pur
export const ProjectPlatform = Object.freeze({
  GITHUB: 'GITHUB',
  GITLAB: 'GITLAB',
  BITBUCKET: 'BITBUCKET',
  AZURE: 'AZURE'
});

export const ProjectVisibility = {
    PUBLIC: 'PUBLIC',
    PRIVATE: 'PRIVATE'
};

/**
 * @typedef {Object} Project
 * @property {number} id
 * @property {string} name
 * @property {string} url
 * @property {string} platform
 * @property {string} createdAt
 * @property {string} [managedBy]
 * @property {User} [manager]
 */

/**
 * @typedef {Object} Branch
 * @property {number} id
 * @property {string} name
 * @property {string} creator
 * @property {string} lastUpdate
 * @property {number} projectId
 */

/**
 * @typedef {Object} Commit
 * @property {number} id
 * @property {string} sha
 * @property {string} message
 * @property {string} author
 * @property {string} date
 * @property {number} branchId
 */

/**
 * @typedef {Object} User
 * @property {number} id
 * @property {string} firstName
 * @property {string} lastName
 * @property {string} email
 * @property {string} role
 * @property {'active' | 'disabled'} [status]
 */

// ... Tu peux ajouter le reste des interfaces sous forme de @typedef si ton équipe en a besoin pour se repérer, mais ce n'est pas obligatoire pour que l'application tourne !