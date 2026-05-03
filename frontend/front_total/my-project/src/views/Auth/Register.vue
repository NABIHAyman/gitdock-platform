<template>
  <div class="register-wrapper">
    <div class="register-card">

      <!-- LEFT -->
      <div class="register-left">
        <div class="logo">⚡ TaskFlow</div>

        <h1 class="title">Create your account 🚀</h1>

        <p class="subtitle">
          Join us and start managing your tasks efficiently.
        </p>

        <div class="features">
          <div>✔ Smart task tracking</div>
          <div>✔ Team collaboration</div>
          <div>✔ Real-time updates</div>
        </div>
      </div>

      <!-- RIGHT -->
      <div class="register-right">

        <h2 class="form-title">Register</h2>

        <v-form @submit.prevent="register">

          <v-text-field
            v-model="firstName"
            label="First Name"
            variant="outlined"
            class="mb-3"
            prepend-inner-icon="mdi-account"
          />

          <v-text-field
            v-model="lastName"
            label="Last Name"
            variant="outlined"
            class="mb-3"
            prepend-inner-icon="mdi-account"
          />

          <v-text-field
            v-model="email"
            label="Email"
            variant="outlined"
            class="mb-3"
            prepend-inner-icon="mdi-email"
          />

          <p v-if="error" style="color:red">{{ error }}</p>
          <p v-if="success" style="color:green">{{ success }}</p>

          <v-btn
            type="submit"
            block
            color="#1d4ed8"
            :loading="loading"
          >
            Create Account
          </v-btn>

        </v-form>

        <div class="login-link">
          Already have an account?
          <router-link to="/login" class="link">
            Login
          </router-link>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { registerUser } from "@/services/springAuthApi";

const firstName = ref("");
const lastName = ref("");
const email = ref("");

const loading = ref(false);
const error = ref("");
const success = ref("");

const register = async () => {
  error.value = "";
  success.value = "";

  if (!firstName.value || !lastName.value || !email.value) {
    error.value = "All fields are required";
    return;
  }

  loading.value = true;

  try {
    await registerUser({
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      username: email.value.split("@")[0]
    });

    success.value = "Account created! Check your email to activate it.";

    firstName.value = "";
    lastName.value = "";
    email.value = "";

  } catch (err) {
    error.value =
      err?.response?.data?.message ||
      "Registration failed";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2f5, #dbeafe);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 1000px;
  display: flex;
  border-radius: 20px;
  overflow: hidden;
  background: white;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08);
}

.register-left {
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
  font-size: 32px;
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
}

.register-right {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 26px;
  font-weight: bold;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  color: #64748b;
}

.link {
  color: #1d4ed8;
  text-decoration: none;
}
</style>