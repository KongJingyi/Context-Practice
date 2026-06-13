/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      fontFamily: {
        inter: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
      },
      colors: {
        brand: {
          DEFAULT: "#3b82f6",
          dark: "#1d4ed8",
          light: "#60a5fa",
          pale: "#dbeafe",
        },
      },
      backgroundImage: {
        "hero-mesh":
          "radial-gradient(at 20% 20%, rgba(96, 165, 250, 0.45) 0, transparent 50%), radial-gradient(at 80% 10%, rgba(59, 130, 246, 0.35) 0, transparent 45%), radial-gradient(at 60% 90%, rgba(147, 197, 253, 0.5) 0, transparent 50%), linear-gradient(135deg, #eff6ff 0%, #ffffff 40%, #e0f2fe 100%)",
      },
      boxShadow: {
        "hero-title": "0 4px 28px rgba(37, 99, 235, 0.18)",
        "hero-card": "0 12px 40px rgba(37, 99, 235, 0.1)",
        "hero-btn": "0 8px 24px rgba(37, 99, 235, 0.35)",
      },
    },
  },
  plugins: [],
};
