<template>
  <div class="auth-root">
    <!-- Arrière-plan avec formes décoratives -->
    <div class="auth-bg">
      <div class="shape-1"></div>
      <div class="shape-2"></div>
    </div>

    <!-- Côté Gauche : Panneau de Branding (Sombre) -->
    <div class="auth-left">
      <div class="auth-left-inner">
        <div class="brand">
          <div class="brand-logo">
            <svg width="20" height="20" viewBox="0 0 28 28" fill="none">
              <path d="M7 14C7 10.134 10.134 7 14 7C17.866 7 21 10.134 21 14C21 17.866 17.866 21 14 21" stroke="white" stroke-width="2.8" stroke-linecap="round"/>
              <circle cx="14" cy="14" r="2.5" fill="white"/>
              <path d="M14 21V24" stroke="white" stroke-width="2.8" stroke-linecap="round"/>
            </svg>
          </div>
          <span class="brand-name">GitDock</span>
        </div>

        <div class="left-main">

          <h1 class="left-title">Built for teams<br/>that <em>move fast.</em></h1>
          <p class="left-desc">Unified Git management, AI-powered security scanning, and real-time team collaboration.</p>

          <!-- Statistiques -->
          <div class="stats">
            <div class="stat" v-for="s in stats" :key="s.label">
              <strong>{{ s.value }}</strong>
              <span>{{ s.label }}</span>
            </div>
          </div>

          <!-- Liste des fonctionnalités -->
          <div class="features">
            <div class="feat" v-for="f in features" :key="f">
              <div class="feat-icon">
                <svg viewBox="0 0 12 12" fill="none" width="9" height="9">
                  <path d="M2 6l3 3 5-5" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              {{ f }}
            </div>
          </div>
        </div>


      </div>
    </div>

    <!-- Côté Droit : Conteneur du Formulaire -->
    <div class="auth-right">
      <div class="form-wrap">
        <!-- C'est ici que Login.vue ou Signup.vue sera injecté -->
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
const stats = [
  { value: '99.9%', label: 'Uptime' },
  { value: '< 1s',  label: 'Sync' },
  { value: '2.4k+', label: 'Teams' },
]

const features = [
  'Real-time Git synchronization',
  'AI-powered anomaly detection',
  'Role-based access control',
  'Built-in gamification engine',
]
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,400;0,500;0,600;0,700;0,800;1,400&family=Fraunces:ital,wght@1,300;1,400&display=swap');

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.auth-root {
  display: flex;
  min-height: 100vh;
  font-family: 'Plus Jakarta Sans', sans-serif;
  position: relative;
  overflow: hidden;
  background: #f8f7f4;
}

/* Background effects */
.auth-bg { position: fixed; inset: 0; pointer-events: none; z-index: 0; }
.shape-1 {
  position: absolute; border-radius: 50%; filter: blur(120px);
  width: 700px; height: 700px;
  background: radial-gradient(circle, rgba(91,19,236,0.07) 0%, transparent 70%);
  top: -200px; right: 100px;
}
.shape-2 {
  position: absolute; border-radius: 50%; filter: blur(100px);
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(16,185,129,0.06) 0%, transparent 70%);
  bottom: -100px; right: 200px;
}

/* ── Left panel ── */
.auth-left {
  position: relative; z-index: 1;
  width: 48%;
  background: #14111f;
  overflow: hidden;
}

.auth-left::before {
  content: '';
  position: absolute; top: -160px; left: -160px;
  width: 560px; height: 560px; border-radius: 50%;
  background: radial-gradient(circle, rgba(109,40,217,0.5) 0%, transparent 65%);
  pointer-events: none;
}

.auth-left::after {
  content: '';
  position: absolute; bottom: -100px; right: -100px;
  width: 400px; height: 400px; border-radius: 50%;
  background: radial-gradient(circle, rgba(16,185,129,0.18) 0%, transparent 65%);
  pointer-events: none;
}

