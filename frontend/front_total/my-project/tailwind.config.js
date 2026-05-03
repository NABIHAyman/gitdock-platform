export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],

  theme: {
    extend: {
      colors: {
        surface: '#0f172a',   // background principal (dark SaaS)
        text: '#e2e8f0',      // texte principal
        muted: '#94a3b8',     // texte secondaire
        accent: '#3b82f6'     // bleu principal
      }
    }
  },

  plugins: [],
}