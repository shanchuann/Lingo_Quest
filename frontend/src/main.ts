import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { MotionPlugin } from '@vueuse/motion'
import App from './App.vue'
import { router } from './router'
import './styles/globals.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(router)
app.use(MotionPlugin)
app.mount('#app')
