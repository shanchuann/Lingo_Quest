import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import { router } from '@/router'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 35_000,
})

http.interceptors.request.use((config) => {
  const token = useAuthStore().token
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  async (error) => {
    if (error?.response?.status === 401) {
      useAuthStore().logout()
      await router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    }
    return Promise.reject(error)
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const resp = await http.request<ApiResult<T>>(config)
  const body = resp.data
  if (body.code !== 0) {
    throw new Error(body.message || 'Request failed')
  }
  return body.data
}

export default http
