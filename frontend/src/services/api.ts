import axios from 'axios'

const AUTH_STORAGE_KEY = 'gol360_auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL as string,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: false,
})

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem(AUTH_STORAGE_KEY)
  if (stored) {
    const parsed = JSON.parse(stored) as { token: string | null }
    if (parsed.token) {
      config.headers.Authorization = `Bearer ${parsed.token}`
    }
  }
  return config
})

export const AUTH_STORAGE_KEY_EXPORT = AUTH_STORAGE_KEY
export default api
