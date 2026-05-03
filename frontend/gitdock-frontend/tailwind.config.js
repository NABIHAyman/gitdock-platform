/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // NotebookLM inspired color palette
        surface: "#ffffff",
        surfaceSubtle: "#f9fafb",
        surfaceHover: "#f3f4f6",
        border: "#e5e7eb",
        borderSubtle: "#f3f4f6",
        text: "#111827",
        textSubtle: "#6b7280",
        muted: "#9ca3af",
        accent: "#1a73e8", // Google Blue
        accentHover: "#1557b0",
        accentSubtle: "#e8f0fe",
        // Semantic colors with softer tones
        success: "#137333",
        successBg: "#e6f4ea",
        warning: "#ea8600",
        warningBg: "#fef7e0",
        error: "#d93025",
        errorBg: "#fce8e6",
        info: "#1a73e8",
        infoBg: "#e8f0fe",
      },
      fontFamily: {
        sans: ["Google Sans", "Roboto", "system-ui", "-apple-system", "Segoe UI", "Arial", "sans-serif"],
        mono: ["Google Sans Mono", "Roboto Mono", "Consolas", "Monaco", "monospace"],
      },
      fontSize: {
        'xs': ['0.75rem', { lineHeight: '1rem' }],
        'sm': ['0.875rem', { lineHeight: '1.25rem' }],
        'base': ['0.9375rem', { lineHeight: '1.5rem' }], // 15px base like NotebookLM
        'lg': ['1.125rem', { lineHeight: '1.75rem' }],
        'xl': ['1.25rem', { lineHeight: '1.75rem' }],
        '2xl': ['1.5rem', { lineHeight: '2rem' }],
        '3xl': ['1.875rem', { lineHeight: '2.25rem' }],
        '4xl': ['2.25rem', { lineHeight: '2.5rem' }],
      },
      borderRadius: {
        'gd': '8px', // More consistent with Google's design
        'gd-lg': '12px',
        'gd-xl': '16px',
      },
      boxShadow: {
        'gd': '0 1px 2px 0 rgba(60, 64, 67, 0.3), 0 1px 3px 1px rgba(60, 64, 67, 0.15)',
        'gd-hover': '0 1px 3px 0 rgba(60, 64, 67, 0.3), 0 4px 8px 3px rgba(60, 64, 67, 0.15)',
        'gd-focus': '0 0 0 3px rgba(26, 115, 232, 0.2)',
        'gd-card': '0 1px 2px 0 rgba(60, 64, 67, 0.3), 0 2px 6px 2px rgba(60, 64, 67, 0.15)',
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
      },
    },
  },
  plugins: [],
}

