<template>
  <div class="login">
    <div class="login-head">
      <h2 class="login-title">Welcome back</h2>
      <p class="login-sub">Sign in to your GitDock workspace.</p>
    </div>

    <!-- Activation warning -->
    <div v-if="showActivationMessage" class="activation-warn">
      <svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15" style="flex-shrink:0;color:#d97706">
        <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
      </svg>
      <div>
        <p>Account not yet activated.</p>
        <button @click="handleResendActivation" :disabled="isResendingActivation">
          {{ isResendingActivation ? 'Sending...' : 'Resend activation email →' }}
        </button>
      </div>
    </div>

    <div class="form-body">
      <div class="field" :class="{ 'field--error': errors.email }">
        <label>Email address</label>
        <div class="input-box">
          <svg class="ico" viewBox="0 0 20 20" fill="currentColor">
            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
          </svg>
          <input v-model="formData.email" type="email" placeholder="you@company.com"
                 @focus="errors.email = ''" @keyup.enter="handleSubmit" />
        </div>
        <p v-if="errors.email" class="err">{{ errors.email }}</p>
      </div>

      <div class="field" :class="{ 'field--error': errors.password }">
        <div class="label-row">
          <label>Password</label>
          <router-link to="/auth/forgot-password" class="forgot">Forgot password?</router-link>
        </div>
        <div class="input-box">
          <svg class="ico" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/>
          </svg>
          <input v-model="formData.password" :type="showPwd ? 'text' : 'password'"
                 placeholder="••••••••" @focus="errors.password = ''" @keyup.enter="handleSubmit" />
          <button class="eye" type="button" @click="showPwd = !showPwd">
            <svg v-if="showPwd" viewBox="0 0 20 20" fill="currentColor" width="15" height="15">
              <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
              <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>
            </svg>
            <svg v-else viewBox="0 0 20 20" fill="currentColor" width="15" height="15">
              <path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/>
              <path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/>
            </svg>
          </button>
        </div>
        <p v-if="errors.password" class="err">{{ errors.password }}</p>
      </div>

      <button class="btn-submit" :disabled="isLoading" @click="handleSubmit">
        <span v-if="isLoading" class="spin"></span>
        <span v-else>Sign in</span>
        <svg v-if="!isLoading" viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"/>
        </svg>
      </button>

      <div class="divider"><span>or</span></div>

      <div class="signup-row">
        <span>No account yet?</span>
        <router-link to="/signup">Create one →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const formData = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const showPwd = ref(false)
const isLoading = ref(false)
const showActivationMessage = ref(false)
const isResendingActivation = ref(false)

const handleSubmit = async () => {
  errors.email    = !formData.email.includes('@') ? 'Enter a valid email' : ''
  errors.password = !formData.password ? 'Password is required' : ''
  if (errors.email || errors.password) return

  isLoading.value = true
  showActivationMessage.value = false
  try {
    const result = await authStore.login({ email: formData.email, password: formData.password })
    if (!result.success && result.needsActivation) showActivationMessage.value = true
  } finally {
    isLoading.value = false
  }
}

const handleResendActivation = async () => {
  if (!formData.email) return
  isResendingActivation.value = true
  try { await authStore.resendActivationEmail(formData.email) }
  finally { isResendingActivation.value = false }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.login { font-family: 'Plus Jakarta Sans', sans-serif; }

.login-head { margin-bottom: 28px; }
.login-title { font-size: 26px; font-weight: 800; color: #0f0a1e; letter-spacing: -0.6px; margin-bottom: 6px; }
.login-sub { font-size: 14px; color: #9ca3af; }

/* Activation warning */
.activation-warn {
  display: flex; align-items: flex-start; gap: 10px;
  background: #fffbeb; border: 1px solid #fde68a;
  border-radius: 10px; padding: 12px 14px;
  margin-bottom: 16px;
}
.activation-warn p { font-size: 13px; color: #92400e; font-weight: 500; margin-bottom: 6px; }
.activation-warn button {
  font-size: 12px; color: #d97706; font-weight: 600;
  background: none; border: none; cursor: pointer; padding: 0;
  text-decoration: underline;
}
.activation-warn button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Form */
.form-body { display: flex; flex-direction: column; gap: 16px; }

.label-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 7px; }
.label-row label { margin-bottom: 0; }

.field label {
  display: block; font-size: 11px; font-weight: 600;
  color: #374151; letter-spacing: 0.2px; margin-bottom: 7px; text-transform: uppercase;
}

.forgot { font-size: 12px; color: #5b13ec; text-decoration: none; font-weight: 600; }
.forgot:hover { color: #4a0fd4; }

.input-box { position: relative; }

.ico {
  position: absolute; left: 13px; top: 50%; transform: translateY(-50%);
  width: 15px; height: 15px; color: #d1d5db; pointer-events: none;
}

.eye {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  background: none; border: none; cursor: pointer; color: #9ca3af; padding: 2px;
  display: flex; align-items: center;
}
.eye:hover { color: #6b7280; }

.input-box input {
  width: 100%; background: #f9fafb;
  border: 1.5px solid #e5e7eb; border-radius: 10px;
  padding: 12px 38px 12px 38px;
  font-size: 14px; color: #111827;
  font-family: 'Plus Jakarta Sans', sans-serif;
  transition: border-color 0.18s, box-shadow 0.18s, background 0.18s;
  outline: none;
}
.input-box input::placeholder { color: #d1d5db; }
.input-box input:focus {
  border-color: #5b13ec; background: #fff;
  box-shadow: 0 0 0 3px rgba(91,19,236,0.08);
}
.field--error .input-box input { border-color: #f87171; background: #fff5f5; }

.err { font-size: 12px; color: #ef4444; margin-top: 5px; font-weight: 500; }

/* Submit */
.btn-submit {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%; padding: 14px 20px;
  background: #0f0a1e; color: #fff;
  border: none; border-radius: 12px;
  font-size: 14px; font-weight: 700;
  font-family: 'Plus Jakarta Sans', sans-serif;
  cursor: pointer; letter-spacing: 0.1px;
  transition: background 0.18s, transform 0.12s, box-shadow 0.18s;
  box-shadow: 0 4px 16px rgba(15,10,30,0.2);
  margin-top: 4px;
}
.btn-submit:hover:not(:disabled) { background: #1e1535; transform: translateY(-1px); box-shadow: 0 8px 24px rgba(15,10,30,0.25); }
.btn-submit:active:not(:disabled) { transform: translateY(0); }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.spin { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.divider { display: flex; align-items: center; gap: 12px; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #f3f4f6; }
.divider span { font-size: 12px; color: #d1d5db; font-weight: 500; }

.signup-row { display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 14px; }
.signup-row span { color: #9ca3af; }
.signup-row a { color: #5b13ec; text-decoration: none; font-weight: 600; }
.signup-row a:hover { color: #4a0fd4; }
</style>