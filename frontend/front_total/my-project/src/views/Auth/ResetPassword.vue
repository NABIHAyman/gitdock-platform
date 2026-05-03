<template>
  <div class="auth-wrapper">
    <div class="auth-card">

      <!-- LEFT -->
      <div class="auth-left">
        <div class="logo">⚡ TaskFlow</div>

        <h1 class="title">Reset Password</h1>

        <p class="subtitle">
          Create a new secure password for your account
        </p>

        <div class="features">
          <div>✔ Secure reset process</div>
          <div>✔ Strong password protection</div>
          <div>✔ Instant access recovery</div>
        </div>
      </div>

      <!-- RIGHT -->
      <div class="auth-right">

        <!-- SUCCESS -->
        <div v-if="isSuccess" class="text-center">
          <v-icon size="60" color="green">mdi-check-circle</v-icon>

          <h2 class="mt-3">Password Reset Successful</h2>

          <p class="text-grey mt-2">
            Redirecting to login...
          </p>

          <v-btn class="mt-4" color="#1d4ed8" to="/login">
            Go to Login
          </v-btn>
        </div>

        <!-- ERROR -->
        <div v-else-if="errorMessage" class="text-center">
          <v-icon size="60" color="red">mdi-close-circle</v-icon>

          <h2 class="mt-3">Error</h2>

          <p class="text-grey">
            {{ errorMessage }}
          </p>

          <v-btn class="mt-4" color="#1d4ed8" to="/login">
            Back to Login
          </v-btn>
        </div>

        <!-- FORM -->
        <div v-else>

          <h2 class="mb-2">Set New Password</h2>
          <p class="mb-4 text-grey">
            Enter your new password below
          </p>

          <v-form @submit.prevent="handleSubmit">

            <v-text-field
              v-model="form.password"
              label="New Password"
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
              :disabled="!token"
            >
              Reset Password
            </v-btn>

            <div class="text-center mt-4">
              <router-link to="/login" class="link">
                Back to login
              </router-link>
            </div>

          </v-form>

        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { confirmResetPassword } from "@/services/springAuthApi";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const isSuccess = ref(false);
const errorMessage = ref("");
const token = ref("");

const form = reactive({
  password: "",
  confirm: ""
});

const errors = reactive({
  password: "",
  confirm: ""
});

// GET TOKEN FROM URL
onMounted(() => {
  token.value = route.query.token;

  if (!token.value) {
    errorMessage.value = "Missing reset token";
  }
});

// VALIDATION
const validate = () => {
  errors.password = "";
  errors.confirm = "";

  let ok = true;

  if (!form.password) {
    errors.password = "Password required";
    ok = false;
  }

  if (form.password.length < 6) {
    errors.password = "Minimum 6 characters";
    ok = false;
  }

  if (!form.confirm) {
    errors.confirm = "Confirmation required";
    ok = false;
  }

  if (form.password !== form.confirm) {
    errors.confirm = "Passwords do not match";
    ok = false;
  }

  return ok;
};

// SUBMIT → BACKEND CALL
const handleSubmit = async () => {
  if (!validate()) return;

  loading.value = true;
  errorMessage.value = "";

  try {
    await confirmResetPassword(token.value, form.password);

    isSuccess.value = true;

    setTimeout(() => {
      router.push("/login");
    }, 1500);

  } catch (err) {
    errorMessage.value =
      err?.response?.data?.message || "Reset password failed";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
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

.link {
  color: #1d4ed8;
  text-decoration: none;
}
.link:hover {
  text-decoration: underline;
}
</style>