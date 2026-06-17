import type { Config } from 'tailwindcss'
import animate from 'tailwindcss-animate'
import typography from '@tailwindcss/typography'

export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    container: {
      center: true,
      padding: { DEFAULT: '1.25rem', lg: '2rem' },
      screens: { '2xl': '1320px' },
    },
    extend: {
      colors: {
        // Universe '26 cream-paper system — all use <alpha-value> so bg-x/40 works
        paper:      'oklch(0.965 0.014 92 / <alpha-value>)',
        'paper-2':  'oklch(0.94 0.014 92 / <alpha-value>)',
        'paper-3':  'oklch(0.91 0.014 92 / <alpha-value>)',
        ink:        'oklch(0.18 0.010 80 / <alpha-value>)',
        'ink-soft': 'oklch(0.36 0.010 80 / <alpha-value>)',
        'ink-faint':'oklch(0.55 0.010 80 / <alpha-value>)',
        rule:       'oklch(0.18 0.010 80 / <alpha-value>)',
        'rule-soft':'oklch(0.78 0.012 90 / <alpha-value>)',

        // Pixel palette — the 8 tile colors
        pixel: {
          green:   'oklch(0.72 0.18 145 / <alpha-value>)',
          mint:    'oklch(0.84 0.12 155 / <alpha-value>)',
          blue:    'oklch(0.62 0.18 245 / <alpha-value>)',
          sky:     'oklch(0.80 0.10 230 / <alpha-value>)',
          violet:  'oklch(0.55 0.20 295 / <alpha-value>)',
          pink:    'oklch(0.78 0.16 5 / <alpha-value>)',
          magenta: 'oklch(0.62 0.22 350 / <alpha-value>)',
          amber:   'oklch(0.82 0.15 85 / <alpha-value>)',
          orange:  'oklch(0.72 0.17 50 / <alpha-value>)',
          red:     'oklch(0.62 0.22 28 / <alpha-value>)',
        },

        // Semantic tile mapping
        tile: {
          start:     'oklch(0.72 0.18 145 / <alpha-value>)',
          end:       'oklch(0.82 0.15 85 / <alpha-value>)',
          vocab:     'oklch(0.62 0.18 245 / <alpha-value>)',
          grammar:   'oklch(0.55 0.20 295 / <alpha-value>)',
          speaking:  'oklch(0.78 0.16 5 / <alpha-value>)',
          lucky:     'oklch(0.84 0.12 155 / <alpha-value>)',
          trap:      'oklch(0.62 0.22 28 / <alpha-value>)',
          challenge: 'oklch(0.72 0.17 50 / <alpha-value>)',
        },

        success: 'oklch(0.55 0.16 145 / <alpha-value>)',
        warning: 'oklch(0.68 0.18 75 / <alpha-value>)',
        danger:  'oklch(0.55 0.22 28 / <alpha-value>)',
      },
      fontFamily: {
        // Latin → BSD; CJK → Noto Sans SC (has true 700/900 weights)
        display: [
          '"Big Shoulders Display"',
          '"Noto Sans SC"',
          'ui-sans-serif',
          'system-ui',
          'sans-serif',
        ],
        sans: [
          'Geist',
          '"Noto Sans SC"',
          'ui-sans-serif',
          'system-ui',
          '-apple-system',
          'sans-serif',
        ],
        mono: [
          '"JetBrains Mono"',
          '"Noto Sans SC"',
          'ui-monospace',
          'SFMono-Regular',
          'Menlo',
          'monospace',
        ],
        // Decorative chunky CJK display, for hero accents only
        zcool: [
          '"ZCOOL QingKe HuangYou"',
          '"Big Shoulders Display"',
          'ui-sans-serif',
          'sans-serif',
        ],
      },
      fontSize: {
        // Universe-style mega display
        'mark':       ['clamp(4rem, 13vw, 11rem)', { lineHeight: '0.84', letterSpacing: '-0.02em' }],
        'display-xl': ['clamp(3rem, 7.5vw, 6.5rem)', { lineHeight: '0.88', letterSpacing: '-0.015em' }],
        'display-lg': ['clamp(2.25rem, 5vw, 4rem)', { lineHeight: '0.92', letterSpacing: '-0.01em' }],
        'display-md': ['clamp(1.5rem, 3vw, 2.25rem)', { lineHeight: '1.0', letterSpacing: '-0.005em' }],
      },
      borderRadius: {
        none: '0',
        sm: '2px',
        DEFAULT: '4px',
        md: '6px',
        lg: '10px',
        xl: '14px',
      },
      boxShadow: {
        // sharp, low — paper system, not glow
        rim: 'inset 0 0 0 1px oklch(0.18 0.010 80 / 0.06)',
        page: '0 1px 0 oklch(0.18 0.010 80 / 0.08), 0 8px 24px -16px oklch(0.18 0.010 80 / 0.18)',
      },
      backgroundImage: {
        ticker:
          "repeating-linear-gradient(90deg, oklch(0.18 0.010 80) 0 28px, transparent 28px 32px)",
      },
      transitionTimingFunction: {
        'out-expo':  'cubic-bezier(0.16, 1, 0.3, 1)',
        'out-quint': 'cubic-bezier(0.22, 1, 0.36, 1)',
      },
      animation: {
        'fade-up':  'fade-up 0.7s cubic-bezier(0.16, 1, 0.3, 1) both',
        'pixel-blink': 'pixel-blink 1.6s steps(2, end) infinite',
        'dice-tumble': 'dice-tumble 0.6s cubic-bezier(0.22, 1, 0.36, 1)',
        'ticker-scroll': 'ticker-scroll 30s linear infinite',
      },
      keyframes: {
        'fade-up': {
          '0%':   { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'pixel-blink': {
          '0%, 49%': { opacity: '1' },
          '50%, 100%': { opacity: '0.35' },
        },
        'dice-tumble': {
          '0%':   { transform: 'rotate(0deg) scale(0.92)' },
          '50%':  { transform: 'rotate(170deg) scale(1.04)' },
          '100%': { transform: 'rotate(360deg) scale(1)' },
        },
        'ticker-scroll': {
          '0%': { transform: 'translateX(0)' },
          '100%': { transform: 'translateX(-50%)' },
        },
      },
    },
  },
  plugins: [animate, typography],
} satisfies Config