.auth-left-inner {
  position: relative; z-index: 2;
  display: flex; flex-direction: column;
  height: 100%; padding: 44px 56px;
}

.brand { display: flex; align-items: center; gap: 10px; }

.brand-logo {
  width: 38px; height: 38px;
  background: linear-gradient(135deg, #6d28d9, #5b13ec);
  border-radius: 11px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 16px rgba(91,19,236,0.45);
}

.brand-name {
  font-size: 19px; font-weight: 800; color: #fff; letter-spacing: -0.4px;
}

.left-main { flex: 1; display: flex; flex-direction: column; justify-content: center; padding: 40px 0; }

.badge {
  display: inline-flex; align-items: center;
  font-size: 10px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase;
  color: rgba(167,139,250,0.85);
  background: rgba(167,139,250,0.1); border: 1px solid rgba(167,139,250,0.2);
  border-radius: 100px; padding: 5px 14px; margin-bottom: 24px; width: fit-content;
}

.left-title {
  font-size: clamp(34px, 3.5vw, 52px);
  font-weight: 800; color: #fff;
  line-height: 1.1; letter-spacing: -1.5px;
  margin-bottom: 18px;
}

.left-title em {
  font-family: 'Fraunces', serif; font-style: italic; font-weight: 300;
  background: linear-gradient(135deg, #a78bfa 0%, #34d399 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}

.left-desc {
  font-size: 14px; color: rgba(255,255,255,0.4); line-height: 1.7;
  max-width: 340px; margin-bottom: 36px;
}

.stats {
  display: flex; gap: 28px; margin-bottom: 32px;
  padding-bottom: 32px; border-bottom: 1px solid rgba(255,255,255,0.07);
}

.stat { display: flex; flex-direction: column; gap: 3px; }
.stat strong { font-size: 22px; font-weight: 800; color: #fff; letter-spacing: -0.5px; }
.stat span { font-size: 11px; color: rgba(255,255,255,0.3); font-weight: 500; letter-spacing: 0.3px; }

.features { display: flex; flex-direction: column; gap: 13px; }

.feat {
  display: flex; align-items: center; gap: 11px;
  font-size: 13px; color: rgba(255,255,255,0.55); font-weight: 500;
}

.feat-icon {
  width: 19px; height: 19px;
  background: linear-gradient(135deg, #6d28d9, #5b13ec);
  border-radius: 5px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

.left-foot {
  display: flex; align-items: center; gap: 12px;
  padding-top: 28px; border-top: 1px solid rgba(255,255,255,0.07);
}
.left-foot p { font-size: 12px; color: rgba(255,255,255,0.3); }
.left-foot p strong { color: rgba(255,255,255,0.55); font-weight: 600; }

.avatars { display: flex; }
.av {
  width: 28px; height: 28px; border-radius: 50%;
  border: 2px solid #14111f;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 700; color: #fff;
  margin-left: -7px;
}
.av:first-child { margin-left: 0; }

/* ── Right panel ── */
.auth-right {
  position: relative; z-index: 1;
  flex: 1; display: flex; align-items: center; justify-content: center;
  padding: 48px 48px;
  background: #f8f7f4;
}

.form-wrap {
  width: 100%; max-width: 400px;
  background: #fff;
  border-radius: 20px;
  padding: 44px 40px;
  border: 1px solid rgba(0,0,0,0.07);
  box-shadow: 0 2px 4px rgba(0,0,0,0.04), 0 12px 40px rgba(0,0,0,0.07), 0 40px 80px rgba(0,0,0,0.05);
}

@media (max-width: 900px) {
  .auth-root { flex-direction: column; }
  .auth-left { width: 100%; padding: 0; }
  .auth-left-inner { padding: 28px 24px; }
  .left-main { padding: 20px 0; }
  .left-desc, .features, .stats, .left-foot { display: none; }
  .left-title { font-size: 30px; }
  .auth-right { padding: 32px 16px; }
  .form-wrap { padding: 32px 24px; }
}
</style>