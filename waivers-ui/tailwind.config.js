/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.elm", "./index.html"],
  theme: {
    extend: {
      colors: {
        blue: {
          DEFAULT: '#5B7FF6'
        },
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}