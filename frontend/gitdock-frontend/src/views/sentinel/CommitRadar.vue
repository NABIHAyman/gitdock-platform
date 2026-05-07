<template>
  <div class="sentinel-shell">

    <!-- ═══════════════════════════ HEADER ═══════════════════════════ -->
    <header class="sentinel-header">
      <div class="logo-block">

        <router-link to="/dashboard/home" class="mr-4 text-slate-400 hover:text-white transition-colors" title="Retour à GitDock">
          <v-icon icon="mdi-arrow-left" size="24"></v-icon>
        </router-link>

        <span class="logo-icon">⬡</span>
        <div>
          <div class="logo-title">GITDOCK SENTINEL</div>
          <div class="logo-sub">AI Security Pipeline · Real-time Audit</div>
        </div>
      </div>

      <div class="header-center">
        <button
          @click="activeView = 'grid'"
          :class="['view-btn', activeView === 'grid' ? 'active' : '']"
        >
          <span class="view-icon">⊞</span> GRID
        </button>
        <button
          @click="activeView = 'stream'"
          :class="['view-btn', activeView === 'stream' ? 'active' : '']"
        >
          <span class="view-icon">⟹</span> STREAM
        </button>
      </div>

      <div class="header-stats">
        <div class="stat-pill">
          <div class="stat-dot dot-white"></div>
          <span class="stat-label">TOTAL</span>
          <span class="stat-val">{{ stats.total }}</span>
        </div>
        <div class="stat-pill">
          <div class="stat-dot dot-green"></div>
          <span class="stat-label">CLEAN</span>
          <span class="stat-val clean">{{ stats.clean }}</span>
        </div>
        <div class="stat-pill">
          <div class="stat-dot dot-red pulse"></div>
          <span class="stat-label">THREATS</span>
          <span class="stat-val threat">{{ stats.threats }}</span>
        </div>
        <div class="conn-badge" :class="isConnected ? 'connected' : 'disconnected'">
          <span class="conn-dot"></span>
          {{ isConnected ? 'LIVE' : 'OFFLINE' }}
        </div>
      </div>
    </header>

    <!-- ═══════════════════════════ MAIN AREA ═══════════════════════════ -->
    <div class="main-area">

      <!-- ─────────────── LEFT: VIEW PANEL ─────────────── -->
      <div class="view-panel">
        <div class="panel-label">
          {{ activeView === 'grid' ? '// COMMIT GRID · hover to inspect' : '// COMMIT STREAM · live feed' }}
        </div>

        <!-- GRID VIEW -->
        <div v-if="activeView === 'grid'" class="grid-view" ref="gridEl">
          <div
            v-for="commit in reversedAudits"
            :key="commit.commit_hash"
            class="commit-tile"
            :class="[
              commit.is_clean ? 'tile-clean' : 'tile-threat',
              selectedAudit?.commit_hash === commit.commit_hash ? 'tile-selected' : ''
            ]"
            @click="selectedAudit = commit"
            @mouseenter="hoveredCommit = commit"
            @mouseleave="hoveredCommit = null"
          >
            <div class="tile-hash">{{ commit.commit_hash }}</div>
            <div class="tile-status-icon">{{ commit.is_clean ? '✓' : '✗' }}</div>
            <div class="tile-lang">{{ detectLanguage(commit) }}</div>

            <!-- Hover tooltip -->
            <div v-if="hoveredCommit?.commit_hash === commit.commit_hash" class="tile-tooltip">
              <div class="tt-row"><span class="tt-key">author</span><span class="tt-val">{{ commit.author }}</span></div>
              <div class="tt-row"><span class="tt-key">lang</span><span class="tt-val lang-badge">{{ detectLanguage(commit) }}</span></div>
              <div class="tt-row"><span class="tt-key">project</span><span class="tt-val">{{ commit.project_id }}</span></div>
              <div class="tt-row"><span class="tt-key">status</span>
                <span class="tt-val" :class="commit.is_clean ? 'tt-clean' : 'tt-threat'">
                  {{ commit.is_clean ? 'CLEAN' : `${commit.vulnerabilities.length} VULN` }}
                </span>
              </div>
            </div>
          </div>

          <div v-if="audits.length === 0" class="empty-state">
            <div class="empty-icon">◌</div>
            <div>Waiting for commits...</div>
            <div class="empty-sub">{{ isConnected ? 'Pipeline connected. Standing by.' : 'WebSocket disconnected.' }}</div>
          </div>
        </div>

        <!-- STREAM VIEW -->
        <div v-else class="stream-view">
          <canvas ref="streamCanvas" class="stream-canvas"></canvas>
          <div class="stream-legend">
            <span class="leg-item"><span class="leg-dot green"></span>Clean</span>
            <span class="leg-item"><span class="leg-dot red"></span>Threat</span>
            <span class="leg-item"><span class="leg-dot grey"></span>Analysing</span>
          </div>
        </div>
      </div>

      <!-- ─────────────── RIGHT: DETAIL PANEL ─────────────── -->
      <div class="detail-panel">
        <div class="panel-label">// AUDIT REPORT</div>

        <!-- Empty state -->
        <div v-if="!selectedAudit" class="detail-empty">
          <div class="detail-empty-icon">◈</div>
          <p>Select a commit to view the AI audit report.</p>
        </div>

        <!-- Report -->
        <div v-else class="detail-content">
          <div class="report-header">
            <div class="report-hash">
              <span class="rh-label">COMMIT</span>
              <span class="rh-hash">{{ selectedAudit.commit_hash }}</span>
            </div>
            <div class="report-badge" :class="selectedAudit.is_clean ? 'badge-clean' : 'badge-threat'">
              {{ selectedAudit.is_clean ? '✓ CLEAN' : '✗ THREAT' }}
            </div>
          </div>

          <div class="report-meta">
            <span class="meta-item"><span class="meta-key">author</span> {{ selectedAudit.author }}</span>
            <span class="meta-sep">·</span>
            <span class="meta-item"><span class="meta-key">project</span> {{ selectedAudit.project_id }}</span>
            <span class="meta-sep">·</span>
            <span class="meta-item"><span class="meta-key">lang</span>
              <span class="lang-badge">{{ detectLanguage(selectedAudit) }}</span>
            </span>
          </div>

          <div class="report-summary">{{ selectedAudit.summary }}</div>

          <!-- Clean result -->
          <div v-if="selectedAudit.is_clean" class="clean-result">
            <div class="clean-icon">✓</div>
            <div>
              <div class="clean-title">No vulnerabilities detected</div>
              <div class="clean-sub">Code validated by Sentinel AI. Safe to deploy.</div>
            </div>
          </div>

          <!-- Vulnerabilities -->
          <div v-else class="vuln-list">
            <div class="vuln-header">
              <span class="pulse-dot"></span>
              {{ selectedAudit.vulnerabilities.length }} VULNERABILIT{{ selectedAudit.vulnerabilities.length > 1 ? 'IES' : 'Y' }} DETECTED
            </div>

            <div
              v-for="(vuln, i) in selectedAudit.vulnerabilities"
              :key="i"
              class="vuln-card"
              :class="`sev-${vuln.severity.toLowerCase()}`"
            >
              <div class="vuln-card-header">
                <span class="vuln-type">{{ vuln.type }}</span>
                <span class="vuln-sev" :class="`sev-badge-${vuln.severity.toLowerCase()}`">{{ vuln.severity }}</span>
              </div>
              <div class="vuln-snippet-label">Incriminated code</div>
              <pre class="vuln-snippet"><code>{{ vuln.line_snippet }}</code></pre>
              <div class="vuln-rec-label">AI Recommendation</div>
              <div class="vuln-rec">{{ vuln.recommendation }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Scanline overlay -->
    <div class="scanlines" aria-hidden="true"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

// ─── Types ───────────────────────────────────────────────────────
interface Vulnerability {
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  type: string
  line_snippet: string
  recommendation: string
}
interface AuditResult {
  type: string
  commit_hash: string
  project_id: string
  author: string
  is_clean: boolean
  summary: string
  vulnerabilities: Vulnerability[]
  diff?: string
}

// ─── State ───────────────────────────────────────────────────────
const socket = ref<WebSocket | null>(null)
const isConnected = ref(false)
const audits = ref<AuditResult[]>([])
const selectedAudit = ref<AuditResult | null>(null)
const hoveredCommit = ref<AuditResult | null>(null)
const activeView = ref<'grid' | 'stream'>('grid')
const streamCanvas = ref<HTMLCanvasElement | null>(null)
// const gridEl = ref<HTMLElement | null>(null)

// ─── Computed ────────────────────────────────────────────────────
const reversedAudits = computed(() => [...audits.value].reverse())

const stats = computed(() => ({
  total: audits.value.length,
  clean: audits.value.filter(a => a.is_clean).length,
  threats: audits.value.filter(a => !a.is_clean).length,
}))

// ─── Language detection (from diff content) ──────────────────────
function detectLanguage(audit: AuditResult): string {
  const text = (audit.diff || '') + (audit.summary || '') + (audit.project_id || '')
  if (/def |import |\.py|psycopg2|requirements/i.test(text)) return 'Python'
  if (/public class|\.java|springframework|@Override/i.test(text)) return 'Java'
  if (/const |=>|\.tsx?|npm|node_modules|require\(/i.test(text)) return 'JS/TS'
  if (/<\?php|namespace App|Symfony|composer/i.test(text)) return 'PHP'
  if (/\.cs|using System|namespace|\.NET|csproj/i.test(text)) return 'C#'
  if (/fn |let mut|\.rs|cargo/i.test(text)) return 'Rust'
  if (/func |go\.mod|package main/i.test(text)) return 'Go'
  if (/(facebook|react|vue|angular)\//.test(audit.project_id)) return 'JS/TS'
  if (/simulated-repo/.test(audit.project_id)) return 'Mixed'
  return 'Unknown'
}

// ─── Stream Canvas ───────────────────────────────────────────────
interface StreamParticle {
  id: string
  x: number
  y: number
  radius: number
  speed: number
  color: string
  glowColor: string
  label: string
  alpha: number
  status: 'pending' | 'clean' | 'threat'
}

let streamParticles: StreamParticle[] = []
let animFrameId: number | null = null
let canvasCtx: CanvasRenderingContext2D | null = null

const COLORS = {
  clean:   { fill: '#10b981', glow: 'rgba(16,185,129,0.4)' },
  threat:  { fill: '#f43f5e', glow: 'rgba(244,63,94,0.5)' },
  pending: { fill: '#475569', glow: 'rgba(71,85,105,0.2)' },
}

function initCanvas() {
  const canvas = streamCanvas.value
  if (!canvas) return
  const parent = canvas.parentElement!
  canvas.width = parent.clientWidth
  canvas.height = parent.clientHeight
  canvasCtx = canvas.getContext('2d')
  if (animFrameId) cancelAnimationFrame(animFrameId)
  animateStream()
}

function spawnStreamParticle(audit: AuditResult) {
  const canvas = streamCanvas.value
  if (!canvas) return
  const c = audit.is_clean ? COLORS.clean : COLORS.threat
  streamParticles.push({
    id: audit.commit_hash,
    x: canvas.width + 60,
    y: 60 + Math.random() * (canvas.height - 120),
    radius: 18 + Math.random() * 8,
    speed: 1.2 + Math.random() * 1.4,
    color: c.fill,
    glowColor: c.glow,
    label: audit.commit_hash,
    alpha: 1,
    status: audit.is_clean ? 'clean' : 'threat',
  })
}

function animateStream() {
  const canvas = streamCanvas.value
  const ctx = canvasCtx
  if (!canvas || !ctx) return

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // Background grid lines
  ctx.strokeStyle = 'rgba(30,41,59,0.6)'
  ctx.lineWidth = 1
  for (let y = 0; y < canvas.height; y += 40) {
    ctx.beginPath()
    ctx.moveTo(0, y)
    ctx.lineTo(canvas.width, y)
    ctx.stroke()
  }

  // Timeline base line
  const mid = canvas.height / 2
  ctx.strokeStyle = 'rgba(99,102,241,0.25)'
  ctx.lineWidth = 1.5
  ctx.setLineDash([6, 8])
  ctx.beginPath()
  ctx.moveTo(0, mid)
  ctx.lineTo(canvas.width, mid)
  ctx.stroke()
  ctx.setLineDash([])

  // Particles
  for (let i = streamParticles.length - 1; i >= 0; i--) {
    const p = streamParticles[i]
    p.x -= p.speed

    if (p.x < -80) {
      streamParticles.splice(i, 1)
      continue
    }

    // Fade out near left edge
    if (p.x < 100) p.alpha = Math.max(0, p.x / 100)

    // Draw connector to timeline
    ctx.strokeStyle = `rgba(99,102,241,${0.15 * p.alpha})`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(p.x, p.y)
    ctx.lineTo(p.x, mid)
    ctx.stroke()

    // Glow
    const grd = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.radius * 2.5)
    grd.addColorStop(0, p.glowColor.replace(')', `,${0.6 * p.alpha})`).replace('rgba(', 'rgba('))
    grd.addColorStop(1, 'transparent')
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius * 2.5, 0, Math.PI * 2)
    ctx.fillStyle = grd
    ctx.fill()

    // Circle
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = p.color + Math.round(p.alpha * 255).toString(16).padStart(2, '0')
    ctx.fill()

    // Inner ring
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius * 0.55, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(2,4,8,${0.6 * p.alpha})`
    ctx.fill()

    // Icon
    ctx.fillStyle = `rgba(255,255,255,${0.9 * p.alpha})`
    ctx.font = `bold ${p.radius * 0.7}px JetBrains Mono`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(p.status === 'clean' ? '✓' : '✗', p.x, p.y)

    // Hash label
    ctx.fillStyle = `rgba(148,163,184,${0.7 * p.alpha})`
    ctx.font = `${Math.max(8, p.radius * 0.45)}px JetBrains Mono`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'top'
    ctx.fillText(p.label, p.x, p.y + p.radius + 4)
  }

  animFrameId = requestAnimationFrame(animateStream)
}

// ─── WebSocket ────────────────────────────────────────────────────
const WS_URL = 'ws://ubuntu-host:8010/api/ws/sentinel'
// const WS_URL = 'ws://ubuntu-host:8013/api/sentinel/ws'

function connectWebSocket() {
  socket.value = new WebSocket(WS_URL)

  socket.value.onopen = () => {
    isConnected.value = true
    console.log('✅ Sentinel connected')
  }

  socket.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data) as AuditResult
      if (data.type === 'AUDIT_RESULT') {
        audits.value.push(data)
        spawnStreamParticle(data)
        if (!selectedAudit.value || !data.is_clean) {
          selectedAudit.value = data
        }
      }
    } catch (e) {
      console.error('WS parse error', e)
    }
  }

  socket.value.onclose = () => {
    isConnected.value = false
    setTimeout(connectWebSocket, 3000)
  }

  socket.value.onerror = () => socket.value?.close()
}

// ─── Lifecycle ────────────────────────────────────────────────────
watch(activeView, async (v) => {
  if (v === 'stream') {
    await nextTick()
    initCanvas()
  } else {
    if (animFrameId) { cancelAnimationFrame(animFrameId); animFrameId = null }
  }
})

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  socket.value?.close()
  if (animFrameId) cancelAnimationFrame(animFrameId)
})
</script>

<style scoped>
/* ── Shell ────────────────────────────────────────────────── */
.sentinel-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  background: #020408;
  font-family: 'JetBrains Mono', monospace;
  position: relative;
  overflow: hidden;
}

/* ── Header ───────────────────────────────────────────────── */
.sentinel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 28px;
  border-bottom: 1px solid rgba(99, 102, 241, 0.2);
  background: rgba(2, 4, 8, 0.95);
  backdrop-filter: blur(12px);
  flex-shrink: 0;
  gap: 20px;
}

.logo-block {
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo-icon {
  font-size: 28px;
  color: #6366f1;
  line-height: 1;
  filter: drop-shadow(0 0 8px rgba(99,102,241,0.8));
}
.logo-title {
  font-family: 'Syne', sans-serif;
  font-weight: 800;
  font-size: 15px;
  color: #e2e8f0;
  letter-spacing: 0.12em;
}
.logo-sub {
  font-size: 10px;
  color: #475569;
  letter-spacing: 0.05em;
  margin-top: 2px;
}

/* ── View toggle ──────────────────────────────────────────── */
.header-center {
  display: flex;
  gap: 4px;
  background: rgba(15, 23, 42, 0.8);
  padding: 4px;
  border-radius: 8px;
  border: 1px solid rgba(99,102,241,0.15);
}
.view-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #475569;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.08em;
  cursor: pointer;
  transition: all 0.15s;
}
.view-btn:hover { color: #94a3b8; }
.view-btn.active {
  background: rgba(99,102,241,0.2);
  color: #a5b4fc;
  box-shadow: 0 0 12px rgba(99,102,241,0.2);
}
.view-icon { font-size: 13px; }

/* ── Stats ────────────────────────────────────────────────── */
.header-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}
.stat-pill {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 5px 12px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(51, 65, 85, 0.6);
  border-radius: 999px;
  font-size: 11px;
}
.stat-dot { width: 7px; height: 7px; border-radius: 50%; }
.dot-white { background: #94a3b8; }
.dot-green { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.8); }
.dot-red   { background: #f43f5e; box-shadow: 0 0 6px rgba(244,63,94,0.8); }
.pulse { animation: pulse 1.4s ease-in-out infinite; }
@keyframes pulse { 0%,100% { opacity:1; transform:scale(1); } 50% { opacity:0.5; transform:scale(0.7); } }

.stat-label { color: #475569; letter-spacing: 0.06em; font-size: 10px; }
.stat-val   { font-weight: 700; color: #94a3b8; }
.stat-val.clean  { color: #10b981; }
.stat-val.threat { color: #f43f5e; }

.conn-badge {
  display: flex; align-items: center; gap: 6px;
  padding: 5px 14px; border-radius: 999px;
  font-size: 11px; font-weight: 700; letter-spacing: 0.1em;
}
.conn-badge.connected    { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.4); color: #10b981; }
.conn-badge.disconnected { background: rgba(244,63,94,0.1);  border: 1px solid rgba(244,63,94,0.4);  color: #f43f5e; }
.conn-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
  animation: pulse 1.2s ease-in-out infinite;
}

/* ── Main area ────────────────────────────────────────────── */
.main-area {
  display: flex;
  flex: 1;
  gap: 0;
  overflow: hidden;
}

/* ── Panels ───────────────────────────────────────────────── */
.view-panel {
  flex: 1.1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(99,102,241,0.12);
  overflow: hidden;
}
.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-label {
  padding: 10px 20px;
  font-size: 10px;
  color: #4f46e5;
  letter-spacing: 0.1em;
  border-bottom: 1px solid rgba(99,102,241,0.1);
  background: rgba(99,102,241,0.03);
  flex-shrink: 0;
}

/* ── Grid view ────────────────────────────────────────────── */
.grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 6px;
  padding: 14px;
  overflow-y: auto;
  align-content: start;
  flex: 1;
}

.commit-tile {
  position: relative;
  aspect-ratio: 1;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border: 1px solid transparent;
  transition: all 0.15s;
  overflow: visible;
}

.tile-clean  {
  background: rgba(16,185,129,0.08);
  border-color: rgba(16,185,129,0.25);
}
.tile-threat {
  background: rgba(244,63,94,0.1);
  border-color: rgba(244,63,94,0.3);
  animation: threat-glow 2s ease-in-out infinite;
}
@keyframes threat-glow {
  0%,100% { box-shadow: 0 0 0 rgba(244,63,94,0); }
  50% { box-shadow: 0 0 12px rgba(244,63,94,0.25); }
}
.tile-clean:hover  { background: rgba(16,185,129,0.16); transform: scale(1.06); z-index: 10; }
.tile-threat:hover { background: rgba(244,63,94,0.18); transform: scale(1.06); z-index: 10; }
.tile-selected { outline: 2px solid #6366f1; outline-offset: 2px; }

.tile-hash {
  font-size: 8px;
  color: #64748b;
  letter-spacing: 0.03em;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  width: 90%;
  text-align: center;
}
.tile-status-icon {
  font-size: 18px;
  line-height: 1;
}
.tile-clean .tile-status-icon  { color: #10b981; }
.tile-threat .tile-status-icon { color: #f43f5e; }

.tile-lang {
  font-size: 7px;
  color: #6366f1;
  letter-spacing: 0.05em;
  background: rgba(99,102,241,0.12);
  padding: 1px 5px;
  border-radius: 3px;
}

/* ── Hover tooltip ────────────────────────────────────────── */
.tile-tooltip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: rgba(10,15,28,0.97);
  border: 1px solid rgba(99,102,241,0.35);
  border-radius: 8px;
  padding: 10px 12px;
  min-width: 160px;
  z-index: 100;
  box-shadow: 0 8px 32px rgba(0,0,0,0.6), 0 0 16px rgba(99,102,241,0.15);
  pointer-events: none;
}
.tt-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 2px 0;
  font-size: 10px;
}
.tt-key  { color: #475569; letter-spacing: 0.05em; flex-shrink: 0; }
.tt-val  { color: #94a3b8; text-align: right; }
.tt-clean  { color: #10b981 !important; font-weight: 700; }
.tt-threat { color: #f43f5e !important; font-weight: 700; }

/* ── Empty state ──────────────────────────────────────────── */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 20px;
  color: #334155;
  font-size: 12px;
  text-align: center;
}
.empty-icon {
  font-size: 40px;
  animation: spin 4s linear infinite;
  color: #1e293b;
}
@keyframes spin { to { transform: rotate(360deg); } }
.empty-sub { font-size: 10px; color: #1e293b; margin-top: 4px; }

/* ── Stream view ──────────────────────────────────────────── */
.stream-view {
  flex: 1;
  position: relative;
  overflow: hidden;
}
.stream-canvas {
  width: 100%;
  height: 100%;
  display: block;
}
.stream-legend {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 16px;
  background: rgba(2,4,8,0.8);
  border: 1px solid rgba(99,102,241,0.15);
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 10px;
  color: #475569;
  letter-spacing: 0.06em;
}
.leg-item { display: flex; align-items: center; gap: 5px; }
.leg-dot { width: 8px; height: 8px; border-radius: 50%; }
.leg-dot.green { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.8); }
.leg-dot.red   { background: #f43f5e; box-shadow: 0 0 6px rgba(244,63,94,0.8); }
.leg-dot.grey  { background: #475569; }

/* ── Detail panel ─────────────────────────────────────────── */
.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #1e293b;
  font-size: 11px;
  text-align: center;
  padding: 40px;
}
.detail-empty-icon { font-size: 48px; opacity: 0.3; }

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.report-hash { display: flex; flex-direction: column; gap: 3px; }
.rh-label { font-size: 9px; color: #4f46e5; letter-spacing: 0.1em; }
.rh-hash  { font-size: 20px; font-family: 'Syne', sans-serif; font-weight: 700; color: #a5b4fc; }

.report-badge {
  padding: 5px 14px; border-radius: 6px;
  font-size: 11px; font-weight: 700; letter-spacing: 0.08em;
  flex-shrink: 0;
}
.badge-clean  { background: rgba(16,185,129,0.15); color: #10b981; border: 1px solid rgba(16,185,129,0.3); }
.badge-threat { background: rgba(244,63,94,0.15);  color: #f43f5e; border: 1px solid rgba(244,63,94,0.3);  }

.report-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 10px;
}
.meta-item { display: flex; align-items: center; gap: 5px; color: #64748b; }
.meta-key  { color: #334155; }
.meta-sep  { color: #1e293b; }

.lang-badge {
  background: rgba(99,102,241,0.15);
  color: #818cf8;
  padding: 1px 7px;
  border-radius: 4px;
  font-size: 10px;
  border: 1px solid rgba(99,102,241,0.25);
}

.report-summary {
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
  padding: 10px 14px;
  background: rgba(15,23,42,0.5);
  border-left: 2px solid rgba(99,102,241,0.4);
  border-radius: 0 4px 4px 0;
}

.clean-result {
  display: flex; align-items: center; gap: 16px;
  padding: 20px;
  background: rgba(16,185,129,0.06);
  border: 1px solid rgba(16,185,129,0.2);
  border-radius: 10px;
}
.clean-icon { font-size: 28px; color: #10b981; width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; background: rgba(16,185,129,0.1); border-radius: 50%; }
.clean-title { color: #10b981; font-weight: 700; font-size: 13px; margin-bottom: 4px; }
.clean-sub   { color: #475569; font-size: 11px; }

/* ── Vuln list ────────────────────────────────────────────── */
.vuln-list { display: flex; flex-direction: column; gap: 10px; }

.vuln-header {
  display: flex; align-items: center; gap: 8px;
  font-size: 10px; font-weight: 700;
  color: #f43f5e; letter-spacing: 0.1em;
}
.pulse-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #f43f5e; box-shadow: 0 0 8px rgba(244,63,94,0.8);
  animation: pulse 1.2s ease-in-out infinite;
}

.vuln-card {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(244,63,94,0.2);
  background: rgba(244,63,94,0.03);
}
.vuln-card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px;
  background: rgba(244,63,94,0.08);
  border-bottom: 1px solid rgba(244,63,94,0.15);
}
.vuln-type { font-size: 11px; font-weight: 700; color: #fca5a5; }
.vuln-sev  { font-size: 9px; font-weight: 700; padding: 2px 8px; border-radius: 4px; letter-spacing: 0.08em; }
.sev-badge-critical { background: #7f1d1d; color: #fca5a5; }
.sev-badge-high     { background: #7c2d12; color: #fdba74; }
.sev-badge-medium   { background: #713f12; color: #fde68a; }
.sev-badge-low      { background: #134e4a; color: #6ee7b7; }

.vuln-snippet-label, .vuln-rec-label {
  padding: 6px 12px 2px;
  font-size: 9px; letter-spacing: 0.08em;
  color: #475569; font-weight: 700;
}
.vuln-snippet {
  margin: 0 12px 8px;
  background: #000;
  border: 1px solid rgba(244,63,94,0.15);
  border-radius: 4px;
  padding: 10px;
  font-size: 11px;
  color: #fca5a5;
  overflow-x: auto;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
.vuln-rec {
  margin: 0 12px 12px;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.6;
  padding: 8px 10px;
  background: rgba(15,23,42,0.6);
  border-left: 2px solid rgba(99,102,241,0.5);
  border-radius: 0 4px 4px 0;
}

/* ── Scrollbar styling ────────────────────────────────────── */
.grid-view::-webkit-scrollbar,
.detail-content::-webkit-scrollbar { width: 4px; }
.grid-view::-webkit-scrollbar-track,
.detail-content::-webkit-scrollbar-track { background: transparent; }
.grid-view::-webkit-scrollbar-thumb,
.detail-content::-webkit-scrollbar-thumb { background: rgba(99,102,241,0.3); border-radius: 2px; }

/* ── Scanlines overlay ────────────────────────────────────── */
.scanlines {
  position: fixed; inset: 0; pointer-events: none; z-index: 999;
  background: repeating-linear-gradient(
    to bottom,
    transparent 0px, transparent 2px,
    rgba(0,0,0,0.06) 2px, rgba(0,0,0,0.06) 4px
  );
}
</style>