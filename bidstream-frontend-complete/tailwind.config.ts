import type { Config } from 'tailwindcss'
import forms from '@tailwindcss/forms'

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // Warm "ink" scale — replaces the default slate palette so every
        // existing slate-* class inherits the refined surface/typography tones.
        slate: {
          50: '#F2F3F6',
          100: '#E3E5EB',
          200: '#C7CBD6',
          300: '#9AA2B3',
          400: '#6E7688',
          500: '#4B5160',
          600: '#333845',
          700: '#232735',
          750: '#1B1E29',
          800: '#15171F',
          850: '#111319',
          900: '#0E1015',
          950: '#0A0B0F',
        },
        // Antique-gold brand accent used for primary actions, focus rings,
        // highlights and the logo mark.
        brand: {
          50: '#FCF7EB',
          100: '#F9EED2',
          200: '#F3DCA1',
          300: '#ECC76B',
          400: '#E5B447',
          500: '#D89E30',
          600: '#BE8230',
          700: '#9E662B',
          800: '#825329',
          900: '#6B4526',
          950: '#221703',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
        display: ['Fraunces', 'Georgia', 'ui-serif', 'serif'],
      },
      boxShadow: {
        card: '0 1px 2px rgba(10, 11, 15, 0.5), 0 16px 40px -20px rgba(10, 11, 15, 0.9)',
        'card-hover':
          '0 2px 4px rgba(10, 11, 15, 0.5), 0 24px 48px -20px rgba(10, 11, 15, 0.95)',
        glow: '0 0 0 1px rgba(216, 158, 48, 0.3), 0 12px 28px -12px rgba(216, 158, 48, 0.5)',
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #ECC76B 0%, #D89E30 45%, #BE8230 100%)',
        'brand-gradient-soft': 'linear-gradient(180deg, rgba(216, 158, 48, 0.14), rgba(216, 158, 48, 0))',
        'ink-radial':
          'radial-gradient(ellipse 80% 50% at 50% -10%, rgba(216, 158, 48, 0.08), transparent 70%)',
      },
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        shimmer: {
          '0%': { transform: 'translateX(-100%)' },
          '100%': { transform: 'translateX(100%)' },
        },
        'pulse-dot': {
          '0%, 100%': { opacity: '1', transform: 'scale(1)' },
          '50%': { opacity: '0.45', transform: 'scale(0.85)' },
        },
      },
      animation: {
        'fade-up': 'fade-up 0.5s cubic-bezier(0.22, 1, 0.36, 1) both',
        'fade-in': 'fade-in 0.4s ease-out both',
        shimmer: 'shimmer 1.6s linear infinite',
        'pulse-dot': 'pulse-dot 2s ease-in-out infinite',
      },
    },
  },
  plugins: [forms],
} satisfies Config
