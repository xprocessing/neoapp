/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#0052D9',
          light: '#366EF4',
          lighter: '#618DFF',
        },
        background: {
          DEFAULT: '#F3F3F3',
          white: '#FFFFFF',
          light: '#FAFAFA',
        },
        text: {
          primary: '#1D2129',
          secondary: '#4E5969',
          tertiary: '#86909C',
        },
        success: '#00A870',
        danger: '#E34D59',
        warning: '#ED7B2F',
      },
      fontFamily: {
        sans: ['PingFang SC', 'Microsoft YaHei', 'sans-serif'],
      },
      fontSize: {
        heading: ['28px', { lineHeight: '40px', fontWeight: 600 }],
        subheading: ['16px', { lineHeight: '24px', fontWeight: 500 }],
        body: ['14px', { lineHeight: '22px', fontWeight: 400 }],
      },
      borderRadius: {
        card: '12px',
        btn: '8px',
        input: '6px',
      },
      boxShadow: {
        card: '0 2px 12px rgba(0,0,0,0.06)',
        'card-hover': '0 4px 20px rgba(0,0,0,0.1)',
      },
      transitionDuration: {
        DEFAULT: '300ms',
      },
    },
  },
  plugins: [
    require('tailwindcss-animate'),
  ],
}
