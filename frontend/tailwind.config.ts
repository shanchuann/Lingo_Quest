import type { Config } from 'tailwindcss'
import animate from 'tailwindcss-animate'
import typography from '@tailwindcss/typography'

export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    container: {
      center: true,
      padding: '1rem',
      screens: { '2xl': '1280px' },
    },
    extend: {
      colors: {
        // GitHub Universe-inspired tokens
        canvas: '#010409',
        surface: '#0d1117',
        'surface-1': '#161b22',
        'surface-2': '#21262d',
        border: '#30363d',
        muted: '#8b949e',
        foreground: '#e6edf3',
        accent: {
          DEFAULT: '#7c3aed',
          subtle: '#a78bfa',
          glow: '#c084fc',
        },
        brand: {
          blue: '#2563eb',
          violet: '#7c3aed',
          fuchsia: '#d946ef',
        },
        success: '#3fb950',
        warning: '#d29922',
        danger: '#f85149',
      },
      fontFamily: {
        sans: [
          'Inter',
          'ui-sans-serif',
          'system-ui',
          '-apple-system',
          'Segoe UI',
          'Roboto',
          'sans-serif',
        ],
        mono: [
          'JetBrains Mono',
          'ui-monospace',
          'SFMono-Regular',
          'Menlo',
          'monospace',
        ],
      },
      borderRadius: {
        sm: '6px',
        DEFAULT: '8px',
        md: '12px',
        lg: '16px',
        xl: '20px',
      },
      boxShadow: {
        glow: '0 0 32px -8px rgba(124, 58, 237, 0.45)',
        'glow-lg': '0 0 64px -12px rgba(124, 58, 237, 0.55)',
      },
      backgroundImage: {
        'gradient-brand':
          'linear-gradient(135deg, #2563eb 0%, #7c3aed 50%, #d946ef 100%)',
        'gradient-radial':
          'radial-gradient(circle at 50% 0%, rgba(124,58,237,0.18), transparent 60%)',
      },
      animation: {
        'gradient-x': 'gradient-x 8s ease infinite',
        'fade-up': 'fade-up 0.6s ease-out',
      },
      keyframes: {
        'gradient-x': {
          '0%, 100%': { 'background-position': '0% 50%' },
          '50%': { 'background-position': '100% 50%' },
        },
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(8px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [animate, typography],
} satisfies Config
