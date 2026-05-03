<template>
  <div class="auth-wrapper">
    <div class="auth-card">

      <!-- LEFT -->
      <div class="auth-left">
        <div class="logo">⚡ TaskFlow</div>

        <h1 class="title">Account Activation</h1>

        <p class="subtitle">
          Secure your account and set your password
        </p>

        <div class="features">
          <div>✔ Secure access</div>
          <div>✔ Quick setup</div>
          <div>✔ Ready to use</div>
        </div>
      </div>

      <!-- RIGHT -->
      <div class="auth-right">

        <!-- LOADING -->
        <div v-if="state === 'loading'" class="text-center">
          <v-progress-circular indeterminate color="#1d4ed8" />
          <p class="mt-4">Checking token...</p>
        </div>

        <!-- ERROR -->
        <div v-else-if="state === 'error'" class="text-center">
          <v-icon size="60" color="red">mdi-close-circle</v-icon>

          <h2 class="mt-3">Activation Error</h2>

          <p class="text-grey">{{ errorMessage }}</p>

          <v-btn class="mt-4" color="#1d4ed8" to="/login">
            Back to Login
          </v-btn>
        </div>

        <!-- FORM -->
        <div v-else>

          <h2 class="mb-2">Set Password</h2>
          <p class="mb-4 text-grey">Create your password to activate account</p>

          <v-form @submit.prevent="handleSubmit">

            <v-text-field
              v-model="form.password"
              label="Password"
              type="password"
              variant="outlined"
              class="mb-3"
              prepend-inner-icon="mdi-lock"
              :error-messages="errors.password"
            />

            <v-text-field
              v-model="form.confirm"
              label="Confirm Password"
              type="password"
              variant="outlined"
              class="mb-4"
              prepend-inner-icon="mdi-lock-check"
              :error-messages="errors.confirm"
            />

            <v-btn
              type="submit"
              block
              color="#1d4ed8"
              :loading="loading"
            >
              Activate Account
            </v-btn>

          </v-form>

        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const state = ref('loading') // loading | form | error
const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  password: '',
  confirm: ''
})

const errors = reactive({
  password: '',
  confirm: ''
})

// fake token check (on test)
onMounted(() => {
  const token = route.query.token

  if (!token) {
    state.value = 'error'
    errorMessage.value = 'Missing activation token'
  } else {
    state.value = 'form'
  }
})

const validate = () => {
  errors.password = ''
  errors.confirm = ''

  let ok = true

  if (!form.password) {
    errors.password = 'Password required'
    ok = false
  }

  if (form.password.length < 6) {
    errors.password = 'Min 6 characters'
    ok = false
  }

  if (!form.confirm) {
    errors.confirm = 'Confirm required'
    ok = false
  }

  if (form.password !== form.confirm) {
    errors.confirm = 'Passwords do not match'
    ok = false
  }

  return ok
}

const handleSubmit = () => {
  if (!validate()) return

  loading.value = true

  setTimeout(() => {
    state.value = 'success'
    router.push('/login')
  }, 1500)
}
</script>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #eef2ff;
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 900px;
  display: flex;
  background: white;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.1);
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: white;
  padding: 50px;
}

.auth-right {
  flex: 1;
  padding: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.logo {
  font-weight: bold;
  font-size: 20px;
  margin-bottom: 30px;
}

.title {
  font-size: 28px;
  margin-bottom: 10px;
}

.subtitle {
  opacity: 0.9;
  margin-bottom: 25px;
}

.features div {
  margin-bottom: 8px;
  font-size: 14px;
}
</style>