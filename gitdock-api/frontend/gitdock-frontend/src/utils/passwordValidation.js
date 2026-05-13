/**
 * Valide la force d'un mot de passe selon plusieurs critères.
 * @param {string} password - Le mot de passe à tester.
 * @returns {Object} - Un objet contenant isValid (boolean) et errors (array).
 */
export const validatePassword = (password) => {
    const errors = []

    // Critère 1 : Longueur minimale
    if (password.length < 8) {
        errors.push('Le mot de passe doit contenir au moins 8 caractères')
    }

    // Critère 2 : Au moins une majuscule
    if (!/[A-Z]/.test(password)) {
        errors.push('Le mot de passe doit contenir au moins une majuscule')
    }

    // Critère 3 : Au moins une minuscule
    if (!/[a-z]/.test(password)) {
        errors.push('Le mot de passe doit contenir au moins une minuscule')
    }

    // Critère 4 : Au moins un chiffre
    if (!/[0-9]/.test(password)) {
        errors.push('Le mot de passe doit contenir au moins un chiffre')
    }

    // Critère 5 : Au moins un caractère spécial
    if (!/[^A-Za-z0-9]/.test(password)) {
        errors.push('Le mot de passe doit contenir au moins un caractère spécial')
    }

    return {
        isValid: errors.length === 0,
        errors,
    }
}