<template>
  <div class="reg">
    <div class="reg-head">
      <h2 class="reg-title">Create account</h2>
      <p class="reg-sub">Start managing your projects smarter.</p>
    </div>

    <div class="form-body">
      <div class="row-2">
        <div class="field" :class="{ 'field--error': errors.first_name }">
          <label>First name</label>
          <div class="input-box">
            <input v-model="formData.firstName" type="text" placeholder="Prénom" @focus="errors.first_name = ''" />
          </div>
          <p v-if="errors.first_name" class="err">{{ errors.first_name }}</p>
        </div>

        <div class="field" :class="{ 'field--error': errors.last_name }">
          <label>Last name</label>
          <div class="input-box">
            <input v-model="formData.lastName" type="text" placeholder="Nom" @focus="errors.last_name = ''" />
          </div>
          <p v-if="errors.last_name" class="err">{{ errors.last_name }}</p>
        </div>
      </div>

      <div class="field" :class="{ 'field--error': errors.email }">
        <label>Email address</label>
        <div class="input-box">
          <svg class="ico" viewBox="0 0 20 20" fill="currentColor">
            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
          </svg>
          <input v-model="formData.email" type="email" placeholder="you@company.com" @focus="errors.email = ''" @keyup.enter="handleSubmit" />
        </div>
        <p v-if="errors.email" class="err">{{ errors.email }}</p>
      </div>

      <button class="btn-submit" :disabled="isLoading" @click="handleSubmit">
        <span v-if="isLoading" class="spin"></span>
        <span v-else>Create account</span>
        <svg v-if="!isLoading" viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"/>
        </svg>
      </button>

      <div class="divider"><span>or</span></div>

      <div class="signin-row">
        <span>Already have an account?</span>
        <router-link to="/login">Sign in →</router-link>
      </div>
    </div>

    <p class="terms">
      By creating an account you agree to our
      <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a>.
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const formData = reactive({ firstName: '', lastName: '', email: '' })
const errors = reactive({ first_name: '', last_name: '', email: '' })
const isLoading = ref(false)

const handleSubmit = async () => {
  errors.first_name = !formData.firstName.trim() ? 'Required' : ''
  errors.last_name  = !formData.lastName.trim()  ? 'Required' : ''
  errors.email      = !formData.email.includes('@') ? 'Enter a valid email' : ''
  if (errors.first_name || errors.last_name || errors.email) return

  isLoading.value = true
  try {
    const result = await authStore.signup({
      firstName: formData.firstName.trim(),
      lastName:  formData.lastName.trim(),
      email:     formData.email.trim(),
    })
    if (!result?.success) isLoading.value = false
  } catch { isLoading.value = false }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.reg { font-family: 'Plus Jakarta Sans', sans-serif; }

.reg-head { margin-bottom: 28px; }

.reg-title {
  font-size: 26px; font-weight: 800; color: #0f0a1e;
  letter-spacing: -0.6px; margin-bottom: 6px;
}

.reg-sub { font-size: 14px; color: #9ca3af; font-weight: 400; }

/* Form */
.form-body { display: flex; flex-direction: column; gap: 16px; }

.row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.field label {
  display: block; font-size: 12px; font-weight: 600;
  color: #374151; letter-spacing: 0.2px; margin-bottom: 7px;
  text-transform: uppercase; font-size: 11px;
}

.input-box {
  position: relative;
}

.ico {
  position: absolute; left: 13px; top: 50%;
  transform: translateY(-50%);
  width: 15px; height: 15px; color: #d1d5db; pointer-events: none;
}

.input-box input {
  width: 100%;
  background: #f9fafb;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px 13px;
  font-size: 14px; color: #111827;
  font-family: 'Plus Jakarta Sans', sans-serif;
  transition: border-color 0.18s, box-shadow 0.18s, background 0.18s;
  outline: none;
}

/* Input with icon */
.input-box:has(.ico) input { padding-left: 38px; }

.input-box input::placeholder { color: #d1d5db; }

.input-box input:focus {
  border-color: #5b13ec;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(91,19,236,0.08);
}

.field--error .input-box input {
  border-color: #f87171;
  background: #fff5f5;
}

.err { font-size: 12px; color: #ef4444; margin-top: 5px; font-weight: 500; }

/* Submit */
.btn-submit {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%; padding: 14px 20px;
  background: #0f0a1e;
  color: #fff;
  border: none; border-radius: 12px;
  font-size: 14px; font-weight: 700;
  font-family: 'Plus Jakarta Sans', sans-serif;
  cursor: pointer; letter-spacing: 0.1px;
  transition: background 0.18s, transform 0.12s, box-shadow 0.18s;
  box-shadow: 0 4px 16px rgba(15,10,30,0.2), 0 1px 2px rgba(0,0,0,0.08);
  margin-top: 4px;
}

.btn-submit:hover:not(:disabled) {
  background: #1e1535;
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(15,10,30,0.25);
}

.btn-submit:active:not(:disabled) { transform: translateY(0); }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.spin {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Divider */
.divider {
  display: flex; align-items: center; gap: 12px;
  color: #e5e7eb;
}
.divider::before, .divider::after {
  content: ''; flex: 1; height: 1px; background: #f3f4f6;
}
.divider span { font-size: 12px; color: #d1d5db; font-weight: 500; }

/* Sign in row */
.signin-row {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  font-size: 14px;
}
.signin-row span { color: #9ca3af; }
.signin-row a { color: #5b13ec; text-decoration: none; font-weight: 600; }
.signin-row a:hover { color: #4a0fd4; }

/* Terms */
.terms {
  margin-top: 20px; font-size: 11px; color: #d1d5db;
  text-align: center; line-height: 1.6;
}
.terms a { color: #9ca3af; text-decoration: none; }
.terms a:hover { color: #6b7280; }

@media (max-width: 480px) {
  .row-2 { grid-template-columns: 1fr; }
}
</style>