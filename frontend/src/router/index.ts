import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',            name: 'home',         component: () => import('@/pages/Home.vue') },
    { path: '/login',       name: 'login',        component: () => import('@/pages/Login.vue'),    meta: { guestOnly: true } },
    { path: '/register',    name: 'register',     component: () => import('@/pages/Register.vue'), meta: { guestOnly: true } },
    { path: '/tutor',       name: 'tutor',        component: () => import('@/pages/Tutor.vue'),        meta: { requiresAuth: true } },
    { path: '/dashboard',   name: 'dashboard',    component: () => import('@/pages/Dashboard.vue'),    meta: { requiresAuth: true } },
    { path: '/game',        name: 'game',         component: () => import('@/pages/Game.vue'),         meta: { requiresAuth: true } },
    { path: '/ranking',     name: 'ranking',      component: () => import('@/pages/Ranking.vue'),      meta: { requiresAuth: true } },
    { path: '/achievements',name: 'achievements', component: () => import('@/pages/Achievements.vue'), meta: { requiresAuth: true } },
    { path: '/report',      name: 'report',       component: () => import('@/pages/Report.vue'),       meta: { requiresAuth: true } },
    { path: '/shop',        name: 'shop',         component: () => import('@/pages/Shop.vue'),         meta: { requiresAuth: true } },
    { path: '/profile',     name: 'profile',      component: () => import('@/pages/Profile.vue'),      meta: { requiresAuth: true } },
    { path: '/wordbooks',          name: 'wordbooks', component: () => import('@/pages/WordBooks.vue'), meta: { requiresAuth: true } },
    { path: '/wordbooks/:id(\\d+)', name: 'wordbook',  component: () => import('@/pages/WordBook.vue'),  meta: { requiresAuth: true } },
    // Public legal & help pages — no auth required.
    { path: '/terms',   name: 'terms',   component: () => import('@/pages/Terms.vue') },
    { path: '/privacy', name: 'privacy', component: () => import('@/pages/Privacy.vue') },
    { path: '/help',    name: 'help',    component: () => import('@/pages/Help.vue') },
    { path: '/:catchAll(.*)', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthed) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthed) {
    return { name: 'dashboard' }
  }
  return true
})
