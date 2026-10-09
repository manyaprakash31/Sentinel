/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        sentinel: {
          950: '#07090e',
          900: '#0d1117',
          850: '#131822',
          800: '#161b26',
          700: '#212836',
          600: '#2e384d',
          accent: '#00f0ff',
          neonGreen: '#00ff88',
          neonAmber: '#ffaa00',
          neonRed: '#ff3366',
        }
      }
    },
  },
  plugins: [],
}
