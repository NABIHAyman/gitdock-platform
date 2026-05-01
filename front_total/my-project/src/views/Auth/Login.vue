<template>
  <div class="auth-wrapper">

    <div class="auth-card">

      <!-- ================= LEFT SIDE ================= -->
      <div class="auth-left">

        <div class="brand">⚡ GitDock</div>

        <h1 class="headline">
          Manage your work<br />
          like a pro
        </h1>

        <p class="description">
          Organize tasks, collaborate with your team, and track progress in real time.
        </p>

        <div class="stats">
          <div class="stat">
            <h3>Fast</h3>
            <p>JWT Auth</p>
          </div>

          <div class="stat">
            <h3>Secure</h3>
            <p>Spring Boot</p>
          </div>

          <div class="stat">
            <h3>Smart</h3>
            <p>Roles system</p>
          </div>
        </div>

      </div>

      <!-- ================= RIGHT SIDE ================= -->
      <div class="auth-right">

        <div class="form-box">

          <h2>Welcome back</h2>
          <p class="sub">Login to continue</p>

          <!-- ERROR -->
          <v-alert
            v-if="errorMessage"
            type="error"
            class="mb-3"
            density="compact"
          >
            {{ errorMessage }}
          </v-alert>

          <!-- FORM -->
          <v-form @submit.prevent="login">

            <v-text-field
              v-model="email"
              label="Email"
              type="email"
              variant="outlined"
              prepend-inner-icon="mdi-email"
              class="input"
              required
            />

            <v-text-field
              v-model="password"
              label="Password"
              type="password"
              variant="outlined"
              prepend-inner-icon="mdi-lock"
              class="input"
              required
            />

            <!-- FORGOT PASSWORD -->
            <div class="actions">
              <router-link to="/forgot-password">
                Forgot password?
              </router-link>
            </div>

            <!-- BUTTON -->
            <v-btn
              :loading="loading"
              :disabled="loading"
              type="submit"
              class="btn"
              block
            >
              Sign in
            </v-btn>

          </v-form>

          <!-- REGISTER -->
          <p class="footer">
            Don't have an account?
            <router-link to="/register">Create account</router-link>
          </p>

        </div>

      </div>

    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginUser } from '@/services/springAuthApi'

const router = useRouter()

const email = ref('')
const password = ref('')

const loading = ref(false)
const errorMessage = ref('')

/* ================= LOGIN ================= */
const login = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await loginUser(email.value, password.value)

    console.log('LOGIN SUCCESS:', data)

    // 💾 STORE JWT
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('user', JSON.stringify(data.user))

    // 🚀 REDIRECT
    router.push('/dashboard')

  } catch (error: any) {
    console.error(error)

    errorMessage.value =
      error?.response?.data?.message ||
      'Invalid email or password'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ================= WRAPPER ================= */
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at top, #0f172a, #020617);
}

/* ================= CARD ================= */
.auth-card {
  width: 1000px;
  height: 600px;
  display: flex;
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255,255,255,0.05);
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 60px rgba(0,0,0,0.6);
}

/* ================= LEFT ================= */
.auth-left {
  flex: 1;
  padding: 60px;
  color: white;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 40px;
}

.headline {
  font-size: 36px;
  font-weight: 800;
  line-height: 1.2;
}

.description {
  margin-top: 15px;
  opacity: 0.85;
}

/* ================= STATS ================= */
.stats {
  display: flex;
  gap: 20px;
  margin-top: 40px;
}

.stat h3 {
  margin: 0;
  font-size: 18px;
}

.stat p {
  font-size: 12px;
  opacity: 0.8;
}

/* ================= RIGHT ================= */
.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f172a;
}

.form-box {
  width: 80%;
  color: white;
}

.form-box h2 {
  font-size: 28px;
}

.sub {
  font-size: 13px;
  opacity: 0.7;
  margin-bottom: 20px;
}

/* ================= INPUT ================= */
.input {
  margin-bottom: 15px;
}

/* ================= BUTTON ================= */
.btn {
  height: 45px;
  font-weight: bold;
  border-radius: 10px;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  color: white;
}

/* ================= LINKS ================= */
.actions {
  display: flex;
  justify-content: flex-end;
  font-size: 12px;
  margin-bottom: 10px;
  opacity: 0.8;
}

.footer {
  margin-top: 15px;
  font-size: 12px;
  opacity: 0.7;
}
</style>