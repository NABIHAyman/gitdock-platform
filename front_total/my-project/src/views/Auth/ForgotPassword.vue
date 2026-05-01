<template>
  <div class="login-wrapper">
    <div class="login-card">

      <!-- LEFT SIDE -->
      <div class="login-left">
        <div class="logo">⚡ TaskFlow</div>

        <h1 class="title">Forgot Password ? 🔐</h1>

        <p class="subtitle">
          Don’t worry. We’ll help you reset your password.
        </p>

        <div class="features">
          <div>✔ Secure reset process</div>
          <div>✔ Quick email recovery</div>
          <div>✔ Safe & encrypted</div>
        </div>
      </div>

      <!-- RIGHT SIDE -->
      <div class="login-right">

        <h2 class="form-title">Reset Password</h2>
        <p class="form-subtitle">Enter your email address</p>

        <v-form @submit.prevent="sendReset">

          <v-text-field
            v-model="email"
            label="Email"
            type="email"
            variant="outlined"
            class="mb-4"
            prepend-inner-icon="mdi-email"
            required
          />

          <v-btn
            type="submit"
            block
            color="#1d4ed8"
            class="login-btn"
          >
            Send Reset Link
          </v-btn>

        </v-form>

        <!-- BACK TO LOGIN -->
        <div class="register-link">
          Remember your password?
          <router-link to="/login" class="link">
            Login
          </router-link>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const email = ref('')

const sendReset = () => {
  console.log('Reset email:', email.value)

  alert('Reset link sent to your email 📩')
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2f5, #dbeafe);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 1000px;
  display: flex;
  border-radius: 20px;
  overflow: hidden;
  background: white;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08);
}

/* LEFT */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: white;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 30px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 10px;
}

.subtitle {
  opacity: 0.9;
  margin-bottom: 25px;
}

.features div {
  font-size: 14px;
  margin-bottom: 10px;
  opacity: 0.95;
}

/* RIGHT */
.login-right {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 26px;
  font-weight: bold;
  color: #1e293b;
}

.form-subtitle {
  margin-bottom: 25px;
  color: #64748b;
  font-size: 14px;
}

/* BUTTON */
.login-btn {
  height: 45px;
  font-weight: bold;
  text-transform: none;
  border-radius: 10px;
}

/* LINKS */
.link {
  color: #1d4ed8;
  font-size: 13px;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

.register-link {
  margin-top: 20px;
  font-size: 13px;
  text-align: center;
  color: #64748b;
}

/* RESPONSIVE */
@media (max-width: 900px) {
  .login-card {
    flex-direction: column;
  }

  .login-left,
  .login-right {
    padding: 30px;
    text-align: center;
  }
}
</style>