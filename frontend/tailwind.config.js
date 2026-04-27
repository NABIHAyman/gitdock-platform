/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'gitdock-purple': '#6366f1',
        'badge-bg': '#f8fafc',
      }
    },
  },
  plugins: [],
}